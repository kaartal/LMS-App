package studyPlanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.util.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import lifemanagmentsystem.SessionManager;
import lifemanagmentsystem.UserService;
import mainPanel.MainPanel;

public class StudyPlanner {

    private JPanel mainPanel;
    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private StudyInformationTransfer dbManager;
    private StudyRecord studentRecord;
    private String currentUserEmail;
    private JButton backButton, pdfButton;

    //private JButton backButton, pdfButton;
    //private String currentUserEmail;

    public StudyPlanner(String email) {
        this.currentUserEmail = email;
        this.dbManager = new StudyInformationTransfer();
        this.studentRecord = dbManager.getRecord(currentUserEmail);

        UserService userService = new UserService();
        String themeName = userService.getUserTheme(currentUserEmail);
        Color backgroundColor = SessionManager.getColorFromTheme(themeName);

        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(backgroundColor);

        backButton = createStyledButton("← Nazad", new Color(127, 140, 141));
        backButton.addActionListener(e -> goBackToMainMenu());
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Planer Studija", SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel subjectsButtonPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        subjectsButtonPanel.setBackground(backgroundColor);
        subjectsButtonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        for (String subjectName : studentRecord.getStudentSubjectGrades().keySet()) {
            JButton subjectBtn = createStyledButton(subjectName, new Color(52, 152, 219));
            subjectBtn.addActionListener(e -> inputGrade(subjectName));
            subjectsButtonPanel.add(subjectBtn);
        }

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setBackground(backgroundColor);
        northContainer.add(topPanel, BorderLayout.NORTH);
        northContainer.add(subjectsButtonPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[]{"Predmet", "Ocjene", "Prosjek"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        gradesTable = new JTable(tableModel);
        gradesTable.setRowHeight(30);
        gradesTable.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        gradesTable.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.getViewport().setBackground(Color.WHITE);

        pdfButton = createStyledButton("Generiši PDF Izvještaj", new Color(155, 89, 182));
        pdfButton.addActionListener(e -> exportToPDF());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(pdfButton);

        mainPanel.add(northContainer, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        updateTable();
        showMissingGradesNotification();
    }

    private void inputGrade(String subjectName) {
        String input = JOptionPane.showInputDialog(mainPanel, "Unesite ocjenu za " + subjectName + " (1-5):");

        if (input != null && !input.trim().isEmpty()) {
            try {
                int gradeValue = Integer.parseInt(input.trim());

                if (gradeValue < 1 || gradeValue > 5) {
                    JOptionPane.showMessageDialog(mainPanel, "Unesite validan broj od 1 do 5!");
                    return;
                }

                int confirmAction = JOptionPane.showConfirmDialog(
                        mainPanel,
                        "Da li ste sigurni da želite dodati ocjenu " + gradeValue + " za predmet " + subjectName + "?",
                        "Potvrda unosa",

                        JOptionPane.YES_NO_OPTION

                );

                if (confirmAction == JOptionPane.YES_OPTION) {

                    studentRecord.addGradeToSubject(subjectName, gradeValue);

                    dbManager.addOrUpdateRecord(studentRecord);

                    updateTable();

                    JOptionPane.showMessageDialog(mainPanel, "Ocjena uspješno dodana!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Greška: Unesite cijeli broj (1-5)!");
            }
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);


        for (Map.Entry<String, ArrayList<Integer>> entry : studentRecord.getStudentSubjectGrades().entrySet()) {
            ArrayList<Integer> gradesList = entry.getValue();


            String gradesString = String.join(", ", gradesList.stream().map(String::valueOf).toArray(String[]::new));


            double averageValue = studentRecord.calculateSubjectAverage(entry.getKey());
            String averageDisplay = gradesList.isEmpty() ? "" : String.format("%.2f", averageValue);
            tableModel.addRow(new Object[]{entry.getKey(), gradesString, averageDisplay});

        }

        double overallAverageValue = studentRecord.calculateOverallGradeAverage();
        String overallDisplay = overallAverageValue == 0 ? "" : String.format("%.2f", overallAverageValue);
        tableModel.addRow(new Object[]{"<html><b>UKUPNI PROSJEK</b></html>", "", "<html><b>" + overallDisplay + "</b></html>"});
    }

    private void exportToPDF() {
        JFileChooser fileSaver = new JFileChooser();
        fileSaver.setSelectedFile(new java.io.File("Izvjestaj_Studije.pdf"));
        if(fileSaver.showSaveDialog(mainPanel) != JFileChooser.APPROVE_OPTION) return;

        String savePath = fileSaver.getSelectedFile().getAbsolutePath();

        try {
            Document pdfDoc = new Document(PageSize.A4, 36, 36, 50, 50);
            PdfWriter.getInstance(pdfDoc, new FileOutputStream(savePath));
            pdfDoc.open();

            Font mainTitleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(44, 62, 80));
            Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font rowFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(41, 128, 185));

            Paragraph title = new Paragraph("IZVJEŠTAJ O ŠKOLSKOM USPJEHU", mainTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            pdfDoc.add(title);

            Paragraph emailInfo = new Paragraph("Student: " + currentUserEmail, subTitleFont);
            emailInfo.setAlignment(Element.ALIGN_CENTER);
            emailInfo.setSpacingAfter(5);
            pdfDoc.add(emailInfo);

            Paragraph line = new Paragraph("______________________________________________________________________________");
            line.setAlignment(Element.ALIGN_CENTER);
            line.setSpacingAfter(20);
            pdfDoc.add(line);

            PdfPTable pdfTable = new PdfPTable(3);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{30f, 50f, 20f});

            String[] headers = {"Predmet", "Lista ocjena", "Prosjek"};
            for(String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(41, 128, 185));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(10);
                cell.setBorderColor(BaseColor.WHITE);
                pdfTable.addCell(cell);
            }

            int rowCount = 0;
            for(Map.Entry<String, ArrayList<Integer>> entry : studentRecord.getStudentSubjectGrades().entrySet()) {
                BaseColor bgColor = (rowCount % 2 == 0) ? new BaseColor(245, 245, 245) : BaseColor.WHITE;

                PdfPCell c1 = new PdfPCell(new Phrase(entry.getKey(), rowFont));
                c1.setBackgroundColor(bgColor);
                c1.setPadding(8);
                pdfTable.addCell(c1);

                String gradesStr = entry.getValue().isEmpty() ? "-" : entry.getValue().toString().replace("[", "").replace("]", "");
                PdfPCell c2 = new PdfPCell(new Phrase(gradesStr, rowFont));
                c2.setBackgroundColor(bgColor);
                c2.setPadding(8);
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(c2);

                double avg = studentRecord.calculateSubjectAverage(entry.getKey());
                String stars = "";
                if (avg >= 4.8) stars = " ***";
                else if (avg >= 4.5) stars = " **";
                else if (avg >= 4.0) stars = " *";

                String avgStr = entry.getValue().isEmpty() ? "0.00" : String.format("%.2f", avg) + stars;
                PdfPCell c3 = new PdfPCell(new Phrase(avgStr, rowFont));
                c3.setBackgroundColor(bgColor);
                c3.setPadding(8);
                c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(c3);

                rowCount++;
            }

            pdfDoc.add(pdfTable);
            pdfDoc.add(new Paragraph("\n"));

            double overallAvg = studentRecord.calculateOverallGradeAverage();
            String overallStars = "";
            if (overallAvg >= 4.8) overallStars = " ***";
            else if (overallAvg >= 4.5) overallStars = " **";
            else if (overallAvg >= 4.0) overallStars = " *";

            PdfPTable summaryTable = new PdfPTable(1);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell summaryCell = new PdfPCell(new Phrase("UKUPNA OCJENA: " + String.format("%.2f", overallAvg) + overallStars, footerFont));
            summaryCell.setBorderWidth(2);
            summaryCell.setBorderColor(new BaseColor(41, 128, 185));
            summaryCell.setPadding(15);
            summaryCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            summaryTable.addCell(summaryCell);
            pdfDoc.add(summaryTable);

            Paragraph medalPara = new Paragraph("Status: " + getMedal(overallAvg), rowFont);
            medalPara.setAlignment(Element.ALIGN_RIGHT);
            pdfDoc.add(medalPara);

            pdfDoc.close();
            JOptionPane.showMessageDialog(mainPanel, "Moderni PDF izvještaj je spremljen!");
        } catch(Exception ex){
            JOptionPane.showMessageDialog(mainPanel, "Greška pri generisanju PDF-a: " + ex.getMessage());
        }
    }

    private String getMedal(double avg) {
        if (avg >= 4.8) return " (Zlatna medalja)";
        if (avg >= 4.5) return " (Srebrna medalja)";
        if (avg >= 4.0) return " (Bronzana medalja)";
        return "";
    }

    private void goBackToMainMenu() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        parentFrame.setContentPane(new MainPanel(currentUserEmail));
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        return button;
    }

    private void showMissingGradesNotification() {
        StringBuilder missingSubjects = new StringBuilder();
        for (Map.Entry<String, ArrayList<Integer>> entry : studentRecord.getStudentSubjectGrades().entrySet()) {
            if (entry.getValue().isEmpty()) missingSubjects.append("- ").append(entry.getKey()).append("\n");
        }
        if (missingSubjects.length() > 0) {
            JOptionPane.showMessageDialog(mainPanel, "Predmeti bez ocjena:\n" + missingSubjects, "Podsjetnik", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public JPanel getMainPanel() { return mainPanel; }
}