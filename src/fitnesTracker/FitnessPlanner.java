package fitnesTracker;

import mainPanel.MainPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FitnessPlanner {

    private JPanel mainPanel;
    private JTextField caloriesField, durationField, distanceField, dateField;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, backButton, exportPdfButton;

    private FitnessDBManager dbManager;
    private String userEmail;

    public FitnessPlanner() {
        userEmail = "test@example.com";
        dbManager = new FitnessDBManager();

        mainPanel = new JPanel(new BorderLayout(20,20));
        mainPanel.setBorder(new EmptyBorder(20,20,20,20));
        mainPanel.setBackground(Color.WHITE);

        // Top bar sa nazad dugmetom
        backButton = createButton("← Nazad", new Color(120,120,120));
        backButton.addActionListener(e -> goBackToMainMenu());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(backButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel sa formom i tabelom
        JPanel centerPanel = new JPanel(new BorderLayout(20,20));
        centerPanel.setBackground(Color.WHITE);

        // Forma za unos
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()),15);
        caloriesField = new JTextField(15);
        durationField = new JTextField(15);
        distanceField = new JTextField(15);

        formPanel.add(createField("Datum (yyyy-MM-dd):", dateField), gbc);
        gbc.gridy++;
        formPanel.add(createField("Kalorije (kcal):", caloriesField), gbc);
        gbc.gridy++;
        formPanel.add(createField("Trajanje (min):", durationField), gbc);
        gbc.gridy++;
        formPanel.add(createField("Distanca (km):", distanceField), gbc);

        // Dugmad ispod forme
        gbc.gridy++;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btnPanel.setBackground(Color.WHITE);
        addButton = createButton("Dodaj trening", new Color(52,152,219));
        addButton.addActionListener(e -> addRecord());
        exportPdfButton = createButton("Export PDF", new Color(46,204,113));
        exportPdfButton.addActionListener(e -> exportCalendarPDF());
        btnPanel.add(addButton);
        btnPanel.add(exportPdfButton);
        formPanel.add(btnPanel, gbc);

        centerPanel.add(formPanel, BorderLayout.NORTH);

        // Tabela zadnjih 5 treninga
        tableModel = new DefaultTableModel(
                new String[]{"Datum","Kalorije","Trajanje","Distanca","Intenzitet"},0
        );
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setEnabled(false);

        // Renderer za boje intenziteta
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                String intensity = (String) table.getValueAt(row,4);
                switch(intensity){
                    case "Slab": c.setBackground(new Color(144,238,144)); break;
                    case "Srednji": c.setBackground(new Color(255,255,102)); break;
                    case "Visok": c.setBackground(new Color(255,165,0)); break;
                    case "Vrlo visok": c.setBackground(new Color(255,69,0)); break;
                    default: c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Zadnji treninzi"));
        tableScroll.setPreferredSize(new Dimension(800,180));

        centerPanel.add(tableScroll, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        loadLast5();
    }

    private JPanel createField(String label, JTextField field){
        JPanel p = new JPanel(new BorderLayout(5,5));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(l, BorderLayout.WEST);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JButton createButton(String text, Color color){
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void addRecord(){
        try{
            String date = dateField.getText();
            double calories = Double.parseDouble(caloriesField.getText());
            int duration = Integer.parseInt(durationField.getText());
            double distance = Double.parseDouble(distanceField.getText());
            String intensity = calculateIntensity(calories);

            FitnessRecord record = new FitnessRecord(userEmail,date,calories,duration,distance,intensity);
            dbManager.addFitnessRecord(record);

            caloriesField.setText("");
            durationField.setText("");
            distanceField.setText("");
            dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            loadLast5();
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Greška u unosu: "+e.getMessage());
        }
    }

    private String calculateIntensity(double calories){
        if(calories<200) return "Slab";
        if(calories<400) return "Srednji";
        if(calories<700) return "Visok";
        return "Vrlo visok";
    }

    private void loadLast5(){
        tableModel.setRowCount(0);
        ArrayList<FitnessRecord> records = dbManager.getAllRecords(userEmail);
        int start = Math.max(0, records.size()-8);
        for(int i=records.size()-1;i>=start;i--){
            FitnessRecord r = records.get(i);
            tableModel.addRow(new Object[]{
                    r.getDate(),
                    r.getCalories(),
                    r.getDuration()+" min",
                    r.getDistance()+" km",
                    r.getIntensity()
            });
        }
    }

    private void exportCalendarPDF(){
        JOptionPane.showMessageDialog(null,"PDF export funkcija ide ovdje.");
    }


    private void goBackToMainMenu() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        frame.setContentPane(new MainPanel(userEmail));
        frame.revalidate();
        frame.repaint();
    }

    public JPanel getMainPanel(){ return mainPanel; }
}
