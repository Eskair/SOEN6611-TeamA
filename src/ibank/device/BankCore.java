package ibank.device;

import ibank.exception.CardNotRecognizedException;
import ibank.model.Cardholder;
import ibank.model.Transaction;
import java.util.List;

/**
 * The bank's core system (a secondary actor in the D1 use case model): it knows the
 * cardholders and keeps the record of their transactions.
 *
 * <p>iBank only talks to this interface. The course version is simulated in memory
 * ({@link SimulatedBankCore}); a real bank connection could replace it without
 * changing any other class.
 */
public interface BankCore {

  /**
   * Finds the cardholder who owns a card.
   *
   * @throws CardNotRecognizedException if no cardholder has that card number
   */
  Cardholder findCardholder(String cardNumber) throws CardNotRecognizedException;

  /** Stores a completed transaction for a cardholder. */
  void recordTransaction(Cardholder cardholder, Transaction transaction);

  /** Returns all transactions of a cardholder, oldest first. */
  List<Transaction> getTransactions(Cardholder cardholder);
}
