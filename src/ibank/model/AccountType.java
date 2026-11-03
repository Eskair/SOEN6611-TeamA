package ibank.model;

/** The kinds of account a cardholder can hold. */
public enum AccountType {
  CHEQUING("account.chequing"),
  SAVINGS("account.savings");

  private final String messageKey;

  AccountType(String messageKey) {
    this.messageKey = messageKey;
  }

  /** Key used to look up the English or French name of this account type. */
  public String getMessageKey() {
    return messageKey;
  }
}
