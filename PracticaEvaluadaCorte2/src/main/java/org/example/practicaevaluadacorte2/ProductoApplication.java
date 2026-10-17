package org.example.practicaevaluadacorte2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProductoApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ProductoApplication.class.getResource("producto-view.fxml"));
        stage.setTitle("Papelería Campus - Catálogo de productos");
        stage.setScene(new Scene(loader.load(), 980, 620));
        stage.show();
    }
}
