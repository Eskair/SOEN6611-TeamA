package ibank.exception;

import ibank.model.Money;

/** Thrown when a withdrawal would go over the daily withdrawal limit. */
public class DailyLimitExceededException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error, showing how much can still be withdrawn today. */
  public DailyLimitExceededException(Money remainingToday) {
    super("error.limit.daily", remainingToday);
  }
}
