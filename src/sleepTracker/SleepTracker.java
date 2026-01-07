package sleepTracker;

import com.mongodb.client.MongoCollection;
import lifemanagmentsystem.MongodbConnection;
import lifemanagmentsystem.SessionManager;
import lifemanagmentsystem.UserService;
import mainPanel.MainPanel;
import org.bson.Document;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class SleepTracker {

    private JPanel mainPanel;
    private JTextField dateField, startField, endField;
    private JButton addButton, backButton, exportButton, deleteButton;
    private JTable sleepTable;
    private DefaultTableModel tableModel;
    private String userEmail;
    private final MongoCollection<Document> collection;

    public SleepTracker(String loggedUserEmail) {
        this.userEmail = loggedUserEmail;
        this.collection = MongodbConnection.getDatabase().getCollection("sleepTracker");

        UserService userService = new UserService();
        String theme = userService.getUserTheme(userEmail);
        Color bgColor = SessionManager.getColorFromTheme(theme);

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(bgColor);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        backButton = newButton("Nazad na glavni panel", new Color(101, 117, 129));
        JPanel backWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backWrapper.setOpaque(false);
        backWrapper.add(backButton);
        topContainer.add(backWrapper);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inputPanel.setOpaque(false);

        dateField = createStyledTextField(10, LocalDate.now().toString());
        startField = createStyledTextField(6, "22:00");
        endField = createStyledTextField(6, "06:00");

        inputPanel.add(createLabel("Datum:"));
        inputPanel.add(dateField);
        inputPanel.add(createLabel("Početak:"));
        inputPanel.add(startField);
        inputPanel.add(createLabel("Kraj:"));
        inputPanel.add(endField);
        topContainer.add(inputPanel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setOpaque(false);
        addButton = newButton("Dodaj san", new Color(72, 201, 176));
        deleteButton = newButton("Obriši san", new Color(231, 76, 60));
        exportButton = newButton("Godišnji PDF", new Color(155, 89, 182));

        actionPanel.add(addButton);
        actionPanel.add(deleteButton);
        actionPanel.add(exportButton);
        topContainer.add(actionPanel);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Datum", "Početak", "Kraj", "Trajanje (h)"}, 0);
        sleepTable = new JTable(tableModel);
        sleepTable.setRowHeight(25);
        sleepTable.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(sleepTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addSleepRecord());
        deleteButton.addActionListener(e -> deleteSleepRecord());
        exportButton.addActionListener(e -> exportYearlyPDF());
        backButton.addActionListener(e -> goBack());

        loadData();
    }

    private JTextField createStyledTextField(int columns, String text) {
        JTextField field = new JTextField(columns);
        field.setText(text);
        field.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
        field.setMargin(new Insets(4, 6, 4, 6));
        return field;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private void addSleepRecord() {
        try {
            LocalDate date = LocalDate.parse(dateField.getText());
            LocalTime start = LocalTime.parse(startField.getText());
            LocalTime end = LocalTime.parse(endField.getText());
            Duration diff = Duration.between(start, end);
            if (diff.isNegative()) diff = diff.plusDays(1);
            double totalHours = diff.toMinutes() / 60.0;

            Document doc = new Document("userEmail", userEmail)
                    .append("date", date.toString())
                    .append("start", start.toString())
                    .append("end", end.toString())
                    .append("durationHours", totalHours);

            collection.insertOne(doc);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Greška u formatu podataka.");
        }
    }

    private void deleteSleepRecord() {
        int selectedRow = sleepTable.getSelectedRow();
        if (selectedRow == -1) return;
        String date = tableModel.getValueAt(selectedRow, 0).toString();
        String start = tableModel.getValueAt(selectedRow, 1).toString();
        if (JOptionPane.showConfirmDialog(null, "Obriši unos?", "Potvrda", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            collection.deleteOne(new Document("userEmail", userEmail).append("date", date).append("start", start));
            loadData();
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (Document doc : collection.find(new Document("userEmail", userEmail))) {
            tableModel.addRow(new Object[]{doc.get("date"), doc.get("start"), doc.get("end"), doc.get("durationHours")});
        }
    }

    private void exportYearlyPDF() {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream("izvjestajSna.pdf"));
            document.open();
            int year = LocalDate.now().getYear();
            Paragraph title = new Paragraph("SLEEP TRACKER - " + year, new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Map<String, Double> sleepMap = new HashMap<>();
            for (Document doc : collection.find(new Document("userEmail", userEmail))) {
                sleepMap.put(doc.getString("date"), Double.parseDouble(doc.get("durationHours").toString()));
            }

            PdfPTable table = new PdfPTable(32);
            table.setWidthPercentage(100);
            float[] widths = new float[32]; widths[0] = 4f; for(int i=1; i<32; i++) widths[i] = 1f;
            table.setWidths(widths);

            table.addCell(createHeaderCell("Mj"));
            for (int d = 1; d <= 31; d++) table.addCell(createHeaderCell(String.valueOf(d)));

            String[] mjeseci = {"Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Avg", "Sep", "Okt", "Nov", "Dec"};
            for (int m = 1; m <= 12; m++) {
                table.addCell(createHeaderCell(mjeseci[m-1]));
                int days = LocalDate.of(year, m, 1).lengthOfMonth();
                for (int d = 1; d <= 31; d++) {
                    if (d <= days) table.addCell(createColorCell(sleepMap.get(String.format("%d-%02d-%02d", year, m, d))));
                    else table.addCell(createEmptyCell());
                }
            }
            document.add(table);
            document.close();
            JOptionPane.showMessageDialog(null, "PDF kreiran!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Greška: " + ex.getMessage());
        }
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createColorCell(Double hours) {
        PdfPCell cell = new PdfPCell();
        cell.setMinimumHeight(15);
        if (hours == null) cell.setBackgroundColor(BaseColor.WHITE);
        else if (hours < 5.0) cell.setBackgroundColor(BaseColor.RED);
        else if (hours <= 8.0) cell.setBackgroundColor(BaseColor.YELLOW);
        else cell.setBackgroundColor(BaseColor.GREEN);
        return cell;
    }

    private PdfPCell createEmptyCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new BaseColor(230, 230, 230));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private void goBack() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        frame.setContentPane(new MainPanel(userEmail));
        frame.revalidate();
    }

    public JPanel getMainPanel() { return mainPanel; }

    private JButton newButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return b;
    }
}