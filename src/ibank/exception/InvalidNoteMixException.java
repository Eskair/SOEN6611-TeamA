package ibank.exception;

import ibank.model.Money;

/**
 * Thrown when the notes a cardholder chose cannot be paid out. Use the factory
 * methods, which name the reason.
 */
public class InvalidNoteMixException extends AtmException {

  private static final long serialVersionUID = 1L;

  private InvalidNoteMixException(String messageKey, Object... args) {
    super(messageKey, args);
  }

  /** The chosen notes add up to a different amount than the one requested. */
  public static InvalidNoteMixException wrongTotal(Money chosenTotal, Money requested) {
    return new InvalidNoteMixException("error.notes.total", chosenTotal, requested);
  }

  /** The machine does not have that many notes of one value (or does not stock it). */
  public static InvalidNoteMixException notEnough(int noteValue, int available) {
    return new InvalidNoteMixException("error.notes.stock", noteValue, available);
  }
}
