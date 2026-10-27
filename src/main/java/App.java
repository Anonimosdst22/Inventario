import conexion.Conexion;
import vista.VentanaInventario;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;

public class App {

    public static void main(String[] args) {
        // Prueba de conexión
        try (Connection con = Conexion.getConexion()) {
            System.out.println("Conexión exitosa");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // se usa el look and feel por defecto
        }
        SwingUtilities.invokeLater(() -> new VentanaInventario().setVisible(true));
    }
}
