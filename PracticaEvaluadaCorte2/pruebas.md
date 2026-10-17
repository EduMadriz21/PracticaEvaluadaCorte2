# Pruebas – Laboratorio TableView CRUD

Base real `papeleria_campus` (PostgreSQL 18.6, localhost:5432). Nunca se recreó la base, no se ejecutaron los scripts SQL ni se usó TRUNCATE. Al terminar cada bloque quedaron los 12 productos iniciales.

## Cómo se comprobó
- **Por código (2026-10-17):** una sonda JavaFX temporal, fuera del proyecto, carga el FXML y el controlador reales, abre la ventana y dispara los botones (incluidos Eliminar y Cancelar del cuadro de confirmación) contra la base real. Resultado completo: `evidencias/registro-comprobaciones.txt` (76 comprobaciones, 76 aprobadas, 0 fallidas).
- **Con mouse y teclado:** **pendiente**, lo realiza el autor (ver al final).
- Las capturas de `evidencias/` son instantáneas de la ventana generadas por JavaFX desde esa misma ejecución; no son capturas de pantalla hechas a mano.

## Historial (2026-10-16, avance 1)
| Verificación | Resultado |
|---|---|
| Pruebas automáticas del DAO (clase `ProductoDAOTest`, ya retirada) | 8 ejecutadas, 8 aprobadas: carga inicial, B1–B6 y `%` como texto. |
| Estructura y datos frente al PDF (metadatos en solo lectura) | Seis campos y tipos, IDENTITY, NOT NULL, UNIQUE, cinco CHECK y 12 registros coinciden con el PDF y con `sql/`. |

## Búsqueda y filtros (B1–B7)
| Caso | Entrada | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| B1 | `CUADERNO` | P001, P002; contador 2 | P001, P002; 2 | Aprobada |
| B2 | `T00` | T001–T004; contador 4 | T001–T004; 4 | Aprobada |
| B3 | Papelería | P001–P004; contador 4 | P001–P004; 4 | Aprobada |
| B4 | Solo con existencias | 9 productos, sin P004, T004, A003 | 9 productos, sin esos tres | Aprobada |
| B5 | `USB` + Tecnología + existencias | T001, T003; contador 2 | T001, T003; 2 | Aprobada (`evidencias/02-filtro-combinado-B5.png`) |
| B6 | `ZZZ999` | 0 filas, contador 0, "Sin coincidencias" | 0 filas, 0, "Sin coincidencias" | Aprobada |
| B7 | Limpiar filtros tras B5 | controles restablecidos, 12 filas | texto vacío, Todas, sin marca, 12 filas | Aprobada |
| Carga inicial | abrir | 12 filas, precios con dos decimales, contador 12 | igual | Aprobada (`evidencias/01-listado-inicial.png`) |
| Recargar, selección, Nuevo | con filtro B5 | Recargar conserva filtros; selección carga el formulario con ID no editable; Nuevo limpia sin quitar filtros | igual | Aprobada |

## Ciclo CRUD (dos ejecuciones separadas; la segunda equivale a reiniciar la aplicación)
| Paso | Esperado | Obtenido | Estado |
|---|---|---|---|
| Precondición | 12 filas y X001 ausente | 12 y ausente | Aprobada |
| Crear X001, Producto de prueba, Accesorios, 10.00, stock 1 | 13 filas; tabla y contador actualizados | 13; mensaje "Producto guardado." | Aprobada (`04-crud-crear-13-registros.png`) |
| Actualizar a precio 15.50 y stock 3 | 13 filas; solo X001 cambia | 13; X001 = 15.50 / 3; P001 y T003 intactos | Aprobada (`05-crud-actualizar-precio-15.50-stock-3.png`) |
| Reiniciar la aplicación | X001 persiste con 15.50 / 3 | persiste; carga 13 filas | Aprobada |
| Cancelar eliminación | X001 permanece | permanece, 13 filas | Aprobada |
| Filtro activo (Accesorios + existencias) y actualizar stock a 0 | X001 desaparece del listado y los filtros se conservan | desaparece; BD conserva X001 con stock 0 | Aprobada |
| Confirmar eliminación | 12 filas, solo se borra X001 | 12; X001 ausente; iniciales intactos | Aprobada (`06-crud-eliminar-12-registros.png`) |

## Validaciones (V1–V10), al crear y al actualizar
Cada rechazo mostró un mensaje concreto, la aplicación siguió abierta y el número de filas y los datos no cambiaron.

| Grupo | Entradas | Obtenido | Estado |
|---|---|---|---|
| V1 | código vacío, solo espacios, 11 caracteres | "El código debe tener entre 1 y 10 caracteres." | Aprobada |
| V2 | nombre en espacios, `AB`, 81 caracteres | "El nombre debe tener entre 3 y 80 caracteres." | Aprobada |
| V3 | categoría sin seleccionar (el ComboBox no permite escribir otra) | "Seleccione una categoría…" | Aprobada |
| V4 | precio `abc` | "El precio debe ser un número con punto decimal…" | Aprobada |
| V5 | precio `0` y `-1` | "El precio debe estar entre 0.01 y 99999999.99." | Aprobada |
| V6 | `12.345` y `100000000.00` | "máximo dos decimales" / "entre 0.01 y 99999999.99" (sin redondear) | Aprobada (`03-validacion-precio-12.345.png`) |
| V7 | stock `abc` y `2.5` | "El stock debe ser un número entero…" | Aprobada |
| V8 | stock `-1`, `2147483648`; luego `0` | rechaza los dos primeros; `0` se acepta | Aprobada |
| V9 | crear con `" p001 "`; actualizar a `P001`; actualizar conservando el propio código | rechaza los dos duplicados; el propio código se permite | Aprobada (el caso del propio código se probó con X001 para no tocar P001) |
| V10 | Actualizar y Eliminar sin selección | "Seleccione un producto de la tabla…" | Aprobada |

## Excepciones (E1–E4)
| Caso | Prueba | Obtenido | Estado |
|---|---|---|---|
| E1 | configuración temporal con puerto 59999 | aviso comprensible, tabla vacía, contador "-", la ventana sigue abierta, no se muestra "Sin coincidencias" | Aprobada |
| E2 | llamadas directas a `ProductoDAO.insertar` (sin validación de interfaz) con código duplicado, código en minúsculas, precio 0 y categoría ajena | `SQLException` con SQLState 23505 / 23514; la tabla sigue con 12 filas | Aprobada |
| E3 | producto temporal borrado directamente en la base; luego Actualizar y Eliminar desde la aplicación | "El producto ya no existe… Pulse Recargar"; sin mensaje de éxito; Recargar funciona | Aprobada |
| E4 | registro en consola | `Operación: listar productos. SQLState=08001. …` sin contraseña; la ventana muestra un mensaje sin traza técnica | Aprobada |

## Pendiente (lo comprueba el autor)
1. Con mouse y teclado en IntelliJ (`Launcher`): B1–B7, selección con flechas, validaciones, ciclo CRUD y el cuadro de confirmación de Eliminar (Cancelar y Eliminar).
2. Aspecto de la ventana en pantalla real (legibilidad, tamaño).
3. E1 a mano: poner una contraseña o un puerto incorrecto en `config/db.properties`, abrir la aplicación y restaurar el valor.
4. Verificar con `SELECT` en pgAdmin (consultas en el README) y, si lo pide el docente, tomar capturas propias.
