# Inventario — Tienda de Don Ernesto

Taller 8 · Programación 1 · Universidad Libre. Aplicación de escritorio (Java Swing + JDBC + MySQL) con patrón DAO.

## Requisitos
- JDK 11 o superior
- Maven
- MySQL (XAMPP, Workbench o consola) corriendo en `localhost:3306`

## Pasos para ejecutar
1. Ejecutar el script `sql/tienda.sql` en MySQL (crea la base `tienda`, las tablas y datos de prueba).
2. Si su usuario/clave de MySQL no son `root` / (vacía), editarlos en `src/main/java/conexion/Conexion.java`.
3. Desde la raíz del proyecto:
   ```
   mvn compile exec:java
   ```
   (o abrir el proyecto en NetBeans/IntelliJ y ejecutar la clase `App`).

## Estructura
`conexion` (Conexion) · `model` (Categoria, Producto) · `dao` (CategoriaDAO, ProductoDAO) · `vista` (VentanaInventario) · `App` (main)

Incluye el reto opcional: filtro por categoría y resaltado en rojo de productos con menos de 5 unidades.

Respuestas de reflexión del Ejercicio 6: ver `RESPUESTAS.md`.
