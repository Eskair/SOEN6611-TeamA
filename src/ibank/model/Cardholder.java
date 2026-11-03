package ibank.model;

import ibank.exception.AccountNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A bank customer: one card and the accounts that card can reach. */
public class Cardholder {

  private final String name;
  private final Card card;
  private final List<Account> accounts;

  /** Creates a cardholder with one card and the accounts it can reach. */
  public Cardholder(String name, Card card, List<Account> accounts) {
    this.name = Objects.requireNonNull(name, "name");
    this.card = Objects.requireNonNull(card, "card");
    this.accounts = new ArrayList<>(accounts);
  }

  public String getName() {
    return name;
  }

  public Card getCard() {
    return card;
  }

  /** Returns the accounts as a read-only list, so callers cannot add or remove any. */
  public List<Account> getAccounts() {
    return Collections.unmodifiableList(accounts);
  }

  /**
   * Finds one of this cardholder's accounts by number.
   *
   * @throws AccountNotFoundException if the cardholder does not own that account
   */
  public Account findAccount(String accountNumber) throws AccountNotFoundException {
    for (Account account : accounts) {
      if (account.getNumber().equals(accountNumber)) {
        return account;
      }
    }
    throw new AccountNotFoundException();
  }
}
