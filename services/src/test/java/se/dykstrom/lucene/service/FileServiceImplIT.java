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
import java.nio.file.Files;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.service.FileServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileServiceImplIT {

    private static final String ISBN = "978-9177423379";
    private static final String TITLE = "Den gamle och havet";
    private static final String AUTHOR = "Ernest Hemingway";
    private static final int NUM_PAGES = 125;
    private static final String DESCRIPTION = """
            Ernest Hemingways roman Den gamle och havet i nyöversättning och med efterord av Christian Ekvall.
            Detta Hemingways kanske mest beundrade mästerverk handlar om den ärrade gamle fiskaren Santiago som åker ut på havet i sin lilla båt 84 dagar i rad utan fiskelycka.
            """;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FileServiceImpl fileService = new FileServiceImpl();

    @Test
    void shouldReadBookFile() throws Exception {
        // Given
        final var expected = new Book(ISBN, TITLE, AUTHOR, NUM_PAGES, DESCRIPTION);
        final var path = Files.createTempFile(null, null);
        final File file = path.toFile();
        file.deleteOnExit();
        OBJECT_MAPPER.writeValue(file, expected);

        // When
        final var actual = fileService.readBook(path);

        // Then
        assertEquals(expected, actual);
    }
}
