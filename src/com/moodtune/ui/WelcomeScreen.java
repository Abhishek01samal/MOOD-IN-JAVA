package com.moodtune.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * WelcomeScreen
 * ─────────────
 * Step 1 of 3: Enter your name & choose content categories.
 */
public class WelcomeScreen extends Panel {

    private final MoodTuneApp app;

    // Form widgets
    private final TextField nameField;
    private final List<Checkbox>  catBoxes  = new ArrayList<>();
    private final List<String>    catNames  = Arrays.asList(
            "music", "movie", "anime", "book", "game", "podcast");
    private final List<String>    catLabels = Arrays.asList(
            "🎵  Music", "🎬  Movies", "⛩️  Anime",
            "📚  Books", "🎮  Games",  "🎙️  Podcasts");

    // State
    private String errorMsg = "";

    public WelcomeScreen(MoodTuneApp app) {
        this.app = app;
        setLayout(null);
        setBackground(Styles.BACKGROUND);

        // ── Name field ────────────────────────────────────────────────────────
        nameField = new TextField(20);
        nameField.setFont(Styles.BODY_FONT);
        nameField.setForeground(Styles.TEXT_MAIN);
        nameField.setBackground(new Color(40, 40, 55));

        // ── Category checkboxes ───────────────────────────────────────────────
        CheckboxGroup dummyGroup = null;   // independent checkboxes (not radio)
        for (int i = 0; i < catNames.size(); i++) {
            Checkbox cb = new Checkbox(catLabels.get(i), true);
            cb.setFont(Styles.BODY_FONT);
            cb.setForeground(Styles.TEXT_MAIN);
            cb.setBackground(Styles.CARD_BG);
            catBoxes.add(cb);
            add(cb);
        }

        // ── Continue button ───────────────────────────────────────────────────
        Button continueBtn = new Button("Continue  →");
        continueBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setBackground(Styles.PRIMARY);
        continueBtn.addActionListener(e -> handleContinue());

        add(nameField);
        add(continueBtn);

        // Position after component is made visible
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { layoutComponents(); }
            @Override public void componentShown(ComponentEvent e)   { layoutComponents(); }
        });
    }

    private void layoutComponents() {
        int W = getWidth(), H = getHeight();
        if (W == 0) return;

        int cx = W / 2;

        // Name field — centred
        int nfW = 320, nfH = 32;
        nameField.setBounds(cx - nfW / 2, 330, nfW, nfH);
        nameField.setText(nameField.getText().isEmpty() ? "" : nameField.getText());

        // Category checkboxes — 3-column grid
        int cols = 3, cbW = 170, cbH = 30, gapX = 20, gapY = 12;
        int gridW = cols * cbW + (cols - 1) * gapX;
        int startX = cx - gridW / 2;
        int startY = 430;
        for (int i = 0; i < catBoxes.size(); i++) {
            int col = i % cols, row = i / cols;
            catBoxes.get(i).setBounds(startX + col * (cbW + gapX),
                    startY + row * (cbH + gapY), cbW, cbH);
        }

        // Continue button
        Component btn = null;
        for (Component c : getComponents()) {
            if (c instanceof Button) { btn = c; break; }
        }
        if (btn != null) btn.setBounds(cx - 100, H - 90, 200, 44);
    }

    private void handleContinue() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorMsg = "Please enter your name to continue.";
            repaint();
            return;
        }

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < catBoxes.size(); i++) {
            if (catBoxes.get(i).getState()) selected.add(catNames.get(i));
        }
        if (selected.isEmpty()) {
            errorMsg = "Please select at least one content category.";
            repaint();
            return;
        }

        errorMsg = "";
        app.setUserName(name);
        app.setSelectedCategories(selected);
        app.showScreen("moodselect");
    }

    // ── Painting ─────────────────────────────────────────────────────────────

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int W = getWidth(), H = getHeight();

        // Background gradient
        Styles.drawBgGradient(g2, W, H);

        // Subtle grid dots
        g2.setColor(new Color(255, 255, 255, 12));
        for (int x = 0; x < W; x += 40)
            for (int y = 0; y < H; y += 40)
                g2.fillOval(x - 1, y - 1, 2, 2);

        // Glow orb top-right
        drawOrb(g2, (int)(W * 0.82), (int)(H * 0.12), 240, Styles.PRIMARY, 35);
        drawOrb(g2, (int)(W * 0.18), (int)(H * 0.85), 180, Styles.PRIMARY_DARK, 28);

        // Logo / icon circle
        int lx = W / 2 - 35, ly = 60, lw = 70, lh = 70;
        drawOrb(g2, W / 2, 95, 90, Styles.PRIMARY, 50);
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        g2.setColor(Color.WHITE);
        drawCentered(g2, "🎵", W / 2, 105);

        // Title
        g2.setFont(Styles.TITLE_FONT);
        g2.setColor(Styles.TEXT_MAIN);
        drawCentered(g2, "MoodTune", W / 2, 175);

        // Subtitle
        g2.setFont(Styles.BODY_FONT);
        g2.setColor(Styles.TEXT_MUTED);
        drawCentered(g2, "Your AI-powered mood-based media companion", W / 2, 205);

        // Card behind form
        Styles.drawCard(g2, W / 2 - 280, 240, 560, 380, 18, Styles.CARD_BG, Styles.BORDER);

        // "What's your name?" label
        g2.setFont(Styles.SUBHEAD_FONT);
        g2.setColor(Styles.TEXT_MAIN);
        drawCentered(g2, "What should we call you?", W / 2, 290);

        g2.setFont(Styles.SMALL_FONT);
        g2.setColor(Styles.TEXT_MUTED);
        drawCentered(g2, "Enter your name below", W / 2, 313);

        // Name field border glow
        int nfX = W / 2 - 160, nfY = 328;
        g2.setColor(Styles.BORDER_GLOW);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(nfX - 3, nfY - 3, 326, 38, 10, 10);

        // Category section label
        g2.setFont(Styles.SUBHEAD_FONT);
        g2.setColor(Styles.TEXT_MAIN);
        drawCentered(g2, "What do you enjoy?", W / 2, 415);

        g2.setFont(Styles.SMALL_FONT);
        g2.setColor(Styles.TEXT_MUTED);
        drawCentered(g2, "Select all that apply", W / 2, 432);

        // Error message
        if (!errorMsg.isEmpty()) {
            g2.setFont(Styles.SMALL_FONT);
            g2.setColor(Styles.DANGER);
            drawCentered(g2, "⚠  " + errorMsg, W / 2, H - 108);
        }

        // Paint children (fields, checkboxes, button) on top
        super.paint(g);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private void drawOrb(Graphics2D g2, int cx, int cy, int r, Color c, int alpha) {
        RadialGradientPaint rp = new RadialGradientPaint(
                new Point(cx, cy), r,
                new float[]{0f, 1f},
                new Color[]{new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha),
                             new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)});
        g2.setPaint(rp);
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}
