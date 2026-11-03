package ibank.gui;

import ibank.device.CashDispenser;
import ibank.device.ReceiptPrinter;
import ibank.exception.AtmException;
import ibank.exception.SessionExpiredException;
import ibank.i18n.Messages;
import ibank.model.Money;
import ibank.service.AtmSession;
import ibank.service.AuthenticationService;
import ibank.service.TransactionService;
import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * The ABM window. It shows one screen at a time, holds the current session and
 * language, and handles every error in one place ({@link #attempt(AtmAction)}).
 */
public class AtmFrame extends JFrame {

  private static final long serialVersionUID = 1L;
  private static final int CHECK_EVERY_MS = 1000;

  private final transient AuthenticationService auth;
  private final transient TransactionService transactions;
  private final transient CashDispenser dispenser;
  private final transient ReceiptPrinter printer = new ReceiptPrinter();
  private transient Messages text = new Messages(Messages.ENGLISH);
  private transient AtmSession session;
  private final transient Runnable saveState;

  /**
   * Creates the ABM window.
   *
   * @param saveState called after every cardholder action, so balances, notes, card
   *     locks and PIN changes survive a restart
   */
  public AtmFrame(AuthenticationService auth, TransactionService transactions,
      CashDispenser dispenser, Runnable saveState) {
    this.auth = auth;
    this.transactions = transactions;
    this.dispenser = dispenser;
    this.saveState = saveState;
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(760, 560));
    setLocationRelativeTo(null);
    watchForInactivity();
    showScreen(new WelcomeScreen(this));
  }

  // ------------------------------------------------------------ navigation

  /** Replaces the current screen. */
  public void showScreen(BaseScreen screen) {
    setTitle(text.get("app.title"));
    setContentPane(screen);
    revalidate();
    repaint();
    screen.focusFirstField();
  }

  /** Shows the main menu. */
  public void showMenu() {
    showScreen(new MenuScreen(this));
  }

  /** Starts a session after a correct PIN and opens the main menu. */
  public void startSession(AtmSession newSession) {
    session = newSession;
    showMenu();
  }

  /** Ends the session, shows a message, and returns to the language screen. */
  public void endSession(String messageKey) {
    if (session != null) {
      session.end();
      session = null;
    }
    showInfo(text.get(messageKey));
    showScreen(new WelcomeScreen(this));
  }

  // ------------------------------------------------------------ errors

  /**
   * Runs a cardholder action and turns any failure into a clear message in the chosen
   * language. An expired session also returns the ABM to the start.
   */
  public void attempt(AtmAction action) {
    try {
      action.run();
    } catch (SessionExpiredException e) {
      session = null;
      showError(text.describe(e));
      showScreen(new WelcomeScreen(this));
    } catch (AtmException e) {
      showError(text.describe(e));
    } catch (NumberFormatException e) {
      showError(text.get("error.amount.format"));
    } finally {
      // Saved even after a failure: a wrong PIN changes the card's attempt count.
      saveState.run();
    }
  }

  /** Shows an error message in a dialog. */
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, text.get("dialog.error"),
        JOptionPane.ERROR_MESSAGE);
  }

  /** Shows an information message in a dialog. */
  public void showInfo(String message) {
    JOptionPane.showMessageDialog(this, message, text.get("dialog.info"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  /** Asks a yes/no question with buttons in the chosen language. */
  public boolean confirm(String question) {
    Object[] options = {text.get("button.yes"), text.get("button.no")};
    int answer = JOptionPane.showOptionDialog(this, question, text.get("dialog.info"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    return answer == 0;
  }

  /**
   * Reads an amount typed by the cardholder, such as "120" or "120.50".
   *
   * @throws NumberFormatException if the text is not a plain amount
   */
  public Money parseAmount(String typed) {
    String cleaned = typed.trim().replace(',', '.').replace("$", "").trim();
    if (!cleaned.matches("\\d{1,7}(\\.\\d{1,2})?")) {
      throw new NumberFormatException(typed);
    }
    return Money.of(cleaned, Money.CAD);
  }

  // ------------------------------------------------------------ inactivity

  /**
   * Any key press or mouse click counts as activity. Once a second, a timer checks
   * whether the session has been idle too long and, if so, returns to the start.
   */
  private void watchForInactivity() {
    Toolkit.getDefaultToolkit().addAWTEventListener(event -> recordActivity(),
        AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    new Timer(CHECK_EVERY_MS, event -> {
      if (session != null && session.isEnded()) {
        endSession("error.session.expired");
      }
    }).start();
  }

  private void recordActivity() {
    if (session != null && !session.isEnded()) {
      try {
        session.ensureActive();
      } catch (SessionExpiredException e) {
        // The timer handles expiry; nothing to do here.
      }
    }
  }

  // ------------------------------------------------------------ access for screens

  /** The text in the cardholder's chosen language. */
  public Messages text() {
    return text;
  }

  public void setLanguage(Locale locale) {
    text = new Messages(locale);
  }

  /** The current session, or null when nobody is logged in. */
  public AtmSession session() {
    return session;
  }

  /** The service for the Authenticate and Change PIN use cases. */
  public AuthenticationService auth() {
    return auth;
  }

  /** The service for the money use cases. */
  public TransactionService transactions() {
    return transactions;
  }

  /** The simulated cash dispenser. */
  public CashDispenser dispenser() {
    return dispenser;
  }

  /** The simulated receipt printer. */
  public ReceiptPrinter printer() {
    return printer;
  }
}
