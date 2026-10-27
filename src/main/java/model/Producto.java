package model;

/**
 * Un Producto TIENE una Categoria (composición): guarda el objeto completo,
 * no solo el id, igual que la llave foránea id_categoria en la tabla.
 */
public class Producto {

    private int idProducto;
    private String nombre;
    private double precio;
    private int cantidad;
    private Categoria categoria;

    public Producto() {
    }

    public Producto(int idProducto, String nombre, double precio, int cantidad, Categoria categoria) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        setPrecio(precio);
        setCantidad(cantidad);
        this.categoria = categoria;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        this.precio = precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        this.cantidad = cantidad;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
