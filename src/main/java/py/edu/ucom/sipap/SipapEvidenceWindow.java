package py.edu.ucom.sipap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Ventana para capturar evidencia visual de una ejecución real del flujo Camel. */
public final class SipapEvidenceWindow {
    private SipapEvidenceWindow() {
    }

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
        String group = args.length == 0 ? "valid" : args[0].toLowerCase();
        SwingUtilities.invokeLater(() -> showWindow(group));
    }

    private static void showWindow(String group) {
        JFrame frame = new JFrame("EVIDENCIA SIPAP - " + group.toUpperCase());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JTextArea output = new JTextArea("Ejecutando el flujo real de Apache Camel...");
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        output.setBackground(new Color(20, 24, 32));
        output.setForeground(new Color(225, 232, 240));
        output.setCaretColor(output.getForeground());
        output.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        frame.add(new JScrollPane(output), BorderLayout.CENTER);
        frame.setSize(1500, 920);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return SipapEvidenceApplication.execute(group);
            }

            @Override
            protected void done() {
                try {
                    output.setText(get());
                    output.setCaretPosition(0);
                } catch (Exception exception) {
                    output.setText("ERROR AL EJECUTAR: " + exception.getMessage());
                }
            }
        }.execute();
    }
}
