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
import se.dykstrom.lucene.service.IndexService;
import se.dykstrom.lucene.service.IndexServiceImpl;

import static se.dykstrom.lucene.client.util.AppConfig.BOOK_PATH;
import static se.dykstrom.lucene.client.util.AppConfig.INDEX_PATH;

public class IndexTask extends Task<Void> {
    @Override
    protected Void call() throws Exception {
        IndexService indexService = new IndexServiceImpl(INDEX_PATH);
        indexService.indexAllBooks(BOOK_PATH);
        return null;
    }

    @Override
    protected void failed() {
        Alerts.showErrorAlert("Failed to index book files:", getException());
    }
}
