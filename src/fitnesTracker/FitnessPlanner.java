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
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

public class FitnessPlanner {

    private JPanel fitnessMainContainerPanel;
    private JTextField inputFieldCaloriesBurned, inputFieldDurationMinutes, inputFieldDistanceKilometers, inputFieldDateOfActivity;
    private JTable fitnessActivitiesDisplayTable;
    private DefaultTableModel fitnessActivitiesTableModel;

    private FitnesInformationTransfer fitnessDatabaseManagerService;
    private UserService fitnessUserAccountService;
    private String activeUserEmailAddress;
    private String activeUserSelectedTheme;

    public FitnessPlanner(String activeUserEmailAddress) {
        this.activeUserEmailAddress = activeUserEmailAddress;
        this.fitnessDatabaseManagerService = new FitnesInformationTransfer();
        this.fitnessUserAccountService = new UserService();
        this.activeUserSelectedTheme = fitnessUserAccountService.getUserTheme(activeUserEmailAddress);

        initializeUserInterfaceComponents();
        loadLastFitnessActivityRecords();
    }

    private void initializeUserInterfaceComponents() {
        Color applicationThemeBackgroundColor = SessionManager.getColorFromTheme(activeUserSelectedTheme);

        fitnessMainContainerPanel = new JPanel(new BorderLayout(20, 20));
        fitnessMainContainerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        fitnessMainContainerPanel.setBackground(applicationThemeBackgroundColor);

        JButton navigateBackToMainPanelButton = createStyledUserInterfaceButton("Nazad na glavni panel", new Color(120, 120, 120));
        navigateBackToMainPanelButton.addActionListener(e -> navigateBackToPreviousScreen());
        JPanel navigationTopBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationTopBarPanel.setOpaque(false);
        navigationTopBarPanel.add(navigateBackToMainPanelButton);
        fitnessMainContainerPanel.add(navigationTopBarPanel, BorderLayout.NORTH);

        JPanel centralContentWrapperPanel = new JPanel(new BorderLayout(20, 20));
        centralContentWrapperPanel.setOpaque(false);

        JPanel inputFormGridPanel = new JPanel(new GridBagLayout());
        inputFormGridPanel.setOpaque(false);
        GridBagConstraints formGridBagConstraints = new GridBagConstraints();
        formGridBagConstraints.insets = new Insets(10, 10, 10, 10);
        formGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        formGridBagConstraints.gridx = 0;

        inputFieldDateOfActivity = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 15);
        inputFieldCaloriesBurned = new JTextField(15);
        inputFieldDurationMinutes = new JTextField(15);
        inputFieldDistanceKilometers = new JTextField(15);
        inputFieldDistanceKilometers.setToolTipText("Opcionalno");

        formGridBagConstraints.gridy = 0; inputFormGridPanel.add(wrapFieldWithLabelComponent("Datum:", inputFieldDateOfActivity), formGridBagConstraints);
        formGridBagConstraints.gridy++; inputFormGridPanel.add(wrapFieldWithLabelComponent("Kalorije:", inputFieldCaloriesBurned), formGridBagConstraints);
        formGridBagConstraints.gridy++; inputFormGridPanel.add(wrapFieldWithLabelComponent("Trajanje (min):", inputFieldDurationMinutes), formGridBagConstraints);
        formGridBagConstraints.gridy++; inputFormGridPanel.add(wrapFieldWithLabelComponent("Distanca (km):", inputFieldDistanceKilometers), formGridBagConstraints);

        JButton addNewFitnessRecordButton = createStyledUserInterfaceButton("Dodaj trening", new Color(52, 152, 219));
        addNewFitnessRecordButton.addActionListener(e -> executeAddFitnessRecordAction());

        JButton exportBestPerformancePdfButton = createStyledUserInterfaceButton("Export PDF", new Color(46, 204, 113));
        exportBestPerformancePdfButton.addActionListener(e -> executePdfExportBestPerformancesAction());

        JButton deleteSelectedFitnessRecordButton = createStyledUserInterfaceButton("Obriši trening", new Color(231, 76, 60));
        deleteSelectedFitnessRecordButton.addActionListener(e -> executeDeleteSelectedRecordAction());

        JPanel actionButtonsFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionButtonsFlowPanel.setOpaque(false);
        actionButtonsFlowPanel.add(addNewFitnessRecordButton);
        actionButtonsFlowPanel.add(exportBestPerformancePdfButton);
        actionButtonsFlowPanel.add(deleteSelectedFitnessRecordButton);

        formGridBagConstraints.gridy++; inputFormGridPanel.add(actionButtonsFlowPanel, formGridBagConstraints);
        centralContentWrapperPanel.add(inputFormGridPanel, BorderLayout.NORTH);

