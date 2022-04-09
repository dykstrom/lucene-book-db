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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.model.FieldName;

import static java.util.Objects.requireNonNull;

public class IndexServiceImpl implements IndexService {

    private final Analyzer analyzer = new StandardAnalyzer();
    private final Path indexPath;
    private final FileService fileService;

    public IndexServiceImpl(final Path indexPath) {
        this(indexPath, new FileServiceImpl());
    }

    public IndexServiceImpl(final Path indexPath, final FileService fileService) {
        this.indexPath = requireNonNull(indexPath);
        this.fileService = requireNonNull(fileService);
    }

    @Override
    public void indexBook(final Path bookFile) throws IOException {
        final IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        try (Directory directory = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(directory, config)) {
            final Book book = fileService.readBook(bookFile);
            writer.updateDocument(new Term(FieldName.ISBN.value(), book.isbn()), createDocument(bookFile, book));
        }
    }

    @Override
    public void indexAllBooks(final Path bookDir) throws IOException {
        final IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try (Directory directory = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(directory, config)) {
            for (File file : requireNonNull(bookDir.toFile().listFiles())) {
                writer.addDocument(createDocument(file.toPath(), fileService.readBook(file.toPath())));
            }
        }
    }

    private Document createDocument(final Path path, final Book book) {
        Document document = new Document();
        document.add(new StringField(FieldName.PATH.value(), path.toString(), Field.Store.YES));
        document.add(new StringField(FieldName.ISBN.value(), book.isbn(), Field.Store.YES));
        document.add(new TextField(FieldName.TITLE.value(), book.title(), Field.Store.YES));
        document.add(new TextField(FieldName.AUTHOR.value(), book.author(), Field.Store.YES));
        document.add(new IntPoint(FieldName.PAGES.value(), book.pages()));
        document.add(new TextField(FieldName.DESCRIPTION.value(), book.description(), Field.Store.NO));
        return document;
    }
}
