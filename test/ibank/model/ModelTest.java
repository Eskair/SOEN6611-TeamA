package ibank.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ibank.exception.InsufficientFundsException;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModelTest {

  @Test
  @DisplayName("Money adds cents exactly: 0.10 + 0.20 = 0.30")
  void moneyIsExact() {
    Money sum = Money.of("0.10", Money.CAD).plus(Money.of("0.20", Money.CAD));
    assertEquals(Money.of("0.30", Money.CAD), sum);
  }

  @Test
  @DisplayName("Money refuses to mix currencies")
  void moneyRefusesMixedCurrencies() {
    Money usd = Money.of("5.00", Currency.getInstance("USD"));
    assertThrows(IllegalArgumentException.class, () -> Money.cad(5).plus(usd));
  }

  @Test
  @DisplayName("Account and card numbers show only the last 4 digits")
  void numbersAreMasked() {
    Account account = new Account("100111101", AccountType.CHEQUING, Money.cad(0));
    Card card = new Card("4500000000001111", "1111");
    assertEquals("*****1101", account.getMaskedNumber());
    assertEquals("************1111", card.getMaskedNumber());
  }

  @Test
  @DisplayName("A failed debit leaves the balance unchanged")
  void failedDebitChangesNothing() {
    Account account = new Account("1", AccountType.CHEQUING, Money.cad(10));
    assertThrows(InsufficientFundsException.class,
        () -> account.debit(Money.of("10.01", Money.CAD)));
    assertEquals(Money.cad(10), account.getBalance());
  }

  @Test
  @DisplayName("Other classes cannot add or remove a cardholder's accounts")
  void accountListIsReadOnly() {
    Cardholder holder = new Cardholder("Test", new Card("1", "1111"),
        List.of(new Account("1", AccountType.CHEQUING, Money.cad(0))));
    assertThrows(UnsupportedOperationException.class, () -> holder.getAccounts().clear());
  }
}
