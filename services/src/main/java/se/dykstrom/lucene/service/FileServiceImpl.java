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

import com.fasterxml.jackson.databind.ObjectMapper;
import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.model.BookReference;

public class FileServiceImpl implements FileService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Book readBook(final Path path) throws IOException {
        return OBJECT_MAPPER.readValue(path.toFile(), Book.class);
    }

    @Override
    public Book readBook(final BookReference reference) throws IOException {
        return readBook(reference.path());
    }

    @Override
    public List<Book> readAllBooks(final List<BookReference> references) throws IOException {
        final List<Book> books = new ArrayList<>();
        for (final BookReference reference : references) {
            books.add(readBook(reference));
        }
        return books;
    }
}
