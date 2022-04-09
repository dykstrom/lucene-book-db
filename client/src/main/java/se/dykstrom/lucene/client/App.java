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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main application.
 */
public class App extends Application {

    @Override
    public void start(final Stage stage) throws IOException {
        final var loader = new FXMLLoader(App.class.getResource("main.fxml"));
        final var scene = new Scene(loader.load(), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Lucene Book Search");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
