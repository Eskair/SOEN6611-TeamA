package ibank.model;

import ibank.security.PinHasher;
import java.util.Objects;

/**
 * A simulated debit card with its PIN and lock state.
 *
 * <p>The card only stores a hash of the PIN. It counts consecutive wrong PINs; the
 * rule for when to lock (three attempts) lives in {@code AuthenticationService}, so
 * the limit can be changed in one place.
 */
public class Card {

  private static final int VISIBLE_DIGITS = 4;

  private final String number;
  private String pinHash;
  private int failedAttempts;
  private boolean locked;

  /** Creates a card from a plain PIN, which is hashed immediately. */
  public Card(String number, String pin) {
    this.number = Objects.requireNonNull(number, "number");
    this.pinHash = PinHasher.hash(pin);
  }

  public String getNumber() {
    return number;
  }

  /** Returns the card number with only the last four digits visible. */
  public String getMaskedNumber() {
    int hidden = Math.max(0, number.length() - VISIBLE_DIGITS);
    return "*".repeat(hidden) + number.substring(hidden);
  }

  /** Returns true if the PIN matches the stored hash. */
  public boolean isPinCorrect(String pin) {
    return PinHasher.matches(pin, pinHash);
  }

  /** Replaces the PIN; only its new hash is kept. */
  public void changePin(String newPin) {
    pinHash = PinHasher.hash(newPin);
  }

  /** The stored salted hash of the PIN (never the PIN itself), for saving the card. */
  public String getPinHash() {
    return pinHash;
  }

  /**
   * Restores the card as it was at the end of the last run: changed PIN, wrong-PIN
   * count and lock. Used only when iBank starts.
   */
  public void restoreState(String savedPinHash, int savedFailedAttempts, boolean savedLocked) {
    this.pinHash = Objects.requireNonNull(savedPinHash, "savedPinHash");
    this.failedAttempts = savedFailedAttempts;
    this.locked = savedLocked;
  }

  /** Records one more wrong PIN and returns the new count. */
  public int recordFailedAttempt() {
    failedAttempts++;
    return failedAttempts;
  }

  /** Called after a correct PIN: the count starts again from zero. */
  public void resetFailedAttempts() {
    failedAttempts = 0;
  }

  public int getFailedAttempts() {
    return failedAttempts;
  }

  /** Locks the card, so no PIN is accepted any more. */
  public void lock() {
    locked = true;
  }

  public boolean isLocked() {
    return locked;
  }
}
