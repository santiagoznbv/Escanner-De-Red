package gui;

import logic.HostProbe;
import logic.HostRecord;
import logic.NetstatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileWriter;
import java.util.List;

public class AppWindow extends JFrame {
    private JTextField ipStart;
    private JTextField ipEnd;
    private JTextField timeout;
    private JTextField searchBox;
    private JCheckBox onlyAlive;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JProgressBar progress;
    private JButton runBtn, clearBtn, exportBtn, copyBtn, resetFilterBtn;

    public AppWindow() {
        setTitle("Scanner de Red");
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel inputs = new JPanel(new GridLayout(2, 6, 8, 8));
        inputs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        inputs.add(new JLabel("Inicio:"));
        ipStart = new JTextField();
        inputs.add(ipStart);

        inputs.add(new JLabel("Fin:"));
        ipEnd = new JTextField();
        inputs.add(ipEnd);

        inputs.add(new JLabel("Timeout (ms):"));
        timeout = new JTextField("1000");
        inputs.add(timeout);

        inputs.add(new JLabel("Buscar/Filtrar:"));
        searchBox = new JTextField();
        inputs.add(searchBox);

        onlyAlive = new JCheckBox("Solo activos");
        inputs.add(onlyAlive);

        runBtn = new JButton("Escanear");
        clearBtn = new JButton("Limpiar");
        exportBtn = new JButton("Exportar CSV");
        copyBtn = new JButton("Copiar fila");
        resetFilterBtn = new JButton("Reset filtro");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(runBtn);
        actions.add(clearBtn);
        actions.add(exportBtn);
        actions.add(copyBtn);
        actions.add(resetFilterBtn);

        model = new DefaultTableModel(new Object[]{"IP", "Host", "Activo", "Latencia (ms)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        progress = new JProgressBar();
        progress.setStringPainted(true);

        setLayout(new BorderLayout());
        add(inputs, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.WEST);
        add(progress, BorderLayout.SOUTH);

        runBtn.addActionListener(evt -> startScan());
        clearBtn.addActionListener(evt -> clearResults());
        exportBtn.addActionListener(evt -> saveCSV());
        copyBtn.addActionListener(evt -> copySelectedRow());
        resetFilterBtn.addActionListener(evt -> searchBox.setText(""));

        searchBox.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        onlyAlive.addActionListener(e -> applyFilters());

        
        JButton btnNetstatAno = new JButton("Netstat -ano");
        btnNetstatAno.addActionListener(e -> {
        String result = NetstatUtils.showActiveConnections();
        showNetstatWindow("Conexiones activas (netstat -ano)", result);
        });

        JButton btnNetstatE = new JButton("Netstat -e");
        btnNetstatE.addActionListener(e -> {
        String result = NetstatUtils.showInterfaceStats();
        showNetstatWindow("Estadísticas de interfaz (netstat -e)", result);
        });

        JButton btnNetstatA = new JButton("Netstat -a");
        btnNetstatA.addActionListener(e -> {
        String result = NetstatUtils.showAllPorts();
        showNetstatWindow("Puertos abiertos (netstat -a)", result);
        });

        
        actions.add(btnNetstatAno);
        actions.add(btnNetstatE);
        actions.add(btnNetstatA);
    }

    private void applyFilters() {
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String query = searchBox.getText().trim().toLowerCase();
                boolean filterAlive = onlyAlive.isSelected();
                String ip = entry.getStringValue(0).toLowerCase();
                String host = entry.getStringValue(1).toLowerCase();
                String alive = entry.getStringValue(2).toLowerCase();
                boolean matchesText = query.isEmpty() || ip.contains(query) || host.contains(query) || alive.contains(query);
                boolean matchesAlive = !filterAlive || "sí".equals(alive);
                return matchesText && matchesAlive;
            }
        };
        sorter.setRowFilter(rf);
    }

    private void startScan() {
        String s = ipStart.getText().trim();
        String e = ipEnd.getText().trim();
        int t;
        try {
            t = Integer.parseInt(timeout.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Timeout inválido.");
            return;
        }
        if (!logic.HostProbe.isValidIPv4(s) || !logic.HostProbe.isValidIPv4(e)) {
            JOptionPane.showMessageDialog(this, "Ingrese IPs válidas IPv4.");
            return;
        }
        model.setRowCount(0);
        new Thread(() -> {
            List<HostRecord> results = HostProbe.probeRange(s, e, t);
            progress.setMaximum(results.size());
            int i = 0;
            for (HostRecord r : results) {
                model.addRow(new Object[]{
                    r.getIp(),
                    r.getHostname(),
                    r.isAlive() ? "Sí" : "No",
                    r.getLatencyMs()
                });
                progress.setValue(++i);
            }
        }).start();
    }

    private void clearResults() {
        model.setRowCount(0);
        progress.setValue(0);
    }

    private void saveCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".csv")) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    fw.write(model.getValueAt(i, 0) + "," +
                             model.getValueAt(i, 1) + "," +
                             model.getValueAt(i, 2) + "," +
                             model.getValueAt(i, 3) + "\n");
                }
                JOptionPane.showMessageDialog(this, "CSV exportado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar CSV.");
            }
        }
    }

    private void copySelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila.");
            return;
        }
        int mrow = table.convertRowIndexToModel(row);
        String data = model.getValueAt(mrow, 0) + "\t" +
                      model.getValueAt(mrow, 1) + "\t" +
                      model.getValueAt(mrow, 2) + "\t" +
                      model.getValueAt(mrow, 3);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), null);
        JOptionPane.showMessageDialog(this, "Fila copiada al portapapeles.");
    }

    
    private void showNetstatWindow(String title, String content) {
        JDialog dialog = new JDialog(this, title, true);
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        dialog.add(scrollPane);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppWindow::new);
    }
}
