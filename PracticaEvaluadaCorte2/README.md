# PracticaEvaluadaCorte2 – Catálogo de productos (Papelería Campus)

Laboratorio JavaFX + PostgreSQL (TableView y CRUD).

**Autor:** _Eduardo Madriz_

## Funcionalidades
- Listado en `TableView` (ID, código, nombre, categoría, precio, stock), orden por código y contador de registros visibles.
- Búsqueda por texto parcial en nombre o código (sin distinguir mayúsculas), filtro por categoría y "Solo con existencias", combinados con AND. Botones Buscar, Limpiar filtros y Recargar.
- Formulario con Nuevo/Limpiar, Guardar, Actualizar y Eliminar (con confirmación). El ID no se edita.
- Validaciones de código, nombre, categoría, precio (máximo dos decimales, sin redondear) y stock; códigos duplicados rechazados.
- Tras cada escritura se consulta de nuevo PostgreSQL y se conservan los filtros aplicados.
- Errores de conexión y de restricciones SQL con mensajes claros; la operación y el SQLState se registran en consola.

## Versiones y dependencias
JDK 21.0.12 · Maven 3.9.16 · JavaFX 21.0.6 (`javafx-controls`, `javafx-fxml`) · PostgreSQL 18 (puerto 5432) · pgJDBC 42.7.4

## Base de datos
1. En pgAdmin crear la base `papeleria_campus` y seleccionarla.
2. Ejecutar `sql/estructura.sql` y luego `sql/datos.sql` (12 productos iniciales). La aplicación no ejecuta estos scripts.
3. Para restaurar los datos de prueba: eliminar de `public.producto` solo las filas añadidas (por ejemplo `DELETE FROM public.producto WHERE codigo = 'X001';`) o, en una base de pruebas vacía, volver a ejecutar `sql/datos.sql`.

Consultas de verificación en pgAdmin:
- `SELECT COUNT(*) FROM public.producto;` → 12 con los datos iniciales.
- `SELECT * FROM public.producto WHERE codigo = 'X001';` → la fila de prueba mientras exista.

## Configuración de la conexión
1. Copiar `config/db.properties.example` como `config/db.properties` (archivo excluido de Git y del ZIP).
2. Completar `db.password`; ajustar host, puerto, base y usuario si es necesario.

## Ejecución desde IntelliJ IDEA
1. Abrir la carpeta del proyecto e importar `pom.xml` (SDK 21).
2. Ejecutar `org.example.practicaevaluadacorte2.Launcher`. Alternativa: `mvn javafx:run`.

## Organización de clases
| Clase | Responsabilidad |
|---|---|
| `model/Producto` | Datos de una fila (precio `BigDecimal`). |
| `database/ConexionBD` | Lee `config/db.properties` y abre la conexión JDBC. |
| `dao/ProductoDAO` | Todo el SQL: listar, existeCodigo, insertar, actualizar, eliminar. |
| `controller/ProductoController` | Eventos, validaciones, mensajes y carga de la tabla. |
| `ProductoApplication` / `Launcher` | Arranque de JavaFX. |
| `producto-view.fxml` | Vista. |

## Flujo de datos
Interfaz (botón) → `ProductoController` lee y valida los campos → `ProductoDAO` ejecuta la sentencia con `PreparedStatement` en PostgreSQL → el controlador vuelve a consultar con los filtros vigentes → cada fila del `ResultSet` se convierte en `Producto` → `ObservableList` → `TableView` y contador.

## Pruebas
Resultados en `pruebas.md`; evidencias en `evidencias/`. Las comprobaciones se hicieron por código; la verificación con mouse y teclado está indicada como pendiente.

## Fuentes y herramientas de apoyo
Enunciado del laboratorio (PDF), documentación de pgJDBC, OpenJFX y PostgreSQL. Claude Code (asistente de IA de Anthropic) se usó para generar y revisar el código, las comprobaciones y la documentación.
