package ibank.device;

import ibank.i18n.Messages;
import ibank.model.Account;
import ibank.model.Card;
import ibank.model.Transaction;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

/**
 * The simulated receipt printer (a secondary actor in the D1 use case model). It
 * produces the receipt as text in the cardholder's language; the GUI shows it on
 * screen instead of printing on paper.
 *
 * <p>Only masked card and account numbers ever appear on a receipt.
 */
public class ReceiptPrinter {

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Builds the receipt text.
   *
   * @param notes the notes paid out, or {@code null} if the transaction was not a
   *     withdrawal
   */
  public String print(Transaction transaction, Card card, NoteBundle notes, Messages text) {
    StringJoiner receipt = new StringJoiner(System.lineSeparator());
    receipt.add(text.get("receipt.title"));
    receipt.add(transaction.getTime().format(TIME_FORMAT));
    receipt.add(text.get("receipt.card", card.getMaskedNumber()));
    receipt.add(text.get("receipt.type", text.get(transaction.getType().getMessageKey())));
    receipt.add(text.get("receipt.amount", transaction.getAmount()));
    addAccountLine(receipt, "receipt.from", transaction.getSource(), text);
    addAccountLine(receipt, "receipt.to", transaction.getTarget(), text);
    if (notes != null) {
      receipt.add(text.get("receipt.notes", notes));
    }
    addBalanceLine(receipt, transaction.getSource(), text);
    addBalanceLine(receipt, transaction.getTarget(), text);
    receipt.add(text.get("receipt.thanks"));
    return receipt.toString();
  }

  private void addAccountLine(StringJoiner receipt, String key, Account account, Messages text) {
    if (account != null) {
      receipt.add(text.get(key, text.get(account.getType().getMessageKey()),
          account.getMaskedNumber()));
    }
  }

  private void addBalanceLine(StringJoiner receipt, Account account, Messages text) {
    if (account != null) {
      receipt.add(text.get("receipt.balance", account.getMaskedNumber(), account.getBalance()));
    }
  }
}
