package org.example.practicaevaluadacorte2.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductoController {

    private static final Logger LOG = Logger.getLogger(ProductoController.class.getName());
    private static final String TODAS = "Todas";
    private static final List<String> CATEGORIAS = List.of("Papelería", "Tecnología", "Accesorios");

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

    // Filtros aplicados por última vez con Buscar; Recargar los reutiliza.
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
        tblProductos.getSelectionModel().clearSelection();
        mostrarEnFormulario(null);
    }

    private void consultar() {
        try {
            List<Producto> resultado = productoDAO.listar(filtroTexto, filtroCategoria, filtroSoloExistencias);
            productos.setAll(resultado);
            lblContador.setText("Registros visibles: " + resultado.size());
            mostrarMensaje(resultado.isEmpty() ? "Sin coincidencias" : "", false);
        } catch (SQLException e) {
            // Un fallo no se muestra como "0 resultados": se vacía la tabla y se avisa del error.
            LOG.log(Level.WARNING, "Operación: listar productos. SQLState={0}. {1}", new Object[]{e.getSQLState(), e.getMessage()});
            productos.clear();
            lblContador.setText("Registros visibles: -");
            mostrarMensaje(mensajeParaUsuario(e), true);
        }
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
        if (estado.isEmpty()) {
            return e.getMessage();
        }
        return "Error al consultar la base de datos (código " + estado + ").";
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #b00020;" : "-fx-text-fill: #555555;");
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
