package accountDetails;

import lifemanagmentsystem.SessionManager;
import lifemanagmentsystem.UserService;
import mainPanel.MainPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AccountDetailsFrame extends JFrame {

    private JTextField editName;

    private JTextField editLastname;

    private JTextField editMail;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;

    private JComboBox<String> themeComboBox;

    private JButton saveChanges;
    private JButton backMain;

    private final UserService userService;
    private final String currentUserEmail;

    private JLabel nameLabel;
    private JLabel lastnameLabel;

    //private final UserService userService;
    //private final String currentUserEmail;
//
    //private JLabel nameLabel;
    //private JLabel lastnameLabel;

    private JLabel mailLabel;

    private JLabel oldPasswordLabel;

    private JLabel newPasswordLabel;

    private JLabel themeLabel;
    private JLabel titleLabel;
    private JLabel editPassword;

    public AccountDetailsFrame(String email) {
        this.currentUserEmail = email;
        //this.currentUserEmail = "test@test.com";
        this.userService = new UserService();

        setTitle("Account Details");
        setSize(950, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //setTitle("Account Details");
        //setSize(750, 420);
        //setLocationRelativeTo(null);
        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        accountDetails();
        loadUserData();
    }

    private void accountDetails() {

        String theme = userService.getUserTheme(currentUserEmail);
        Color bgColor = SessionManager.getColorFromTheme(theme);

        JPanel root = new JPanel(new BorderLayout());
            root.setBackground(bgColor);
        root.setBorder(new EmptyBorder(40, 40, 40, 40));
            setContentPane(root);

            JLabel title = new JLabel("Profil korisnika");
        UserService userService = new UserService();
            title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
            root.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(bgColor);
        card.setBorder(new EmptyBorder(30, 60, 30, 60));
        root.add(card, BorderLayout.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);


        nameLabel = createLabelDesign("Ime", labelFont);
        addLabelDesign(card, nameLabel, gbc, 0);

        editName = createFieldDesign();

        addFieldDesign(card, editName, gbc, 0);


        lastnameLabel = createLabelDesign("Prezime", labelFont);

        addLabelDesign(card, lastnameLabel, gbc, 1);

        editLastname = createFieldDesign();
        addFieldDesign(card, editLastname, gbc, 1);


        mailLabel = createLabelDesign("Email", labelFont);
        addLabelDesign(card, mailLabel, gbc, 2);
        editMail = createFieldDesign();
        editMail.setEditable(false);
        addFieldDesign(card, editMail, gbc, 2);


        oldPasswordLabel = createLabelDesign("Stara šifra", labelFont);
        addLabelDesign(card, oldPasswordLabel, gbc, 3);
        oldPasswordField = new JPasswordField();
        styleFieldDesign(oldPasswordField);
        addFieldDesign(card, oldPasswordField, gbc, 3);


        newPasswordLabel = createLabelDesign("Nova šifra", labelFont);
        addLabelDesign(card, newPasswordLabel, gbc, 4);
        newPasswordField = new JPasswordField();
        styleFieldDesign(newPasswordField);
        addFieldDesign(card, newPasswordField, gbc, 4);


        themeLabel = createLabelDesign("Tema", labelFont);
        addLabelDesign(card, themeLabel, gbc, 5);
        themeComboBox = new JComboBox<>(new String[]{
                "Zelena", "Plava", "Roza", "Narandzasta", "Cyberpunk"
        });
        styleComboDesign(themeComboBox);
        addFieldDesign(card, themeComboBox, gbc, 5);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        buttonPanel.setOpaque(false);

        backMain = changeButtonDesign("Nazad", new Color(80, 80, 80));
        saveChanges = changeButtonDesign("Sačuvaj", new Color(40, 160, 90));

        buttonPanel.add(backMain);
        buttonPanel.add(saveChanges);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        card.add(buttonPanel, gbc);

        saveChanges.addActionListener(e -> saveChangesAction());
        backMain.addActionListener(e -> goBackToMainPanel());
    }

    //private void loadInfo() {
        //var user = userService.getUserByEmail(currentUserEmail);
        //if (user != null) {
            //editName.setText(user.getString("name"));
            //editLastname.setText(user.getString("lastname"));
        //}
    //}


    private void loadUserData() {
        var user = userService.getUserByEmail(currentUserEmail);
        if (user != null) {
            editName.setText(user.getString("name"));
            editLastname.setText(user.getString("lastname"));
            editMail.setText(user.getString("email"));
            themeComboBox.setSelectedItem(user.getString("theme"));
        }
    }

    private void saveChangesAction() {
        String name = editName.getText();

        String lastname = editLastname.getText();
         String oldPass = new String(oldPasswordField.getPassword());
         String newPass = new String(newPasswordField.getPassword());
        String theme = (String) themeComboBox.getSelectedItem();

        var user = userService.getUserByEmail(currentUserEmail);
        if (user == null) return;
        //if (user != null) return;
        //boolean changed = true;
        boolean changed = false;
        String finalPassword = user.getString("password");



        if (!name.equals(user.getString("name"))) changed = true;
        if (!lastname.equals(user.getString("lastname"))) changed = true;
        if (!theme.equals(user.getString("theme"))) changed = true;

        //if (!name.equals(user.getString("name"))) changed = false;
        //if (!lastname.equals(user.getString("lastname"))) changed = false;
        //if (!theme.equals(user.getString("theme"))) changed = false;

        if (!newPass.isEmpty()) {
            if (oldPass.isEmpty()) {
                showMessageToUser("Unesite staru šifru.");
                return;
            }
            if (!userService.loginUser(currentUserEmail, oldPass)) {
                showMessageToUser("Stara šifra nije tačna.");
                return;
            }
            if (newPass.length() < 6) {
                showMessageToUser("Nova šifra mora imati barem 6 karaktera.");
                return;
            }
            finalPassword = newPass;
            changed = true;
        }

        if (!changed) {
            showMessageToUser("Nema promjena.");
            return;
        }

        //if (changed) {
        //    showMsg("Nema promjena.");
          //  return;
       // }

        boolean updated = userService.updateUserTheme(
                currentUserEmail, name, lastname, finalPassword, theme
        );

        if (updated) {
            showMessageToUser("Podaci uspješno sačuvani!");
            dispose();
            new AccountDetailsFrame(currentUserEmail).setVisible(true);
        } else {
            showMessageToUser("Greška pri snimanju.");
        }
    }

    private void goBackToMainPanel() {
        dispose();
        JFrame frame = new JFrame("Life Management System");
        frame.setSize(950, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new MainPanel(currentUserEmail));
        frame.setVisible(true);
    }



    private JLabel createLabelDesign(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(Color.WHITE);
        return label;
    }

    //private JLabel label(String text) {
        //JLabel label = new JLabel(text);
        //label.setFont(font);
        //label.setForeground(Color.WHITE);
        //return label;
    //}

    private void addLabelDesign(JPanel panel, JLabel label, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(label, gbc);
    }

    private void addFieldDesign(JPanel panel, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridx = 1;
        gbc.gridy = y;
        panel.add(field, gbc);
    }

    private JTextField createFieldDesign() {
        JTextField field = new JTextField();
        styleFieldDesign(field);
        return field;
    }

    private void styleFieldDesign(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    }

    private void styleComboDesign(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    }


    private JButton changeButtonDesign(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hover = baseColor.brighter();

        button.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            public void mouseExited(MouseEvent e) {

                button.setBackground(baseColor);
            }
        });

        return button;
    }

    private void showMessageToUser(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}
