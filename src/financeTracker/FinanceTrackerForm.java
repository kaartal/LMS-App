package financeTracker;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import mainPanel.MainPanel;

import lifemanagmentsystem.SessionManager;

public class FinanceTrackerForm {

    private JPanel mainPanel;
    private JTextField amountField, descriptionField;
    private JComboBox<String> typeDropdownMenu, categoryCombo;
    private JButton addTransactionButton, updateButton, deleteButton,
            deleteAllTransactionButton, exportButton, backButton;
    private JTable transactionTableSection;
    private JLabel incomeLabel, expenseLabel, balanceLabel;

    private final TransactionManager manager;
    private final String userEmail;

    public FinanceTrackerForm() {
        manager = new TransactionManager();
        userEmail = SessionManager.getLoggedUserEmail();


        Color themeColor = SessionManager.getThemeColor();

        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(themeColor);
        mainPanel.setSize(950, 720);



        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(themeColor);


        backButton = createModernButton("← Nazad", new Color(127, 140, 141));
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(themeColor);
        backPanel.add(backButton);
        topPanel.add(backPanel);

        typeDropdownMenu = new JComboBox<>(new String[]{"Prihod", "Rashod"});
        topPanel.add(createNewDisplay("Vrsta transakcije:", typeDropdownMenu));

        amountField = new JTextField();
        topPanel.add(createNewDisplay("Iznos:", amountField));

        descriptionField = new JTextField();
        topPanel.add(createNewDisplay("Opis:", descriptionField));

        categoryCombo = new JComboBox<>();
        topPanel.add(createNewDisplay("Kategorija:", categoryCombo));

        typeDropdownMenu.addActionListener(e -> showHideCategoryByInput());
        showHideCategoryByInput();

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBackground(themeColor);

        addTransactionButton = createModernButton("Dodaj", new Color(72, 201, 176));
        updateButton = createModernButton("Ažuriraj", new Color(52, 152, 219));
        deleteButton = createModernButton("Obriši označene", new Color(231, 76, 60));
        deleteAllTransactionButton = createModernButton("Obriši sve", new Color(241, 196, 15));
        exportButton = createModernButton("Eksportuj", new Color(155, 89, 182));

        buttonPanel.add(addTransactionButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllTransactionButton);
        buttonPanel.add(exportButton);

        topPanel.add(buttonPanel);


        transactionTableSection = new JTable();
        transactionTableSection.setRowHeight(28);
        transactionTableSection.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionTableSection.setBackground(Color.WHITE);

        JScrollPane tableScroll = new JScrollPane(transactionTableSection);


        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        summaryPanel.setBackground(themeColor);

        incomeLabel = createSummaryLabel("Prihod: 0.0", new Color(39, 174, 96));
        expenseLabel = createSummaryLabel("Rashod: 0.0", new Color(192, 57, 43));
        balanceLabel = createSummaryLabel("Saldo: 0.0", new Color(41, 128, 185));

        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);
        summaryPanel.add(balanceLabel);


        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);


        backButton.addActionListener(e -> goBackToMainMenu());
        addTransactionButton.addActionListener(e -> addNewTransaction());
        updateButton.addActionListener(e -> updateSelectedTransaction());
        deleteButton.addActionListener(e -> deleteSelectedTransactions());
        deleteAllTransactionButton.addActionListener(e -> deleteAllTransactions());
        exportButton.addActionListener(e -> exportData());

        transactionTableSection.getSelectionModel()
                .addListSelectionListener(e -> loadSelectedTransactionIntoFields());

        loadDataIntoTable();
        updateSummary();
    }



    private void goBackToMainMenu() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        frame.setContentPane(new MainPanel(userEmail));
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createNewDisplay(String labelText, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(130, 28));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(label);

        field.setPreferredSize(new Dimension(200, 28));
        panel.add(field);

        return panel;
    }

    private void showHideCategoryByInput() {
        categoryCombo.removeAllItems();
        if ("Prihod".equals(typeDropdownMenu.getSelectedItem())) {
            categoryCombo.addItem("Plata");
            categoryCombo.addItem("Ostalo");
        } else {
            categoryCombo.addItem("Hrana");
            categoryCombo.addItem("Racuni");
            categoryCombo.addItem("Zabava");
            categoryCombo.addItem("Prijevoz");
            categoryCombo.addItem("Ostalo");
        }
    }

    private JButton createModernButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return b;
    }

    private JLabel createSummaryLabel(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(c);
        return l;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


    private void addNewTransaction() {
        try {
            manager.addNewTransaction(new Transaction(
                    userEmail,
                    typeDropdownMenu.getSelectedItem().toString(),
                    Double.parseDouble(amountField.getText()),
                    descriptionField.getText(),
                    categoryCombo.getSelectedItem().toString()
            ));
            loadDataIntoTable();
            updateSummary();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Greška pri unosu!");
        }
    }

    private void updateSelectedTransaction() {
        int row = transactionTableSection.getSelectedRow();
        if (row == -1) return;

        ArrayList<Transaction> list = manager.getUserTransactions(userEmail);
        manager.updateSelectedTransaction(new Transaction(
                list.get(row).getId(),
                userEmail,
                typeDropdownMenu.getSelectedItem().toString(),
                Double.parseDouble(amountField.getText()),
                descriptionField.getText(),
                categoryCombo.getSelectedItem().toString()
        ));

        loadDataIntoTable();
        updateSummary();
    }

    private void deleteSelectedTransactions() {
        ArrayList<Transaction> list = manager.getUserTransactions(userEmail);
        for (int i = transactionTableSection.getRowCount() - 1; i >= 0; i--) {
            Boolean checked = (Boolean) transactionTableSection.getValueAt(i, 0);
            if (checked != null && checked) {
                manager.deleteMarkedTransaction(list.get(i).getId(), userEmail);
            }
        }
        loadDataIntoTable();
        updateSummary();
    }

    private void deleteAllTransactions() {
        manager.deleteAllTransactions(userEmail);
        loadDataIntoTable();
        updateSummary();
    }

    private void exportData() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("finance_export.txt"));
            pw.println("Saldo: " + (manager.getTotalIncome(userEmail) - manager.getTotalExpense(userEmail)));
            pw.close();
            JOptionPane.showMessageDialog(null, "Eksport uspješan!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Greška pri eksportu!");
        }
    }

    private void loadDataIntoTable() {
        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int r, int c) { return c == 0; }
            public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };

        model.addColumn("");
        model.addColumn("Vrsta");
        model.addColumn("Iznos");
        model.addColumn("Opis");
        model.addColumn("Kategorija");

        for (Transaction t : manager.getUserTransactions(userEmail)) {
            model.addRow(new Object[]{false, t.getType(), t.getAmount(), t.getDescription(), t.getCategory()});
        }

        transactionTableSection.setModel(model);
    }

    private void loadSelectedTransactionIntoFields() {
        int row = transactionTableSection.getSelectedRow();
        if (row == -1) return;

        typeDropdownMenu.setSelectedItem(transactionTableSection.getValueAt(row, 1));
        amountField.setText(transactionTableSection.getValueAt(row, 2).toString());
        descriptionField.setText(transactionTableSection.getValueAt(row, 3).toString());
        categoryCombo.setSelectedItem(transactionTableSection.getValueAt(row, 4));
    }

    private void updateSummary() {
        double income = manager.getTotalIncome(userEmail);
        double expense = manager.getTotalExpense(userEmail);
        balanceLabel.setText("Saldo: " + (income - expense));
        incomeLabel.setText("Prihod: " + income);
        expenseLabel.setText("Rashod: " + expense);
    }
}
