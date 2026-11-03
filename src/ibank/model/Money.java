package ibank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * An amount of money in one currency.
 *
 * <p>Money is immutable: every operation returns a new object. Amounts are kept as
 * {@link BigDecimal} with two decimal places, because {@code double} cannot store
 * values such as 0.10 exactly.
 */
public final class Money implements Comparable<Money> {

  /** The currency this ABM is configured for. */
  public static final Currency CAD = Currency.getInstance("CAD");

  private final BigDecimal amount;
  private final Currency currency;

  private Money(BigDecimal amount, Currency currency) {
    this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    this.currency = Objects.requireNonNull(currency, "currency");
  }

  /** Creates an amount in the given currency, for example {@code Money.of("20.00", CAD)}. */
  public static Money of(String amount, Currency currency) {
    return new Money(new BigDecimal(amount), currency);
  }

  /** Creates a whole-dollar amount in Canadian dollars. */
  public static Money cad(long dollars) {
    return new Money(BigDecimal.valueOf(dollars), CAD);
  }

  /** Returns zero in the given currency. */
  public static Money zero(Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Currency getCurrency() {
    return currency;
  }

  /** Returns the sum of this amount and another in the same currency. */
  public Money plus(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  /** Returns this amount minus another in the same currency. */
  public Money minus(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public boolean isPositive() {
    return amount.signum() > 0;
  }

  /** Returns true if this amount is larger than the other. */
  public boolean isGreaterThan(Money other) {
    return compareTo(other) > 0;
  }

  @Override
  public int compareTo(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount);
  }

  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Cannot combine " + currency + " with " + other.currency);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money)) {
      return false;
    }
    Money other = (Money) o;
    return amount.equals(other.amount) && currency.equals(other.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

  /** Returns a plain form such as "CAD 120.00"; the GUI formats it per language. */
  @Override
  public String toString() {
    return currency.getCurrencyCode() + " " + amount.toPlainString();
  }
}
