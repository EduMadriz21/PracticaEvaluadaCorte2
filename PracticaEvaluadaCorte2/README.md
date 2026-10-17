# PracticaEvaluadaCorte2 – Catálogo de productos (Papelería Campus)

Laboratorio JavaFX + PostgreSQL (TableView). **Avance 1 de 3.**

## Alcance implementado (avance 1)
- Conexión JDBC a PostgreSQL.
- Listado en `TableView` (ID, código, nombre, categoría, precio, stock), orden por código, contador de registros visibles.
- Búsqueda por texto parcial (nombre **o** código, sin distinguir mayúsculas), filtro por categoría y "Solo con existencias", combinados con AND.
- Botones Buscar, Limpiar filtros, Recargar (conserva los filtros aplicados) y Nuevo/Limpiar formulario.
- Seleccionar una fila carga el formulario; el ID queda en un campo no editable.

## Versiones
JDK 21.0.12 · JavaFX 21.0.6 · Maven 3.9.16 · PostgreSQL 18 (puerto 5432) · pgJDBC 42.7.4

## Configuración de la conexión
1. Copiar `config/db.properties.example` a `config/db.properties` (ya existe una copia local).
2. Completar `db.password` en `config/db.properties`. Ese archivo está en `.gitignore`.
3. Host, puerto, base y usuario se ajustan en el mismo archivo.

La base `papeleria_campus` debe existir. Para crearla: crear la base en pgAdmin, seleccionarla y ejecutar `sql/estructura.sql` y luego `sql/datos.sql`. La aplicación **no** ejecuta estos scripts.
Para restaurar los 12 datos iniciales: vaciar la tabla (`TRUNCATE public.producto RESTART IDENTITY;`) y volver a ejecutar `sql/datos.sql`.

## Ejecución desde IntelliJ IDEA
1. Abrir la carpeta del proyecto y dejar que importe `pom.xml` (SDK 21).
2. Ejecutar `org.example.practicaevaluadacorte2.Launcher` (o `ProductoApplication`).
3. Alternativa en terminal: `mvn javafx:run`.
4. Verificar B1–B7 desde la ventana (ver `pruebas.md`).

## Organización de clases
| Clase | Responsabilidad |
|---|---|
| `model/Producto` | Datos de una fila (atributos, constructor, getters; precio `BigDecimal`). |
| `database/ConexionBD` | Lee `config/db.properties` y abre la conexión JDBC. |
| `dao/ProductoDAO` | Todo el SQL (`PreparedStatement`, `executeQuery`, try-with-resources). |
| `controller/ProductoController` | Eventos de la ventana, llama al DAO y llena la tabla. |
| `ProductoApplication` / `Launcher` | Arranque de JavaFX. |
| `producto-view.fxml` | Vista. |

## Flujo de datos
Interfaz (Buscar/Recargar) → `ProductoController` lee los controles → `ProductoDAO.listar(...)` ejecuta el `SELECT` con parámetros en PostgreSQL → cada fila del `ResultSet` se convierte en un `Producto` → la lista vuelve al controlador → `ObservableList` → `TableView` y contador.

## Fuentes y herramientas de apoyo
Enunciado del laboratorio (PDF), documentación de pgJDBC, OpenJFX y PostgreSQL, y Claude Code (asistente de IA) para generar y revisar el código de este avance.

## Pendiente (otras partes)
Validaciones del formulario, Guardar, Actualizar, Eliminar con confirmación, pruebas de CRUD/excepciones, evidencias y documentación final.
