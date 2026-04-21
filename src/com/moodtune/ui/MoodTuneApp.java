package com.moodtune.ui;

import com.moodtune.db.DatabaseConfig;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

/**
 * MoodTuneApp — Main window controller.
 *
 * Manages:
 *  • The AWT Frame (window)
 *  • CardLayout navigation between the 3 screens
 *  • Shared application state (userName, mood, categories, etc.)
 *
 * Screens:
 *  "welcome"    → WelcomeScreen     (name + category prefs)
 *  "moodselect" → MoodSelectScreen  (camera + mood buttons)
 *  "detection"  → DetectionScreen   (recommendation dashboard)
 */
public class MoodTuneApp extends Frame {

    // ── Navigation ────────────────────────────────────────────────────────────
    private final Panel     contentPanel;
    private final CardLayout cardLayout;

    // ── Shared state ──────────────────────────────────────────────────────────
    private String       userName           = "";
    private List<String> selectedCategories = Arrays.asList("music","movie","anime","book","game","podcast");
    private int          recommendationCount = 6;
    private String       detectedMood        = "happy";

    // ── Constructor ───────────────────────────────────────────────────────────

    public MoodTuneApp() {
        // Window chrome
        setTitle("MoodTune  —  AI Mood Detective");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setBackground(Styles.BACKGROUND);
        setLocationRelativeTo(null);

        // Custom icon (graceful fallback if image absent)
        try {
            java.net.URL iconUrl = getClass().getClassLoader().getResource("icon.png");
            if (iconUrl != null) {
                setIconImage(Toolkit.getDefaultToolkit().createImage(iconUrl));
            }
        } catch (Exception ignored) {}

        // Card container
        cardLayout   = new CardLayout();
        contentPanel = new Panel(cardLayout);
        contentPanel.setBackground(Styles.BACKGROUND);

        contentPanel.add(new WelcomeScreen(this),    "welcome");
        contentPanel.add(new MoodSelectScreen(this), "moodselect");
        contentPanel.add(new DetectionScreen(this),  "detection");

        add(contentPanel);

        // Graceful close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConfig.closeConnection();
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
        System.out.println("[MoodTune] Application started.");
    }

    // ── Navigation API ────────────────────────────────────────────────────────

    public void showScreen(String name) {
        cardLayout.show(contentPanel, name);
        contentPanel.validate();
        contentPanel.repaint();
    }

    // ── State getters / setters ───────────────────────────────────────────────

    public String       getUserName()            { return userName; }
    public void         setUserName(String n)    { this.userName = n; }

    public List<String> getSelectedCategories()         { return selectedCategories; }
    public void         setSelectedCategories(List<String> c) { this.selectedCategories = c; }

    public int          getRecommendationCount()       { return recommendationCount; }
    public void         setRecommendationCount(int n)  { this.recommendationCount = n; }

    public String       getDetectedMood()        { return detectedMood; }
    public void         setDetectedMood(String m){ this.detectedMood = m; }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Enable anti-aliased text on all platforms
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Launch on AWT event thread
        EventQueue.invokeLater(MoodTuneApp::new);
    }
}
