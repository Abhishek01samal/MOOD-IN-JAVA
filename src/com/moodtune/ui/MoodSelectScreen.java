package com.moodtune.ui;

import com.moodtune.service.CameraService;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

/**
 * MoodSelectScreen
 * ────────────────
 * Sequential flow:
 *   1. Live camera feed + 4-second countdown
 *   2. CLICK — capture the photo (flash effect)
 *   3. SCAN  — freeze the captured photo, show scanning animation over it
 *   4. ANALYZE — call MediaPipe AI on the captured photo
 *   5. RESULT — display detected mood for 2 seconds
 *   6. RECOMMEND — transition to DetectionScreen
 */
public class MoodSelectScreen extends Panel {

    private final MoodTuneApp app;
    private final CameraService cameraService;

    // Mood buttons
    private static final String[] MOODS  = {"happy","sad","angry","surprised","neutral","fearful","disgusted"};
    private static final String[] EMOJIS = {"😄",   "😢", "😠",   "😲",       "😐",     "😨",     "🤢"};
    private static final String[] LABELS = {"Happy","Sad","Angry","Surprised","Neutral","Fearful","Disgusted"};

    private final Button[] moodBtns = new Button[MOODS.length];
    private String hoveredMood = "";

    // Camera area
    private Panel camPanel;
    private int camX, camY, camW, camH;

    // ── Phase enum ──
    private enum Phase { COUNTDOWN, FLASH, FROZEN, SCANNING, ANALYZING, RESULT }
    private Phase phase = Phase.COUNTDOWN;

    private int     countdown       = 4;
    private float   scanProgress    = 0f;
    private String  aiResult        = "";
    private BufferedImage capturedImage = null;   // the frozen photo

    private javax.swing.Timer countdownTimer;
    private javax.swing.Timer scanTimer;

