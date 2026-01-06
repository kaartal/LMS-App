package financeTracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import mainPanel.MainPanel;
import lifemanagmentsystem.SessionManager;
//import accountDetails.AccountDetailsFrame;
//import financeTracker.FinanceTrackerForm;
//import fitnesTracker.FitnessPlanner;
//import habitTracker.HabitRecord;
//import habitTracker.HabitTracker;
//import lifemanagmentsystem.SessionManager;
//import lifemanagmentsystem.UserService;
//import lifemanagmentsystem.UserTheme;
//import loginUser.LoginUser;
//import sleepTracker.SleepTracker;
//import studyPlanner.StudyPlanner;
//import studyPlanner.StudyRecord;
//import studyPlanner.StudyPlanner;

import lifemanagmentsystem.UserService;

public class FinanceTrackerForm {

    private JPanel mainPanel;

    private JTextField amountField, descriptionField;

    private JComboBox<String> typeDropdownMenu, categoryCombo;

    private JButton addTransactionButton, updateButton, deleteButton, deleteAllTransactionButton, exportButton, backButton;
    private JTable transactionTableSection;


    private JLabel incomeLabel, expenseLabel, balanceLabel;

    // private JButton newTransaction, update
    private final TransactionTransfer manager;
    private final String userEmail;
    private String currentUserEmail;

