package utils;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_KEY = "app_theme_mode";
    private static final String DARK_MODE = "dark";
    private static final String LIGHT_MODE = "light";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    public static void initializeTheme() {
        String theme = prefs.get(THEME_KEY, LIGHT_MODE);
        applyTheme(theme);
    }

    public static void toggleTheme(JFrame frame) {
        String currentTheme = prefs.get(THEME_KEY, LIGHT_MODE);
        String newTheme = currentTheme.equals(DARK_MODE) ? LIGHT_MODE : DARK_MODE;
        
        prefs.put(THEME_KEY, newTheme);
        applyTheme(newTheme);
        
        // Update UI recursively for all components in the frame
        SwingUtilities.updateComponentTreeUI(frame);
        
        // Make sure it repaints nicely
        frame.revalidate();
        frame.repaint();
    }

    public static boolean isDarkMode() {
        return prefs.get(THEME_KEY, LIGHT_MODE).equals(DARK_MODE);
    }

    private static void applyTheme(String theme) {
        try {
            if (theme.equals(DARK_MODE)) {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
                setCustomDarkColors();
            } else {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                setCustomLightColors();
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme: " + e.getMessage());
        }
    }

    private static void setCustomLightColors() {
        // We can define custom UI overrides if necessary, FlatLaf handles most of it beautifully
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
    }

    private static void setCustomDarkColors() {
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
    }
}
