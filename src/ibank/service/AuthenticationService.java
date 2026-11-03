package ibank.service;

import ibank.config.AtmConfig;
import ibank.device.BankCore;
import ibank.exception.CardLockedException;
import ibank.exception.CardNotRecognizedException;
import ibank.exception.InvalidPinFormatException;
import ibank.exception.PinMismatchException;
import ibank.exception.SessionExpiredException;
import ibank.exception.WrongPinException;
import ibank.model.Card;
import ibank.model.Cardholder;
import java.time.Clock;

/**
 * The Authenticate, Lock Card and Change PIN use cases.
 *
 * <p>The number of PIN attempts and the PIN length come from {@link AtmConfig}.
 */
public class AuthenticationService {

  private final BankCore bankCore;
  private final AtmConfig config;
  private final Clock clock;

  /** Creates the service. */
  public AuthenticationService(BankCore bankCore, AtmConfig config, Clock clock) {
    this.bankCore = bankCore;
    this.config = config;
    this.clock = clock;
  }

  /**
   * Starts a session if the PIN is correct. After the last allowed wrong PIN the card
   * is locked (Lock Card use case, which extends Authenticate).
   *
   * @throws CardNotRecognizedException if the card number is unknown
   * @throws CardLockedException if the card is, or has just become, locked
   * @throws WrongPinException if the PIN is wrong; the message gives the attempts left
   */
  public AtmSession authenticate(String cardNumber, String pin)
      throws CardNotRecognizedException, CardLockedException, WrongPinException {
    Cardholder cardholder = bankCore.findCardholder(cardNumber);
    Card card = cardholder.getCard();
    if (card.isLocked()) {
      throw new CardLockedException();
    }
    if (card.isPinCorrect(pin)) {
      card.resetFailedAttempts();
      return new AtmSession(cardholder, clock, config.getSessionTimeout());
    }
    int failures = card.recordFailedAttempt();
    int attemptsLeft = config.getMaxPinAttempts() - failures;
    if (attemptsLeft <= 0) {
      card.lock();
      throw new CardLockedException();
    }
    throw new WrongPinException(attemptsLeft);
  }

  /**
   * Changes the PIN of the session's card. The cardholder is already authenticated in
   * this session, so the current PIN is not asked again.
   *
   * @throws InvalidPinFormatException if the new PIN is not the configured number of digits
   * @throws PinMismatchException if the confirmation does not match the new PIN
   */
  public void changePin(AtmSession session, String newPin, String confirmation)
      throws SessionExpiredException, InvalidPinFormatException, PinMismatchException {
    session.ensureActive();
    if (!isValidPin(newPin)) {
      throw new InvalidPinFormatException(config.getPinLength());
    }
    if (!newPin.equals(confirmation)) {
      throw new PinMismatchException();
    }
    session.getCardholder().getCard().changePin(newPin);
  }

  private boolean isValidPin(String pin) {
    return pin != null && pin.length() == config.getPinLength() && pin.chars().allMatch(
        Character::isDigit);
  }
}
