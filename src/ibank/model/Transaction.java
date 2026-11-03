package ibank.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A completed transaction. Transactions are immutable once created; they are used for
 * the receipt and for checking the daily withdrawal limit.
 */
public final class Transaction {

  private final TransactionType type;
  private final Money amount;
  private final Account source;
  private final Account target;
  private final LocalDateTime time;

  /**
   * Creates a transaction.
   *
   * @param source the account money left; {@code null} for a deposit
   * @param target the account money went to; {@code null} for a withdrawal
   */
  public Transaction(
      TransactionType type, Money amount, Account source, Account target, LocalDateTime time) {
    this.type = Objects.requireNonNull(type, "type");
    this.amount = Objects.requireNonNull(amount, "amount");
    this.source = source;
    this.target = target;
    this.time = Objects.requireNonNull(time, "time");
  }

  public TransactionType getType() {
    return type;
  }

  public Money getAmount() {
    return amount;
  }

  public Account getSource() {
    return source;
  }

  public Account getTarget() {
    return target;
  }

  public LocalDateTime getTime() {
    return time;
  }
}
