package dao;

import conexion.Conexion;
import model.Categoria;
import model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SELECT_BASE =
            "SELECT p.id_producto, p.nombre, p.precio, p.cantidad, "
            + "c.id_categoria, c.nombre AS nombre_categoria "
            + "FROM producto p "
            + "INNER JOIN categoria c ON p.id_categoria = c.id_categoria ";

    public boolean insertar(Producto p) {
        String sql = "INSERT INTO producto (nombre, precio, cantidad, id_categoria) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getCantidad());
            ps.setInt(4, p.getCategoria().getIdCategoria());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el producto: " + e.getMessage(), e);
        }
    }

    public List<Producto> listar() {
        String sql = SELECT_BASE + "ORDER BY p.id_producto";
        List<Producto> lista = new ArrayList<>();

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Producto> listarPorCategoria(int idCategoria) {
        String sql = SELECT_BASE + "WHERE p.id_categoria = ? ORDER BY p.id_producto";
        List<Producto> lista = new ArrayList<>();

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al filtrar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    public Producto buscarPorId(int id) {
        String sql = SELECT_BASE + "WHERE p.id_producto = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el producto: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean actualizar(Producto p) {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, cantidad = ?, id_categoria = ? "
                   + "WHERE id_producto = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getCantidad());
            ps.setInt(4, p.getCategoria().getIdCategoria());
            ps.setInt(5, p.getIdProducto());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el producto: " + e.getMessage(), e);
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id_producto = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el producto: " + e.getMessage(), e);
        }
    }

    /** Convierte una fila del JOIN en un Producto con su Categoria. */
    private Producto mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria(rs.getInt("id_categoria"), rs.getString("nombre_categoria"));
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getInt("cantidad"),
                c);
    }
}
