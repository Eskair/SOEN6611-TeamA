package ibank.model;

/** The kinds of transaction recorded by iBank. */
public enum TransactionType {
  WITHDRAWAL("transaction.withdrawal"),
  DEPOSIT("transaction.deposit"),
  TRANSFER("transaction.transfer");

  private final String messageKey;

  TransactionType(String messageKey) {
    this.messageKey = messageKey;
  }

  /** Key used to look up the English or French name of this transaction type. */
  public String getMessageKey() {
    return messageKey;
  }
}
