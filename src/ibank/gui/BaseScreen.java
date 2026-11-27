package ibank.gui;

import ibank.i18n.Messages;
import ibank.model.Account;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Layout shared by every screen: the iBank header bar, a title, a white panel with the
 * form (and the on-screen keypad where needed), and large buttons at the bottom.
 * Screens only add their own fields and buttons, so the look and the keyboard
 * behaviour are the same everywhere (internal reuse).
 */
public abstract class BaseScreen extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final int GAP = 12;

  protected final transient AtmFrame frame;
  private final JPanel panel = new JPanel(new BorderLayout(GAP * 2, 0));
  private final JPanel form = new JPanel(new GridBagLayout());
  private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, GAP));
  private int nextRow;
  private JComponent firstField;

  /** Builds the shared layout with a title in the chosen language. */
  protected BaseScreen(AtmFrame frame, String titleKey, Object... titleArgs) {
    super(new BorderLayout());
    this.frame = frame;
    setBackground(Theme.PAGE);
    add(headerBar(), BorderLayout.NORTH);
    add(body(text().get(titleKey, titleArgs)), BorderLayout.CENTER);
  }

  /** The dark bar at the top: "iBank" on the left, the branch on the right. */
  private JPanel headerBar() {
    JPanel bar = new JPanel(new BorderLayout());
    bar.setBackground(Theme.NAVY);
    bar.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
    JLabel brand = new JLabel("iBank");
    brand.setFont(brand.getFont().deriveFont(Font.BOLD, 26f));
    brand.setForeground(Color.WHITE);
    JLabel branch = new JLabel(text().get("header.branch"));
    branch.setForeground(Color.WHITE);
    bar.add(brand, BorderLayout.WEST);
    bar.add(branch, BorderLayout.EAST);
    return bar;
  }

  /** The title, the white panel holding the form, and the button row. */
  private JPanel body(String titleText) {
    JPanel body = new JPanel(new BorderLayout(GAP, GAP));
    body.setOpaque(false);
    body.setBorder(BorderFactory.createEmptyBorder(16, 32, 8, 32));
    JLabel title = new JLabel(titleText, SwingConstants.CENTER);
    title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
    title.setForeground(Theme.NAVY);
    panel.setBackground(Theme.PANEL);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Theme.LINE, 1, true),
        BorderFactory.createEmptyBorder(16, 24, 16, 24)));
    form.setOpaque(false);
    panel.add(form, BorderLayout.CENTER);
    buttons.setOpaque(false);
    body.add(title, BorderLayout.NORTH);
    body.add(panel, BorderLayout.CENTER);
    body.add(buttons, BorderLayout.SOUTH);
    return body;
  }

  /** The text in the cardholder's chosen language. */
  protected Messages text() {
    return frame.text();
  }

  /** Adds a labelled field to the form. The first field added gets the keyboard focus. */
  protected void addField(String labelKey, JComponent field) {
    addFieldText(text().get(labelKey), field);
  }

  /** Same as {@link #addField}, for a label whose text is already built. */
  protected void addFieldText(String label, JComponent field) {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(8, 8, 8, 8);
    c.gridy = nextRow++;
    c.anchor = GridBagConstraints.EAST;
    form.add(new JLabel(label), c);
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    form.add(field, c);
    if (firstField == null) {
      firstField = field;
    }
  }

  /** Adds a line of text to the form that wraps instead of being cut off. */
  protected void addNote(String note) {
    addLine(new JLabel("<html><div style='width:330px'>" + note + "</div></html>"));
  }

  /** Adds a full-width line of text or a component to the form. */
  protected void addLine(Component component) {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(8, 8, 8, 8);
    c.gridy = nextRow++;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    form.add(component, c);
  }

  /** Adds the on-screen number pad to the right of the form. */
  protected void addKeypad(boolean withDecimalPoint) {
    panel.add(new KeypadPanel(text(), withDecimalPoint), BorderLayout.EAST);
  }

  /**
   * Adds a large button at the bottom of the screen. The first button is the main
   * action (dark blue); later ones are secondary (white).
   */
  protected JButton addButton(String key, Runnable onClick) {
    JButton button = new JButton(text().get(key));
    if (buttons.getComponentCount() == 0) {
      Theme.primary(button);
    } else {
      Theme.secondary(button);
    }
    button.setFont(button.getFont().deriveFont(Font.BOLD, 20f));
    button.setPreferredSize(new Dimension(
        Math.max(160, button.getPreferredSize().width + 24), 56)); // Large, easy to press.
    button.addActionListener(event -> onClick.run());
    buttons.add(button);
    return button;
  }

  /** Makes a button respond to the Enter key. */
  protected void setEnterButton(JButton button) {
    addHierarchyListener(event -> {
      if (getRootPane() != null) {
        getRootPane().setDefaultButton(button);
      }
    });
  }

  /** A drop-down of the cardholder's accounts, shown as "Chequing *****1101". */
  protected JComboBox<Account> accountChooser() {
    JComboBox<Account> chooser = new JComboBox<>(
        frame.session().getCardholder().getAccounts().toArray(new Account[0]));
    chooser.setRenderer(new DefaultListCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public Component getListCellRendererComponent(
          JList<?> list, Object value, int index, boolean selected, boolean focused) {
        super.getListCellRendererComponent(list, value, index, selected, focused);
        if (value instanceof Account) {
          Account account = (Account) value;
          setText(text().get(account.getType().getMessageKey()) + " "
              + account.getMaskedNumber());
        }
        return this;
      }
    });
    return chooser;
  }

  /** Puts the cursor in the first field, so the cardholder can type straight away. */
  void focusFirstField() {
    if (firstField != null) {
      firstField.requestFocusInWindow();
    }
  }
}
