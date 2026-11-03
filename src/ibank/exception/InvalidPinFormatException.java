package ibank.exception;

/** Thrown when a new PIN does not have the required number of digits. */
public class InvalidPinFormatException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error, giving the required number of digits. */
  public InvalidPinFormatException(int requiredDigits) {
    super("error.pin.format", requiredDigits);
  }
}
