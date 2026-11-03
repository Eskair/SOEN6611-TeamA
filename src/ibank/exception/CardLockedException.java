package ibank.exception;

/** Thrown when the card is locked after too many wrong PINs. */
public class CardLockedException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error. */
  public CardLockedException() {
    super("error.card.locked");
  }
}
