package ibank.exception;

/** Thrown when the PIN entered does not match the card. */
public class WrongPinException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error, saying how many attempts are left. */
  public WrongPinException(int attemptsLeft) {
    super("error.pin.wrong", attemptsLeft);
  }
}
