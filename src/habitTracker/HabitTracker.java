package habitTracker;

import mainPanel.MainPanel;
import lifemanagmentsystem.SessionManager;
import lifemanagmentsystem.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class HabitTracker {

    private JPanel mainPanel;
    private JTextField habitField;
    private JCheckBox completedCheckBox;
    private JButton addButton, backButton, pdfButton;
    private JTable habitTable;
    private DefaultTableModel tableModel;

    private final HabitInformationTransfer dbManager;
    private final String userEmail;
    private Color themeColor;

    public HabitTracker(String userEmail) {
        this.userEmail = userEmail;
        this.dbManager = new HabitInformationTransfer();

        UserService userService = new UserService();
        String themeName = userService.getUserTheme(userEmail);
        this.themeColor = SessionManager.getColorFromTheme(themeName);

        initUI();
        updateTable();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(25, 25));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(themeColor);


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        backButton = createButton("← NAZAD", new Color(44, 62, 80));
        backButton.addActionListener(e -> goBack());
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("SISTEM ZA PRAĆENJE NAVIKA");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);


        JPanel centerContainer = new JPanel(new BorderLayout(20, 20));
        centerContainer.setOpaque(false);


        JPanel inputCard = new JPanel(new GridBagLayout());
        inputCard.setBackground(new Color(255, 255, 255, 40));
        inputCard.setBorder(new LineBorder(Color.WHITE, 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        JLabel inputLabel = new JLabel("Naziv nove navike:");
        inputLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        inputLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        inputCard.add(inputLabel, gbc);

        habitField = new JTextField(25);
        habitField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
        habitField.setPreferredSize(new Dimension(350, 45));
        gbc.gridy = 1;
        inputCard.add(habitField, gbc);

        completedCheckBox = new JCheckBox("Označi kao izvršeno za danas");
        completedCheckBox.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        completedCheckBox.setForeground(Color.WHITE);
        completedCheckBox.setOpaque(false);
        gbc.gridy = 2;
        inputCard.add(completedCheckBox, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        addButton = createButton("DODAJ NAVIKU", new Color(39, 174, 96));
        pdfButton = createButton("IZVEZI PDF", new Color(41, 128, 185));

        addButton.addActionListener(e -> addHabit());
        pdfButton.addActionListener(e -> exportPDF());

        btnPanel.add(addButton);
        btnPanel.add(pdfButton);
        gbc.gridy = 3;
        inputCard.add(btnPanel, gbc);

        centerContainer.add(inputCard, BorderLayout.NORTH);


        tableModel = new DefaultTableModel(new Object[]{"NAVIKA", "DANAŠNJI STATUS", "PROGRESS"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        habitTable = new JTable(tableModel);
        habitTable.setRowHeight(40);
        habitTable.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
        habitTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(habitTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.WHITE), "AKTIVNE NAVIKE",
                TitledBorder.LEFT, TitledBorder.TOP,
                new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14), Color.WHITE));

        centerContainer.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerContainer, BorderLayout.CENTER);
    }

    private void addHabit() {
        String habit = habitField.getText().trim();
        if (habit.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Unesite validan naziv navike.");
            return;
        }

        dbManager.addHabitRecord(new HabitRecord(
                userEmail, habit, completedCheckBox.isSelected(), LocalDate.now().toString()
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
            String status = completed ? "✓ IZVRŠENO" : "○ NA ČEKANJU";
            String progress = String.format("%.1f%%", dbManager.getCompletionPercentage(userEmail, habit));

            tableModel.addRow(new Object[]{habit, status, progress});
        }
    }

    private void exportPDF() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("HabitStatistic" + LocalDate.now() + ".pdf"));
            if (fileChooser.showSaveDialog(mainPanel) != JFileChooser.APPROVE_OPTION) return;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileChooser.getSelectedFile()));
            document.open();


            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.BLACK);
            Paragraph title = new Paragraph("IZVJEŠTAJ O NAVIKAMA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph("Korisnik: " + userEmail + "\nDatum izvještaja: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n\n");
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);


            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15f);

            BaseColor headColor = new BaseColor(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue());
            String[] headers = {"NAVIKA", "DANAŠNJI STATUS", "UKUPAN PROGRES"};

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
                cell.setBackgroundColor(headColor);
                cell.setPadding(10);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            ArrayList<String> habits = dbManager.getUniqueHabits(userEmail);
            for (String habit : habits) {
                boolean completed = dbManager.isHabitCompletedToday(userEmail, habit);
                double percentage = dbManager.getCompletionPercentage(userEmail, habit);

                table.addCell(new PdfPCell(new Paragraph(habit)));
                table.addCell(new PdfPCell(new Paragraph(completed ? "Izvrseno" : "Nije izvrseno")));
                table.addCell(new PdfPCell(new Paragraph(String.format("%.1f%%", percentage))));
            }

            document.add(table);
            document.close();
            JOptionPane.showMessageDialog(mainPanel, "Profesionalni PDF izvještaj je generisan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Greška pri generisanju PDF-a: " + e.getMessage());
        }
    }

    private void goBack() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        frame.setContentPane(new MainPanel(userEmail));
        frame.revalidate();
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        return b;
    }

    public JPanel getMainPanel() { return mainPanel; }
}