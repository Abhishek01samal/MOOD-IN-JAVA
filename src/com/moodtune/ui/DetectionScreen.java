package com.moodtune.ui;

import com.moodtune.model.Recommendation;
import com.moodtune.service.RecommendationService;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * DetectionScreen
 * ───────────────
 * Step 3 of 3 — shows the detected mood banner and a grid of recommendation cards.
 */
public class DetectionScreen extends Panel {

    private final MoodTuneApp           app;
    private final RecommendationService svc = new RecommendationService();

    // Scrollable recommendation area
    private final Panel scrollArea;      // inner scrollable panel
    private final ScrollPane scrollPane;

    // Current state
    private String currentMood = "";
    private List<Recommendation> recs;

    public DetectionScreen(MoodTuneApp app) {
        this.app = app;
        setLayout(new BorderLayout(0, 0));
        setBackground(Styles.BACKGROUND);

        // ── Header bar (top) ─────────────────────────────────────────────────
        Panel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── Scrollable card grid ─────────────────────────────────────────────
        scrollArea = new Panel(null);   // manual layout inside
        scrollArea.setBackground(Styles.BACKGROUND);

        scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scrollPane.add(scrollArea);
        scrollPane.setBackground(Styles.BACKGROUND);
        add(scrollPane, BorderLayout.CENTER);

        // ── Footer ───────────────────────────────────────────────────────────
        Panel footer = buildFooter();
        add(footer, BorderLayout.SOUTH);

        // Reload whenever screen becomes visible
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { reloadData(); }
        });
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private Panel buildHeader() {
        Panel h = new Panel(null) {
            @Override public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();

                // Background
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 30), W, 0, new Color(25, 20, 40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, W, H);

                // Bottom separator line
                g2.setColor(Styles.BORDER);
                g2.drawLine(0, H - 1, W, H - 1);

                // Logo text
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.setColor(Styles.PRIMARY_LIGHT);
                g2.drawString("🎵  MoodTune", 24, H / 2 + 6);

                // Greeting
                g2.setFont(Styles.SMALL_FONT);
                g2.setColor(Styles.TEXT_MUTED);
                String greet = "Hi, " + app.getUserName() + "  |  Detected mood:";
                g2.drawString(greet, W / 2 - g2.getFontMetrics().stringWidth(greet) / 2, H / 2 - 5);

                // Mood pill
                String mood  = currentMood.isEmpty() ? "—" : currentMood;
                String label = Styles.moodEmoji(mood) + "  " + capitalize(mood);
                Color mc = Styles.moodColor(mood);

                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int pw = fm.stringWidth(label) + 22, ph = 26;
                int px = W / 2 - pw / 2, py = H / 2 + 4;
                g2.setColor(new Color(mc.getRed(), mc.getGreen(), mc.getBlue(), 50));
                g2.fillRoundRect(px, py, pw, ph, 14, 14);
                g2.setColor(mc);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(px, py, pw, ph, 14, 14);
                g2.drawString(label, px + 11, py + 17);

                super.paint(g);
            }
        };
        h.setPreferredSize(new Dimension(0, 90));
        h.setBackground(new Color(20, 20, 30));

        // Back button
        Button back = new Button("← Back");
        back.setFont(Styles.SMALL_FONT);
        back.setForeground(Styles.TEXT_MUTED);
        back.setBackground(new Color(30, 30, 42));
        back.addActionListener(e -> app.showScreen("moodselect"));

        // Retry button
        Button retry = new Button("↻ Retry");
        retry.setFont(Styles.SMALL_FONT);
        retry.setForeground(Styles.TEXT_MUTED);
        retry.setBackground(new Color(30, 30, 42));
        retry.addActionListener(e -> reloadData());

        // Layout buttons on resize
        h.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int W = h.getWidth(), H = h.getHeight();
                back.setBounds(W - 200, H / 2 - 14, 80, 28);
                retry.setBounds(W - 110, H / 2 - 14, 80, 28);
            }
        });

        h.add(back);
        h.add(retry);
        return h;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private Panel buildFooter() {
        Panel f = new Panel(null) {
            @Override public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(18, 18, 28));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Styles.BORDER);
                g2.drawLine(0, 0, getWidth(), 0);
                g2.setFont(Styles.SMALL_FONT);
                g2.setColor(Styles.TEXT_DIM);
                String info = recs == null ? "" :
                        "Showing " + recs.size() + " recommendation" + (recs.size() != 1 ? "s" : "");
                g2.drawString(info, 24, 22);
                String cr = "MoodTune  •  By Abhishek Samal";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(cr, getWidth() - fm.stringWidth(cr) - 24, 22);
                super.paint(g);
            }
        };
        f.setPreferredSize(new Dimension(0, 36));
        return f;
    }

    // ── Data reload ───────────────────────────────────────────────────────────

    private void reloadData() {
        currentMood = app.getDetectedMood();
        recs = svc.getRecommendations(currentMood,
                app.getSelectedCategories(), app.getRecommendationCount());
        buildCards();
        repaint();
    }

    // ── Card grid ─────────────────────────────────────────────────────────────

    private void buildCards() {
        scrollArea.removeAll();
        if (recs == null || recs.isEmpty()) {
            buildEmptyState();
            return;
        }

        int cols   = 3;
        int cardW  = 260;
        int cardH  = 170;
        int gapX   = 18;
        int gapY   = 18;
        int padX   = 28;
        int padTop = 24;

        // Section heading
        Label heading = new Label("  Recommendations for you:");
        heading.setFont(Styles.SUBHEAD_FONT);
        heading.setForeground(Styles.TEXT_MAIN);
        heading.setBackground(Styles.BACKGROUND);

        // Use AWT Panel trick: absolute-positioned labels
        int rows = (int) Math.ceil((double) recs.size() / cols);
        int totalW = cols * cardW + (cols - 1) * gapX + padX * 2;
        int totalH = padTop + 30 + rows * (cardH + gapY) + 30;

        scrollArea.setPreferredSize(new Dimension(Math.max(totalW, getWidth()), totalH));
        scrollArea.setSize(Math.max(totalW, getWidth()), totalH);

        // Heading label
        heading.setBounds(padX, padTop, 400, 24);
        scrollArea.add(heading);

        // Cards
        for (int i = 0; i < recs.size(); i++) {
            Recommendation rec = recs.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx  = padX + col * (cardW + gapX);
            int cy  = padTop + 38 + row * (cardH + gapY);

            RecommendationCard card = new RecommendationCard(rec, cardW, cardH);
            card.setBounds(cx, cy, cardW, cardH);
            scrollArea.add(card);
        }

        scrollArea.validate();
        scrollArea.repaint();
        scrollPane.validate();
    }

    private void buildEmptyState() {
        scrollArea.setPreferredSize(new Dimension(getWidth(), 300));
        Label lbl = new Label("No recommendations found for this mood/category combo.", Label.CENTER);
        lbl.setFont(Styles.BODY_FONT);
        lbl.setForeground(Styles.TEXT_MUTED);
        lbl.setBackground(Styles.BACKGROUND);
        lbl.setBounds(0, 120, getWidth(), 30);
        scrollArea.add(lbl);
        scrollArea.validate();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Inner: RecommendationCard ─────────────────────────────────────────────

    private class RecommendationCard extends Panel {
        private final Recommendation rec;
        private boolean hovered = false;

        RecommendationCard(Recommendation rec, int w, int h) {
            this.rec = rec;
            setPreferredSize(new Dimension(w, h));
            setBackground(Styles.CARD_BG);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                public void mouseClicked(MouseEvent e) {
                    if (Desktop.isDesktopSupported() && !rec.getLink().isEmpty()) {
                        try {
                            Desktop.getDesktop().browse(new java.net.URI(rec.getLink()));
                        } catch (Exception ex) {
                            System.err.println("Cannot open link: " + ex.getMessage());
                        }
                    }
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int W = getWidth(), H = getHeight();

            Color bg     = hovered ? Styles.CARD_HOVER : Styles.CARD_BG;
            Color border = hovered ? Styles.PRIMARY    : Styles.BORDER;
            Styles.drawCard(g2, 0, 0, W, H, 14, bg, border);

            // Category icon bar (top)
            Color mc = Styles.moodColor(app.getDetectedMood());
            g2.setColor(new Color(mc.getRed(), mc.getGreen(), mc.getBlue(), 40));
            g2.fillRoundRect(0, 0, W, 38, 14, 14);
            g2.fillRect(0, 18, W, 20);

            String icon = Styles.categoryIcon(rec.getCategory());
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2.setColor(mc);
            g2.drawString(icon, 14, 27);

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(new Color(mc.getRed(), mc.getGreen(), mc.getBlue(), 200));
            g2.drawString(rec.getCategory().toUpperCase(), 42, 26);

            // Title (wrapped, max 2 lines)
            g2.setFont(Styles.SUBHEAD_FONT);
            g2.setColor(Styles.TEXT_MAIN);
            drawWrapped(g2, rec.getTitle(), 14, 58, W - 28, 2);

            // Platform
            g2.setFont(Styles.SMALL_FONT);
            g2.setColor(Styles.TEXT_MUTED);
            g2.drawString(rec.getPlatform(), 14, 115);

            // Rating bar
            int ratingBarW = W - 28;
            g2.setColor(Styles.BORDER);
            g2.fillRoundRect(14, 128, ratingBarW, 6, 4, 4);
            int fill = (int) (ratingBarW * rec.getRating() / 10.0);
            g2.setColor(mc);
            g2.fillRoundRect(14, 128, fill, 6, 4, 4);

            // Rating number
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(mc);
            String rStr = String.format("%.1f ★", rec.getRating());
            g2.drawString(rStr, 14, 152);

            // "Open" hint
            if (hovered && !rec.getLink().isEmpty()) {
                g2.setFont(Styles.SMALL_FONT);
                g2.setColor(Styles.TEXT_MUTED);
                String hint = "Click to open ↗";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, W - fm.stringWidth(hint) - 14, 152);
            }
        }

        /** Draw text wrapped to {@code maxLines} lines inside width {@code maxW}. */
        private void drawWrapped(Graphics2D g2, String text, int x, int y, int maxW, int maxLines) {
            FontMetrics fm = g2.getFontMetrics();
            int lineH = fm.getHeight();
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            int linesDrawn = 0;
            for (String w : words) {
                String test = line.length() == 0 ? w : line + " " + w;
                if (fm.stringWidth(test) > maxW && line.length() > 0) {
                    if (linesDrawn == maxLines - 1) {
                        // Last allowed line — truncate with ellipsis
                        while (fm.stringWidth(line + "…") > maxW && line.length() > 0)
                            line.deleteCharAt(line.length() - 1);
                        g2.drawString(line + "…", x, y + linesDrawn * lineH);
                        return;
                    }
                    g2.drawString(line.toString(), x, y + linesDrawn * lineH);
                    linesDrawn++;
                    line = new StringBuilder(w);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (line.length() > 0 && linesDrawn < maxLines)
                g2.drawString(line.toString(), x, y + linesDrawn * lineH);
        }
    }
}
