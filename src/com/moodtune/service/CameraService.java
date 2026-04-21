package com.moodtune.service;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * CameraService
 * ─────────────
 * Provides either:
 *   • A real webcam panel via the Sarxos Webcam API (if JAR is on classpath).
 *   • A polished "CAMERA SIMULATOR" mock panel with animated overlay.
 *
 * The actual mood detection in this build is done by the user clicking a mood
 * button (MoodSelectScreen). CameraService supplies the visual feed / preview.
 */
public class CameraService {

    private final boolean hasSarxos;

    public CameraService() {
        boolean found = false;
        try {
            Class.forName("com.github.sarxos.webcam.Webcam");
            found = true;
            System.out.println("[Camera] Sarxos Webcam API found ✓");
        } catch (ClassNotFoundException e) {
            System.out.println("[Camera] Sarxos not found — Mock camera active.");
        }
        hasSarxos = found;
    }

    /** Returns a Panel showing either the real webcam or a mock preview. */
    public Panel getCameraPanel(int width, int height) {
        if (hasSarxos) {
            return buildSarxosPanel(width, height);
        }
        return buildMockPanel(width, height);
    }

    // ── Mock camera panel ────────────────────────────────────────────────────

    private Panel buildMockPanel(int w, int h) {
        return new Panel() {
            private int frame = 0;
            private Thread animator;

            {
                setPreferredSize(new Dimension(w, h));
                setBackground(new Color(15, 15, 20));
                animator = new Thread(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        frame++;
                        repaint();
                        try { Thread.sleep(80); } catch (InterruptedException e) { break; }
                    }
                });
                animator.setDaemon(true);
                animator.start();
            }

            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int W = getWidth(), H = getHeight();

                // Background
                g2.setColor(new Color(15, 15, 20));
                g2.fillRect(0, 0, W, H);

                // Scanlines
                g2.setColor(new Color(255, 255, 255, 8));
                for (int y = 0; y < H; y += 4) g2.drawLine(0, y, W, y);

                // Pulsing face silhouette
                double pulse = 0.5 + 0.5 * Math.sin(frame * 0.08);
                int faceW = (int) (180 + 10 * pulse);
                int faceH = (int) (220 + 10 * pulse);
                int fx = (W - faceW) / 2, fy = (H - faceH) / 2 - 20;

                // Face oval glow
                g2.setColor(new Color(147, 112, 219, 30));
                g2.fillOval(fx - 20, fy - 20, faceW + 40, faceH + 40);
                g2.setColor(new Color(147, 112, 219, 60));
                g2.fillOval(fx - 8, fy - 8, faceW + 16, faceH + 16);

                // Face outline
                g2.setColor(new Color(147, 112, 219, 180));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(fx, fy, faceW, faceH);

                // Eyes
                int eyeY = fy + faceH / 3;
                int eyeR = 10;
                g2.setColor(new Color(147, 112, 219, 220));
                g2.fillOval(fx + faceW / 4 - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2);
                g2.fillOval(fx + 3 * faceW / 4 - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2);

                // Scanning beam
                int beamY = fy + (int) ((frame * 3) % (faceH));
                GradientPaint beam = new GradientPaint(
                        0, beamY - 15, new Color(147, 112, 219, 0),
                        0, beamY, new Color(147, 112, 219, 120),
                        false);
                g2.setPaint(beam);
                g2.fillRect(fx, beamY - 15, faceW, 15);

                // Corner brackets
                int bLen = 24;
                g2.setColor(new Color(147, 112, 219));
                g2.setStroke(new BasicStroke(2.5f));
                // TL
                g2.drawLine(10, 10, 10 + bLen, 10);
                g2.drawLine(10, 10, 10, 10 + bLen);
                // TR
                g2.drawLine(W - 10, 10, W - 10 - bLen, 10);
                g2.drawLine(W - 10, 10, W - 10, 10 + bLen);
                // BL
                g2.drawLine(10, H - 10, 10 + bLen, H - 10);
                g2.drawLine(10, H - 10, 10, H - 10 - bLen);
                // BR
                g2.drawLine(W - 10, H - 10, W - 10 - bLen, H - 10);
                g2.drawLine(W - 10, H - 10, W - 10, H - 10 - bLen);

                // Status text
                g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                String status = "ANALYZING" + ".".repeat((frame / 8) % 4);
                g2.setColor(new Color(147, 112, 219));
                g2.drawString(status, W / 2 - 45, H - 20);
                g2.setColor(new Color(80, 80, 80));
                g2.drawString("SIMULATOR MODE", W / 2 - 55, H - 8);
            }
        };
    }

    // ── Real Sarxos panel ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Panel buildSarxosPanel(int w, int h) {
        Panel p = new Panel(new BorderLayout());
        try {
            Class<?> wcClass = Class.forName("com.github.sarxos.webcam.Webcam");
            Class<?> wpClass = Class.forName("com.github.sarxos.webcam.WebcamPanel");

            // List webcams for debugging
            java.util.List<?> webcams = (java.util.List<?>) wcClass.getMethod("getWebcams").invoke(null);
            System.out.println("[Camera] Found " + (webcams == null ? 0 : webcams.size()) + " webcam(s)");
            
            Object webcam = wcClass.getMethod("getDefault").invoke(null);
            if (webcam == null) {
                System.err.println("[Camera] No default webcam found. Using mock.");
                return buildMockPanel(w, h);
            }

            System.out.println("[Camera] Selected: " + webcam.toString());

            // Use the first supported resolution
            try {
                Dimension[] sizes = (Dimension[]) wcClass.getMethod("getViewSizes").invoke(webcam);
                if (sizes != null && sizes.length > 0) {
                    wcClass.getMethod("setViewSize", Class.forName("java.awt.Dimension"))
                            .invoke(webcam, sizes[sizes.length - 1]); // biggest usually
                    System.out.println("[Camera] Resolution set to: " + sizes[sizes.length - 1]);
                }
            } catch (Exception e) {
                System.out.println("[Camera] Could not set resolution: " + e.getMessage());
            }

            Object panel = wpClass.getConstructor(wcClass).newInstance(webcam);
            
            // Explicitly start the panel
            wpClass.getMethod("start").invoke(panel);
            
            // Set panel to start automatically
            wpClass.getMethod("setFPSDisplayed", boolean.class).invoke(panel, true);
            
            p.add((Component) panel, BorderLayout.CENTER);
        } catch (Exception e) {
            System.err.println("[Camera] Sarxos init error: " + e);
            e.printStackTrace();
            p.add(buildMockPanel(w, h), BorderLayout.CENTER);
        }
        return p;
    }

    /**
     * Capture a single frame from webcam and return it as a BufferedImage.
     * Returns null if unavailable or in mock mode.
     */
    public BufferedImage captureImage() {
        if (!hasSarxos) return null;
        try {
            Class<?> wc  = Class.forName("com.github.sarxos.webcam.Webcam");
            Object   cam = wc.getMethod("getDefault").invoke(null);
            wc.getMethod("open").invoke(cam);
            return (BufferedImage) wc.getMethod("getImage").invoke(cam);
        } catch (Exception e) {
            System.err.println("[Camera] Image capture error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Capture a single frame from webcam and return it as a Base64 JPEG string.
     * Returns null if unavailable.
     */
    public String captureFrameAsBase64() {
        BufferedImage img = captureImage();
        if (img == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            System.err.println("[Camera] Base64 encoding error: " + e.getMessage());
            return null;
        }
    }
}
