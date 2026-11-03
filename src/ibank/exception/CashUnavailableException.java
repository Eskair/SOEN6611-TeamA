package ibank.exception;

/** Thrown when the machine does not have the notes needed to pay out an amount. */
public class CashUnavailableException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error. */
  public CashUnavailableException() {
    super("error.cash.unavailable");
  }
}
