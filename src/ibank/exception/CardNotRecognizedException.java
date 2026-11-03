package ibank.exception;

/** Thrown when the card number entered does not belong to any cardholder. */
public class CardNotRecognizedException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error. */
  public CardNotRecognizedException() {
    super("error.card.unknown");
  }
}
