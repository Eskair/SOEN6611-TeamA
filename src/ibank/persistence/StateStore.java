package ibank.persistence;

import ibank.device.CashDispenser;
import ibank.device.SimulatedBankCore;
import ibank.model.Account;
import ibank.model.Card;
import ibank.model.Cardholder;
import ibank.model.Money;
import ibank.model.Transaction;
import ibank.model.TransactionType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Saves the ABM's state to files and restores it at the next start, like a real ABM
 * that keeps its records when it is switched off.
 *
 * <p>Saved: account balances, card state (PIN hash, wrong-PIN count, lock), the
 * transaction history (so the daily limit continues), and the notes left in the
 * machine. The starting data in {@code data/cardholders.csv} is never changed; delete
 * the state folder to start again from it.
 *
 * <p>Each file is written to a temporary file first and then moved into place, so a
 * crash during saving cannot leave a half-written file.
 */
public class StateStore {

  private static final String ACCOUNTS = "accounts.csv";
  private static final String CARDS = "cards.csv";
  private static final String TRANSACTIONS = "transactions.csv";
  private static final String NOTES = "notes.csv";
  private static final String NONE = "-";

  private final Path folder;
  private final Currency currency;

  /** Creates a store that keeps its files in the given folder. */
  public StateStore(Path folder, Currency currency) {
    this.folder = folder;
    this.currency = currency;
  }

  // ------------------------------------------------------------------ save

  /** Writes the current state of every cardholder and of the dispenser. */
  public void save(SimulatedBankCore bankCore, CashDispenser dispenser) throws IOException {
    Files.createDirectories(folder);
    List<String> accounts = new ArrayList<>();
    List<String> cards = new ArrayList<>();
    List<String> transactions = new ArrayList<>();
    for (Cardholder holder : bankCore.getCardholders()) {
      Card card = holder.getCard();
      cards.add(String.join(",", card.getNumber(), card.getPinHash(),
          Integer.toString(card.getFailedAttempts()), Boolean.toString(card.isLocked())));
      for (Account account : holder.getAccounts()) {
        accounts.add(account.getNumber() + "," + account.getBalance().getAmount().toPlainString());
      }
      for (Transaction t : bankCore.getTransactions(holder)) {
        transactions.add(String.join(",", card.getNumber(), t.getType().name(),
            t.getAmount().getAmount().toPlainString(), numberOrNone(t.getSource()),
            numberOrNone(t.getTarget()), t.getTime().toString()));
      }
    }
    List<String> notes = new ArrayList<>();
    for (Map.Entry<Integer, Integer> entry : dispenser.getStock().entrySet()) {
      notes.add(entry.getKey() + "," + entry.getValue());
    }
    write(ACCOUNTS, "# accountNumber,balance", accounts);
    write(CARDS, "# cardNumber,pinHash,failedAttempts,locked", cards);
    write(TRANSACTIONS, "# cardNumber,type,amount,fromAccount,toAccount,time", transactions);
    write(NOTES, "# noteValue,count", notes);
  }

  private static String numberOrNone(Account account) {
    return account == null ? NONE : account.getNumber();
  }

  private void write(String fileName, String header, List<String> lines) throws IOException {
    Path target = folder.resolve(fileName);
    Path temporary = folder.resolve(fileName + ".tmp");
    List<String> content = new ArrayList<>();
    content.add(header);
    content.addAll(lines);
    Files.write(temporary, content, StandardCharsets.UTF_8);
    try {
      Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  // ------------------------------------------------------------------ restore

  /**
   * Applies the saved balances, card states and transactions to freshly loaded
   * cardholders. Does nothing on the very first run, when no state is saved yet.
   *
   * @throws IllegalArgumentException if a saved file is damaged; the message names it
   */
  public void restore(SimulatedBankCore bankCore) throws IOException {
    Map<String, Account> accountsByNumber = new HashMap<>();
    Map<String, Cardholder> holdersByCard = new HashMap<>();
    for (Cardholder holder : bankCore.getCardholders()) {
      holdersByCard.put(holder.getCard().getNumber(), holder);
      for (Account account : holder.getAccounts()) {
        accountsByNumber.put(account.getNumber(), account);
      }
    }
    for (String[] row : read(ACCOUNTS, 2)) {
      Account account = accountsByNumber.get(row[0]);
      if (account != null) {
        account.restoreBalance(Money.of(row[1], currency));
      }
    }
    for (String[] row : read(CARDS, 4)) {
      Cardholder holder = holdersByCard.get(row[0]);
      if (holder != null) {
        holder.getCard().restoreState(row[1], Integer.parseInt(row[2]),
            Boolean.parseBoolean(row[3]));
      }
    }
    for (String[] row : read(TRANSACTIONS, 6)) {
      Cardholder holder = holdersByCard.get(row[0]);
      if (holder != null) {
        bankCore.recordTransaction(holder, new Transaction(TransactionType.valueOf(row[1]),
            Money.of(row[2], currency), accountsByNumber.get(row[3]),
            accountsByNumber.get(row[4]), LocalDateTime.parse(row[5])));
      }
    }
  }

  /** The notes left in the machine at the end of the last run, if any were saved. */
  public Optional<Map<Integer, Integer>> savedNotes() throws IOException {
    List<String[]> rows = read(NOTES, 2);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    Map<Integer, Integer> notes = new TreeMap<>();
    for (String[] row : rows) {
      notes.put(Integer.parseInt(row[0]), Integer.parseInt(row[1]));
    }
    return Optional.of(notes);
  }

  /** Reads a saved file as rows of fields; an absent file gives no rows. */
  private List<String[]> read(String fileName, int fieldCount) throws IOException {
    Path file = folder.resolve(fileName);
    List<String[]> rows = new ArrayList<>();
    if (!Files.exists(file)) {
      return rows;
    }
    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      String[] fields = line.split(",", -1);
      if (fields.length != fieldCount) {
        throw new IllegalArgumentException(file + " line " + (i + 1) + " is damaged");
      }
      rows.add(fields);
    }
    return rows;
  }
}
