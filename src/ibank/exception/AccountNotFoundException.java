package ibank.exception;

/** Thrown when an account number does not belong to the cardholder. */
public class AccountNotFoundException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error. */
  public AccountNotFoundException() {
    super("error.account.notFound");
  }
}
