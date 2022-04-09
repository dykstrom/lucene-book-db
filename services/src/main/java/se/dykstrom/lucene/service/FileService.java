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
import java.util.List;

import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.model.BookReference;

public interface FileService {

    Book readBook(final Path path) throws IOException;

    Book readBook(final BookReference reference) throws IOException;

    List<Book> readAllBooks(final List<BookReference> references) throws IOException;
}
