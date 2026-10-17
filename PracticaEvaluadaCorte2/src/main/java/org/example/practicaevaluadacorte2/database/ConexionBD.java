package org.example.practicaevaluadacorte2.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

    private static final Path ARCHIVO_CONFIG = Path.of("config", "db.properties");
    private static final int TIMEOUT_CONEXION_SEG = 5; // evita que la ventana quede colgada si el servidor no responde

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        Properties config = leerConfiguracion();
        String url = "jdbc:postgresql://" + config.getProperty("db.host", "localhost")
                + ":" + config.getProperty("db.port", "5432")
                + "/" + config.getProperty("db.name", "papeleria_campus");

        Properties propiedades = new Properties();
        propiedades.setProperty("user", config.getProperty("db.user", "postgres"));
        propiedades.setProperty("password", config.getProperty("db.password", ""));
        propiedades.setProperty("connectTimeout", String.valueOf(TIMEOUT_CONEXION_SEG));
        return DriverManager.getConnection(url, propiedades);
    }

    private static Properties leerConfiguracion() throws SQLException {
        Properties config = new Properties();
        try (InputStream in = Files.newInputStream(ARCHIVO_CONFIG)) {
            config.load(in);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer " + ARCHIVO_CONFIG + ". Copie db.properties.example a ese nombre y complete la contraseña.", e);
        }
        return config;
    }
}
