package org.example.practicaevaluadacorte2.dao;

import org.example.practicaevaluadacorte2.database.ConexionBD;
import org.example.practicaevaluadacorte2.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // El OR de nombre/código va entre paréntesis para que los demás filtros se combinen con AND.
    // Categoría NULL o existencias false desactivan ese filtro.
    private static final String SQL_LISTAR = """
            SELECT id_producto, codigo, nombre, categoria, precio, stock
            FROM public.producto
            WHERE (nombre ILIKE ? OR codigo ILIKE ?)
              AND (?::varchar IS NULL OR categoria = ?)
              AND (? = FALSE OR stock > 0)
            ORDER BY codigo ASC
            """;

    public List<Producto> listar(String texto, String categoria, boolean soloConExistencias) throws SQLException {
        String patron = "%" + escaparComodines(texto.trim()) + "%";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR)) {
            sentencia.setString(1, patron);
            sentencia.setString(2, patron);
            sentencia.setString(3, categoria);
            sentencia.setString(4, categoria);
            sentencia.setBoolean(5, soloConExistencias);
            try (ResultSet filas = sentencia.executeQuery()) {
                return convertir(filas);
            }
        }
    }

    private List<Producto> convertir(ResultSet filas) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        while (filas.next()) {
            productos.add(new Producto(
                    filas.getInt("id_producto"),
                    filas.getString("codigo"),
                    filas.getString("nombre"),
                    filas.getString("categoria"),
                    filas.getBigDecimal("precio"),
                    filas.getInt("stock")));
        }
        return productos;
    }

    // % y _ escritos por el usuario se buscan como texto literal, no como comodines de LIKE.
    private String escaparComodines(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
