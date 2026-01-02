package loginUser;

import javax.swing.*;
import java.awt.*;
import mainPanel.MainPanel;
import lifemanagmentsystem.SessionManager;
import lifemanagmentsystem.UserService;
import registerUser.RegisterUser;

public class LoginUser extends JFrame {
    private JPanel passwordLabel;
    private JTextField inputEmail;
    private JPasswordField inputPassword;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private final UserService userService;

    public LoginUser() {
        userService = new UserService();

        setTitle("LMS - Prijava");
        setSize(650, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        JPanel panel = new JPanel() {

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(101, 116, 128),
                        0, height, new Color(21,101,192));
                g2.setPaint(gp);
                g2.fillRect(0, 0, width, height);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;

        Font titleFont = new Font("Segoe UI", Font.BOLD, 32);
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 16);


        JLabel titleLabel = new JLabel("Life Management System", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        panel.add(Box.createVerticalStrut(20), gbc);



        panel.add(createFieldPanel("Email", labelFont, inputFont, true), gbc);
        gbc.gridy++;


        panel.add(createFieldPanel("Password", labelFont, inputFont, false), gbc);
        gbc.gridy++;



        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(new Color(0,0,0,0));

        loginButton = createButton("Prijava", new Color(76, 175, 80));
        registerButton = createButton("Registracija", new Color(255, 193, 7));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(buttonPanel, gbc);



        loginButton.addActionListener(e -> loginAction());
        registerButton.addActionListener(e -> {
            dispose();
            new RegisterUser().setVisible(true);
        });
    }

    private JPanel createFieldPanel(String labelText, Font labelFont, Font inputFont, boolean isEmail) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setBackground(new Color(0,0,0,0));

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field;
        if (isEmail) {
            inputEmail = new JTextField();
            field = inputEmail;
        } else {
            inputPassword = new JPasswordField();
            field = inputPassword;
        }
        field.setFont(inputFont);
        field.setMaximumSize(new Dimension(450, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));


        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(5));
        fieldPanel.add(field);

        return fieldPanel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 45));
        return button;
    }

    private void loginAction() {
        String email = inputEmail.getText();
        String password = new String(inputPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unesite email i šifru!");
            return;
        }

        if (userService.loginUser(email, password)) {
            SessionManager.login(email);

            dispose();
            JFrame frame = new JFrame("Life Management System");
            frame.setSize(650, 720);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new MainPanel(email));
            frame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Pogrešan email ili šifra!");
        }
    }


}
