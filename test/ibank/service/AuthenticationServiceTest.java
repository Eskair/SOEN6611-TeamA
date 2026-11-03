package ibank.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ibank.exception.CardLockedException;
import ibank.exception.CardNotRecognizedException;
import ibank.exception.InvalidPinFormatException;
import ibank.exception.PinMismatchException;
import ibank.exception.WrongPinException;
import ibank.support.TestBank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthenticationServiceTest {

  private final TestBank bank = new TestBank();

  @Test
  @DisplayName("Correct card and PIN start a session")
  void correctPinStartsSession() throws Exception {
    AtmSession session = bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
    assertNotNull(session);
    assertEquals("Amira Haddad", session.getCardholder().getName());
  }

  @Test
  @DisplayName("An unknown card number is rejected")
  void unknownCardRejected() {
    assertThrows(CardNotRecognizedException.class,
        () -> bank.auth.authenticate("9999", TestBank.PIN));
  }

  @Test
  @DisplayName("Wrong PINs report 2 then 1 attempts left, and the 3rd locks the card")
  void lockAfterThreeWrongPins() {
    WrongPinException first = assertThrows(WrongPinException.class,
        () -> bank.auth.authenticate(TestBank.CARD, "0000"));
    assertArrayEquals(new Object[] {2}, first.getMessageArgs());
    WrongPinException second = assertThrows(WrongPinException.class,
        () -> bank.auth.authenticate(TestBank.CARD, "0000"));
    assertArrayEquals(new Object[] {1}, second.getMessageArgs());
    assertThrows(CardLockedException.class,
        () -> bank.auth.authenticate(TestBank.CARD, "0000"));
    assertTrue(bank.main.getCard().isLocked());
  }

  @Test
  @DisplayName("A locked card stays locked even with the correct PIN")
  void lockedCardRejectsCorrectPin() {
    bank.main.getCard().lock();
    assertThrows(CardLockedException.class,
        () -> bank.auth.authenticate(TestBank.CARD, TestBank.PIN));
  }

  @Test
  @DisplayName("A correct PIN after two wrong ones resets the count")
  void correctPinResetsCount() throws Exception {
    assertThrows(WrongPinException.class, () -> bank.auth.authenticate(TestBank.CARD, "0000"));
    assertThrows(WrongPinException.class, () -> bank.auth.authenticate(TestBank.CARD, "0000"));
    bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
    assertEquals(0, bank.main.getCard().getFailedAttempts());
  }

  @Test
  @DisplayName("After a PIN change, only the new PIN works")
  void changedPinIsUsedNextTime() throws Exception {
    AtmSession session = bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
    bank.auth.changePin(session, "4321", "4321");
    assertThrows(WrongPinException.class,
        () -> bank.auth.authenticate(TestBank.CARD, TestBank.PIN));
    assertNotNull(bank.auth.authenticate(TestBank.CARD, "4321"));
  }

  @ParameterizedTest(name = "New PIN \"{0}\" is rejected")
  @ValueSource(strings = {"123", "12345", "12a4", ""})
  @DisplayName("A new PIN must be exactly 4 digits (boundaries 3 and 5, and non-digits)")
  void newPinMustBeFourDigits(String pin) throws Exception {
    AtmSession session = bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
    assertThrows(InvalidPinFormatException.class, () -> bank.auth.changePin(session, pin, pin));
  }

  @Test
  @DisplayName("The new PIN and its confirmation must match")
  void confirmationMustMatch() throws Exception {
    AtmSession session = bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
    assertThrows(PinMismatchException.class,
        () -> bank.auth.changePin(session, "4321", "4322"));
  }
}
