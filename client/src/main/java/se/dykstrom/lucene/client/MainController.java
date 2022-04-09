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

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import se.dykstrom.lucene.client.task.IndexTask;
import se.dykstrom.lucene.client.task.QueryForBookTask;
import se.dykstrom.lucene.client.task.QueryForListTask;
import se.dykstrom.lucene.client.util.MoreBindings;
import se.dykstrom.lucene.model.Book;
import se.dykstrom.lucene.model.BookReference;
import se.dykstrom.lucene.service.FileService;
import se.dykstrom.lucene.service.FileServiceImpl;

import static javafx.beans.binding.Bindings.notEqual;
import static se.dykstrom.lucene.client.util.MoreBindings.isNotBlank;

/**
 * Controller class for the main window.
 */
public class MainController {

    private static final FileService FILE_SERVICE = new FileServiceImpl();

    private final Service<Void> indexService = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return new IndexTask();
        }
    };

    private final Service<ObservableList<BookReference>> queryForListService = new Service<>() {
        @Override
        protected Task<ObservableList<BookReference>> createTask() {
            return new QueryForListTask(
                    authorField.getText(),
                    titleField.getText(),
                    descriptionField.getText(),
                    minPagesSpinner.getValue(),
                    maxPagesSpinner.getValue()
            );
        }
    };

    private final Service<Book> queryForBookService = new Service<>() {
        @Override
        protected Task<Book> createTask() {
            return new QueryForBookTask(isbnField.getText());
        }
    };

    @FXML private TextField isbnField;
    @FXML private TextField authorField;
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private Spinner<Integer> minPagesSpinner;
    @FXML private Spinner<Integer> maxPagesSpinner;
    @FXML private TableView<BookReference> table;
    @FXML private ProgressBar progressBar;

    @FXML
    private void initialize() {
        initializeFields();
        initializeTable();
        initializeServices();
    }

    private void initializeFields() {
        authorField.disableProperty().bind(isNotBlank(isbnField.textProperty()));
        titleField.disableProperty().bind(isNotBlank(isbnField.textProperty()));
        descriptionField.disableProperty().bind(isNotBlank(isbnField.textProperty()));
        minPagesSpinner.disableProperty().bind(isNotBlank(isbnField.textProperty()));
        maxPagesSpinner.disableProperty().bind(isNotBlank(isbnField.textProperty()));
        isbnField.disableProperty().bind(MoreBindings.or(
                isNotBlank(authorField.textProperty()),
                isNotBlank(titleField.textProperty()),
                isNotBlank(descriptionField.textProperty()),
                notEqual(0, minPagesSpinner.valueProperty()),
                notEqual(0, maxPagesSpinner.valueProperty())
        ));
    }

    private void initializeTable() {
        final var columns = table.getColumns();
        initializeColumn(columns, 0, BookReference::author);
        initializeColumn(columns, 1, BookReference::title);
        initializeColumn(columns, 2, reference -> Float.toString(reference.score()));

        table.itemsProperty().bind(queryForListService.valueProperty());
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                final var reference = table.getSelectionModel().getSelectedItem();
                Platform.runLater(() -> showDetails(reference));
            }
        });
    }

    private void initializeColumn(final List<TableColumn<BookReference, ?>> columns,
                                  final int columnIndex,
                                  final Function<BookReference, String> extractor) {
        final var column = (TableColumn<BookReference, String>) columns.get(columnIndex);
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(extractor.apply(param.getValue())));
    }

    private void initializeServices() {
        queryForBookService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> showDetails(newValue));
            }
        });
    }

    private void showDetails(final BookReference reference) {
        try {
            showDetails(FILE_SERVICE.readBook(reference));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails(final Book book) {
        final var loader = new FXMLLoader(MainController.class.getResource("details.fxml"));
        try {
            final DialogPane dialogPane = loader.load();

            DetailsController controller = loader.getController();
            controller.initialize(book);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(book.title());
            dialog.setDialogPane(dialogPane);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleMaybeSearchAction(final KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            handleSearchAction();
        }
    }

    @FXML
    public void handleSearchAction() {
        if (isbnField.getText().isBlank()) {
            startService(queryForListService);
        } else {
            startService(queryForBookService);
        }
    }

    @FXML
    public void handleFileIndexAction() {
        startService(indexService);
    }

    @FXML
    public void handleFileExitAction() {
        Platform.exit();
    }

    private void startService(final Service<?> service) {
        progressBar.visibleProperty().bind(service.runningProperty());
        service.restart();
    }
}
