package ibank.config;

import ibank.model.Money;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Every business rule of the ABM, kept in one place.
 *
 * <p>Changing a rule (for example the daily limit or the note values) means changing a
 * value here, not changing code elsewhere. Tests create their own configurations.
 */
public final class AtmConfig {

  private final Currency currency;
  private final List<Integer> noteValues;
  private final Money dailyWithdrawalLimit;
  private final int maxPinAttempts;
  private final int pinLength;
  private final Duration sessionTimeout;

  /**
   * Creates a configuration.
   *
   * @param noteValues the note values the dispenser holds, in dollars, for example 20
   */
  public AtmConfig(
      Currency currency,
      List<Integer> noteValues,
      Money dailyWithdrawalLimit,
      int maxPinAttempts,
      int pinLength,
      Duration sessionTimeout) {
    this.currency = Objects.requireNonNull(currency, "currency");
    List<Integer> sorted = new ArrayList<>(noteValues);
    sorted.sort(Collections.reverseOrder());
    this.noteValues = Collections.unmodifiableList(sorted);
    this.dailyWithdrawalLimit = Objects.requireNonNull(dailyWithdrawalLimit, "limit");
    this.maxPinAttempts = maxPinAttempts;
    this.pinLength = pinLength;
    this.sessionTimeout = Objects.requireNonNull(sessionTimeout, "sessionTimeout");
  }

  /**
   * The configuration used by the Montreal Interac ABM.
   *
   * <ul>
   *   <li>Notes: $5, $10, $20, $50 and $100, as offered at RBC branch ATMs.
   *   <li>Daily withdrawal limit: $1,000 (team assumption from D1).
   *   <li>Card locks after 3 wrong PINs (team assumption from D1).
   *   <li>4-digit PIN (team assumption from D1).
   *   <li>Session ends after 60 seconds without activity.
   * </ul>
   */
  public static AtmConfig montrealDefaults() {
    return new AtmConfig(
        Money.CAD, List.of(100, 50, 20, 10, 5), Money.cad(1000), 3, 4, Duration.ofSeconds(60));
  }

  public Currency getCurrency() {
    return currency;
  }

  /** Note values in dollars, largest first. */
  public List<Integer> getNoteValues() {
    return noteValues;
  }

  public Money getDailyWithdrawalLimit() {
    return dailyWithdrawalLimit;
  }

  public int getMaxPinAttempts() {
    return maxPinAttempts;
  }

  public int getPinLength() {
    return pinLength;
  }

  public Duration getSessionTimeout() {
    return sessionTimeout;
  }
}
