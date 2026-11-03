package ibank.device;

import ibank.exception.CardNotRecognizedException;
import ibank.model.Cardholder;
import ibank.model.Transaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An in-memory bank core for the course prototype. Its state is saved to files and
 * restored at the next start by {@code ibank.persistence.StateStore}.
 */
public class SimulatedBankCore implements BankCore {

  private final Map<String, Cardholder> cardholdersByCard = new LinkedHashMap<>();
  private final Map<Cardholder, List<Transaction>> transactions = new HashMap<>();

  /** Creates the bank core holding the given cardholders. */
  public SimulatedBankCore(List<Cardholder> cardholders) {
    for (Cardholder cardholder : cardholders) {
      cardholdersByCard.put(cardholder.getCard().getNumber(), cardholder);
      transactions.put(cardholder, new ArrayList<>());
    }
  }

  /** All cardholders, so their state can be saved. */
  public List<Cardholder> getCardholders() {
    return new ArrayList<>(cardholdersByCard.values());
  }

  @Override
  public Cardholder findCardholder(String cardNumber) throws CardNotRecognizedException {
    Cardholder cardholder = cardholdersByCard.get(cardNumber);
    if (cardholder == null) {
      throw new CardNotRecognizedException();
    }
    return cardholder;
  }

  @Override
  public void recordTransaction(Cardholder cardholder, Transaction transaction) {
    transactions.computeIfAbsent(cardholder, key -> new ArrayList<>()).add(transaction);
  }

  @Override
  public List<Transaction> getTransactions(Cardholder cardholder) {
    return Collections.unmodifiableList(
        transactions.getOrDefault(cardholder, Collections.emptyList()));
  }
}
