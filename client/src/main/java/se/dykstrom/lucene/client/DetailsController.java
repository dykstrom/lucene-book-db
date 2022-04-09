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

package se.dykstrom.lucene.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import se.dykstrom.lucene.model.Book;

/**
 * A controller class for the Details dialog.
 */
public class DetailsController {

    @FXML private Label isbnLabel;
    @FXML private Label authorLabel;
    @FXML private Label titleLabel;
    @FXML private Label pagesLabel;
    @FXML private TextArea descriptionArea;

    public void initialize(final Book book) {
        isbnLabel.setText(book.isbn());
        authorLabel.setText(book.author());
        titleLabel.setText(book.title());
        pagesLabel.setText(Integer.toString(book.pages()));
        descriptionArea.setText(book.description());
    }
}