    public MoodSelectScreen(MoodTuneApp app) {
        this.app = app;
        this.cameraService = new CameraService();
        setLayout(null);
        setBackground(Styles.BACKGROUND);

        // ── Mood buttons ──
        for (int i = 0; i < MOODS.length; i++) {
            final String mood = MOODS[i];
            Button btn = new Button(EMOJIS[i] + "  " + LABELS[i]);
            btn.setFont(new Font("SansSerif", Font.BOLD, 13));
            btn.setForeground(Styles.TEXT_MAIN);
            btn.setBackground(Styles.CARD_BG);
            btn.addActionListener(e -> { stopAllTimers(); selectMood(mood); });
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hoveredMood = mood; repaint(); }
                public void mouseExited (MouseEvent e) { hoveredMood = "";   repaint(); }
            });
            moodBtns[i] = btn;
            add(btn);
        }

        // ── Back button ──
        Button backBtn = new Button("← Back");
        backBtn.setFont(Styles.BODY_FONT);
        backBtn.setForeground(Styles.TEXT_MUTED);
        backBtn.setBackground(Styles.CARD_BG);
        backBtn.addActionListener(e -> { stopAllTimers(); app.showScreen("welcome"); });
        backBtn.setName("backBtn");
        add(backBtn);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { layoutComponents(); }
            @Override public void componentShown(ComponentEvent e) {
                layoutComponents();
                buildCameraPanel();
                startWorkflow();
            }
            @Override public void componentHidden(ComponentEvent e) { stopAllTimers(); }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WORKFLOW — strictly sequential
    // ═══════════════════════════════════════════════════════════════════════════

    /** Phase 1: Show live feed + countdown from 4 to 0 */
    private void startWorkflow() {
        phase = Phase.COUNTDOWN;
        countdown = 4;
        scanProgress = 0f;
        aiResult = "";
        capturedImage = null;

        // Show the camera panel (live feed)
        if (camPanel != null) camPanel.setVisible(true);

        stopAllTimers();

        countdownTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;
                repaint();
                if (countdown <= 0) {
                    countdownTimer.stop();
                    doCapture();   // → Phase 2
                }
            }
        });
        countdownTimer.setInitialDelay(1000);
        countdownTimer.start();
    }

    /** Phase 2: Flash + capture the photo, then freeze it */
    private void doCapture() {
        // Capture the image NOW (before any visual delay)
        capturedImage = cameraService.captureImage();

        // Save to disk immediately for AI
        if (capturedImage != null) {
            try {
                ImageIO.write(capturedImage, "jpg", new File("temp_capture.jpg"));
                System.out.println("[Capture] Photo saved to temp_capture.jpg");
            } catch (Exception ex) {
                System.err.println("[Capture] Save error: " + ex.getMessage());
            }
        }

        // Show flash
        phase = Phase.FLASH;
        repaint();

        // After 200ms: hide live feed, show frozen image, start scanning
        new javax.swing.Timer(200, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            phase = Phase.FROZEN;

            // Hide the live camera panel — we'll paint the frozen image ourselves
            if (camPanel != null) camPanel.setVisible(false);
            repaint();

            // Brief pause to show frozen photo, then start scan
            new javax.swing.Timer(500, e2 -> {
                ((javax.swing.Timer) e2.getSource()).stop();
                doScan();   // → Phase 3
            }).start();
        }).start();
    }

    /** Phase 3: Scanning animation over the frozen photo */
    private void doScan() {
        phase = Phase.SCANNING;
        scanProgress = 0f;

        scanTimer = new javax.swing.Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scanProgress += 0.015f;
                repaint();
                if (scanProgress >= 1.0f) {
                    scanProgress = 1.0f;
                    scanTimer.stop();
                    doAnalyze();   // → Phase 4
                }
            }
        });
        scanTimer.start();
    }

    /** Phase 4: Call MediaPipe AI to analyze the captured photo */
    private void doAnalyze() {
        phase = Phase.ANALYZING;
        repaint();

        new Thread(() -> {
            String detected = "neutral";

            if (capturedImage != null) {
                try {
                    File tempFile = new File("temp_capture.jpg");
                    ProcessBuilder pb = new ProcessBuilder("python", "scanner.py", tempFile.getAbsolutePath());
                    pb.redirectErrorStream(true);
                    Process p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    p.waitFor();

                    String out = sb.toString();
                    System.out.println("[AI] Raw output: " + out);

                    if (out.contains("\"mood\": \"")) {
                        int start = out.indexOf("\"mood\": \"") + 9;
                        int end = out.indexOf("\"", start);
                        detected = out.substring(start, end);
                    }
                } catch (Exception e) {
                    System.err.println("[AI] Error: " + e.getMessage());
                    detected = MOODS[new Random().nextInt(MOODS.length)];
                }
            } else {
                // Mock mode fallback
                detected = MOODS[new Random().nextInt(MOODS.length)];
            }

            final String finalMood = detected;
            EventQueue.invokeLater(() -> doResult(finalMood));   // → Phase 5
        }).start();
    }

    /** Phase 5: Show the detected mood for 2 seconds */
    private void doResult(String mood) {
        aiResult = mood;
        phase = Phase.RESULT;
        repaint();

        new javax.swing.Timer(2500, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            selectMood(aiResult);   // → Phase 6: go to recommendations
        }).start();
    }

    // ═══════════════════════════════════════════════════════════════════════════

    private void stopAllTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (scanTimer != null) scanTimer.stop();
    }

    private void selectMood(String mood) {
        stopAllTimers();
        app.setDetectedMood(mood);
        app.showScreen("detection");
    }

    // ── Layout ──

    private void layoutComponents() {
        int W = getWidth(), H = getHeight();
        if (W == 0) return;

        camW = (int)(W * 0.42);
        camH = (int)(camW * 0.75);
        camX = (int)(W * 0.06);
        camY = (H - camH) / 2 + 20;

        if (camPanel != null) {
            camPanel.setBounds(camX, camY, camW, camH);
        }

        int btnW = 230, btnH = 42, gap = 8;
        int totalH = MOODS.length * (btnH + gap) - gap;
        int bx = (int)(W * 0.58);
        int by = (H - totalH) / 2 + 20;
        for (int i = 0; i < moodBtns.length; i++) {
            moodBtns[i].setBounds(bx, by + i * (btnH + gap), btnW, btnH);
        }

        for (Component c : getComponents()) {
            if ("backBtn".equals(c.getName())) {
                c.setBounds(20, 20, 90, 30);
            }
        }
    }

    private void buildCameraPanel() {
        if (camPanel != null) remove(camPanel);
        camPanel = cameraService.getCameraPanel(camW, camH);
        camPanel.setBounds(camX, camY, camW, camH);
        camPanel.setIgnoreRepaint(true); // Stop conflict with manual paint
        add(camPanel);
        validate();
        repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PAINTING — overlays for each phase
    // ═══════════════════════════════════════════════════════════════════════════

    // Double-buffer to prevent flickering
    private Image offscreen;
    private Graphics2D offG;

    @Override
    public void update(Graphics g) {
        paint(g);   // skip default clear → no flicker
    }

    @Override
    public void paint(Graphics g) {
        int W = getWidth(), H = getHeight();
        if (W <= 0 || H <= 0) return;

        // Create or resize offscreen buffer
        if (offscreen == null || offscreen.getWidth(null) != W || offscreen.getHeight(null) != H) {
            offscreen = createImage(W, H);
        }
        Graphics2D g2 = (Graphics2D) offscreen.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Styles.drawBgGradient(g2, W, H);

        // Background orbs
        drawOrb(g2, (int)(W * 0.1), (int)(H * 0.15), 180, Styles.PRIMARY, 30);
        drawOrb(g2, (int)(W * 0.9), (int)(H * 0.8),  160, Styles.PRIMARY_DARK, 25);

        // ── Title ──
        g2.setFont(Styles.HEADING_FONT);
        g2.setColor(Styles.TEXT_MAIN);
        String title;
        switch (phase) {
            case COUNTDOWN: title = "Get Ready...";           break;
            case FLASH:     title = "Captured!";              break;
            case FROZEN:    title = "Photo Captured!";        break;
            case SCANNING:  title = "Scanning Your Face...";  break;
            case ANALYZING: title = "Analyzing Expression...";break;
            case RESULT:    title = "Mood Detected!";         break;
            default:        title = "Analyzing Your Mood..."; break;
        }
        drawCentered(g2, title, W / 2, 52);

        g2.setFont(Styles.BODY_FONT);
        g2.setColor(Styles.TEXT_MUTED);
        String subtitle;
        switch (phase) {
            case COUNTDOWN: subtitle = "Stay still while we prepare to capture"; break;
            case FLASH:
            case FROZEN:    subtitle = "Great! Now analyzing your photo...";     break;
            case SCANNING:  subtitle = "Scanning facial landmarks...";           break;
            case ANALYZING: subtitle = "MediaPipe AI is identifying your mood";  break;
            case RESULT:    subtitle = "Preparing your personalized recommendations"; break;
            default:        subtitle = "";                                        break;
        }
        drawCentered(g2, subtitle, W / 2, 78);

        // ── Camera frame border ──
        Color borderCol;
        switch (phase) {
            case COUNTDOWN: borderCol = Styles.PRIMARY_LIGHT; break;
            case SCANNING:  borderCol = Styles.PRIMARY;       break;
            case RESULT:    borderCol = Styles.moodColor(aiResult); break;
            default:        borderCol = Styles.PRIMARY;       break;
        }
        Styles.drawCard(g2, camX - 4, camY - 4, camW + 8, camH + 8, 14,
                new Color(0,0,0,0), borderCol);

        // ── Draw frozen captured image (when camera panel is hidden) ──
        if (phase != Phase.COUNTDOWN && capturedImage != null && (camPanel == null || !camPanel.isVisible())) {
            g2.drawImage(capturedImage, camX, camY, camW, camH, null);
        }

        // ── Phase-specific overlays ──

        // COUNTDOWN: big number
        if (phase == Phase.COUNTDOWN && countdown > 0) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 72));
            g2.setColor(new Color(255, 255, 255, 180));
            String txt = String.valueOf(countdown);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, camX + camW/2 - fm.stringWidth(txt)/2, camY + camH/2 + 25);

            g2.setFont(Styles.SUBHEAD_FONT);
            g2.setColor(Color.WHITE);
            drawCentered(g2, "Capturing in...", camX + camW/2, camY + camH/2 - 50);
        }

        // FLASH: white overlay
        if (phase == Phase.FLASH) {
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRect(camX, camY, camW, camH);
        }

        // SCANNING: green/purple laser line + progress bar
        if (phase == Phase.SCANNING) {
            int sy = camY + (int)(camH * scanProgress);
            // Glow trail
            GradientPaint gp = new GradientPaint(
                0, sy - 30, new Color(Styles.PRIMARY.getRed(), Styles.PRIMARY.getGreen(), Styles.PRIMARY.getBlue(), 0),
                0, sy, Styles.PRIMARY);
            g2.setPaint(gp);
            g2.fillRect(camX, sy - 30, camW, 30);
            // Bright scan line
            g2.setColor(new Color(255, 255, 255, 220));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(camX, sy, camX + camW, sy);

            // Progress bar at bottom of camera area
            int barY = camY + camH + 10;
            int barW = camW;
            int barH = 6;
            g2.setColor(new Color(40, 40, 60));
            g2.fillRoundRect(camX, barY, barW, barH, 4, 4);
            g2.setColor(Styles.PRIMARY);
            g2.fillRoundRect(camX, barY, (int)(barW * scanProgress), barH, 4, 4);

            // Status text
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(Styles.PRIMARY_LIGHT);
            drawCentered(g2, "SCANNING FACE: " + (int)(scanProgress * 100) + "%", camX + camW/2, barY + 24);
        }

        // ANALYZING: pulsing text
        if (phase == Phase.ANALYZING) {
            // Semi-transparent overlay
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(camX, camY, camW, camH);

            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.setColor(Styles.PRIMARY_LIGHT);
            long dots = (System.currentTimeMillis() / 500) % 4;
            drawCentered(g2, "ANALYZING" + ".".repeat((int) dots), camX + camW/2, camY + camH/2);

            g2.setFont(Styles.SMALL_FONT);
            g2.setColor(Styles.TEXT_MUTED);
            drawCentered(g2, "MediaPipe Face Mesh", camX + camW/2, camY + camH/2 + 25);

            // Keep repainting for animation
            repaint();
        }

        // RESULT: big mood emoji + name
        if (phase == Phase.RESULT && !aiResult.isEmpty()) {
            // Tinted overlay
            Color mc = Styles.moodColor(aiResult);
            g2.setColor(new Color(mc.getRed(), mc.getGreen(), mc.getBlue(), 80));
            g2.fillRect(camX, camY, camW, camH);

            g2.setFont(new Font("SansSerif", Font.BOLD, 36));
            g2.setColor(Color.WHITE);
            String label = Styles.moodEmoji(aiResult) + " " + aiResult.toUpperCase();
            drawCentered(g2, label, camX + camW/2, camY + camH/2 - 5);

            g2.setFont(Styles.BODY_FONT);
            g2.setColor(new Color(255, 255, 255, 180));
            drawCentered(g2, "Loading recommendations...", camX + camW/2, camY + camH/2 + 30);
        }

        // ── Right panel ──
        int bx = (int)(W * 0.54), by = 110;
        Styles.drawCard(g2, bx, by, (int)(W * 0.4), H - 140, 16, Styles.CARD_BG, Styles.BORDER);

        g2.setFont(Styles.SUBHEAD_FONT);
        g2.setColor(Styles.TEXT_MAIN);
        g2.drawString("Manual Selection", bx + 24, by + 45);

        g2.setFont(Styles.SMALL_FONT);
        g2.setColor(Styles.TEXT_MUTED);
        g2.drawString("Or pick a mood manually", bx + 24, by + 65);

        // Hovered mood glow
        if (!hoveredMood.isEmpty()) {
            for (int i = 0; i < MOODS.length; i++) {
                if (MOODS[i].equals(hoveredMood)) {
                    Rectangle r = moodBtns[i].getBounds();
                    g2.setColor(Styles.moodColor(hoveredMood));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4, 10, 10);
                }
            }
        }

        // User greeting
        g2.setFont(Styles.SMALL_FONT);
        g2.setColor(Styles.TEXT_DIM);
        String greet = "Analyzing for " + app.getUserName();
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(greet, W - fm2.stringWidth(greet) - 30, H - 20);

        // Blit offscreen buffer to screen (flicker-free)
        g2.dispose();
        g.drawImage(offscreen, 0, 0, null);

        // Paint child components (buttons, camera panel) on top
        paintComponents(g);
    }

    // ── Helpers ──

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
