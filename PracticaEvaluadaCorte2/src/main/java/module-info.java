module org.example.practicaevaluadacorte2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;
    requires org.postgresql.jdbc;

    opens org.example.practicaevaluadacorte2 to javafx.fxml;
    opens org.example.practicaevaluadacorte2.controller to javafx.fxml;
    opens org.example.practicaevaluadacorte2.model to javafx.base;
    exports org.example.practicaevaluadacorte2;
}
