module lee.journalj.journalj {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.xerial.sqlitejdbc;
    requires org.flywaydb.core;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.sql;
    requires java.logging;

    opens lee.journalj to javafx.fxml;
    opens lee.journalj.ui to javafx.fxml;
    opens lee.journalj.data.model to javafx.base;
    opens lee.journalj.data.util to org.flywaydb.core;
    exports lee.journalj;
    exports lee.journalj.ui;
    exports lee.journalj.data.model;

    // Разрешаем доступ к ресурсам
    opens db.migration to org.flywaydb.core;
}