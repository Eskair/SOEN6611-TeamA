package ibank.exception;

import ibank.model.Money;

/** Thrown when an account does not hold enough money for a withdrawal or transfer. */
public class InsufficientFundsException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error, showing the balance that is available. */
  public InsufficientFundsException(Money available) {
    super("error.funds.insufficient", available);
  }
}
