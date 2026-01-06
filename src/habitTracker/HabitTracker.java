package habitTracker;

import mainPanel.MainPanel;
import lifemanagmentsystem.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

// Ispravni importi za PDF
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// Import za datum
import java.time.LocalDate;

public class HabitTracker {

    private JPanel mainPanel;
    private JTextField habitField;
    private JCheckBox completedCheckBox;
    private JButton addButton, backButton, pdfButton;
    private JTable habitTable;
    private DefaultTableModel tableModel;

    private final HabitInformationTransfer dbManager;
    private final String userEmail;

    public HabitTracker(String userEmail) {
        this.userEmail = userEmail;
        dbManager = new HabitInformationTransfer();

        initUI();
        updateTable();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Top bar: nazad dugme + naslov
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        backButton = createButton("← Nazad", new Color(127, 140, 141));
        backButton.addActionListener(e -> goBack());
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("📝 Habit Tracker");
        titleLabel.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.setBackground(Color.WHITE);

        habitField = new JTextField(15);
        completedCheckBox = new JCheckBox("Završeno");
        addButton = createButton("Dodaj", new Color(72, 201, 176));
        addButton.addActionListener(e -> addHabit());

        pdfButton = createButton("Export PDF", new Color(52, 152, 219));
        pdfButton.addActionListener(e -> exportPDF());

        inputPanel.add(new JLabel("Navika:"));
        inputPanel.add(habitField);
        inputPanel.add(completedCheckBox);
        inputPanel.add(addButton);
        inputPanel.add(pdfButton);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Tabela navika
        tableModel = new DefaultTableModel(new Object[]{"Navika", "Status", "Procent"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        habitTable = new JTable(tableModel);
        habitTable.setRowHeight(28);
        habitTable.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        habitTable.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        habitTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(habitTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Sve navike"));
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
    }

    private void addHabit() {
        String habit = habitField.getText().trim();
        if (habit.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Unesite naziv navike!");
            return;
        }

        dbManager.addHabitRecord(new HabitRecord(
                userEmail,
                habit,
                completedCheckBox.isSelected(),
                LocalDate.now().toString()
        ));

        habitField.setText("");
        completedCheckBox.setSelected(false);
        updateTable();
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        ArrayList<String> habits = dbManager.getUniqueHabits(userEmail);

        for (String habit : habits) {
            boolean completed = dbManager.isHabitCompletedToday(userEmail, habit);

            String status = completed ? "✅ Završeno" : "⚠ Fokus";

            tableModel.addRow(new Object[]{habit, status, completed ? "Završeno" : ""});
        }
    }


    private void exportPDF() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Odaberite lokaciju za PDF");
            fileChooser.setSelectedFile(new File("HabitsReport.pdf"));
            int userSelection = fileChooser.showSaveDialog(mainPanel);
            if (userSelection != JFileChooser.APPROVE_OPTION) return;

            File pdfFile = fileChooser.getSelectedFile();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Naslov
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("Habit Tracker – Izvještaj", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Tabela
            PdfPTable tablePdf = new PdfPTable(3);
            tablePdf.setWidthPercentage(100);
            tablePdf.setWidths(new int[]{5, 3, 2});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE);
            BaseColor headerColor = new BaseColor(52, 152, 219);
            String[] headers = {"Navika", "Status", "Procent"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                tablePdf.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            ArrayList<String> habits = dbManager.getUniqueHabits(userEmail);
            for (String habit : habits) {
                boolean completed = dbManager.isHabitCompletedToday(userEmail, habit);

                String status = completed ? "✅ Završeno" : "⚠ Fokus";

                tablePdf.addCell(new PdfPCell(new Paragraph(habit, cellFont)));
                tablePdf.addCell(new PdfPCell(new Paragraph(status, cellFont)));
                tablePdf.addCell(new PdfPCell(new Paragraph(completed ? "Završeno" : "", cellFont)));
            }


            document.add(tablePdf);
            document.close();

            JOptionPane.showMessageDialog(mainPanel, "PDF uspješno kreiran na: " + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Greška pri kreiranju PDF-a: " + e.getMessage());
        }
    }

    private void goBack() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        frame.setContentPane(new MainPanel(userEmail));
        frame.revalidate();
        frame.repaint();
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
