package ibank.gui;

import ibank.device.NoteBundle;
import ibank.exception.AtmException;
import ibank.model.Account;
import ibank.model.Money;
import ibank.model.Transaction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * One screen for both Withdraw Cash and Deposit Cash: choose an account, type an
 * amount, confirm. Only the action differs, so it is passed in (internal reuse).
 */
public class AmountScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;

  /** What happens when the cardholder confirms the amount. */
  @FunctionalInterface
  interface AmountHandler {
    void handle(AtmFrame frame, Account account, Money amount) throws AtmException;
  }

  private final JComboBox<Account> account = accountChooser();
  private final JTextField amount = new JTextField(10);
  private final transient AmountHandler handler;

  private AmountScreen(AtmFrame frame, String titleKey, String hint, AmountHandler handler) {
    super(frame, titleKey);
    this.handler = handler;
    addField("field.account", account);
    addField("field.amount", amount);
    addLine(new JLabel(hint));
    JButton ok = addButton("button.ok", this::confirm);
    addButton("button.back", frame::showMenu);
    setEnterButton(ok);
  }

  /**
   * Withdraw Cash, first step: after the amount, the cardholder chooses the notes on
   * {@link NoteMixScreen}. The receipt is then always shown (the include relationship of the
   * D1 model).
   */
  public static AmountScreen withdraw(AtmFrame frame) {
    String hint = frame.text().get("withdraw.hint", frame.dispenser().describeNoteValues(),
        frame.transactions().remainingDailyLimit(frame.session().getCardholder()));
    return new AmountScreen(frame, "withdraw.title", hint, (f, account, money) -> {
      NoteBundle suggestion = f.transactions().suggestNotes(f.session(), money);
      f.showScreen(new NoteMixScreen(f, account, money, suggestion));
    });
  }

  /** Deposit Cash: the receipt is offered, not forced. */
  public static AmountScreen deposit(AtmFrame frame) {
    return new AmountScreen(frame, "deposit.title", frame.text().get("deposit.hint"),
        (f, account, money) -> {
          Transaction t = f.transactions().deposit(f.session(), account.getNumber(), money);
          ReceiptScreen.offer(f, t);
        });
  }

  private void confirm() {
    frame.attempt(() -> handler.handle(
        frame, (Account) account.getSelectedItem(), frame.parseAmount(amount.getText())));
  }
}
