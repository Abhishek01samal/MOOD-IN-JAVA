package com.moodtune.ui;

import java.awt.*;

/**
 * Styles — global design tokens for MoodTune AWT.
 *
 * Mirrors the original React/Tailwind dark theme:
 *   background  : #1a1a1a
 *   card        : #262626
 *   border      : #404040
 *   accent      : #9370DB  (medium purple)
 *   accent2     : #C084FC  (light purple)
 */
public final class Styles {

    private Styles() {}   // utility class — no instances

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color BACKGROUND   = new Color(18,  18,  22 );
    public static final Color CARD_BG      = new Color(30,  30,  38 );
    public static final Color CARD_HOVER   = new Color(40,  40,  52 );
    public static final Color BORDER       = new Color(60,  60,  80 );
    public static final Color BORDER_GLOW  = new Color(147, 112, 219, 90);

    public static final Color PRIMARY      = new Color(147, 112, 219);   // #9370DB
    public static final Color PRIMARY_LIGHT= new Color(192, 132, 252);   // #C084FC
    public static final Color PRIMARY_DARK = new Color(109,  40, 217);

    public static final Color TEXT_MAIN    = new Color(245, 245, 250);
    public static final Color TEXT_MUTED   = new Color(148, 148, 175);
    public static final Color TEXT_DIM     = new Color(90,  90, 120);

    public static final Color SUCCESS      = new Color( 74, 222, 128);
    public static final Color WARNING      = new Color(250, 204,  21);
    public static final Color DANGER       = new Color(248,  70,  70);

    // Mood colours
    public static final Color MOOD_HAPPY   = new Color(250, 204,  21);
    public static final Color MOOD_SAD     = new Color( 96, 165, 250);
    public static final Color MOOD_ANGRY   = new Color(248,  70,  70);
    public static final Color MOOD_SURP    = new Color(251, 146,  60);
    public static final Color MOOD_NEUTRAL = new Color(148, 163, 184);
    public static final Color MOOD_FEAR    = new Color(167, 139, 250);
    public static final Color MOOD_DISGUST = new Color(110, 231, 183);

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font TITLE_FONT   = new Font("SansSerif", Font.BOLD,  42);
    public static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD,  22);
    public static final Font SUBHEAD_FONT = new Font("SansSerif", Font.BOLD,  16);
    public static final Font BODY_FONT    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font SMALL_FONT   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font MONO_FONT    = new Font("Monospaced",Font.PLAIN, 12);

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Return the theme colour associated with a mood string. */
    public static Color moodColor(String mood) {
        if (mood == null) return PRIMARY;
        switch (mood.toLowerCase()) {
            case "happy":     return MOOD_HAPPY;
            case "sad":       return MOOD_SAD;
            case "angry":     return MOOD_ANGRY;
            case "surprised": return MOOD_SURP;
            case "neutral":   return MOOD_NEUTRAL;
            case "fearful":   return MOOD_FEAR;
            case "disgusted": return MOOD_DISGUST;
            default:          return PRIMARY;
        }
    }

    /** Return the emoji associated with a mood string. */
    public static String moodEmoji(String mood) {
        if (mood == null) return "🎵";
        switch (mood.toLowerCase()) {
            case "happy":     return "😄";
            case "sad":       return "😢";
            case "angry":     return "😠";
            case "surprised": return "😲";
            case "neutral":   return "😐";
            case "fearful":   return "😨";
            case "disgusted": return "🤢";
            default:          return "🎵";
        }
    }

    /** Category icon (text emoji). */
    public static String categoryIcon(String cat) {
        if (cat == null) return "📌";
        switch (cat.toLowerCase()) {
            case "music":   return "🎵";
            case "movie":   return "🎬";
            case "anime":   return "⛩️";
            case "book":    return "📚";
            case "game":    return "🎮";
            case "podcast": return "🎙️";
            default:        return "📌";
        }
    }

    /** Draw a rounded-rectangle filled with {@code fill} and outlined with {@code border}. */
    public static void drawCard(Graphics2D g2, int x, int y, int w, int h,
                                 int arc, Color fill, Color border) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x, y, w, h, arc, arc);
        }
    }

    /** Draw a gradient background vertically. */
    public static void drawBgGradient(Graphics2D g2, int w, int h) {
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(18, 18, 28),
                0, h, new Color(10, 10, 18));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
    }

    /** Convenience: measure string width. */
    public static int strWidth(Graphics g, Font f, String s) {
        return g.getFontMetrics(f).stringWidth(s);
    }
}
