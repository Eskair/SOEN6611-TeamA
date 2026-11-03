package ibank.gui;

import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Authenticate use case. The PIN field shows dots, never the digits. After the last
 * wrong PIN the card is locked (Lock Card use case) and the cardholder is told so.
 */
public class LoginScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;

  private final JTextField cardNumber = new JTextField(18);
  private final JPasswordField pin = new JPasswordField(6);

  /** Builds the card number and PIN screen. */
  public LoginScreen(AtmFrame frame) {
    super(frame, "login.title");
    addField("login.card", cardNumber);
    addField("login.pin", pin);
    JButton ok = addButton("button.ok", this::logIn);
    addButton("button.cancel", () -> frame.showScreen(new WelcomeScreen(frame)));
    setEnterButton(ok);
  }

  private void logIn() {
    frame.attempt(() -> {
      try {
        frame.startSession(
            frame.auth().authenticate(cardNumber.getText().trim(), new String(pin.getPassword())));
      } finally {
        pin.setText(""); // Never leave a PIN in the field.
      }
    });
  }
}
