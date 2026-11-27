package ibank.gui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

/**
 * The ABM's colours and button styles, kept in one place so every screen looks the
 * same. Colours follow real ABM conventions: a dark header, high-contrast text, and a
 * yellow Clear key.
 */
public final class Theme {

  /** Header bar and main buttons: dark blue. */
  public static final Color NAVY = new Color(0, 51, 102);
  /** Page background behind the white panel. */
  public static final Color PAGE = new Color(236, 240, 246);
  /** Panel and secondary-button background. */
  public static final Color PANEL = Color.WHITE;
  /** Body text: near black for contrast. */
  public static final Color INK = new Color(20, 24, 31);
  /** Thin lines around panels and keys. */
  public static final Color LINE = new Color(196, 204, 216);
  /** The Clear key, yellow as on most ABM keypads. */
  public static final Color CLEAR_KEY = new Color(242, 194, 0);

  private Theme() {
    // Constants and helpers only.
  }

  /** Styles a main action (OK, menu choices): white text on dark blue. */
  public static void primary(JButton button) {
    style(button, NAVY, Color.WHITE, NAVY);
  }

  /** Styles a secondary action (Back, Cancel): dark blue text on white. */
  public static void secondary(JButton button) {
    style(button, PANEL, NAVY, NAVY);
  }

  /** Styles a keypad key: dark text on white, or on yellow for Clear. */
  public static void key(JButton button, boolean isClear) {
    style(button, isClear ? CLEAR_KEY : PANEL, INK, LINE);
    button.setFont(button.getFont().deriveFont(Font.BOLD, 22f));
  }

  private static void style(JButton button, Color fill, Color text, Color edge) {
    button.setBackground(fill);
    button.setForeground(text);
    button.setOpaque(true);
    button.setFocusPainted(false);
    Border outline = BorderFactory.createLineBorder(edge, 2, true);
    button.setBorder(BorderFactory.createCompoundBorder(
        outline, BorderFactory.createEmptyBorder(8, 18, 8, 18)));
  }
}
