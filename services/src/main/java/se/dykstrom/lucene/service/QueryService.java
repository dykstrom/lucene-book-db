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
import java.util.List;
import java.util.Optional;

import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.model.BookReference;

public interface QueryService {

    Optional<Book> findBookByIsbn(final String isbn) throws IOException;

    List<Book> findBooksByAuthor(final String author) throws IOException;

    List<Book> findBooksByPagesRange(final int min, final int max) throws IOException;

    List<BookReference> findReferencesByAuthor(final String author) throws IOException;

    List<BookReference> findReferencesByDescription(final String description) throws IOException;

    List<BookReference> findReferencesByFields(final String author,
                                               final String title,
                                               final String description,
                                               final int minPages,
                                               final int maxPages) throws IOException;

    List<BookReference> findReferencesByQuery(final String text) throws IOException;
}
