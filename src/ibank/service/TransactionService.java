package ibank.service;

import ibank.config.AtmConfig;
import ibank.device.BankCore;
import ibank.device.CashDispenser;
import ibank.device.NoteBundle;
import ibank.exception.AccountNotFoundException;
import ibank.exception.CashUnavailableException;
import ibank.exception.DailyLimitExceededException;
import ibank.exception.InsufficientFundsException;
import ibank.exception.InvalidAmountException;
import ibank.exception.InvalidNoteMixException;
import ibank.exception.SameAccountTransferException;
import ibank.exception.SessionExpiredException;
import ibank.model.Account;
import ibank.model.Cardholder;
import ibank.model.Money;
import ibank.model.Transaction;
import ibank.model.TransactionType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The money use cases: Check Balance, Withdraw Cash, Deposit Cash and Transfer.
 *
 * <p>Every operation checks all its rules first and changes balances only after every
 * check has passed, so a failed transaction never leaves an account half-updated.
 */
public class TransactionService {

  private final BankCore bankCore;
  private final CashDispenser dispenser;
  private final AtmConfig config;
  private final Clock clock;

  /** Creates the service. */
  public TransactionService(
      BankCore bankCore, CashDispenser dispenser, AtmConfig config, Clock clock) {
    this.bankCore = bankCore;
    this.dispenser = dispenser;
    this.config = config;
    this.clock = clock;
  }

  /** Check Balance use case. */
  public Money balance(AtmSession session, String accountNumber)
      throws SessionExpiredException, AccountNotFoundException {
    session.ensureActive();
    return session.getCardholder().findAccount(accountNumber).getBalance();
  }

  /**
   * Withdraw Cash use case, paid with the fewest notes the machine has.
   *
   * @see #withdraw(AtmSession, String, Money, NoteBundle)
   */
  public WithdrawalResult withdraw(AtmSession session, String accountNumber, Money amount)
      throws SessionExpiredException, AccountNotFoundException, InvalidAmountException,
          CashUnavailableException, InvalidNoteMixException, DailyLimitExceededException,
          InsufficientFundsException {
    session.ensureActive();
    return withdraw(session, accountNumber, amount, dispenser.plan(amount));
  }

  /**
   * Withdraw Cash use case, paid with the notes the cardholder chose. Checks, in order:
   * the notes add up to the amount and the machine has them, the daily limit, and the
   * account balance. Nothing changes unless every check passes.
   */
  public WithdrawalResult withdraw(
      AtmSession session, String accountNumber, Money amount, NoteBundle notes)
      throws SessionExpiredException, AccountNotFoundException, InvalidNoteMixException,
          DailyLimitExceededException, InsufficientFundsException {
    session.ensureActive();
    Cardholder cardholder = session.getCardholder();
    Account account = cardholder.findAccount(accountNumber);
    dispenser.checkMix(notes, amount);
    checkDailyLimit(cardholder, amount);
    account.debit(amount); // Throws before any change if the balance is too low.
    dispenser.dispense(notes);
    Transaction transaction = record(cardholder, TransactionType.WITHDRAWAL, amount, account, null);
    return new WithdrawalResult(transaction, notes);
  }

  /**
   * Suggests the fewest notes for an amount, so the cardholder can start from it and
   * change the mix. Nothing is withdrawn.
   */
  public NoteBundle suggestNotes(AtmSession session, Money amount)
      throws SessionExpiredException, InvalidAmountException, CashUnavailableException,
          DailyLimitExceededException {
    session.ensureActive();
    checkDailyLimit(session.getCardholder(), amount);
    return dispenser.plan(amount);
  }

  /** Deposit Cash use case. The ABM accepts whole notes only. */
  public Transaction deposit(AtmSession session, String accountNumber, Money amount)
      throws SessionExpiredException, AccountNotFoundException, InvalidAmountException {
    session.ensureActive();
    Cardholder cardholder = session.getCardholder();
    Account account = cardholder.findAccount(accountNumber);
    checkDepositable(amount);
    account.credit(amount);
    return record(cardholder, TransactionType.DEPOSIT, amount, null, account);
  }

  /** Transfer use case, between two accounts of the same cardholder. */
  public Transaction transfer(
      AtmSession session, String fromNumber, String toNumber, Money amount)
      throws SessionExpiredException, AccountNotFoundException, InvalidAmountException,
          SameAccountTransferException, InsufficientFundsException {
    session.ensureActive();
    if (!amount.isPositive()) {
      throw InvalidAmountException.notPositive();
    }
    if (fromNumber.equals(toNumber)) {
      throw new SameAccountTransferException();
    }
    Cardholder cardholder = session.getCardholder();
    Account from = cardholder.findAccount(fromNumber);
    Account to = cardholder.findAccount(toNumber);
    from.debit(amount); // Throws before any change if the balance is too low.
    to.credit(amount);
    return record(cardholder, TransactionType.TRANSFER, amount, from, to);
  }

  /** How much more the cardholder may withdraw today. Never negative. */
  public Money remainingDailyLimit(Cardholder cardholder) {
    Money remaining = config.getDailyWithdrawalLimit().minus(withdrawnToday(cardholder));
    return remaining.isPositive() ? remaining : Money.zero(config.getCurrency());
  }

  // ---------------------------------------------------------------- helpers

  private void checkDailyLimit(Cardholder cardholder, Money amount)
      throws DailyLimitExceededException {
    Money remaining = remainingDailyLimit(cardholder);
    if (amount.isGreaterThan(remaining)) {
      throw new DailyLimitExceededException(remaining);
    }
  }

  private Money withdrawnToday(Cardholder cardholder) {
    LocalDate today = LocalDate.now(clock);
    Money total = Money.zero(config.getCurrency());
    for (Transaction t : bankCore.getTransactions(cardholder)) {
      if (t.getType() == TransactionType.WITHDRAWAL && t.getTime().toLocalDate().equals(today)) {
        total = total.plus(t.getAmount());
      }
    }
    return total;
  }

  private void checkDepositable(Money amount) throws InvalidAmountException {
    if (!amount.isPositive()) {
      throw InvalidAmountException.notPositive();
    }
    int smallestNote = config.getNoteValues().get(config.getNoteValues().size() - 1);
    BigDecimal[] division = amount.getAmount().divideAndRemainder(BigDecimal.valueOf(smallestNote));
    if (division[1].signum() != 0) {
      throw InvalidAmountException.notDepositable(amount, smallestNote);
    }
  }

  private Transaction record(
      Cardholder cardholder, TransactionType type, Money amount, Account source, Account target) {
    Transaction transaction =
        new Transaction(type, amount, source, target, LocalDateTime.now(clock));
    bankCore.recordTransaction(cardholder, transaction);
    return transaction;
  }
}
