package ibank.device;

import ibank.model.Account;
import ibank.model.AccountType;
import ibank.model.Card;
import ibank.model.Cardholder;
import ibank.model.Money;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the simulated cardholders from a CSV file.
 *
 * <p>One line per account: {@code cardNumber,pin,name,accountNumber,accountType,balance}.
 * A cardholder with two accounts has two lines with the same card number. Blank lines
 * and lines starting with {@code #} are ignored. PINs in the file are test data only;
 * they are hashed as soon as they are read.
 */
public final class CardholderCsvLoader {

  private static final int FIELD_COUNT = 6;

  private CardholderCsvLoader() {
    // Utility class: no instances.
  }

  /**
   * Loads all cardholders from the file.
   *
   * @throws IOException if the file cannot be read
   * @throws IllegalArgumentException if a line is malformed; the message names the line
   */
  public static List<Cardholder> load(Path file, Currency currency) throws IOException {
    Map<String, CardholderDraft> drafts = new LinkedHashMap<>();
    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      addLine(drafts, line, i + 1, file, currency);
    }
    List<Cardholder> cardholders = new ArrayList<>();
    for (CardholderDraft draft : drafts.values()) {
      cardholders.add(draft.build());
    }
    return cardholders;
  }

  private static void addLine(
      Map<String, CardholderDraft> drafts, String line, int lineNumber, Path file,
      Currency currency) {
    String[] fields = line.split(",", -1);
    if (fields.length != FIELD_COUNT) {
      throw badLine(file, lineNumber, "expected " + FIELD_COUNT + " fields");
    }
    try {
      String cardNumber = fields[0].trim();
      CardholderDraft draft = drafts.computeIfAbsent(
          cardNumber, number -> new CardholderDraft(number, fields[1].trim(), fields[2].trim()));
      AccountType type = AccountType.valueOf(fields[4].trim());
      Money balance = Money.of(fields[5].trim(), currency);
      draft.accounts.add(new Account(fields[3].trim(), type, balance));
    } catch (IllegalArgumentException e) {
      throw badLine(file, lineNumber, e.getMessage());
    }
  }

  private static IllegalArgumentException badLine(Path file, int lineNumber, String reason) {
    return new IllegalArgumentException(file + " line " + lineNumber + ": " + reason);
  }

  /** Collects the accounts of one cardholder before the Cardholder is created. */
  private static final class CardholderDraft {
    final String cardNumber;
    final String pin;
    final String name;
    final List<Account> accounts = new ArrayList<>();

    CardholderDraft(String cardNumber, String pin, String name) {
      this.cardNumber = cardNumber;
      this.pin = pin;
      this.name = name;
    }

    Cardholder build() {
      return new Cardholder(name, new Card(cardNumber, pin), accounts);
    }
  }
}
