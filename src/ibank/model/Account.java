package ibank.model;

import ibank.exception.InsufficientFundsException;
import java.util.Objects;

/** A bank account owned by one cardholder. */
public class Account {

  private static final int VISIBLE_DIGITS = 4;

  private final String number;
  private final AccountType type;
  private Money balance;

  /** Creates an account with its opening balance. */
  public Account(String number, AccountType type, Money openingBalance) {
    this.number = Objects.requireNonNull(number, "number");
    this.type = Objects.requireNonNull(type, "type");
    this.balance = Objects.requireNonNull(openingBalance, "openingBalance");
  }

  public String getNumber() {
    return number;
  }

  public AccountType getType() {
    return type;
  }

  public Money getBalance() {
    return balance;
  }

  /** Sets the balance saved at the end of the last run. Used only when iBank starts. */
  public void restoreBalance(Money savedBalance) {
    balance = Objects.requireNonNull(savedBalance, "savedBalance");
  }

  /** Adds money to this account. */
  public void credit(Money amount) {
    balance = balance.plus(amount);
  }

  /**
   * Takes money out of this account.
   *
   * @throws InsufficientFundsException if the balance is lower than the amount
   */
  public void debit(Money amount) throws InsufficientFundsException {
    if (amount.isGreaterThan(balance)) {
      throw new InsufficientFundsException(balance);
    }
    balance = balance.minus(amount);
  }

  /**
   * Returns the account number with all but the last four digits hidden, for example
   * "******4821". Screens and receipts only ever show this form.
   */
  public String getMaskedNumber() {
    int hidden = Math.max(0, number.length() - VISIBLE_DIGITS);
    return "*".repeat(hidden) + number.substring(hidden);
  }
}
