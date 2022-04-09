module se.dykstrom.lucene.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires se.dykstrom.lucene.services;

    opens se.dykstrom.lucene.client to javafx.fxml;
    opens se.dykstrom.lucene.client.util to javafx.fxml;

    exports se.dykstrom.lucene.client;
    exports se.dykstrom.lucene.client.util;
}
