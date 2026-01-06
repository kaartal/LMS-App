package lifemanagmentsystem;

import java.awt.*;

public class SessionManager {

    private static String loggedUserEmail;


    private static String currentTheme = "Zelena";

    public static void login(String email) {
        loggedUserEmail = email;
    }

    public static void logout() {
        loggedUserEmail = null;
    }

    public static String getLoggedUserEmail() {
        return loggedUserEmail;
    }

    public static boolean isLoggedIn() {
        return loggedUserEmail != null;
    }


    public static void setTheme(String theme) {
        currentTheme = theme;
    }


    public static String getTheme() {
        return currentTheme;
    }


    public static Color getThemeColor() {
        return getColorFromTheme(currentTheme);
    }


    public static Color getColorFromTheme(String theme) {
        if (theme == null) {
            return new Color(166, 244, 136);
        }

        switch (theme) {
            case "Plava":
                return new Color(0, 102, 204);
            case "Roza":
                return new Color(204, 0, 102);
            case "Narandzasta":
                return new Color(204, 102, 0);
            case "Tamna/Dark":
                return new Color(30, 34, 42);
            case "Cyberpunk":
                return new Color(20, 20, 30);
            case "Zelena":
            default:
                return new Color(50, 150, 50);
        }
    }
}
