package ibank.gui;

import ibank.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * An on-screen number pad, as on a real ABM. Each key types into whichever field has
 * the cursor (card number, PIN or amount). The keys never take the cursor themselves,
 * so the cardholder can move between fields by clicking them.
 */
public class KeypadPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Dimension KEY_SIZE = new Dimension(78, 60);

  /**
   * Creates the keypad.
   *
   * @param withDecimalPoint true on amount screens, where "." is needed for cents
   */
  public KeypadPanel(Messages text, boolean withDecimalPoint) {
    super(new BorderLayout(0, 8));
    setOpaque(false);
    JPanel grid = new JPanel(new GridLayout(4, 3, 8, 8));
    grid.setOpaque(false);
    for (String digit : new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9"}) {
      grid.add(key(digit, false, () -> type(digit)));
    }
    grid.add(withDecimalPoint ? key(".", false, () -> type(".")) : new JLabel());
    grid.add(key("0", false, () -> type("0")));
    grid.add(new JLabel());
    JPanel corrections = new JPanel(new GridLayout(1, 2, 8, 0)); // Wide keys fit the labels.
    corrections.setOpaque(false);
    corrections.add(key(text.get("keypad.delete"), false, KeypadPanel::deleteLast));
    corrections.add(key(text.get("keypad.clear"), true, KeypadPanel::clearField));
    add(grid, BorderLayout.CENTER);
    add(corrections, BorderLayout.SOUTH);
  }

  private static JButton key(String label, boolean isClear, Runnable action) {
    JButton button = new JButton(label);
    Theme.key(button, isClear);
    if (label.length() > 1) {
      button.setFont(button.getFont().deriveFont(18f)); // Word keys: smaller text.
    }
    button.setPreferredSize(KEY_SIZE);
    button.setFocusable(false); // Keeps the cursor in the field being typed into.
    button.addActionListener(event -> action.run());
    return button;
  }

  /** The editable field that has the cursor, or null if there is none. */
  private static JTextComponent focusedField() {
    Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (owner instanceof JTextComponent && ((JTextComponent) owner).isEditable()) {
      return (JTextComponent) owner;
    }
    return null;
  }

  private static void type(String characters) {
    JTextComponent field = focusedField();
    if (field != null) {
      field.replaceSelection(characters);
    }
  }

  private static void deleteLast() {
    JTextComponent field = focusedField();
    if (field == null) {
      return;
    }
    Document document = field.getDocument();
    if (document.getLength() > 0) {
      try {
        document.remove(document.getLength() - 1, 1);
      } catch (BadLocationException e) {
        // Cannot happen: the position is always inside the text.
      }
    }
  }

  private static void clearField() {
    JTextComponent field = focusedField();
    if (field != null) {
      field.setText("");
    }
  }
}
