package ibank.exception;

/** Thrown when the new PIN and its confirmation do not match. */
public class PinMismatchException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error. */
  public PinMismatchException() {
    super("error.pin.mismatch");
  }
}
