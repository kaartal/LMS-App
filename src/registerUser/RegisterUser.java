package registerUser;

import loginUser.LoginUser;
import lifemanagmentsystem.SessionManager;
import lifemanagmentsystem.UserService;
import javax.swing.*;
import java.awt.*;

public class RegisterUser extends JFrame { private JTextField inputName;
    private JLabel titleLabel;
    private JPanel nameLabel;
    private JTextField inputLastname;
    private JLabel lastnameLabel;
    private JLabel emailLabel;
    private JTextField inputEmail;
    private JLabel passwordLabel;
    private JPasswordField inputPassword;
    private JPasswordField inputRepeatPassword;
    private JLabel labelRepeatPassword;
    private JButton registerButton;
    private JButton backLogin;
    private final UserService userService;
    private JComboBox<String> themeComboBox;


    public RegisterUser() {
        userService = new UserService();
        setTitle("LMS - Registracija korisnika");
        setSize(650, 720);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();


                GradientPaint gp = new GradientPaint(0, 0, new Color(101, 116, 128),
                        0, height, new Color(21,101,192));
                g2.setPaint(gp);
                g2.fillRect(0, 0, width, height);
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 16);

        JLabel titleLabel = new JLabel("Kreirajte račun");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 38));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        mainPanel.add(titleLabel, gbc);

        gbc.gridy++;
        mainPanel.add(Box.createVerticalStrut(15), gbc);

        gbc.gridy++;
        mainPanel.add(createFieldPanel("Ime", labelFont, inputFont, "name"), gbc);
        gbc.gridy++;
        mainPanel.add(createFieldPanel("Prezime", labelFont, inputFont, "lastname"), gbc);
        gbc.gridy++;
        mainPanel.add(createFieldPanel("Email adresa", labelFont, inputFont, "email"), gbc);
        gbc.gridy++;
        mainPanel.add(createFieldPanel("Lozinka", labelFont, inputFont, "password"), gbc);
        gbc.gridy++;
        mainPanel.add(createFieldPanel("Ponovi lozinku", labelFont, inputFont, "repeat"), gbc);



        gbc.gridy++;
        JPanel themePanel = new JPanel();
        themePanel.setLayout(new BoxLayout(themePanel, BoxLayout.Y_AXIS));
        themePanel.setOpaque(false);

        JLabel themeLabel = new JLabel("Tema aplikacije");
        //themeLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        themeLabel.setFont(labelFont);
        themeLabel.setForeground(Color.WHITE);
        themeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        themeComboBox = new JComboBox<>(new String[]{"Zelena", "Plava", "Roza", "Narandzasta", "Tamna", "Cyberpunk"});
        themeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        themeComboBox.setFont(new Font("Arial", Font.PLAIN, 16));

        themePanel.add(themeLabel);
        themePanel.add(Box.createVerticalStrut(5));
        themePanel.add(themeComboBox);
        mainPanel.add(themePanel, gbc);



        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0, 0);
        registerButton = new JButton("Registruj se");
        buttonRegisterNewUser(registerButton, new Color(76, 175, 80));
        mainPanel.add(registerButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        backLogin = new JButton("Vrati se na prijavu");
        buttonGoBackToLogin(backLogin);
        mainPanel.add(backLogin, gbc);

        add(mainPanel);



        registerButton.addActionListener(e -> registerAction());
        backLogin.addActionListener(e -> {
            dispose();
            new LoginUser().setVisible(true);
        });
    }

    private JPanel createFieldPanel(String labelText, Font labelFont, Font inputFont, String type) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(new Color(255, 254, 254));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent field;
        switch (type) {
            case "password": field = inputPassword = new JPasswordField(); break;
            case "repeat": field = inputRepeatPassword = new JPasswordField(); break;
            case "email": field = inputEmail = new JTextField(); break;
            case "name": field = inputName = new JTextField(); break;
            default: field = inputLastname = new JTextField(); break;
        }

        field.setFont(inputFont);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(5));
        fieldPanel.add(field);
        return fieldPanel;
    }

    // CHECK IS EMAIL INPUT FROM USER VALID
    private boolean isValidEmail(String email)
    {
        return email.contains("@") && email.contains(".");
    }

    // BUTTON FOR REGISTER NEW USER
    private void buttonRegisterNewUser(JButton button, Color color) {
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 17));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
    }

    // BUTTON FOR GO BACK TO LOGIN SECTION
    private void buttonGoBackToLogin(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void registerAction() {
        String name = inputName.getText();
        String lastname = inputLastname.getText();
        String email = inputEmail.getText();
        String password = new String(inputPassword.getPassword());
        String repeatPassword = new String(inputRepeatPassword.getPassword());
        String theme = (String) themeComboBox.getSelectedItem();
        if (name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Popunite sva polja!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Neispravan email format!");
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Šifra mora imati najmanje 6 karaktera!");
            return;
        }

        if (!password.equals(repeatPassword)) {

            JOptionPane.showMessageDialog(this, "Šifre se ne poklapaju!");
            return;
        }

        if (userService.registerUser(name, lastname, email, password, theme)) {
            JOptionPane.showMessageDialog(this, "Registracija uspješna!");
            dispose();
            new LoginUser().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Korisnik već postoji!");
        }
    }
}