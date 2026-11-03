package ibank.gui;

import ibank.i18n.Messages;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Select Language use case. This first screen is always bilingual, because the
 * cardholder has not chosen a language yet.
 */
public class WelcomeScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;

  /** Builds the bilingual language screen. */
  public WelcomeScreen(AtmFrame frame) {
    super(frame, "welcome.title");
    addLine(new JLabel(new Messages(Messages.ENGLISH).get("welcome.choose"),
        SwingConstants.CENTER));
    addLine(new JLabel(new Messages(Messages.FRENCH).get("welcome.choose"),
        SwingConstants.CENTER));
    addButton("welcome.english", () -> choose(Messages.ENGLISH));
    addButton("welcome.french", () -> choose(Messages.FRENCH));
  }

  private void choose(Locale locale) {
    frame.setLanguage(locale);
    frame.showScreen(new LoginScreen(frame));
  }
}
