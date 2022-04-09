module se.dykstrom.lucene.services {
    requires com.fasterxml.jackson.databind;
    requires org.apache.lucene.core;
    requires org.apache.lucene.queryparser;

    opens se.dykstrom.lucene.model to com.fasterxml.jackson.databind;

    exports se.dykstrom.lucene.model;
    exports se.dykstrom.lucene.service;
}
