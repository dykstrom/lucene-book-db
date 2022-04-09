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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.util.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.dykstrom.lucene.model.Book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryServiceImplIT {

    private static final String AUTHOR = "Ernest Hemingway";

    private static final String ISBN_0 = "978-9177423379";
    private static final String TITLE_0 = "Den gamle och havet";
    private static final int NUM_PAGES_0 = 125;
    private static final String DESCRIPTION_0 = """
            Ernest Hemingways roman Den gamle och havet i nyöversättning och med efterord av Christian Ekvall.
            Detta Hemingways kanske mest beundrade mästerverk handlar om den ärrade gamle fiskaren Santiago som åker ut på havet i sin lilla båt 84 dagar i rad utan fiskelycka.
            """;
    private static final String ISBN_1 = "978-9177424765";
    private static final String TITLE_1 = "Den orörda platsen";
    private static final int NUM_PAGES_1 = 177;
    private static final String DESCRIPTION_1 = """
            Den orörda platsen - en samlingsvolym med noveller av Ernest Hemingway. Översättning och efterord av Christian Ekvall.
            """;
    private static final String ISBN_2 = "978-9177424253";
    private static final String TITLE_2 = "Snön på Kilimanjaro";
    private static final int NUM_PAGES_2 = 270;
    private static final String DESCRIPTION_2 = """
            En volym med utvalda noveller av Ernest Hemingway som fått titeln Snön på Kilimanjaro – nyöversättning och efterord av Christian Ekvall.
            """;

    private static final Book BOOK_0 = new Book(ISBN_0, TITLE_0, AUTHOR, NUM_PAGES_0, DESCRIPTION_0);
    private static final Book BOOK_1 = new Book(ISBN_1, TITLE_1, AUTHOR, NUM_PAGES_1, DESCRIPTION_1);
    private static final Book BOOK_2 = new Book(ISBN_2, TITLE_2, AUTHOR, NUM_PAGES_2, DESCRIPTION_2);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final FileService FILE_SERVICE = new FileServiceImpl();

    private static Path indexPath;
    private static QueryService queryService;

    @BeforeAll
    static void setUpClass() throws IOException {
        indexPath = Files.createTempDirectory("tempIndex");

        IndexService indexService = new IndexServiceImpl(indexPath, FILE_SERVICE);
        indexService.indexBook(writeAsJson(BOOK_0));
        indexService.indexBook(writeAsJson(BOOK_1));
        indexService.indexBook(writeAsJson(BOOK_2));

        queryService = new QueryServiceImpl(indexPath, FILE_SERVICE);
    }

    @AfterAll
    static void tearDownClass() throws IOException {
        IOUtils.rm(indexPath);
    }

    @Test
    void shouldFindBookByIsbn() throws Exception {
        // When
        final var optionalBook0 = queryService.findBookByIsbn(ISBN_0);
        final var optionalBook1 = queryService.findBookByIsbn(ISBN_1);
        final var optionalBook2 = queryService.findBookByIsbn(ISBN_2);

        // Then
        assertTrue(optionalBook0.isPresent());
        assertEquals(BOOK_0, optionalBook0.get());
        assertTrue(optionalBook1.isPresent());
        assertEquals(BOOK_1, optionalBook1.get());
        assertTrue(optionalBook2.isPresent());
        assertEquals(BOOK_2, optionalBook2.get());
    }

    @Test
    void shouldNotFindBookByIsbn() throws Exception {
        // When
        final var optionalBook = queryService.findBookByIsbn("");

        // Then
        assertTrue(optionalBook.isEmpty());
    }

    @Test
    void shouldFindBooksByAuthor() throws Exception {
        // When
        final var books = queryService.findBooksByAuthor("Hemingway");

        // Then
        assertEquals(3, books.size());
        assertTrue(books.contains(BOOK_0));
        assertTrue(books.contains(BOOK_1));
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldNotFindBooksByAuthor() throws Exception {
        // When
        final var books = queryService.findBooksByAuthor("");

        // Then
        assertTrue(books.isEmpty());
    }

    @Test
    void shouldFuzzyFindBooksByAuthor() throws Exception {
        // When
        // Misspelling of "Hemingway"
        final var books = queryService.findBooksByAuthor("Hem0ngwa1");

        // Then
        assertEquals(3, books.size());
        assertTrue(books.contains(BOOK_0));
        assertTrue(books.contains(BOOK_1));
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldNotFuzzyFindBooksByAuthor() throws Exception {
        // When
        // Too much (three characters) misspelling of "Hemingway"
        final var books = queryService.findBooksByAuthor("Hem0ng2a1");

        // Then
        assertEquals(0, books.size());
    }

    @Test
    void shouldNotFindBooksByPagesRange() throws Exception {
        // When
        final var books = queryService.findBooksByPagesRange(1, 10);

        // Then
        assertTrue(books.isEmpty());
    }

    @Test
    void shouldFindAllBooksByPagesRange() throws Exception {
        // When
        final var books = queryService.findBooksByPagesRange(1, 1000);

        // Then
        assertEquals(3, books.size());
        assertTrue(books.contains(BOOK_0));
        assertTrue(books.contains(BOOK_1));
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldFindOneBookByPagesRange() throws Exception {
        // When
        final var books = queryService.findBooksByPagesRange(200, 1000);

        // Then
        assertEquals(1, books.size());
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldFindBooksByPagesRangeOfOne() throws Exception {
        // When
        final var books = queryService.findBooksByPagesRange(270, 270);

        // Then
        assertEquals(1, books.size());
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldFindReferencesByAuthor() throws Exception {
        // When
        final var references = queryService.findReferencesByAuthor("Hemingway");

        // Then
        assertEquals(3, references.size());

        final List<Book> books = FILE_SERVICE.readAllBooks(references);
        assertTrue(books.contains(BOOK_0));
        assertTrue(books.contains(BOOK_1));
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldFindReferencesByDescription() throws Exception {
        // When
        // Leaving out "av" between "noveller" and "Ernest"
        final var references = queryService.findReferencesByDescription("noveller Ernest Hemingway");

        // Then
        assertEquals(2, references.size());

        final List<Book> books = FILE_SERVICE.readAllBooks(references);
        assertTrue(books.contains(BOOK_1));
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldNotFindReferencesByDescription() throws Exception {
        // When
        // Leaving out "av Ernest" between "noveller" and "Hemingway"
        final var references = queryService.findReferencesByDescription("noveller Hemingway");

        // Then
        // No matches because only one missing word is allowed
        assertEquals(0, references.size());
    }

    @Test
    void shouldFindReferencesByQuery0() throws Exception {
        // When
        final var query = "+author:Hemingway AND +description:noveller";
        final var references = queryService.findReferencesByQuery(query);

        // Then
        assertEquals(2, references.size());

        final List<Book> books = FILE_SERVICE.readAllBooks(references);
        assertTrue(books.contains(BOOK_1));
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldFindReferencesByQuery1() throws Exception {
        // When
        final var query = "+title:Den";
        final var references = queryService.findReferencesByQuery(query);

        // Then
        assertEquals(2, references.size());

        final List<Book> books = FILE_SERVICE.readAllBooks(references);
        assertTrue(books.contains(BOOK_0));
        assertTrue(books.contains(BOOK_1));
    }

    @Test
    void shouldFindReferencesByQuery2() throws Exception {
        // When
        final var query = "+description:Ekvall AND -title:orörda";
        final var references = queryService.findReferencesByQuery(query);

        // Then
        assertEquals(2, references.size());

        final List<Book> books = FILE_SERVICE.readAllBooks(references);
        assertTrue(books.contains(BOOK_0));
        assertTrue(books.contains(BOOK_2));
    }

    @Test
    void shouldNotParseInvalidQuery() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> queryService.findReferencesByQuery("+author:"));
    }

    private static Path writeAsJson(final Book book) throws IOException {
        final var path = Files.createTempFile(null, null);
        final var file = path.toFile();
        file.deleteOnExit();
        OBJECT_MAPPER.writeValue(file, book);
        return path;
    }
}
