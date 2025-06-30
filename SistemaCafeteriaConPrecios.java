import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SistemaCafeteriaConPrecios extends JFrame {
    private static final int MAX_PEDIDOS = 50;
    private static final int MAX_ITEMS_POR_PEDIDO = 20;
    
    // Menú con precios
    private static final Map<String, Double> MENU = Map.of(
        "Café Americano", 2.50,
        "Café Latte", 3.00,
        "Cappuccino", 3.50,
        "Espresso", 2.00,
        "Té Verde", 2.00,
        "Té Negro", 1.80,
        "Croissant", 2.20,
        "Muffin", 2.50,
        "Sándwich", 4.50,
        "Ensalada", 5.00
    );
    
    private final Pedido[] pedidos = new Pedido[MAX_PEDIDOS];
    private int numPedidos = 0;
    private Pedido pedidoActual = null;
    
    // Componentes de la interfaz
    private JTextArea areaTexto;
    private DefaultListModel<String> modeloItems;
    private JList<String> listaItems;

    public SistemaCafeteriaConPrecios() {
        setTitle("Sistema de Cafetería con Precios");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        configurarInterfaz();
    }
    
    private void configurarInterfaz() {
        // Panel principal con fondo
        JPanel panelPrincipal = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(250, 240, 230);
                Color color2 = new Color(220, 200, 180);
                g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(6, 1, 10, 10));
        panelBotones.setOpaque(false);
        
        JButton btnNuevo = crearBoton("Nuevo Pedido", Color.decode("#4CAF50"), e -> nuevoPedido());
        JButton btnSiguiente = crearBoton("Siguiente Pedido", Color.decode("#FF9800"), e -> tomarPedido());
        JButton btnMarcar = crearBoton("Marcar Preparado", Color.decode("#FFC107"), e -> marcarPreparado());
        JButton btnPendientes = crearBoton("Pedidos Pendientes", Color.decode("#2196F3"), e -> verPendientes());
        JButton btnTotal = crearBoton("Calcular Total", Color.decode("#9C27B0"), e -> calcularTotal());
        JButton btnVerMenu = crearBoton("Ver Menú", Color.decode("#607D8B"), e -> verMenu());
        
        panelBotones.add(btnNuevo);
        panelBotones.add(btnSiguiente);
        panelBotones.add(btnMarcar);
        panelBotones.add(btnPendientes);
        panelBotones.add(btnTotal);
        panelBotones.add(btnVerMenu);
        
        // Área central
        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Ink Free", Font.PLAIN, 18));
        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        
        modeloItems = new DefaultListModel<>();
        listaItems = new JList<>(modeloItems);
        JScrollPane scrollItems = new JScrollPane(listaItems);
        scrollItems.setBorder(BorderFactory.createTitledBorder("Ítems del Pedido (Click para marcar)"));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTexto, scrollItems);
        splitPane.setResizeWeight(0.6);
        
        panelPrincipal.add(panelBotones, BorderLayout.WEST);
        panelPrincipal.add(splitPane, BorderLayout.CENTER);
        
        add(panelPrincipal);
    }
    
    private JButton crearBoton(String texto, Color color, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Ink Free", Font.BOLD, 18));
        boton.addActionListener(accion);
        return boton;
    }
    
    private void verMenu() {
        StringBuilder sb = new StringBuilder("=== MENÚ DISPONIBLE ===\n");
        MENU.forEach((item, precio) -> 
            sb.append(String.format("- %-15s $%.2f%n", item, precio))
        );
        areaTexto.setText(sb.toString());
    }
    
    private void nuevoPedido() {
        if (numPedidos >= MAX_PEDIDOS) {
            JOptionPane.showMessageDialog(this, "Límite de pedidos alcanzado", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialogo = new JDialog(this, "Nuevo Pedido", true);
        dialogo.setSize(450, 400);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel panelForm = new JPanel(new GridLayout(3, 1, 5, 5));
        panelForm.add(new JLabel("Cliente:"));
        JTextField txtCliente = new JTextField();
        panelForm.add(txtCliente);
        panelForm.add(new JLabel("Ítems (separados por coma):"));
        JTextArea txtItems = new JTextArea(3, 20);
        panelForm.add(new JScrollPane(txtItems));
        
        JButton btnConfirmar = new JButton("Confirmar");
        btnConfirmar.addActionListener(e -> {
            if (txtCliente.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Ingrese nombre de cliente", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String[] items = txtItems.getText().split(",");
            if (items.length == 0) {
                JOptionPane.showMessageDialog(dialogo, "Ingrese al menos un ítem", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Crear nuevo pedido
            Pedido nuevoPedido = new Pedido(txtCliente.getText().trim());
            
            // Agregar items válidos del menú
            for (String item : items) {
                String itemNombre = item.trim();
                if (!itemNombre.isEmpty() && MENU.containsKey(itemNombre)) {
                    nuevoPedido.agregarItem(itemNombre, MENU.get(itemNombre));
                }
            }
            
            if (nuevoPedido.getNumItems() == 0) {
                JOptionPane.showMessageDialog(dialogo, "No se agregaron ítems válidos del menú", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Almacenar en el array
            pedidos[numPedidos++] = nuevoPedido;
            areaTexto.append(String.format(
                "Nuevo pedido #%d para %s - %d ítems - Total: $%.2f%n",
                numPedidos, nuevoPedido.getCliente(), 
                nuevoPedido.getNumItems(), nuevoPedido.getTotal())
            );
            dialogo.dispose();
        });
        
        panel.add(panelForm, BorderLayout.CENTER);
        panel.add(btnConfirmar, BorderLayout.SOUTH);
        dialogo.add(panel);
        dialogo.setVisible(true);
    }
    
    private void tomarPedido() {
        if (pedidoActual != null && !pedidoActual.estaCompleto()) {
            JOptionPane.showMessageDialog(this, 
                "Complete el pedido actual primero: " + pedidoActual.getCliente(), 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Buscar el próximo pedido no atendido
        for (int i = 0; i < numPedidos; i++) {
            if (pedidos[i] != null && !pedidos[i].estaAtendido()) {
                pedidoActual = pedidos[i];
                pedidoActual.marcarAtendido();
                actualizarListaItems();
                areaTexto.append(String.format(
                    "Preparando pedido de %s - Total: $%.2f%n",
                    pedidoActual.getCliente(), pedidoActual.getTotal())
                );
                return;
            }
        }
        
        JOptionPane.showMessageDialog(this, "No hay pedidos pendientes", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void marcarPreparado() {
        if (pedidoActual == null) {
            JOptionPane.showMessageDialog(this, "No hay pedido en preparación", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int indice = listaItems.getSelectedIndex();
        if (indice == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String item = pedidoActual.getItems()[indice].getNombre();
        pedidoActual.marcarItemPreparado(indice);
        
        areaTexto.append("Ítem preparado: " + item + " - $" + 
            String.format("%.2f", pedidoActual.getItems()[indice].getPrecio()) + "\n");
        actualizarListaItems();
        
        if (pedidoActual.estaCompleto()) {
            areaTexto.append(String.format(
                "¡Pedido completado! Total final: $%.2f%n",
                pedidoActual.getTotal())
            );
            pedidoActual = null;
        }
    }
    
    private void verPendientes() {
        if (numPedidos == 0) {
            areaTexto.setText("No hay pedidos registrados\n");
            return;
        }
        
        StringBuilder sb = new StringBuilder("=== PEDIDOS PENDIENTES ===\n");
        int contador = 0;
        
        for (int i = 0; i < numPedidos; i++) {
            if (pedidos[i] != null && !pedidos[i].estaAtendido()) {
                sb.append(String.format(
                    "%d. %s - %d ítems - Total: $%.2f%n",
                    ++contador, pedidos[i].getCliente(),
                    pedidos[i].getNumItems(), pedidos[i].getTotal())
                );
            }
        }
        
        if (contador == 0) {
            sb.append("No hay pedidos pendientes\n");
        } else {
            sb.append(String.format("Total pendiente: $%.2f%n", 
                calcularTotalPendientes()));
        }
        
        areaTexto.setText(sb.toString());
    }
    
    private double calcularTotalPendientes() {
        double total = 0;
        for (int i = 0; i < numPedidos; i++) {
            if (pedidos[i] != null && !pedidos[i].estaAtendido()) {
                total += pedidos[i].getTotal();
            }
        }
        return total;
    }
    
    private void calcularTotal() {
        if (pedidoActual == null) {
            JOptionPane.showMessageDialog(this, "No hay pedido en preparación", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int itemsPreparados = 0;
        double totalPreparado = 0;
        double totalPedido = pedidoActual.getTotal();
        
        for (ItemPedido item : pedidoActual.getItems()) {
            if (item.estaPreparado()) {
                itemsPreparados++;
                totalPreparado += item.getPrecio();
            }
        }
        
        String mensaje = String.format("""
                                       Pedido de %s
                                       \u00cdtems totales: %d
                                       \u00cdtems preparados: %d
                                       Total preparado: $%.2f
                                       Total pendiente: $%.2f
                                       Total del pedido: $%.2f
                                       Porcentaje completado: %.0f%%""",
            pedidoActual.getCliente(),
            pedidoActual.getNumItems(),
            itemsPreparados,
            totalPreparado,
            totalPedido - totalPreparado,
            totalPedido,
            (itemsPreparados * 100.0 / pedidoActual.getNumItems())
        );
        
        JOptionPane.showMessageDialog(this, mensaje, 
            "Detalles del Pedido", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void actualizarListaItems() {
        modeloItems.clear();
        
        if (pedidoActual != null) {
            for (ItemPedido item : pedidoActual.getItems()) {
                modeloItems.addElement(String.format("%s %-20s $%.2f",
                    item.estaPreparado() ? "[✓]" : "[ ]",
                    item.getNombre(),
                    item.getPrecio())
                );
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SistemaCafeteriaConPrecios sistema = new SistemaCafeteriaConPrecios();
            sistema.setVisible(true);
        });
    }
}

class Pedido {
    private final String cliente;
    private final ItemPedido[] items;
    private boolean atendido;
    private int numItems;
    
    public Pedido(String cliente) {
        this.cliente = cliente;
        this.items = new ItemPedido[20]; // Tamaño máximo
        this.numItems = 0;
        this.atendido = false;
    }
    
    public void agregarItem(String nombre, double precio) {
        if (numItems < items.length) {
            items[numItems] = new ItemPedido(nombre, precio);
            numItems++;
        }
    }
    
    public void marcarItemPreparado(int indice) {
        if (indice >= 0 && indice < numItems) {
            items[indice].marcarPreparado();
        }
    }
    
    public boolean estaCompleto() {
        for (int i = 0; i < numItems; i++) {
            if (!items[i].estaPreparado()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean estaAtendido() {
        return atendido;
    }
    
    public void marcarAtendido() {
        atendido = true;
    }
    
    public double getTotal() {
        double total = 0;
        for (int i = 0; i < numItems; i++) {
            total += items[i].getPrecio();
        }
        return total;
    }
    
    // Getters
    public String getCliente() { return cliente; }
    public ItemPedido[] getItems() { return Arrays.copyOf(items, numItems); }
    public int getNumItems() { return numItems; }
}

class ItemPedido {
    private final String nombre;
    private final double precio;
    private boolean preparado;
    
    public ItemPedido(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
        this.preparado = false;
    }
    
    public void marcarPreparado() { preparado = true; }
    public boolean estaPreparado() { return preparado; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
}