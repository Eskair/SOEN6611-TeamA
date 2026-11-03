package ibank.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ibank.config.AtmConfig;
import ibank.exception.CashUnavailableException;
import ibank.exception.InvalidAmountException;
import ibank.model.Money;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CashDispenserTest {

  private final AtmConfig config = AtmConfig.montrealDefaults();

  private CashDispenser fullDispenser() {
    return new CashDispenser(config, Map.of(100, 10, 50, 10, 20, 10, 10, 10, 5, 10));
  }

  @ParameterizedTest(name = "${0} is paid with {1} notes")
  @CsvSource({"5, 1", "15, 2", "60, 2", "185, 5", "500, 5"})
  @DisplayName("Valid amounts are paid with the fewest notes")
  void paysWithFewestNotes(int dollars, int expectedNotes) throws Exception {
    NoteBundle notes = fullDispenser().plan(Money.cad(dollars));
    assertEquals(Money.cad(dollars), notes.getTotal());
    assertEquals(expectedNotes, notes.getNoteCount());
  }

  @Test
  @DisplayName("$60 with one $50 and three $20 is paid as 3 x $20 (largest-first would fail)")
  void avoidsLargestFirstTrap() throws Exception {
    CashDispenser dispenser = new CashDispenser(config, Map.of(50, 1, 20, 3));
    NoteBundle notes = dispenser.plan(Money.cad(60));
    assertEquals(Map.of(20, 3), notes.getNotes());
  }

  @ParameterizedTest(name = "${0} is not a valid amount")
  @CsvSource({"7", "1", "12.50", "99"})
  @DisplayName("Amounts that no notes can make are rejected")
  void rejectsAmountsNoNotesCanMake(String amount) {
    assertThrows(InvalidAmountException.class,
        () -> fullDispenser().plan(Money.of(amount, Money.CAD)));
  }

  @ParameterizedTest(name = "${0} is rejected as not positive")
  @CsvSource({"0", "-20"})
  @DisplayName("Zero and negative amounts are rejected")
  void rejectsZeroAndNegative(String amount) {
    assertThrows(InvalidAmountException.class,
        () -> fullDispenser().plan(Money.of(amount, Money.CAD)));
  }

  @Test
  @DisplayName("A valid amount the current notes cannot make is reported as cash unavailable")
  void reportsCashUnavailable() {
    CashDispenser dispenser = new CashDispenser(config, Map.of(50, 1, 20, 3));
    assertThrows(CashUnavailableException.class, () -> dispenser.plan(Money.cad(30)));
  }

  @Test
  @DisplayName("Asking for more than the machine holds is reported as cash unavailable")
  void reportsCashUnavailableWhenEmpty() {
    CashDispenser dispenser = new CashDispenser(config, Map.of(20, 2));
    assertThrows(CashUnavailableException.class, () -> dispenser.plan(Money.cad(60)));
  }

  @Test
  @DisplayName("Planning does not remove notes; dispensing does")
  void onlyDispenseChangesStock() throws Exception {
    CashDispenser dispenser = fullDispenser();
    NoteBundle notes = dispenser.plan(Money.cad(150));
    assertEquals(Money.cad(1850), dispenser.getTotalCash());
    dispenser.dispense(notes);
    assertEquals(Money.cad(1700), dispenser.getTotalCash());
    assertEquals(9, dispenser.getStock().get(100));
    assertEquals(9, dispenser.getStock().get(50));
  }
}