    public FinanceTrackerForm(String email) {
        this.currentUserEmail = email;
        this.userEmail = SessionManager.getLoggedUserEmail();

        manager = new TransactionTransfer();

        //CATCH THEME FROM USER
        UserService userService = new UserService();
        String theme = userService.getUserTheme(currentUserEmail);
        Color bgColor = SessionManager.getColorFromTheme(theme);

        // GIVE THEME FROM USER TO PANEL
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(bgColor);
        mainPanel.setSize(950, 720);


        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(bgColor);

        backButton = newDesign("← Nazad", new Color(127, 140, 141));
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(bgColor);
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



        // +++++BUTTONS+++++
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBackground(bgColor);

        addTransactionButton = newDesign("Dodaj", new Color(72, 201, 176));
        updateButton = newDesign("Ažuriraj", new Color(52, 152, 219));
        deleteButton = newDesign("Obriši označene", new Color(231, 76, 60));
        deleteAllTransactionButton = newDesign("Obriši sve", new Color(241, 196, 15));
        exportButton = newDesign("Eksportuj", new Color(155, 89, 182));

        buttonPanel.add(addTransactionButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllTransactionButton);
        buttonPanel.add(exportButton);

        topPanel.add(buttonPanel);

        // ++++TABLE++++
        transactionTableSection = new JTable();
        transactionTableSection.setRowHeight(28);
        transactionTableSection.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionTableSection.setBackground(Color.WHITE);
        JScrollPane tableScroll = new JScrollPane(transactionTableSection);


        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        summaryPanel.setBackground(bgColor);

        incomeLabel = createSummaryLabel("Prihod: 0.0", Color.WHITE);
        expenseLabel = createSummaryLabel("Rashod: 0.0", Color.WHITE);
        balanceLabel = createSummaryLabel("Saldo: 0.0", Color.WHITE);

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




    private void addNewTransaction() {
        try {
            manager.addNewTransaction(new Transaction(
                    userEmail,
                    typeDropdownMenu.getSelectedItem().toString(),
                    Double.parseDouble(amountField.getText()),
                    descriptionField.getText(),
                    categoryCombo.getSelectedItem().toString()
            ));

            amountField.setText("");
            descriptionField.setText("");
            amountField.requestFocus();

            loadDataIntoTable();
            updateSummary();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Greška pri unosu!");
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



    //private void deleteSelectedTransactions() {
        //ArrayList<Transaction> list = manager.getUserTransactions(userEmail);
       // int deletedCount = 0;
//
        // for (int i = transactionTableSection.getRowCount() - 1; i >= 0; i--) {
          //  Boolean checked = (Boolean) transactionTableSection.getValueAt(i, 0);
          //  if (checked != null && checked) {
             //   manager.deleteMarkedTransaction(list.get(i).getId(), userEmail);
              //  deletedCount++;
           // }
      //  }

   // }


    private void deleteSelectedTransactions() {
        ArrayList<Transaction> list = manager.getUserTransactions(userEmail);
        int deletedCount = 0;

        for (int i = transactionTableSection.getRowCount() - 1; i >= 0; i--) {
            Boolean checked = (Boolean) transactionTableSection.getValueAt(i, 0);
            if (checked != null && checked) {
                manager.deleteMarkedTransaction(list.get(i).getId(), userEmail);
                deletedCount++;
            }
        }

        if (deletedCount == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Nema označenih transakcija za brisanje!");
            return;
        }

             loadDataIntoTable();
                     updateSummary();
    }

    private void deleteAllTransactions() {
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "Da li ste sigurni da želite obrisati sve transakcije?",
                "Potvrda brisanja",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.deleteAllTransactions(userEmail);
            loadDataIntoTable();
            updateSummary();
        }
    }

    //private void exportData() {
       // try {
           // JFileChooser fileChooser = new JFileChooser();
           // fileChooser.setDialogTitle("Odaberite lokaciju za eksport");
           // fileChooser.setSelectedFile(new File("finance_export.txt"));

    private void exportData() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Odaberite lokaciju za eksport");
            fileChooser.setSelectedFile(new File("finance_export.txt"));
            int option = fileChooser.showSaveDialog(mainPanel);
            if (option != JFileChooser.APPROVE_OPTION) return;

            File file = fileChooser.getSelectedFile();
            PrintWriter pw = new PrintWriter(new FileWriter(file));

            TransactionTransfer.FinancialSummary summary = manager.getFinancialSummary(userEmail);

            pw.println("****** Finansijski Izvještaj ******");
            pw.println("Ukupni Prihodi: " + summary.totalIncome);
            pw.println("Ukupni Rashodi: " + summary.totalExpense);
            pw.println("Saldo: " + summary.getBalance());
            pw.println();

            pw.println("****** Rashodi po kategorijama ******");
            for (Map.Entry<String, Double> e : summary.expenseByCategory.entrySet()) {
                pw.println(e.getKey() + ": " + e.getValue());
            }
            pw.println();

            pw.println("****** Prihodi po kategorijama ******");
            for (Map.Entry<String, Double> e : summary.incomeByCategory.entrySet()) {
                pw.println(e.getKey() + ": " + e.getValue());

            }


            //pw.println("=== Spisak transakcije ===");
            //for (Map.Entry<String, Double> e : summary.incomeByCategory.entrySet()) {
                //pw.println(e.getKey() + ": " + e.getValue());
            //}


            pw.println("****** Detaljna lista svih transakcija ******");
            pw.println(String.format("%-10s | %-10s | %-20s | %-15s", "Vrsta", "Iznos", "Opis", "Kategorija"));
            pw.println("----------------------------------------------------------------------");
            ArrayList<Transaction> lista = manager.getUserTransactions(userEmail);

            for (Transaction t : lista) {
                pw.println(String.format("%-10s | %-10.2f | %-20s | %-15s",
                        t.getType(),
                        t.getAmount(),
                        t.getDescription(),
                        t.getCategory()));
            }

            pw.close();
            JOptionPane.showMessageDialog(mainPanel, "Eksport uspješan na: " + file.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Greška pri eksportu: " + e.getMessage());
        }




        //pw.close();
        //JOptionPane.showMessageDialog(mainPanel, "Eksport uspješan na: " + file.getAbsolutePath());
    //} catch (Exception e) {
        //JOptionPane.showMessageDialog(mainPanel, "Greška pri eksportu: " + e.getMessage());
    //}
    }



    // LOAD ITEMS IN THE TABLE
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


    // LOAD SELECTED ITEM IN INPUT PLACE
    private void loadSelectedTransactionIntoFields() {
        int row = transactionTableSection.getSelectedRow();
        if (row == -1) return;

        typeDropdownMenu.setSelectedItem(transactionTableSection.getValueAt(row, 1));
        amountField.setText(transactionTableSection.getValueAt(row, 2).toString());
        descriptionField.setText(transactionTableSection.getValueAt(row, 3).toString());
        categoryCombo.setSelectedItem(transactionTableSection.getValueAt(row, 4));
    }



    private void updateSummary() {
        TransactionTransfer.FinancialSummary summary = manager.getFinancialSummary(userEmail);
        incomeLabel.setText("Prihod: " + summary.totalIncome);
        expenseLabel.setText("Rashod: " + summary.totalExpense);
        balanceLabel.setText("Saldo: " + summary.getBalance());
    }



    //private void goBack() {
        //JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        //frame.setContentPane(new MainPanel(userEmail));
        //frame.revalidate();
    //}

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


    //private void showHideCategoryByInput() {
        //categoryCombo.removeAllItems();
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

    private JButton newDesign(String text, Color bg) {
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
}
