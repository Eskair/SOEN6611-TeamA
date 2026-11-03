package ibank.gui;

import javax.swing.JButton;
import javax.swing.JPasswordField;

/** Change PIN use case: the new PIN is typed twice to avoid mistakes. */
public class ChangePinScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;

  private final JPasswordField newPin = new JPasswordField(6);
  private final JPasswordField confirmation = new JPasswordField(6);

  /** Builds the Change PIN screen. */
  public ChangePinScreen(AtmFrame frame) {
    super(frame, "pin.title");
    addField("pin.new", newPin);
    addField("pin.confirm", confirmation);
    JButton ok = addButton("button.ok", this::change);
    addButton("button.back", frame::showMenu);
    setEnterButton(ok);
  }

  private void change() {
    frame.attempt(() -> {
      try {
        frame.auth().changePin(frame.session(),
            new String(newPin.getPassword()), new String(confirmation.getPassword()));
        frame.showInfo(text().get("pin.done"));
        frame.showMenu();
      } finally {
        newPin.setText("");
        confirmation.setText("");
      }
    });
  }
}
