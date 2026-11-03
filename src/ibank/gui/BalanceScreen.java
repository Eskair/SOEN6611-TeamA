package ibank.gui;

import ibank.model.Account;
import ibank.model.Money;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/** Check Balance use case. */
public class BalanceScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;

  private final JComboBox<Account> account = accountChooser();
  private final JLabel result = new JLabel(" ");

  /** Builds the Check Balance screen. */
  public BalanceScreen(AtmFrame frame) {
    super(frame, "balance.title");
    addField("field.account", account);
    result.setFont(result.getFont().deriveFont(22f));
    addLine(result);
    JButton show = addButton("balance.show", this::showBalance);
    addButton("button.back", frame::showMenu);
    setEnterButton(show);
  }

  private void showBalance() {
    frame.attempt(() -> {
      Account chosen = (Account) account.getSelectedItem();
      Money balance = frame.transactions().balance(frame.session(), chosen.getNumber());
      result.setText(text().get("balance.result", chosen.getMaskedNumber(), balance));
    });
  }
}
