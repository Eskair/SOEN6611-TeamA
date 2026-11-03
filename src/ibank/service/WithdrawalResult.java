package ibank.service;

import ibank.device.NoteBundle;
import ibank.model.Transaction;

/** What a successful withdrawal produces: the transaction and the notes paid out. */
public final class WithdrawalResult {

  private final Transaction transaction;
  private final NoteBundle notes;

  /** Creates the result of a withdrawal. */
  public WithdrawalResult(Transaction transaction, NoteBundle notes) {
    this.transaction = transaction;
    this.notes = notes;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public NoteBundle getNotes() {
    return notes;
  }
}
