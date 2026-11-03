package ibank.gui;

import ibank.model.Account;
import ibank.model.Transaction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/** Transfer use case: between two of the cardholder's own accounts. */
public class TransferScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;

  private final JComboBox<Account> from = accountChooser();
  private final JComboBox<Account> to = accountChooser();
  private final JTextField amount = new JTextField(10);

  /** Builds the Transfer screen. */
  public TransferScreen(AtmFrame frame) {
    super(frame, "transfer.title");
    if (to.getItemCount() > 1) {
      to.setSelectedIndex(1); // Start with two different accounts selected.
    }
    addField("transfer.from", from);
    addField("transfer.to", to);
    addField("field.amount", amount);
    JButton ok = addButton("button.ok", this::transfer);
    addButton("button.back", frame::showMenu);
    setEnterButton(ok);
  }

  private void transfer() {
    frame.attempt(() -> {
      Transaction t = frame.transactions().transfer(frame.session(),
          ((Account) from.getSelectedItem()).getNumber(),
          ((Account) to.getSelectedItem()).getNumber(),
          frame.parseAmount(amount.getText()));
      ReceiptScreen.offer(frame, t);
    });
  }
}
