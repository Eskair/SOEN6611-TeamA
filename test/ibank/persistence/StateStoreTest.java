package ibank.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ibank.device.CashDispenser;
import ibank.exception.CardLockedException;
import ibank.exception.DailyLimitExceededException;
import ibank.model.Money;
import ibank.service.AtmSession;
import ibank.support.TestBank;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Each test uses the ABM, saves, then starts a fresh ABM from the same starting data
 * and restores the saved state, exactly as happens when iBank is closed and reopened.
 */
class StateStoreTest {

  @TempDir
  Path folder;

  private StateStore store() {
    return new StateStore(folder, Money.CAD);
  }

  /** A new ABM from the starting data, with the saved state applied. */
  private TestBank restart() throws Exception {
    TestBank fresh = new TestBank();
    store().restore(fresh.bankCore);
    return fresh;
  }

  @Test
  @DisplayName("Balances and the notes left in the machine survive a restart")
  void balancesAndNotesSurvive() throws Exception {
    TestBank before = new TestBank();
    AtmSession session = before.auth.authenticate(TestBank.CARD, TestBank.PIN);
    before.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(150));
    store().save(before.bankCore, before.dispenser);

    TestBank after = restart();
    AtmSession again = after.auth.authenticate(TestBank.CARD, TestBank.PIN);
    assertEquals(Money.cad(1350), after.transactions.balance(again, TestBank.CHEQUING));
    Map<Integer, Integer> notes = store().savedNotes().orElseThrow();
    assertEquals(9, notes.get(100));
    assertEquals(9, notes.get(50));
    assertEquals(Money.cad(1700),
        new CashDispenser(after.config, notes).getTotalCash());
  }

  @Test
  @DisplayName("Today's withdrawals survive a restart, so restarting cannot reset the daily limit")
  void dailyLimitContinues() throws Exception {
    TestBank before = new TestBank();
    AtmSession session = before.auth.authenticate(TestBank.CARD, TestBank.PIN);
    before.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(1000));
    store().save(before.bankCore, before.dispenser);

    TestBank after = restart();
    AtmSession again = after.auth.authenticate(TestBank.CARD, TestBank.PIN);
    assertThrows(DailyLimitExceededException.class,
        () -> after.transactions.withdraw(again, TestBank.SAVINGS, Money.cad(5)));
  }

  @Test
  @DisplayName("A locked card stays locked after a restart")
  void lockSurvives() throws Exception {
    TestBank before = new TestBank();
    before.main.getCard().lock();
    store().save(before.bankCore, before.dispenser);

    TestBank after = restart();
    assertThrows(CardLockedException.class,
        () -> after.auth.authenticate(TestBank.CARD, TestBank.PIN));
  }

  @Test
  @DisplayName("A changed PIN survives a restart, and the PIN itself is never written to disk")
  void changedPinSurvives() throws Exception {
    TestBank before = new TestBank();
    AtmSession session = before.auth.authenticate(TestBank.CARD, TestBank.PIN);
    before.auth.changePin(session, "4321", "4321");
    store().save(before.bankCore, before.dispenser);

    String savedCards = Files.readString(folder.resolve("cards.csv"));
    assertFalse(savedCards.contains(",4321,"));
    TestBank after = restart();
    assertTrue(after.main.getCard().isPinCorrect("4321"));
    assertFalse(after.main.getCard().isPinCorrect(TestBank.PIN));
  }

  @Test
  @DisplayName("On the very first run there is nothing saved, and nothing changes")
  void firstRunHasNoState() throws Exception {
    TestBank fresh = restart();
    assertTrue(store().savedNotes().isEmpty());
    AtmSession session = fresh.auth.authenticate(TestBank.CARD, TestBank.PIN);
    assertEquals(Money.cad(1500), fresh.transactions.balance(session, TestBank.CHEQUING));
  }

  @Test
  @DisplayName("A damaged saved file is reported clearly instead of being half-read")
  void damagedFileReported() throws Exception {
    Files.writeString(folder.resolve("accounts.csv"), "# header\n100111101\n");
    TestBank fresh = new TestBank();
    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> store().restore(fresh.bankCore));
    assertTrue(error.getMessage().contains("line 2"));
  }
}
