package fitnesTracker;

import mainPanel.MainPanel;
import lifemanagmentsystem.UserService;
import lifemanagmentsystem.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Font;          // za font u Swing komponentama
import java.util.List;         // za List


public class FitnessPlanner {

    private JPanel mainPanel;
    private JTextField caloriesField, durationField, distanceField, dateField;
    private JTable table;
    private DefaultTableModel tableModel;

    private FitnesInformationTransfer dbManager;
    private UserService userService;
    private String userEmail;
    private String currentTheme;

    public FitnessPlanner(String userEmail) {
        this.userEmail = userEmail;
        this.dbManager = new FitnesInformationTransfer();
        this.userService = new UserService();
        this.currentTheme = userService.getUserTheme(userEmail);

        initUI();
        loadLastRecords();
    }

    private void initUI() {
        // Tema
        Color bgColor = SessionManager.getColorFromTheme(currentTheme);

        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(bgColor);

        // Top bar
        JButton backButton = createButton("← Nazad", new Color(120, 120, 120));
        backButton.addActionListener(e -> goBack());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(bgColor);
        top.add(backButton);
        mainPanel.add(top, BorderLayout.NORTH);

        // Center panel
        JPanel center = new JPanel(new BorderLayout(20, 20));
        center.setBackground(bgColor);

        // Forma
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(bgColor);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 15);
        caloriesField = new JTextField(15);
        durationField = new JTextField(15);
        distanceField = new JTextField(15);

        g.gridy = 0; form.add(createField("Datum:", dateField), g);
        g.gridy++; form.add(createField("Kalorije:", caloriesField), g);
        g.gridy++; form.add(createField("Trajanje (min):", durationField), g);
        g.gridy++; form.add(createField("Distanca (km):", distanceField), g);

        // Dugmad
        JButton addButton = createButton("Dodaj trening", new Color(52, 152, 219));
        addButton.addActionListener(e -> addRecord());

        JButton pdfButton = createButton("Export PDF", new Color(46, 204, 113));
        pdfButton.addActionListener(e -> exportPDF());

        JButton deleteButton = createButton("Obriši trening", new Color(231, 76, 60));
        deleteButton.addActionListener(e -> deleteSelectedRecord());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(bgColor);
        btnPanel.add(addButton);
        btnPanel.add(pdfButton);
        btnPanel.add(deleteButton);

        g.gridy++; form.add(btnPanel, g);
        center.add(form, BorderLayout.NORTH);

        // Tabela
        tableModel = new DefaultTableModel(
                new String[]{"Datum", "Kal", "Min", "Km", "Intenzitet"}, 0
        );
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setEnabled(true);

        table.getColumnModel().getColumn(4).setCellRenderer(new IntensityRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(800, 150));
        scroll.setBorder(BorderFactory.createTitledBorder("Zadnji treninzi"));
        center.add(scroll, BorderLayout.CENTER);

        mainPanel.add(center, BorderLayout.CENTER);
    }

    private void addRecord() {
        try {
            double calories = Double.parseDouble(caloriesField.getText());
            int duration = Integer.parseInt(durationField.getText());
            double distance = Double.parseDouble(distanceField.getText());

            String intensity = calculateIntensity(calories, duration, distance);

            FitnessRecord record = new FitnessRecord(
                    userEmail,
                    dateField.getText(),
                    calories,
                    duration,
                    distance,
                    intensity
            );

            dbManager.addFitnessRecord(record);
            clearFields();
            loadLastRecords();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Greška u unosu: " + e.getMessage());
        }
    }

    private void loadLastRecords() {
        tableModel.setRowCount(0);
        List<FitnessRecord> records = dbManager.getAllRecords(userEmail);
        int start = Math.max(0, records.size() - 5);

        for (int i = records.size() - 1; i >= start; i--) {
            FitnessRecord r = records.get(i);
            tableModel.addRow(new Object[]{
                    r.getDate(), r.getCalories(), r.getDurationMinutes(),
                    r.getDistanceKilometers(), r.getIntensityLevel()
            });
        }
    }

    private String calculateIntensity(double calories, int duration, double distance) {
        double score = calories / 10 + duration / 5.0 + distance * 2;
        if (score < 50) return "Slab";
        if (score < 100) return "Srednji";
        if (score < 150) return "Visok";
        return "Vrlo visok";
    }

    private void deleteSelectedRecord() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(mainPanel, "Molimo označite trening za brisanje.", "Greška", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String date = tableModel.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "Da li želite obrisati trening za datum:\n" + date + "?",
                "Potvrda brisanja",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dbManager.deleteFitnessRecord(userEmail, date);
            loadLastRecords();
        }
    }

    private void exportPDF() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Odaberite lokaciju za PDF");
            fileChooser.setSelectedFile(new File("FitnessCalendar.pdf"));
            int userSelection = fileChooser.showSaveDialog(mainPanel);
            if (userSelection != JFileChooser.APPROVE_OPTION) return;

            File pdfFile = fileChooser.getSelectedFile();
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
            doc.open();

            doc.add(new Paragraph("Fitness Calendar – Godina"));

            Map<String, FitnessRecord> map = new HashMap<>();
            for (FitnessRecord r : dbManager.getAllRecords(userEmail))
                map.put(r.getDate(), r);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_YEAR, 1);

            for (int month = 0; month < 12; month++) {
                calendar.set(Calendar.MONTH, month);
                int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                PdfPTable tablePdf = new PdfPTable(days);

                for (int day = 1; day <= days; day++) {
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    String key = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                    PdfPCell cell = new PdfPCell();
                    if (map.containsKey(key)) {
                        cell.setBackgroundColor(colorForIntensity(map.get(key).getIntensityLevel()));
                    }
                    cell.setFixedHeight(18);
                    tablePdf.addCell(cell);
                }
                doc.add(tablePdf);
            }

            doc.close();
            JOptionPane.showMessageDialog(mainPanel, "PDF uspješno kreiran na lokaciji:\n" + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Greška pri kreiranju PDF-a: " + e.getMessage());
        }
    }

    private BaseColor colorForIntensity(String intensity) {
        switch (intensity) {
            case "Slab": return new BaseColor(144, 238, 144);
            case "Srednji": return new BaseColor(255, 255, 102);
            case "Visok": return new BaseColor(255, 165, 0);
            default: return new BaseColor(255, 69, 0);
        }
    }

    private void clearFields() {
        caloriesField.setText("");
        durationField.setText("");
        distanceField.setText("");
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    private JPanel createField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SessionManager.getColorFromTheme(currentTheme));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void goBack() {
        JFrame f = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        f.setContentPane(new MainPanel(userEmail));
        f.revalidate();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private static class IntensityRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setBackground(Color.WHITE);
            setHorizontalAlignment(CENTER);
            return comp;
        }
    }
}
