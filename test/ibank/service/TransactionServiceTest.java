package ibank.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ibank.device.NoteBundle;
import ibank.exception.AccountNotFoundException;
import ibank.exception.DailyLimitExceededException;
import ibank.exception.InsufficientFundsException;
import ibank.exception.InvalidAmountException;
import ibank.exception.InvalidNoteMixException;
import ibank.exception.SameAccountTransferException;
import ibank.exception.SessionExpiredException;
import ibank.model.Money;
import ibank.model.TransactionType;
import ibank.support.TestBank;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransactionServiceTest {

  private TestBank bank;
  private AtmSession session;

  @BeforeEach
  void logIn() throws Exception {
    bank = new TestBank();
    session = bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
  }

  private Money balanceOf(String account) throws Exception {
    return bank.transactions.balance(session, account);
  }

  @Nested
  @DisplayName("Withdraw")
  class Withdraw {
    @Test
    @DisplayName("An over-limit amount is rejected before choosing notes")
    void suggestNotesRejectsOverDailyLimit() throws Exception {
      DailyLimitExceededException error = assertThrows(DailyLimitExceededException.class,
          () -> bank.transactions.suggestNotes(session, Money.cad(2000)));

      assertEquals(Money.cad(1000), error.getMessageArgs()[0]);
      assertEquals(Money.cad(1500), balanceOf(TestBank.CHEQUING));
      assertEquals(Money.cad(1850), bank.dispenser.getTotalCash());
      assertEquals(0, bank.bankCore.getTransactions(bank.main).size());
    }
    @Test
    @DisplayName("A valid withdrawal lowers the balance, removes notes and is recorded")
    void validWithdrawal() throws Exception {
      WithdrawalResult result =
          bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(120));
      assertEquals(Money.cad(1380), balanceOf(TestBank.CHEQUING));
      assertEquals(Money.cad(1730), bank.dispenser.getTotalCash());
      assertEquals(TransactionType.WITHDRAWAL, result.getTransaction().getType());
      assertEquals(1, bank.bankCore.getTransactions(bank.main).size());
    }

    @Test
    @DisplayName("Withdrawing the exact balance is allowed (boundary)")
    void exactBalanceAllowed() throws Exception {
      AtmSession low = bank.auth.authenticate(TestBank.LOW_CARD, TestBank.LOW_PIN);
      bank.transactions.withdraw(low, TestBank.LOW_CHEQUING, Money.cad(35));
      assertEquals(Money.cad(0), bank.transactions.balance(low, TestBank.LOW_CHEQUING));
    }

    @Test
    @DisplayName("Insufficient funds: nothing changes (balance, notes, records)")
    void insufficientFundsChangesNothing() throws Exception {
      AtmSession low = bank.auth.authenticate(TestBank.LOW_CARD, TestBank.LOW_PIN);
      assertThrows(InsufficientFundsException.class,
          () -> bank.transactions.withdraw(low, TestBank.LOW_CHEQUING, Money.cad(40)));
      assertEquals(Money.cad(35), bank.transactions.balance(low, TestBank.LOW_CHEQUING));
      assertEquals(Money.cad(1850), bank.dispenser.getTotalCash());
      assertEquals(0, bank.bankCore.getTransactions(bank.lowBalance).size());
    }

    @Test
    @DisplayName("Withdrawing exactly the $1,000 daily limit is allowed (boundary)")
    void exactDailyLimitAllowed() {
      assertDoesNotThrow(
          () -> bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(1000)));
    }

    @Test
    @DisplayName("$5 over the daily limit is refused, and the message gives the amount left")
    void overDailyLimitRefused() throws Exception {
      bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(995));
      DailyLimitExceededException error = assertThrows(DailyLimitExceededException.class,
          () -> bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(10)));
      assertEquals(Money.cad(5), error.getMessageArgs()[0]);
      assertDoesNotThrow(
          () -> bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(5)));
    }

    @Test
    @DisplayName("The daily limit starts again the next day")
    void dailyLimitResetsNextDay() throws Exception {
      bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(1000));
      bank.clock.advance(Duration.ofDays(1));
      AtmSession tomorrow = bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
      assertDoesNotThrow(
          () -> bank.transactions.withdraw(tomorrow, TestBank.SAVINGS, Money.cad(100)));
    }

    @Test
    @DisplayName("The cardholder's own mix is paid: $100 as 5 x $20")
    void chosenMixIsPaid() throws Exception {
      NoteBundle chosen = new NoteBundle(Map.of(20, 5), Money.CAD);
      WithdrawalResult result =
          bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(100), chosen);
      assertEquals(Map.of(20, 5), result.getNotes().getNotes());
      assertEquals(5, bank.dispenser.getStock().get(20));
    }

    @Test
    @DisplayName("A mix that does not add up to the amount is refused, nothing changes")
    void wrongTotalRefused() throws Exception {
      NoteBundle chosen = new NoteBundle(Map.of(20, 4), Money.CAD);
      assertThrows(InvalidNoteMixException.class, () ->
          bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(100), chosen));
      assertEquals(Money.cad(1500), balanceOf(TestBank.CHEQUING));
    }

    @Test
    @DisplayName("More notes of a value than the machine holds are refused (boundary 10 vs 11)")
    void moreNotesThanStockRefused() {
      NoteBundle eleven = new NoteBundle(Map.of(5, 11), Money.CAD);
      NoteBundle ten = new NoteBundle(Map.of(5, 10), Money.CAD);
      assertThrows(InvalidNoteMixException.class, () ->
          bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(55), eleven));
      assertDoesNotThrow(() ->
          bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(50), ten));
    }

    @Test
    @DisplayName("An amount no notes can make is refused before anything changes")
    void undispensableAmountRefused() throws Exception {
      assertThrows(InvalidAmountException.class,
          () -> bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(7)));
      assertEquals(Money.cad(1500), balanceOf(TestBank.CHEQUING));
    }
  }

  @Nested
  @DisplayName("Deposit")
  class Deposit {

    @Test
    @DisplayName("Depositing $5, the smallest note, is accepted (boundary)")
    void smallestNoteAccepted() throws Exception {
      bank.transactions.deposit(session, TestBank.CHEQUING, Money.cad(5));
      assertEquals(Money.cad(1505), balanceOf(TestBank.CHEQUING));
    }

    @Test
    @DisplayName("A deposit that is not whole notes ($12) is refused")
    void nonNoteAmountRefused() {
      assertThrows(InvalidAmountException.class,
          () -> bank.transactions.deposit(session, TestBank.CHEQUING, Money.cad(12)));
    }

    @Test
    @DisplayName("A zero deposit is refused")
    void zeroRefused() {
      assertThrows(InvalidAmountException.class,
          () -> bank.transactions.deposit(session, TestBank.CHEQUING, Money.cad(0)));
    }
  }

  @Nested
  @DisplayName("Transfer")
  class Transfer {

    @Test
    @DisplayName("A transfer moves the money between the two accounts, cents included")
    void validTransfer() throws Exception {
      bank.transactions.transfer(
          session, TestBank.SAVINGS, TestBank.CHEQUING, Money.of("250.50", Money.CAD));
      assertEquals(Money.of("2749.50", Money.CAD), balanceOf(TestBank.SAVINGS));
      assertEquals(Money.of("1750.50", Money.CAD), balanceOf(TestBank.CHEQUING));
    }

    @Test
    @DisplayName("A transfer to the same account is refused")
    void sameAccountRefused() {
      assertThrows(SameAccountTransferException.class, () -> bank.transactions.transfer(
          session, TestBank.CHEQUING, TestBank.CHEQUING, Money.cad(10)));
    }

    @Test
    @DisplayName("Insufficient funds: neither account changes")
    void insufficientFundsChangesNeither() throws Exception {
      assertThrows(InsufficientFundsException.class, () -> bank.transactions.transfer(
          session, TestBank.CHEQUING, TestBank.SAVINGS, Money.cad(5000)));
      assertEquals(Money.cad(1500), balanceOf(TestBank.CHEQUING));
      assertEquals(Money.cad(3000), balanceOf(TestBank.SAVINGS));
    }

    @Test
    @DisplayName("Another cardholder's account cannot be used")
    void otherCardholdersAccountRefused() {
      assertThrows(AccountNotFoundException.class, () -> bank.transactions.transfer(
          session, TestBank.CHEQUING, TestBank.LOW_CHEQUING, Money.cad(10)));
    }
  }

  @Nested
  @DisplayName("Session")
  class Session {

    @Test
    @DisplayName("At exactly 60 seconds idle the session still works (boundary)")
    void sixtySecondsStillActive() {
      bank.clock.advance(Duration.ofSeconds(60));
      assertDoesNotThrow(() -> balanceOf(TestBank.CHEQUING));
    }

    @Test
    @DisplayName("After 61 seconds idle the session has ended")
    void sixtyOneSecondsExpired() {
      bank.clock.advance(Duration.ofSeconds(61));
      assertThrows(SessionExpiredException.class, () -> balanceOf(TestBank.CHEQUING));
    }

    @Test
    @DisplayName("After End Session nothing more can be done")
    void endedSessionRefused() {
      session.end();
      assertThrows(SessionExpiredException.class, () -> balanceOf(TestBank.CHEQUING));
    }
  }
}
