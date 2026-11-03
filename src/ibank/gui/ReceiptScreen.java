package ibank.gui;

import ibank.device.NoteBundle;
import ibank.model.Transaction;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Print Receipt use case: the simulated printer's receipt, shown on screen. */
public class ReceiptScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;

  /**
   * Shows the receipt.
   *
   * @param notes the notes paid out, or {@code null} for a deposit or transfer
   */
  public ReceiptScreen(AtmFrame frame, Transaction transaction, NoteBundle notes) {
    super(frame, "receipt.screenTitle");
    JTextArea paper = new JTextArea(frame.printer().print(
        transaction, frame.session().getCardholder().getCard(), notes, frame.text()));
    paper.setEditable(false);
    paper.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    addLine(new JScrollPane(paper));
    JButton done = addButton("button.done", frame::showMenu);
    setEnterButton(done);
  }

  /** After a deposit or transfer: asks whether the cardholder wants a receipt. */
  static void offer(AtmFrame frame, Transaction transaction) {
    if (frame.confirm(frame.text().get("receipt.offer"))) {
      frame.showScreen(new ReceiptScreen(frame, transaction, null));
    } else {
      frame.showInfo(frame.text().get("transaction.done"));
      frame.showMenu();
    }
  }
}
