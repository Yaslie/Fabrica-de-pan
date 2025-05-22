import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class PanSimulator extends JFrame {
    private boolean masaLista = false;
    private boolean fermentacionOk = false;
    private int rebanadas = 0;
    private boolean calidad = false;

    private JLabel estadoLabel;
    private JLabel imagenEstado;

    private Set<String> accionesDisponibles = new HashSet<>();
    private final String archivoPlan = "panblanco.plan";

    public PanSimulator() {
        String bienvenida = "<html><b>¡Bienvenido a la Fábrica Virtual de Pan Blanco!</b><br><br>" +
            "Tu misión: <br>¡Producir pan de calidad y empaquetarlo como un auténtico panadero!<br><br>" +
            "<b>Objetivo final:</b><br>" +
            "Empaquetar el pan correctamente con 3 rebanadas horneadas.<br><br>" +
            "<b>Pasos:</b><br>" +
            "<b>Mezclar la masa:</b> Esto prepara la masa base. No puedes fermentar sin <br>haber mezclado primero.<br><br>" +
            "<b>Fermentar la masa:</b> Solo puedes fermentar si la masa ya fue mezclada. <br>Este paso es crucial para que el pan tenga la textura y volumen adecuados.<br><br>" +
            "<b>Hornear la masa:</b> Cada vez que horneas, se obtiene una rebanada de pan.<br> Debes hornear tres veces para tener las tres rebanadas necesarias para empaquetar.<br><br>" +
            "<b>Empaquetar el pan:</b> Solo puedes empaquetar si tienes al menos tres <br>rebanadas horneadas. Al empaquetar, las rebanadas se consumen y se alcanza la meta de calidad del producto.</html>";
        JOptionPane.showMessageDialog(this, bienvenida, "¡Bienvenido!", JOptionPane.INFORMATION_MESSAGE);

        setTitle("Fábrica de pan");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(220, 207, 154)); // Fondo general

        // Título
        JLabel titulo = new JLabel("Fábrica de pan", JLabel.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 32));
        titulo.setForeground(Color.BLACK);
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Panel central con estado e imagen
        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 20, 0));
        panelCentro.setOpaque(false);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Panel de estado
        JPanel panelEstado = new JPanel();
        panelEstado.setBackground(new Color(196, 176, 98));
        panelEstado.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        panelEstado.setLayout(new BorderLayout());
        estadoLabel = new JLabel();
        estadoLabel.setVerticalAlignment(SwingConstants.TOP);
        estadoLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        estadoLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelEstado.add(estadoLabel, BorderLayout.CENTER);
        panelCentro.add(panelEstado);

        // Panel de imagen
        JPanel panelImagen = new JPanel();
        panelImagen.setBackground(new Color(196, 176, 98));
        panelImagen.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        panelImagen.setLayout(new BorderLayout());
        imagenEstado = new JLabel("", JLabel.CENTER);
        panelImagen.add(imagenEstado, BorderLayout.CENTER);
        panelCentro.add(panelImagen);

        add(panelCentro, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(1, 4, 10, 0));
        panelBotones.setBackground(new Color(206, 189, 112));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        JButton mezclarBtn = new JButton("Mezclar");
        JButton fermentarBtn = new JButton("Fermentar");
        JButton hornearBtn = new JButton("Hornear");
        JButton empaquetarBtn = new JButton("Empaquetar");
        JButton[] botones = {mezclarBtn, fermentarBtn, hornearBtn, empaquetarBtn};
        for (JButton btn : botones) {
            btn.setBackground(new Color(196, 176, 98));
            btn.setFont(new Font("Serif", Font.BOLD, 18));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        }
        panelBotones.add(mezclarBtn);
        panelBotones.add(fermentarBtn);
        panelBotones.add(hornearBtn);
        panelBotones.add(empaquetarBtn);
        add(panelBotones, BorderLayout.SOUTH);

        cargarEstadoInicial();

        mezclarBtn.addActionListener(e -> {
            if (accionesDisponibles.contains("mezclar")) {
                masaLista = true;
                mostrarMensaje("Masa mezclada", "img/mezclar.png");
            } else {
                mostrarError("¡Acción no permitida!");
            }
            actualizarEstado();
        });

        fermentarBtn.addActionListener(e -> {
            if (accionesDisponibles.contains("fermentar")) {
                fermentacionOk = true;
                mostrarMensaje("Fermentación iniciada", "img/fermentar.png");
            } else {
                mostrarError("Primero mezcla la masa.");
            }
            actualizarEstado();
        });

        hornearBtn.addActionListener(e -> {
            if (accionesDisponibles.contains("hornear")) {
                if (rebanadas < 3) {
                    rebanadas++;
                    mostrarMensaje("Rebanada horneada", "img/hornear" + rebanadas + ".png");
                } else {
                    mostrarError("¡Máximo de rebanadas alcanzado!");
                }
            } else {
                mostrarError("Primero mezcla y fermenta la masa.");
            }
            actualizarEstado();
        });

        empaquetarBtn.addActionListener(e -> {
            if (accionesDisponibles.contains("empaquetar")) {
                calidad = true;
                rebanadas = 0;
                mostrarMensaje("Producto empaquetado", "img/empaquetar.png");
            } else {
                mostrarError("No puedes empaquetar aún.");
            }
            actualizarEstado();
        });

        actualizarEstado();
    }

    private void cargarEstadoInicial() {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoPlan))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("initially:")) break;
            }
            while ((linea = reader.readLine()) != null && !linea.startsWith("%")) {
                if (linea.contains("masa_lista")) masaLista = !linea.contains("-");
                if (linea.contains("fermentacion_ok")) fermentacionOk = !linea.contains("-");
                if (linea.contains("rebanadas_de_pan_horneadas")) {
                    String num = linea.replaceAll("\\D+", "");
                    rebanadas = Integer.parseInt(num);
                }
                if (linea.contains("calidad_producto")) calidad = !linea.contains("-");
            }
        } catch (IOException e) {
            mostrarError("Error al leer el archivo .plan");
        }
    }

    private void actualizarEstado() {
        accionesDisponibles.clear();
        accionesDisponibles.add("mezclar");
        if (masaLista) accionesDisponibles.add("fermentar");
        if (masaLista && fermentacionOk) accionesDisponibles.add("hornear");
        if (rebanadas >= 3) accionesDisponibles.add("empaquetar");

        estadoLabel.setText("<html><b>Estado actual:</b><br><br>" +
            "Masa lista: " + masaLista + "<br>" +
            "Fermentacion ok: " + fermentacionOk + "<br>" +
            "Rebanadas horneadas: " + rebanadas + "<br>" +
            "Producto de calidad: " + (calidad ? "1" : "0") + "</html>");

        String imgPath = "";
        if (calidad) {
            imgPath = "img/empaquetar.png";
        } else if (rebanadas == 3) {
            imgPath = "img/hornear3.png";
        } else if (rebanadas == 2) {
            imgPath = "img/hornear2.png";
        } else if (rebanadas == 1) {
            imgPath = "img/hornear1.png";
        } else if (fermentacionOk) {
            imgPath = "img/fermentar.png";
        } else if (masaLista) {
            imgPath = "img/mezclar.png";
        } else {
            imgPath = "img/espera.png";
        }
        imagenEstado.setIcon(new ImageIcon(imgPath));
        imagenEstado.setText("");

        // Mostrar ventana de felicitaciones si el producto es de calidad
        if (calidad) {
            Object[] options = {"Salir", "Empezar de nuevo"};
            int opcion = JOptionPane.showOptionDialog(
                this,
                "<html>Felicidades<br>El proceso de producción ha sido terminado con éxito</html>",
                "¡Proceso terminado!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );
            if (opcion == JOptionPane.YES_OPTION) {
                System.exit(0);
            } else if (opcion == JOptionPane.NO_OPTION) {
                reiniciarFluentes();
            }
        }
    }

    // Agregamos este método para reiniciar los fluentes
    private void reiniciarFluentes() {
        masaLista = false;
        fermentacionOk = false;
        rebanadas = 0;
        calidad = false;
        actualizarEstado();
    }

    private void mostrarMensaje(String mensaje, String imgPath) {
        JOptionPane.showMessageDialog(this, mensaje, "Acción Exitosa", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(imgPath));
    }

    private void mostrarError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PanSimulator().setVisible(true));
    }
}
