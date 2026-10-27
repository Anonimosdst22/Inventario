package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza la conexión a MySQL.
 * Cambie USUARIO y CLAVE según su instalación (XAMPP suele ser root sin clave).
 */
public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/tienda";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    private Conexion() {
    }

    /**
     * Abre una conexión nueva. Si falla, captura la SQLException y lanza una
     * RuntimeException con un mensaje claro para que la vista lo muestre.
     */
    public static Connection getConexion() {
        try {
            return DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (SQLException e) {
            String mensaje = "No se pudo conectar a MySQL (" + URL + "). "
                    + "Verifique que el servidor esté encendido, el usuario/clave y que exista la base 'tienda'. "
                    + "Detalle: " + e.getMessage();
            System.err.println(mensaje);
            throw new IllegalStateException(mensaje, e);
        }
    }
}
