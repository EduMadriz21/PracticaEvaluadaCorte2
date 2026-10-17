# Pruebas – Avance 1 (conexión, listado, búsqueda y filtros)

Revisión del 2026-10-16 con la base real `papeleria_campus` (PostgreSQL 18.6, localhost:5432). Todas las consultas fueron de lectura; no se modificó la base ni se ejecutaron los scripts SQL. No hay capturas incluidas.

## Resumen
| Verificación | Estado |
|---|---|
| Compilación (`mvn clean compile`, JDK 21, tras retirar las pruebas y JUnit) | **Comprobada** |
| Conexión JDBC | **Comprobada**: la aplicación consulta `public.producto`; hay exactamente 12 registros. |
| Consultas del DAO | **Comprobadas** de forma automática antes de retirar `ProductoDAOTest`: 8 ejecutadas, 8 aprobadas, 0 fallidas, 0 omitidas. La clase ya no existe en el proyecto. |
| Estructura y datos vs. PDF | **Comprobadas** por metadatos (ver abajo): sin diferencias. |
| Interfaz accionada por código | **Comprobada** con una sonda JavaFX temporal (carga el FXML y el controlador reales, abre la ventana y dispara los botones). No es interacción manual ni revisión visual. |
| Interfaz con mouse y teclado | **Pendiente** (lista al final). |

## Consultas del DAO (comprobación automática anterior a la retirada de `ProductoDAOTest`)
Resultado de esa ejecución con la base real; no son pruebas manuales ni se repiten con `mvn test`.

| Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| Carga inicial | sin filtros | 12 filas, primera A001 | 12 filas, primera A001 | Aprobada |
| B1 | `CUADERNO` | P001, P002 | P001, P002 | Aprobada |
| B2 | `T00` | T001–T004 | T001–T004 | Aprobada |
| B3 | Papelería | P001–P004 | P001–P004 | Aprobada |
| B4 | solo con existencias | 9, sin P004/T004/A003 | 9, sin P004/T004/A003 | Aprobada |
| B5 | `USB` + Tecnología + existencias | T001, T003 | T001, T003 | Aprobada |
| B6 | `ZZZ999` | 0 filas | 0 filas | Aprobada |
| Comodín | `%` | 0 filas (se busca como texto) | 0 filas | Aprobada |

## Estructura y datos de la base frente al PDF
Consultados `information_schema.columns`, `pg_constraint` y `public.producto` en una conexión de solo lectura.
- Seis campos y tipos: `id_producto` integer, `codigo` varchar(10), `nombre` varchar(80), `categoria` varchar(20), `precio` numeric(10,2), `stock` integer. Coincide.
- `id_producto`: IDENTITY `ALWAYS` y clave primaria. Coincide.
- NOT NULL en los cinco campos de entrada y UNIQUE en `codigo`. Coincide.
- Cinco CHECK (código normalizado de 1 a 10, nombre de 3 a 80, categoría permitida, precio > 0, stock >= 0). Coinciden.
- 12 registros: idénticos a `sql/datos.sql` (comparación automática de código, nombre, categoría, precio y stock) y a la tabla del PDF.
- `sql/estructura.sql` define lo mismo que la base real. No hubo errores de transcripción que corregir.

## Interfaz accionada por código (sonda JavaFX temporal, no incluida en el proyecto)
| Caso | Resultado obtenido | Estado |
|---|---|---|
| Columnas | ID, Código, Nombre, Categoría, Precio, Stock | Aprobada |
| Carga inicial | 12 filas por código; contador "Registros visibles: 12" | Aprobada |
| Precios | celdas con dos decimales (850.00, 95.00, 72.50…) | Aprobada |
| B1–B5 | mismas filas y contadores que en el DAO | Aprobada |
| B6 | 0 filas, contador 0, aviso "Sin coincidencias" | Aprobada |
| B7 | tras B5, Limpiar filtros: texto vacío, Todas, casilla desmarcada, 12 filas | Aprobada |
| Recargar | con filtros de B5 mantiene T001 y T003 | Aprobada |
| Selección | fila T003: ID 7, campos y categoría cargados; ID no editable | Aprobada |
| Nuevo/Limpiar formulario | formulario vacío, sin selección; filtros y tabla intactos | Aprobada |
| Guardar, Actualizar, Eliminar | deshabilitados | Aprobada |

## Pendiente de comprobar a mano en IntelliJ
1. Ejecutar `Launcher`: la ventana abre sin errores en la consola y se ve legible.
2. Probar B1–B7 con el mouse y teclado (Enter en Buscar también busca):

| Caso | Entrada | Resultado esperado |
|---|---|---|
| B1 | texto `CUADERNO` | P001 y P002; contador 2 |
| B2 | texto `T00` | T001 a T004; contador 4 |
| B3 | categoría Papelería | P001 a P004; contador 4 |
| B4 | Solo con existencias | 9 productos, sin P004, T004 y A003 |
| B5 | `USB` + Tecnología + Solo con existencias | T001 y T003; contador 2 |
| B6 | texto `ZZZ999` | 0 filas, contador 0, "Sin coincidencias" |
| B7 | Limpiar filtros después de B5 | controles restablecidos y 12 filas |

3. Seleccionar filas con mouse y flechas; comprobar que el ID no se puede escribir.
4. Mensaje de error de conexión: poner una contraseña incorrecta en `config/db.properties` y abrir la aplicación; debe verse un aviso en rojo, tabla vacía y no "Sin coincidencias". Restaurar la contraseña después.
5. Recargar tras agregar una fila temporal en pgAdmin (retirarla luego).
