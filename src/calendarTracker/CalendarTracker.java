package calendarTracker;

import mainPanel.MainPanel;
import lifemanagmentsystem.SessionManager;
import lifemanagmentsystem.UserService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.Map;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class CalendarTracker {

    private JPanel mainPanel;
    private JTextField dateField, planField;
      private JButton addButton, updateButton, deleteButton, backButton, pdfButton;
     private JTable planTable;
     private DefaultTableModel tableModel;
        private final CalendarInformationTransfer dbManager;
     private final String userEmail;
     private Color themeColor;
    private String selectedPlanForUpdate = "";

    public CalendarTracker(String userEmail) {
         this.userEmail = userEmail;
        this.dbManager = new CalendarInformationTransfer();
           UserService userService = new UserService();

           this.themeColor = SessionManager.getColorFromTheme(userService.getUserTheme(userEmail));

        initUI();
        loadTableData();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(20, 20));

        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(themeColor);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        backButton = createButton("← NAZAD", new Color(44, 62, 80));
        backButton.addActionListener(e -> goBack());

        JLabel title = new JLabel("GODIŠNJI PLANER I KALENDAR");
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        title.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        inputPanel.setOpaque(false);

        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        fieldsPanel.setOpaque(false);
            dateField = new JTextField(10);
        dateField.setText(LocalDate.now().toString());

        dateField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
        planField = new JTextField(25);
        planField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
        fieldsPanel.add(createLabel("Datum:"));

        fieldsPanel.add(dateField);
        fieldsPanel.add(createLabel("Plan:"));
        fieldsPanel.add(planField);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            actionsPanel.setOpaque(false);
        addButton = createButton("DODAJ", new Color(39, 174, 96));

        updateButton = createButton("UREDI", new Color(52, 152, 219));
        deleteButton = createButton("OBRIŠI", new Color(231, 76, 60));
        pdfButton = createButton("PDF EKSPORT", new Color(155, 89, 182));

        actionsPanel.add(addButton);
        actionsPanel.add(updateButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(pdfButton);
        inputPanel.add(fieldsPanel);
        inputPanel.add(actionsPanel);
        mainPanel.add(inputPanel, BorderLayout.CENTER);


        tableModel = new DefaultTableModel(new String[]{"DATUM", "OPIS PLANA"}, 0);
        planTable = new JTable(tableModel);
        planTable.setRowHeight(30);

        planTable.getSelectionModel().addListSelectionListener(e -> fillFieldsFromTable());
        JScrollPane scroll = new JScrollPane(planTable);
            scroll.setPreferredSize(new Dimension(800, 300));
            scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "LISTA PLANOVA", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
        mainPanel.add(scroll, BorderLayout.SOUTH);


        addButton.addActionListener(e -> addPlan());


        updateButton.addActionListener(e -> updatePlan());
            deleteButton.addActionListener(e -> deletePlan());
            pdfButton.addActionListener(e -> exportYearlyPDF());
    }

    private void fillFieldsFromTable() {
        int row = planTable.getSelectedRow();
        if (row != -1) {
            dateField.setText(tableModel.getValueAt(row, 0).toString());
            planField.setText(tableModel.getValueAt(row, 1).toString());
            selectedPlanForUpdate = tableModel.getValueAt(row, 1).toString();
        }
    }

    private void addPlan() {
        String date = dateField.getText().trim();
        String desc = planField.getText().trim();
        if (desc.isEmpty()) return;
        dbManager.addPlan(new CalendarRecord(userEmail, date, desc));
        planField.setText("");
        loadTableData();
    }

    private void updatePlan() {
        int row = planTable.getSelectedRow();
        if (row == -1) return;
        String date = dateField.getText().trim();
        String newDesc = planField.getText().trim();
        dbManager.deletePlan(userEmail, tableModel.getValueAt(row, 0).toString(), selectedPlanForUpdate);
        dbManager.addPlan(new CalendarRecord(userEmail, date, newDesc));
        loadTableData();
    }

    private void deletePlan() {
        int row = planTable.getSelectedRow();
        if (row == -1) return;
        dbManager.deletePlan(userEmail, tableModel.getValueAt(row, 0).toString(), tableModel.getValueAt(row, 1).toString());
        loadTableData();
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        for (CalendarRecord r : dbManager.getPlans(userEmail)) {
            tableModel.addRow(new Object[]{r.getDate(), r.getPlanDescription()});
        }
    }

    private void exportYearlyPDF() {
        try {
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream("GodisnjiPlan" + userEmail + ".pdf"));
            doc.open();
                int year = LocalDate.now().getYear();
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
                Paragraph p = new Paragraph("GODIŠNJI RASPORED AKTIVNOSTI - " + year, titleFont);
                p.setAlignment(Element.ALIGN_CENTER);
                p.setSpacingAfter(20);
            doc.add(p);
            Map<String, String> data = dbManager.getPlansGroupedByDate(userEmail);
            String[] months = {"Januar", "Februar", "Mart", "April", "Maj", "Juni", "Juli", "August", "Septembar", "Oktobar", "Novembar", "Decembar"};
            for (int m = 1; m <= 12; m++) {
                doc.add(new Paragraph(months[m - 1], new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5);
                table.setSpacingAfter(15);
                String[] days = {"Pon", "Uto", "Sri", "Cet", "Pet", "Sub", "Ned"};
                for (String d : days) {
                    PdfPCell c = new PdfPCell(new Phrase(d, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
                    c.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    c.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(c);
                }
                LocalDate first = LocalDate.of(year, m, 1);
                int offset = first.getDayOfWeek().getValue() - 1;
                for (int i = 0; i < offset; i++) table.addCell("");
                for (int d = 1; d <= first.lengthOfMonth(); d++) {
                    String dateKey = String.format("%d-%02d-%02d", year, m, d);
                    String planText = data.getOrDefault(dateKey, "");
                    PdfPCell cell = new PdfPCell(new Phrase(d + "\n" + planText, new Font(Font.FontFamily.HELVETICA, 7)));
                    cell.setMinimumHeight(50);
                    if (!planText.isEmpty()) cell.setBackgroundColor(new BaseColor(themeColor.getRGB()));
                    table.addCell(cell);
                }
                while (table.getRows().size() % 7 != 0) table.addCell("");
                doc.add(table);
            }
            doc.close();
            JOptionPane.showMessageDialog(mainPanel, "PDF generisan!");


           // catch (Exception e) {
            //    JOptionPane.showMessageDialog(mainPanel, "Greška: " + e.getMessage());
           // }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Greška: " + e.getMessage());
        }
    }

    private JLabel createLabel(String text) {
            JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        return l;
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
            b.setBackground(bg);
         b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return b;
    }


    //private JButton createButton(String text, Color bg) {
      //  JButton b = new JButton(text);
      //  b.setBackground(bg);
      //  b.setForeground(Color.WHITE);
       // b.setFocusPainted(false);
      //  b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
       // return b;
    //}

    private void goBack() {
         JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
            frame.setContentPane(new MainPanel(userEmail));
        frame.revalidate();
    }

    public JPanel getMainPanel() { return mainPanel; }
}