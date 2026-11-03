package ibank.exception;

import ibank.model.Money;

/**
 * Thrown when an amount is not acceptable. Use the factory methods, which name the
 * reason, instead of the constructor.
 */
public class InvalidAmountException extends AtmException {

  private static final long serialVersionUID = 1L;

  private InvalidAmountException(String messageKey, Object... args) {
    super(messageKey, args);
  }

  /** The amount is zero or negative. */
  public static InvalidAmountException notPositive() {
    return new InvalidAmountException("error.amount.notPositive");
  }

  /**
   * The amount cannot be made from the note values the machine uses, for example $30
   * when the machine holds only $20 and $50 notes.
   */
  public static InvalidAmountException notDispensable(Money amount, String noteValues) {
    return new InvalidAmountException("error.amount.notDispensable", amount, noteValues);
  }

  /**
   * A cash deposit is not made of whole bank notes. The ABM accepts notes only, and
   * the smallest Canadian bank note is $5, so deposits must be multiples of $5.
   */
  public static InvalidAmountException notDepositable(Money amount, int smallestNote) {
    return new InvalidAmountException("error.amount.notDepositable", amount, smallestNote);
  }
}
