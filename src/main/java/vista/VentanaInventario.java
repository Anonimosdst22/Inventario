package vista;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import model.Categoria;
import model.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal. No contiene SQL: solo habla con los DAO.
 */
public class VentanaInventario extends JFrame {

    private static final int STOCK_BAJO = 5;
    private static final String TODAS = "Todas";

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtPrecio = new JTextField(20);
    private final JTextField txtCantidad = new JTextField(20);
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private final JComboBox<Object> cmbFiltro = new JComboBox<>();

    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Precio", "Cant.", "Categoría"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    /** id del producto seleccionado en la tabla (-1 = ninguno). */
    private int idSeleccionado = -1;
    private boolean cargando = false;

    public VentanaInventario() {
        super("Inventario — Tienda de Don Ernesto");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarCategorias();
        refrescarTabla();
    }

    // ---------------------------------------------------------------- UI

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        agregarFila(form, gbc, 0, "Nombre:", txtNombre);
        agregarFila(form, gbc, 1, "Precio:", txtPrecio);
        agregarFila(form, gbc, 2, "Cantidad:", txtCantidad);
        agregarFila(form, gbc, 3, "Categoría:", cmbCategoria);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // Tabla + filtro
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(3).setMaxWidth(70);
        tabla.setDefaultRenderer(Object.class, new RenderizadorStockBajo());

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Filtrar por categoría:"));
        filtro.add(cmbFiltro);
        filtro.add(new JLabel("   (en rojo: menos de " + STOCK_BAJO + " unidades)"));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        centro.add(filtro, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        // Eventos
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        cmbFiltro.addActionListener(e -> {
            if (!cargando) {
                refrescarTabla();
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !cargando) {
                cargarFilaEnFormulario();
            }
        });
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
        panel.add(campo, gbc);
    }

    // -------------------------------------------------------------- Datos

    private void cargarCategorias() {
        try {
            List<Categoria> categorias = categoriaDAO.listar();
            cargando = true;
            cmbCategoria.removeAllItems();
            cmbFiltro.removeAllItems();
            cmbFiltro.addItem(TODAS);
            for (Categoria c : categorias) {
                cmbCategoria.addItem(c);
                cmbFiltro.addItem(c);
            }
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        } finally {
            cargando = false;
        }
    }

    private void refrescarTabla() {
        try {
            List<Producto> productos;
            Object filtro = cmbFiltro.getSelectedItem();
            if (filtro instanceof Categoria) {
                productos = productoDAO.listarPorCategoria(((Categoria) filtro).getIdCategoria());
            } else {
                productos = productoDAO.listar();
            }

            cargando = true;
            modeloTabla.setRowCount(0);
            for (Producto p : productos) {
                modeloTabla.addRow(new Object[]{
                        p.getIdProducto(), p.getNombre(), p.getPrecio(),
                        p.getCantidad(), p.getCategoria().getNombre()});
            }
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        } finally {
            cargando = false;
        }
        idSeleccionado = -1;
    }

    private void cargarFilaEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        try {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            Producto p = productoDAO.buscarPorId(id);
            if (p == null) {
                mostrarError("El producto ya no existe en la base de datos.");
                refrescarTabla();
                return;
            }
            idSeleccionado = p.getIdProducto();
            txtNombre.setText(p.getNombre());
            txtPrecio.setText(String.valueOf(p.getPrecio()));
            txtCantidad.setText(String.valueOf(p.getCantidad()));
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                if (cmbCategoria.getItemAt(i).getIdCategoria() == p.getCategoria().getIdCategoria()) {
                    cmbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // ------------------------------------------------------------ Acciones

    private void guardar() {
        Producto p = leerFormulario();
        if (p == null) {
            return;
        }
        try {
            if (productoDAO.insertar(p)) {
                refrescarTabla();
                limpiarFormulario();
                JOptionPane.showMessageDialog(this, "Producto guardado.");
            }
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void actualizar() {
        if (idSeleccionado < 0) {
            mostrarError("Seleccione primero un producto de la tabla.");
            return;
        }
        Producto p = leerFormulario();
        if (p == null) {
            return;
        }
        p.setIdProducto(idSeleccionado);
        try {
            if (productoDAO.actualizar(p)) {
                refrescarTabla();
                limpiarFormulario();
                JOptionPane.showMessageDialog(this, "Producto actualizado.");
            }
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado < 0) {
            mostrarError("Seleccione primero un producto de la tabla.");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar este producto?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (productoDAO.eliminar(idSeleccionado)) {
                refrescarTabla();
                limpiarFormulario();
            }
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtPrecio.setText("");
        txtCantidad.setText("");
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        idSeleccionado = -1;
        tabla.clearSelection();
        txtNombre.requestFocus();
    }

    /** Valida los campos y arma el Producto; si algo falla muestra el error y retorna null. */
    private Producto leerFormulario() {
        String nombre = txtNombre.getText().trim();
        String textoPrecio = txtPrecio.getText().trim().replace(',', '.');
        String textoCantidad = txtCantidad.getText().trim();
        Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();

        if (nombre.isEmpty() || textoPrecio.isEmpty() || textoCantidad.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.");
            return null;
        }
        if (categoria == null) {
            mostrarError("No hay una categoría seleccionada.");
            return null;
        }
        try {
            Producto p = new Producto();
            p.setNombre(nombre);
            p.setPrecio(Double.parseDouble(textoPrecio));
            p.setCantidad(Integer.parseInt(textoCantidad));
            p.setCategoria(categoria);
            return p;
        } catch (NumberFormatException ex) {
            mostrarError("El precio debe ser un número y la cantidad un entero.");
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
        return null;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ----------------------------------------------------------- Renderizador

    /** Pinta de rojo claro las filas con cantidad menor a STOCK_BAJO. */
    private class RenderizadorStockBajo extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            int cantidad = (int) modeloTabla.getValueAt(t.convertRowIndexToModel(fila), 3);
            if (!seleccionada) {
                c.setBackground(cantidad < STOCK_BAJO ? new Color(255, 205, 205) : t.getBackground());
            }
            return c;
        }
    }
}
