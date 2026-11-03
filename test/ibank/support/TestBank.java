package ibank.support;

import ibank.config.AtmConfig;
import ibank.device.CashDispenser;
import ibank.device.SimulatedBankCore;
import ibank.model.Account;
import ibank.model.AccountType;
import ibank.model.Card;
import ibank.model.Cardholder;
import ibank.model.Money;
import ibank.service.AuthenticationService;
import ibank.service.TransactionService;
import java.util.List;
import java.util.Map;

/**
 * A fresh, fully wired iBank for each test, with known data.
 *
 * <ul>
 *   <li>Card 1111 / PIN 1111: chequing $1,500.00 and savings $3,000.00.
 *   <li>Card 2222 / PIN 2222: chequing $35.00 only (low balance).
 *   <li>The dispenser holds 10 notes of each value ($1,850 in total).
 * </ul>
 */
public final class TestBank {

  public static final String CARD = "4500000000001111";
  public static final String PIN = "1111";
  public static final String CHEQUING = "100111101";
  public static final String SAVINGS = "100112201";
  public static final String LOW_CARD = "4500000000002222";
  public static final String LOW_PIN = "2222";
  public static final String LOW_CHEQUING = "100221102";

  public final AtmConfig config = AtmConfig.montrealDefaults();
  public final MutableClock clock = MutableClock.startOfTestDay();
  public final Cardholder main;
  public final Cardholder lowBalance;
  public final SimulatedBankCore bankCore;
  public final CashDispenser dispenser;
  public final AuthenticationService auth;
  public final TransactionService transactions;

  /** Builds a fresh ABM with the data described above. */
  public TestBank() {
    main = new Cardholder("Amira Haddad", new Card(CARD, PIN), List.of(
        new Account(CHEQUING, AccountType.CHEQUING, Money.of("1500.00", Money.CAD)),
        new Account(SAVINGS, AccountType.SAVINGS, Money.of("3000.00", Money.CAD))));
    lowBalance = new Cardholder("Etienne Gagnon", new Card(LOW_CARD, LOW_PIN), List.of(
        new Account(LOW_CHEQUING, AccountType.CHEQUING, Money.of("35.00", Money.CAD))));
    bankCore = new SimulatedBankCore(List.of(main, lowBalance));
    dispenser = new CashDispenser(config, Map.of(100, 10, 50, 10, 20, 10, 10, 10, 5, 10));
    auth = new AuthenticationService(bankCore, config, clock);
    transactions = new TransactionService(bankCore, dispenser, config, clock);
  }
}
