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
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(bgColor);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(bgColor);


        backButton = createModernButton("← Nazad", new Color(127, 140, 141));
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(bgColor);
        backPanel.add(backButton);
        topPanel.add(backPanel);


        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBackground(bgColor);

        dateField = new JTextField(8);
        dateField.setText(LocalDate.now().toString());
        startField = new JTextField(5);
        startField.setText("22:00");
        endField = new JTextField(5);
        endField.setText("06:00");

        addButton = createModernButton("Dodaj san", new Color(72, 201, 176));
        deleteButton = createModernButton("Obriši san", new Color(231, 76, 60));
        exportButton = createModernButton("Godišnji PDF", new Color(155, 89, 182));

        inputPanel.add(new JLabel("Datum:"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Početak:"));
        inputPanel.add(startField);
        inputPanel.add(new JLabel("Kraj:"));
        inputPanel.add(endField);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);
        inputPanel.add(exportButton);

        topPanel.add(inputPanel);


        tableModel = new DefaultTableModel(new String[]{"Datum", "Početak", "Kraj", "Trajanje (h)"}, 0);
        sleepTable = new JTable(tableModel);
        sleepTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sleepTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(sleepTable);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);


        addButton.addActionListener(e -> addSleepRecord());
        deleteButton.addActionListener(e -> deleteSleepRecord());
        exportButton.addActionListener(e -> exportYearlyPDF());
        backButton.addActionListener(e -> goBack());

        loadData();
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
            JOptionPane.showMessageDialog(null, "Greška! Provjerite formate (YYYY-MM-DD i HH:mm).");
        }
    }

    private void deleteSleepRecord() {
        int selectedRow = sleepTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Molimo označite red u tabeli koji želite obrisati.");
            return;
        }

        String date = tableModel.getValueAt(selectedRow, 0).toString();
        String start = tableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(null, "Da li ste sigurni da želite obrisati ovaj unos?", "Potvrda", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Document query = new Document("userEmail", userEmail)
                    .append("date", date)
                    .append("start", start);

            collection.deleteOne(query);
            loadData();
            JOptionPane.showMessageDialog(null, "Unos obrisan.");
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (Document doc : collection.find(new Document("userEmail", userEmail))) {
            tableModel.addRow(new Object[]{
                    doc.get("date"),
                    doc.get("start"),
                    doc.get("end"),
                    doc.get("durationHours")
            });
        }
    }

    private void exportYearlyPDF() {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream("Godisnji_Izvjestaj_Sna.pdf"));
            document.open();

            int yearToShow = LocalDate.now().getYear();
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph("FITNESS CALENDAR - GODINA " + yearToShow, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Map<String, Double> sleepMap = new HashMap<>();
            for (Document doc : collection.find(new Document("userEmail", userEmail))) {
                String dStr = doc.getString("date");
                Object durObj = doc.get("durationHours");
                if (dStr != null && durObj != null) {
                    sleepMap.put(dStr, Double.parseDouble(durObj.toString()));
                }
            }

            PdfPTable table = new PdfPTable(32);
            table.setWidthPercentage(100);
            float[] widths = new float[32];
            widths[0] = 4f;
            for(int i=1; i<32; i++) widths[i] = 1f;
            table.setWidths(widths);

            table.addCell(createHeaderCell("Mj"));
            for (int d = 1; d <= 31; d++) table.addCell(createHeaderCell(String.valueOf(d)));

            String[] mjeseci = {"Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Avg", "Sep", "Okt", "Nov", "Dec"};

            for (int m = 1; m <= 12; m++) {
                table.addCell(createHeaderCell(mjeseci[m-1]));
                int daysInMonth = LocalDate.of(yearToShow, m, 1).lengthOfMonth();
                for (int d = 1; d <= 31; d++) {
                    if (d <= daysInMonth) {
                        String key = String.format("%d-%02d-%02d", yearToShow, m, d);
                        table.addCell(createColorCell(sleepMap.get(key)));
                    } else {
                        table.addCell(createEmptyCell());
                    }
                }
            }
            document.add(table);

            document.add(new Paragraph("\nLegenda:"));
            PdfPTable legenda = new PdfPTable(2);
            legenda.setWidthPercentage(30);
            legenda.setHorizontalAlignment(Element.ALIGN_LEFT);
            addLegendaRow(legenda, "Loše (< 5h)", BaseColor.RED);
            addLegendaRow(legenda, "Može bolje (5-8h)", BaseColor.YELLOW);
            addLegendaRow(legenda, "Odlično (> 8h)", BaseColor.GREEN);
            document.add(legenda);

            document.close();
            JOptionPane.showMessageDialog(null, "PDF kreiran!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Greška: " + ex.getMessage());
        }
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 7, com.itextpdf.text.Font.BOLD)));
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

    private void addLegendaRow(PdfPTable table, String text, BaseColor color) {
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(color);
        table.addCell(c);
        table.addCell(new Phrase(text, new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8)));
    }

    private void goBack() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        frame.setContentPane(new MainPanel(userEmail));
        frame.revalidate();
        frame.repaint();
    }

    public JPanel getMainPanel() { return mainPanel; }

    private JButton createModernButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        b.setFocusPainted(false);
        return b;
    }
}