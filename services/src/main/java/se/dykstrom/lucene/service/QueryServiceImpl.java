/*
 * Copyright 2022 Johan Dykström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.dykstrom.lucene.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.model.BookReference;
import se.dykstrom.lucene.model.FieldName;

import static java.util.Objects.requireNonNull;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class QueryServiceImpl implements QueryService {

    private static final int PAGE_SIZE = 10;

    private final Analyzer analyzer = new StandardAnalyzer();
    private final Path indexPath;
    private final FileService fileService;

    public QueryServiceImpl(final Path indexPath) {
        this(indexPath, new FileServiceImpl());
    }

    public QueryServiceImpl(final Path indexPath, final FileService fileService) {
        this.indexPath = requireNonNull(indexPath);
        this.fileService = requireNonNull(fileService);
    }

    @Override
    public Optional<Book> findBookByIsbn(final String isbn) throws IOException {
        final var query = new TermQuery(new Term(FieldName.ISBN.value(), isbn));
        final var books = executeBookQuery(query);

        if (books.size() > 1) {
            throw new IllegalStateException("more than one document matched ISBN " + isbn);
        } else {
            return books.stream().findFirst();
        }
    }

    @Override
    public List<Book> findBooksByAuthor(final String author) throws IOException {
        final var query = new FuzzyQuery(new Term(FieldName.AUTHOR.value(), author.toLowerCase()));
        return executeBookQuery(query);
    }

    @Override
    public List<Book> findBooksByPagesRange(final int min, final int max) throws IOException {
        final var query = IntPoint.newRangeQuery(FieldName.PAGES.value(), min, max);
        return executeBookQuery(query);
    }

    @Override
    public List<BookReference> findReferencesByAuthor(final String author) throws IOException {
        final var query = new TermQuery(new Term(FieldName.AUTHOR.value(), author.toLowerCase()));
        return executeReferenceQuery(query);
    }

    @Override
    public List<BookReference> findReferencesByDescription(final String description) throws IOException {
        // Assume description field contains a sequence of words separated by white space
        final var terms = description.strip().split("\\s+");
        final var builder = new PhraseQuery.Builder();
        // Match even if one (1) word is missing in a phrase
        builder.setSlop(1);
        for (int i = 0; i < terms.length; i++) {
            builder.add(new Term(FieldName.DESCRIPTION.value(), terms[i].toLowerCase()), i);
        }
        final var query = builder.build();
        return executeReferenceQuery(query);
    }

    @Override
    public List<BookReference> findReferencesByFields(final String author,
                                                      final String title,
                                                      final String description,
                                                      final int minPages,
                                                      final int maxPages) throws IOException {
        final var builder = new BooleanQuery.Builder();
        if (!author.isBlank()) {
            builder.add(new TermQuery(new Term(FieldName.AUTHOR.value(), author.toLowerCase())), MUST);
        }
        if (!title.isBlank()) {
            builder.add(new TermQuery(new Term(FieldName.TITLE.value(), title.toLowerCase())), MUST);
        }
        if (!description.isBlank()) {
            builder.add(new TermQuery(new Term(FieldName.DESCRIPTION.value(), description.toLowerCase())), MUST);
        }
        if (minPages != 0 || maxPages != 0) {
            builder.add(IntPoint.newRangeQuery(FieldName.PAGES.value(), minPages, maxPages), MUST);
        }
        final var query = builder.build();
        if (query.clauses().isEmpty()) {
            throw new IllegalArgumentException("all fields are empty or 0");
        }
        return executeReferenceQuery(query);
    }

    @Override
    public List<BookReference> findReferencesByQuery(final String text) throws IOException {
        QueryParser parser = new QueryParser(FieldName.DESCRIPTION.value(), analyzer);
        try {
            Query query = parser.parse(text);
            return executeReferenceQuery(query);
        } catch (ParseException e) {
            throw new IllegalArgumentException("invalid query: " + text);
        }
    }

    private List<Book> executeBookQuery(final Query query) throws IOException {
        try (final Directory directory = FSDirectory.open(indexPath);
             final DirectoryReader reader = DirectoryReader.open(directory)) {
            final var searcher = new IndexSearcher(reader);
            final List<Book> books = new ArrayList<>();

            ScoreDoc[] hits = searcher.search(query, PAGE_SIZE).scoreDocs;
            while (hits.length > 0) {
                for (final ScoreDoc hit : hits) {
                    final var document = searcher.doc(hit.doc);
                    final var path = Path.of(document.get(FieldName.PATH.value()));
                    books.add(fileService.readBook(path));
                }
                hits = searcher.searchAfter(hits[hits.length - 1], query, PAGE_SIZE).scoreDocs;
            }

            return books;
        }
    }

    private List<BookReference> executeReferenceQuery(final Query query) throws IOException {
        try (final Directory directory = FSDirectory.open(indexPath);
             final DirectoryReader reader = DirectoryReader.open(directory)) {
            final var searcher = new IndexSearcher(reader);
            final List<BookReference> references = new ArrayList<>();

            ScoreDoc[] hits = searcher.search(query, PAGE_SIZE).scoreDocs;
            while (hits.length > 0) {
                for (final ScoreDoc hit : hits) {
                    final var document = searcher.doc(hit.doc);
                    final var author = document.get(FieldName.AUTHOR.value());
                    final var title = document.get(FieldName.TITLE.value());
                    final var path = Path.of(document.get(FieldName.PATH.value()));
                    references.add(new BookReference(author, title, path, hit.score));
                }
                hits = searcher.searchAfter(hits[hits.length - 1], query, PAGE_SIZE).scoreDocs;
            }

            return references;
        }
    }
}