        fitnessActivitiesTableModel = new DefaultTableModel(
                new String[]{"Datum", "Kalorije", "Minute", "Km", "Intenzitet"}, 0
        );
        fitnessActivitiesDisplayTable = new JTable(fitnessActivitiesTableModel);
        fitnessActivitiesDisplayTable.setRowHeight(30);
        fitnessActivitiesDisplayTable.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        fitnessActivitiesDisplayTable.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        fitnessActivitiesDisplayTable.getColumnModel().getColumn(4).setCellRenderer(new FitnessIntensityCellRenderer());

        JScrollPane fitnessActivitiesTableScrollPane = new JScrollPane(fitnessActivitiesDisplayTable);
        fitnessActivitiesTableScrollPane.setPreferredSize(new Dimension(800, 200));
        fitnessActivitiesTableScrollPane.setBorder(BorderFactory.createTitledBorder("Pregled zadnjih aktivnosti"));
        centralContentWrapperPanel.add(fitnessActivitiesTableScrollPane, BorderLayout.CENTER);

        fitnessMainContainerPanel.add(centralContentWrapperPanel, BorderLayout.CENTER);
    }

    private void applyModernStyleToTextField(JTextField targetInputTextField) {
        targetInputTextField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        targetInputTextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
    }

    private JPanel wrapFieldWithLabelComponent(String labelTitleText, JTextField targetInputTextField) {
        JPanel fieldAndLabelWrapperPanel = new JPanel(new BorderLayout(15, 0));
        fieldAndLabelWrapperPanel.setOpaque(false);
        JLabel inputFieldDescriptionLabel = new JLabel(labelTitleText);
        inputFieldDescriptionLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        inputFieldDescriptionLabel.setPreferredSize(new Dimension(110, 35));
        applyModernStyleToTextField(targetInputTextField);
        fieldAndLabelWrapperPanel.add(inputFieldDescriptionLabel, BorderLayout.WEST);
        fieldAndLabelWrapperPanel.add(targetInputTextField, BorderLayout.CENTER);
        return fieldAndLabelWrapperPanel;
    }

    private void executeAddFitnessRecordAction() {
        try {
            double amountOfCaloriesBurned = Double.parseDouble(inputFieldCaloriesBurned.getText());
            int workoutDurationInMinutes = Integer.parseInt(inputFieldDurationMinutes.getText());
            String rawDistanceInputString = inputFieldDistanceKilometers.getText().trim();
            double distanceTraveledInKilometers = rawDistanceInputString.isEmpty() ? 0.0 : Double.parseDouble(rawDistanceInputString);

            String calculatedFitnessIntensityLevel = calculateFitnessActivityIntensityLevel(amountOfCaloriesBurned, workoutDurationInMinutes, distanceTraveledInKilometers);

            FitnessRecord newFitnessActivityRecord = new FitnessRecord(activeUserEmailAddress, inputFieldDateOfActivity.getText(), amountOfCaloriesBurned, workoutDurationInMinutes, distanceTraveledInKilometers, calculatedFitnessIntensityLevel);
            fitnessDatabaseManagerService.addFitnessRecord(newFitnessActivityRecord);
            resetInputFormTextFields();
            loadLastFitnessActivityRecords();
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(fitnessMainContainerPanel, "Unesite ispravne brojeve!");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(fitnessMainContainerPanel, "Greška: " + exception.getMessage());
        }
    }

    private String calculateFitnessActivityIntensityLevel(double amountOfCaloriesBurned, int workoutDurationInMinutes, double distanceTraveledInKilometers) {
        double totalIntensityScoreValue = amountOfCaloriesBurned / 10 + workoutDurationInMinutes / 5.0 + distanceTraveledInKilometers * 2;
        if (totalIntensityScoreValue < 50) return "Slab";
        if (totalIntensityScoreValue < 100) return "Srednji";
        if (totalIntensityScoreValue < 150) return "Visok";
        return "Vrlo visok";
    }

    private void loadLastFitnessActivityRecords() {
        fitnessActivitiesTableModel.setRowCount(0);
        List<FitnessRecord> allFitnessActivityRecordsList = fitnessDatabaseManagerService.getAllRecords(activeUserEmailAddress);
        int startIndexForLastRecords = Math.max(0, allFitnessActivityRecordsList.size() - 8);
        for (int i = allFitnessActivityRecordsList.size() - 1; i >= startIndexForLastRecords; i--) {
            FitnessRecord fitnessRecordObject = allFitnessActivityRecordsList.get(i);
            fitnessActivitiesTableModel.addRow(new Object[]{fitnessRecordObject.getDate(), fitnessRecordObject.getCalories(), fitnessRecordObject.getDurationMinutes(), fitnessRecordObject.getDistanceKilometers(), fitnessRecordObject.getIntensityLevel()});
        }
    }

    private void executeDeleteSelectedRecordAction() {
        int selectedTableRowIndex = fitnessActivitiesDisplayTable.getSelectedRow();
        if (selectedTableRowIndex == -1) return;
        String selectedActivityDateString = fitnessActivitiesTableModel.getValueAt(selectedTableRowIndex, 0).toString();
        if (JOptionPane.showConfirmDialog(null, "Obrisati?", "Potvrda", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            fitnessDatabaseManagerService.deleteFitnessRecord(activeUserEmailAddress, selectedActivityDateString);
            loadLastFitnessActivityRecords();
        }
    }

    private void executePdfExportBestPerformancesAction() {
        try {
            JFileChooser pdfExportFileChooser = new JFileChooser();
            pdfExportFileChooser.setSelectedFile(new File("FitnesStatistic.pdf"));
            if (pdfExportFileChooser.showSaveDialog(fitnessMainContainerPanel) != JFileChooser.APPROVE_OPTION) return;

            Document pdfDocumentObject = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(pdfDocumentObject, new FileOutputStream(pdfExportFileChooser.getSelectedFile()));
            pdfDocumentObject.open();

            Font pdfHeaderTitleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph pdfMainHeaderTitleParagraph = new Paragraph("GODIŠNJI FITNESS IZVJEŠTAJ (BEST PERFORMANCE)\n\n", pdfHeaderTitleFont);
            pdfMainHeaderTitleParagraph.setAlignment(Element.ALIGN_CENTER);
            pdfDocumentObject.add(pdfMainHeaderTitleParagraph);

            Map<String, FitnessRecord> filteredBestRecordsByDateMap = new HashMap<>();
            for (FitnessRecord fitnessRecordItem : fitnessDatabaseManagerService.getAllRecords(activeUserEmailAddress)) {
                String activityDateString = fitnessRecordItem.getDate();
                if (!filteredBestRecordsByDateMap.containsKey(activityDateString) || fitnessRecordItem.getCalories() > filteredBestRecordsByDateMap.get(activityDateString).getCalories()) {
                    filteredBestRecordsByDateMap.put(activityDateString, fitnessRecordItem);
                }
            }

            String[] namesOfMonthsInYearArray = {"Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Avg", "Sep", "Okt", "Nov", "Dec"};
            Calendar calendarDateNavigatorInstance = Calendar.getInstance();
            int currentCalendarYearValue = calendarDateNavigatorInstance.get(Calendar.YEAR);

            PdfPTable headerDayNumbersTable = new PdfPTable(32);
            headerDayNumbersTable.setWidthPercentage(100);
            float[] headerWidths = new float[32];
            headerWidths[0] = 4f;
            for (int i = 1; i < 32; i++) headerWidths[i] = 1f;
            headerDayNumbersTable.setWidths(headerWidths);

            PdfPCell emptyCornerCell = new PdfPCell(new Phrase(""));
            emptyCornerCell.setBorder(Rectangle.NO_BORDER);
            headerDayNumbersTable.addCell(emptyCornerCell);

            for (int dayNum = 1; dayNum <= 31; dayNum++) {
                PdfPCell dayNumCell = new PdfPCell(new Phrase(String.valueOf(dayNum), new Font(Font.FontFamily.HELVETICA, 7)));
                dayNumCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                dayNumCell.setBorder(Rectangle.NO_BORDER);
                headerDayNumbersTable.addCell(dayNumCell);
            }
            pdfDocumentObject.add(headerDayNumbersTable);

            for (int monthIndexValue = 0; monthIndexValue < 12; monthIndexValue++) {
                calendarDateNavigatorInstance.set(currentCalendarYearValue, monthIndexValue, 1);
                int totalDaysInCurrentMonth = calendarDateNavigatorInstance.getActualMaximum(Calendar.DAY_OF_MONTH);

                PdfPTable monthCalendarGridTable = new PdfPTable(32);
                monthCalendarGridTable.setWidthPercentage(100);
                monthCalendarGridTable.setSpacingBefore(1f);
                monthCalendarGridTable.setWidths(headerWidths);

                PdfPCell monthNameIdentifierCell = new PdfPCell(new Phrase(namesOfMonthsInYearArray[monthIndexValue], new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD)));
                monthNameIdentifierCell.setBackgroundColor(new BaseColor(230, 230, 230));
                monthNameIdentifierCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                monthNameIdentifierCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                monthNameIdentifierCell.setFixedHeight(18f);
                monthCalendarGridTable.addCell(monthNameIdentifierCell);

                for (int dayIndexValue = 1; dayIndexValue <= 31; dayIndexValue++) {
                    PdfPCell dayActivityVisualCell = new PdfPCell();
                    dayActivityVisualCell.setFixedHeight(18f);
                    dayActivityVisualCell.setBorderColor(BaseColor.WHITE);

                    if (dayIndexValue <= totalDaysInCurrentMonth) {
                        String formattedMapDateKey = String.format("%d-%02d-%02d", currentCalendarYearValue, monthIndexValue + 1, dayIndexValue);
                        if (filteredBestRecordsByDateMap.containsKey(formattedMapDateKey)) {
                            dayActivityVisualCell.setBackgroundColor(getItextBaseColorForIntensityLevel(filteredBestRecordsByDateMap.get(formattedMapDateKey).getIntensityLevel()));
                        } else {
                            dayActivityVisualCell.setBackgroundColor(new BaseColor(245, 245, 245));
                        }
                    } else {
                        dayActivityVisualCell.setBackgroundColor(BaseColor.WHITE);
                        dayActivityVisualCell.setBorder(Rectangle.NO_BORDER);
                    }
                    monthCalendarGridTable.addCell(dayActivityVisualCell);
                }
                pdfDocumentObject.add(monthCalendarGridTable);
            }

            pdfDocumentObject.add(new Paragraph("\nLegenda intenziteta:", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));
            PdfPTable pdfLegendInformationalTable = new PdfPTable(8);
            pdfLegendInformationalTable.setWidthPercentage(50);
            pdfLegendInformationalTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfLegendInformationalTable.setSpacingBefore(10f);

            addLegendItemToPdfTable(pdfLegendInformationalTable, "Slab", new BaseColor(144, 238, 144));
            addLegendItemToPdfTable(pdfLegendInformationalTable, "Srednji", new BaseColor(255, 255, 102));
            addLegendItemToPdfTable(pdfLegendInformationalTable, "Visok", new BaseColor(255, 165, 0));
            addLegendItemToPdfTable(pdfLegendInformationalTable, "Vrlo visok", new BaseColor(255, 69, 0));

            pdfDocumentObject.add(pdfLegendInformationalTable);
            pdfDocumentObject.close();
            JOptionPane.showMessageDialog(fitnessMainContainerPanel, "PDF kreiran!");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(fitnessMainContainerPanel, "Greška: " + exception.getMessage());
        }
    }

    private void addLegendItemToPdfTable(PdfPTable targetPdfLegendTable, String legendLabelText, BaseColor legendIndicatorColor) {
        PdfPCell legendColorIndicatorBox = new PdfPCell();
        legendColorIndicatorBox.setBackgroundColor(legendIndicatorColor);
        legendColorIndicatorBox.setFixedHeight(12f);
        legendColorIndicatorBox.setBorder(Rectangle.NO_BORDER);
        targetPdfLegendTable.addCell(legendColorIndicatorBox);

        PdfPCell legendLabelTextCell = new PdfPCell(new Phrase(legendLabelText, new Font(Font.FontFamily.HELVETICA, 8)));
        legendLabelTextCell.setBorder(Rectangle.NO_BORDER);
        legendLabelTextCell.setPaddingLeft(5f);
        targetPdfLegendTable.addCell(legendLabelTextCell);
    }

    private BaseColor getItextBaseColorForIntensityLevel(String intensityLevelString) {
        switch (intensityLevelString) {
            case "Slab": return new BaseColor(144, 238, 144);
            case "Srednji": return new BaseColor(255, 255, 102);
            case "Visok": return new BaseColor(255, 165, 0);
            default: return new BaseColor(255, 69, 0);
        }
    }

    private void resetInputFormTextFields() {
        inputFieldCaloriesBurned.setText("");
        inputFieldDurationMinutes.setText("");
        inputFieldDistanceKilometers.setText("");
        inputFieldDateOfActivity.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    private JButton createStyledUserInterfaceButton(String buttonDisplayTitle, Color buttonBackgroundColor) {
        JButton customStyledActionButton = new JButton(buttonDisplayTitle);
        customStyledActionButton.setBackground(buttonBackgroundColor);
        customStyledActionButton.setForeground(Color.WHITE);
        customStyledActionButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        customStyledActionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        customStyledActionButton.setFocusPainted(false);
        customStyledActionButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return customStyledActionButton;
    }

    private void navigateBackToPreviousScreen() {
        JFrame parentWindowFrameReference = (JFrame) SwingUtilities.getWindowAncestor(fitnessMainContainerPanel);
        parentWindowFrameReference.setContentPane(new MainPanel(activeUserEmailAddress));
        parentWindowFrameReference.revalidate();
    }

    public JPanel getMainPanel() { return fitnessMainContainerPanel; }

    private static class FitnessIntensityCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable targetTable, Object cellValue, boolean isCellSelected, boolean hasCellFocus, int rowIndex, int columnIndex) {
            Component renderedCellComponent = super.getTableCellRendererComponent(targetTable, cellValue, isCellSelected, hasCellFocus, rowIndex, columnIndex);
            setHorizontalAlignment(SwingConstants.CENTER);
            return renderedCellComponent;
        }
    }
}