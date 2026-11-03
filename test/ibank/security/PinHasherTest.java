package ibank.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PinHasherTest {

  @Test
  @DisplayName("The stored value never contains the PIN itself")
  void pinIsNotStored() {
    assertFalse(PinHasher.hash("1234").contains("1234"));
  }

  @Test
  @DisplayName("The same PIN gives a different stored value each time (random salt)")
  void saltMakesHashesDiffer() {
    assertNotEquals(PinHasher.hash("1234"), PinHasher.hash("1234"));
  }

  @Test
  @DisplayName("Only the correct PIN matches the stored value")
  void onlyCorrectPinMatches() {
    String stored = PinHasher.hash("1234");
    assertTrue(PinHasher.matches("1234", stored));
    assertFalse(PinHasher.matches("1235", stored));
  }
}
