package org.example.practicaevaluadacorte2.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.practicaevaluadacorte2.dao.ProductoDAO;
import org.example.practicaevaluadacorte2.model.Producto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductoController {

    private static final Logger LOG = Logger.getLogger(ProductoController.class.getName());
    private static final String TODAS = "Todas";
    private static final List<String> CATEGORIAS = List.of("Papelería", "Tecnología", "Accesorios");
    private static final BigDecimal PRECIO_MINIMO = new BigDecimal("0.01");
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");
    private static final BigInteger STOCK_MAXIMO = BigInteger.valueOf(Integer.MAX_VALUE);

    private record DatosFormulario(String codigo, String nombre, String categoria, BigDecimal precio, int stock) {
    }

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroCategoria;
    @FXML private CheckBox chkSoloExistencias;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private Label lblContador;
    @FXML private Label lblMensaje;

    @FXML private TextField txtId;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    // Filtros aplicados por última vez con Buscar; Recargar y las operaciones de escritura los reutilizan.
    private String filtroTexto = "";
    private String filtroCategoria = null;
    private boolean filtroSoloExistencias = false;

    private Integer idSeleccionado = null;

    @FXML
    private void initialize() {
        configurarColumnas();
        configurarControles();
        tblProductos.setItems(productos);
        tblProductos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> mostrarEnFormulario(actual));
        consultar();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPrecio.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean vacio) {
                super.updateItem(precio, vacio);
                setText(vacio || precio == null ? null : precio.setScale(2).toPlainString());
            }
        });
    }

    private void configurarControles() {
        cbFiltroCategoria.getItems().add(TODAS);
        cbFiltroCategoria.getItems().addAll(CATEGORIAS);
        cbFiltroCategoria.setValue(TODAS);
        cbCategoria.getItems().addAll(CATEGORIAS);
        cbCategoria.setPromptText("Seleccione");
        txtId.setEditable(false);
    }

    @FXML
    private void onBuscar() {
        filtroTexto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim();
        String categoria = cbFiltroCategoria.getValue();
        filtroCategoria = (categoria == null || TODAS.equals(categoria)) ? null : categoria;
        filtroSoloExistencias = chkSoloExistencias.isSelected();
        consultar();
    }

    @FXML
    private void onLimpiarFiltros() {
        txtBuscar.clear();
        cbFiltroCategoria.setValue(TODAS);
        chkSoloExistencias.setSelected(false);
        onBuscar();
    }

    @FXML
    private void onRecargar() {
        consultar();
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
    }

    @FXML
    private void onGuardar() {
        DatosFormulario datos = leerFormulario();
        if (datos == null) {
            return;
        }
        try {
            if (productoDAO.existeCodigo(datos.codigo(), 0)) {
                mostrarMensaje("Ya existe un producto con el código " + datos.codigo() + ".", true);
                return;
            }
            productoDAO.insertar(datos.codigo(), datos.nombre(), datos.categoria(), datos.precio(), datos.stock());
        } catch (SQLException e) {
            avisarError("guardar producto", e);
            return;
        }
        terminarEscritura("Producto guardado.");
    }

    @FXML
    private void onActualizar() {
        if (idSeleccionado == null) {
            mostrarMensaje("Seleccione un producto de la tabla para actualizarlo.", true);
            return;
        }
        DatosFormulario datos = leerFormulario();
        if (datos == null) {
            return;
        }
        try {
            if (productoDAO.existeCodigo(datos.codigo(), idSeleccionado)) {
                mostrarMensaje("Ya existe otro producto con el código " + datos.codigo() + ".", true);
                return;
            }
            int filas = productoDAO.actualizar(idSeleccionado, datos.codigo(), datos.nombre(),
                    datos.categoria(), datos.precio(), datos.stock());
            if (filas == 0) {
                avisarProductoInexistente();
                return;
            }
        } catch (SQLException e) {
            avisarError("actualizar producto", e);
            return;
        }
        terminarEscritura("Producto actualizado.");
    }

    @FXML
    private void onEliminar() {
        if (idSeleccionado == null) {
            mostrarMensaje("Seleccione un producto de la tabla para eliminarlo.", true);
            return;
        }
        if (!confirmarEliminacion()) {
            return;
        }
        try {
            if (productoDAO.eliminar(idSeleccionado) == 0) {
                avisarProductoInexistente();
                return;
            }
        } catch (SQLException e) {
            avisarError("eliminar producto", e);
            return;
        }
        terminarEscritura("Producto eliminado.");
    }

    private boolean confirmarEliminacion() {
        ButtonType eliminar = new ButtonType("Eliminar", ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto " + txtCodigo.getText() + " - " + txtNombre.getText() + "?", eliminar, cancelar);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText(null);
        alerta.initOwner(tblProductos.getScene().getWindow());
        return alerta.showAndWait().filter(boton -> boton == eliminar).isPresent();
    }

    // Tras una escritura exitosa se vuelve a consultar PostgreSQL con los filtros vigentes.
    private void terminarEscritura(String mensaje) {
        limpiarFormulario();
        if (consultar()) {
            mostrarMensaje(mensaje, false);
        }
    }

    private void avisarProductoInexistente() {
        mostrarMensaje("El producto ya no existe en la base de datos. Pulse Recargar para actualizar el listado.", true);
    }

    // Devuelve null (y muestra el motivo) si algún campo no es válido; no toca la base de datos.
    private DatosFormulario leerFormulario() {
        String codigo = txtCodigo.getText().trim().toUpperCase(Locale.ROOT);
        if (codigo.isEmpty() || codigo.length() > 10) {
            return rechazar("El código debe tener entre 1 y 10 caracteres.");
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.length() < 3 || nombre.length() > 80) {
            return rechazar("El nombre debe tener entre 3 y 80 caracteres.");
        }
        String categoria = cbCategoria.getValue();
        if (categoria == null || !CATEGORIAS.contains(categoria)) {
            return rechazar("Seleccione una categoría: Papelería, Tecnología o Accesorios.");
        }
        BigDecimal precio = leerPrecio();
        if (precio == null) {
            return null;
        }
        Integer stock = leerStock();
        if (stock == null) {
            return null;
        }
        return new DatosFormulario(codigo, nombre, categoria, precio, stock);
    }

    // No se redondea: 12.345 se rechaza en lugar de guardarse como 12.35.
    private BigDecimal leerPrecio() {
        String texto = txtPrecio.getText().trim();
        if (!texto.matches("-?\\d+(\\.\\d+)?")) {
            mostrarMensaje("El precio debe ser un número con punto decimal, por ejemplo 12.50.", true);
            return null;
        }
        BigDecimal precio = new BigDecimal(texto);
        if (precio.stripTrailingZeros().scale() > 2) {
            mostrarMensaje("El precio admite como máximo dos decimales.", true);
            return null;
        }
        if (precio.compareTo(PRECIO_MINIMO) < 0 || precio.compareTo(PRECIO_MAXIMO) > 0) {
            mostrarMensaje("El precio debe estar entre 0.01 y 99999999.99.", true);
            return null;
        }
        return precio.setScale(2);
    }

    private Integer leerStock() {
        String texto = txtStock.getText().trim();
        if (!texto.matches("-?\\d+")) {
            mostrarMensaje("El stock debe ser un número entero, sin decimales ni letras.", true);
            return null;
        }
        BigInteger stock = new BigInteger(texto);
        if (stock.signum() < 0 || stock.compareTo(STOCK_MAXIMO) > 0) {
            mostrarMensaje("El stock debe estar entre 0 y 2147483647.", true);
            return null;
        }
        return stock.intValue();
    }

    private DatosFormulario rechazar(String mensaje) {
        mostrarMensaje(mensaje, true);
        return null;
    }

    private boolean consultar() {
        try {
            List<Producto> resultado = productoDAO.listar(filtroTexto, filtroCategoria, filtroSoloExistencias);
            productos.setAll(resultado);
            lblContador.setText("Registros visibles: " + resultado.size());
            mostrarMensaje(resultado.isEmpty() ? "Sin coincidencias" : "", false);
            return true;
        } catch (SQLException e) {
            // Un fallo no se muestra como "0 resultados": se vacía la tabla y se avisa del error.
            productos.clear();
            lblContador.setText("Registros visibles: -");
            avisarError("listar productos", e);
            return false;
        }
    }

    private void avisarError(String operacion, SQLException e) {
        LOG.log(Level.WARNING, "Operación: {0}. SQLState={1}. {2}", new Object[]{operacion, e.getSQLState(), e.getMessage()});
        mostrarMensaje(mensajeParaUsuario(e), true);
    }

    private String mensajeParaUsuario(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        if (estado.startsWith("08")) {
            return "No se pudo conectar con PostgreSQL. Verifique que el servidor esté activo y el host, puerto y contraseña en config/db.properties.";
        }
        if (estado.startsWith("28")) {
            return "PostgreSQL rechazó el usuario o la contraseña. Revise config/db.properties.";
        }
        if ("3D000".equals(estado)) {
            return "La base de datos configurada no existe. Revise db.name en config/db.properties.";
        }
        if ("23505".equals(estado)) {
            return "Ya existe un producto con ese código.";
        }
        if ("23514".equals(estado)) {
            return "Los datos no cumplen las restricciones de la base de datos (código, nombre, categoría, precio o stock).";
        }
        if ("23502".equals(estado)) {
            return "Falta un dato obligatorio.";
        }
        if (estado.isEmpty()) {
            return e.getMessage();
        }
        return "Error al operar con la base de datos (código " + estado + ").";
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #b00020;" : "-fx-text-fill: #555555;");
    }

    private void limpiarFormulario() {
        tblProductos.getSelectionModel().clearSelection();
        mostrarEnFormulario(null);
    }

    private void mostrarEnFormulario(Producto p) {
        idSeleccionado = p == null ? null : p.getIdProducto();
        txtId.setText(p == null ? "" : String.valueOf(p.getIdProducto()));
        txtCodigo.setText(p == null ? "" : p.getCodigo());
        txtNombre.setText(p == null ? "" : p.getNombre());
        cbCategoria.setValue(p == null ? null : p.getCategoria());
        txtPrecio.setText(p == null ? "" : p.getPrecio().setScale(2).toPlainString());
        txtStock.setText(p == null ? "" : String.valueOf(p.getStock()));
    }
}
