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

package se.dykstrom.lucene.client.task;

import javafx.concurrent.Task;
import se.dykstrom.lucene.client.util.Alerts;
import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.service.QueryService;
import se.dykstrom.lucene.service.QueryServiceImpl;

import static se.dykstrom.lucene.client.util.AppConfig.INDEX_PATH;

public class QueryForBookTask extends Task<Book> {

    private final QueryService queryService = new QueryServiceImpl(INDEX_PATH);
    private final String isbn;

    public QueryForBookTask(final String isbn) {
        this.isbn = isbn.strip();
    }

    @Override
    protected Book call() throws Exception {
        return queryService.findBookByIsbn(isbn).orElse(null);
    }

    @Override
    protected void failed() {
        Alerts.showErrorAlert("Failed to find book:", getException());
    }
}
