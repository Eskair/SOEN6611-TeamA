package ibank.exception;

/** Thrown when the source and target of a transfer are the same account. */
public class SameAccountTransferException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error. */
  public SameAccountTransferException() {
    super("error.transfer.sameAccount");
  }
}
