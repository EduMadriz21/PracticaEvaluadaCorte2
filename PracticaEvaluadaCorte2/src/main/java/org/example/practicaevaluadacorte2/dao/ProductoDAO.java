package org.example.practicaevaluadacorte2.dao;

import org.example.practicaevaluadacorte2.database.ConexionBD;
import org.example.practicaevaluadacorte2.model.Producto;

import java.math.BigDecimal;
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

    private static final String SQL_EXISTE_CODIGO = """
            SELECT 1 FROM public.producto WHERE codigo = ? AND id_producto <> ?
            """;

    private static final String SQL_INSERTAR = """
            INSERT INTO public.producto (codigo, nombre, categoria, precio, stock)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_ACTUALIZAR = """
            UPDATE public.producto
            SET codigo = ?, nombre = ?, categoria = ?, precio = ?, stock = ?
            WHERE id_producto = ?
            """;

    private static final String SQL_ELIMINAR = "DELETE FROM public.producto WHERE id_producto = ?";

    // idExcluir permite que un producto conserve su propio código al actualizar; usar 0 al crear.
    public boolean existeCodigo(String codigo, int idExcluir) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_EXISTE_CODIGO)) {
            sentencia.setString(1, codigo);
            sentencia.setInt(2, idExcluir);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next();
            }
        }
    }

    public int insertar(String codigo, String nombre, String categoria, BigDecimal precio, int stock) throws SQLException {
        return escribir(SQL_INSERTAR, codigo, nombre, categoria, precio, stock);
    }

    // Devuelve las filas afectadas: 0 significa que el producto ya no existe.
    public int actualizar(int id, String codigo, String nombre, String categoria, BigDecimal precio, int stock) throws SQLException {
        return escribir(SQL_ACTUALIZAR, codigo, nombre, categoria, precio, stock, id);
    }

    public int eliminar(int id) throws SQLException {
        return escribir(SQL_ELIMINAR, id);
    }

    private int escribir(String sql, Object... valores) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                sentencia.setObject(i + 1, valores[i]);
            }
            return sentencia.executeUpdate();
        }
    }

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
