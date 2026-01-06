import loginUser.LoginUser;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
            LoginUser login = new LoginUser();
        JFrame mainFrame = new JFrame("Life Management System");
        login.setSize(650, 720);
        login.setLocationRelativeTo(null);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login.setVisible(true);

        }
    }




