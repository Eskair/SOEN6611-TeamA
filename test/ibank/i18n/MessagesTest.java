package ibank.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ibank.exception.InsufficientFundsException;
import ibank.model.Money;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessagesTest {

  private static Set<String> keys(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("ibank.i18n.messages", locale);
    return new TreeSet<>(Collections.list(bundle.getKeys()));
  }

  @Test
  @DisplayName("English and French have exactly the same message keys")
  void languagesHaveSameKeys() {
    assertEquals(keys(Messages.ENGLISH), keys(Messages.FRENCH));
  }

  @Test
  @DisplayName("Money is written the English way and the French way")
  void moneyFormatPerLanguage() {
    Money amount = Money.of("1234.50", Money.CAD);
    assertEquals("$1,234.50", new Messages(Messages.ENGLISH).format(amount));
    // French Canada: space as thousands separator, comma for decimals, $ after the number.
    String french = new Messages(Messages.FRENCH).format(amount).replace('\u00a0', ' ')
        .replace('\u202f', ' ');
    assertEquals("1 234,50 $", french);
  }

  @Test
  @DisplayName("An error message shows the formatted amount in the chosen language")
  void errorMessagesAreTranslated() {
    InsufficientFundsException error = new InsufficientFundsException(Money.cad(35));
    assertTrue(new Messages(Messages.ENGLISH).describe(error).contains("$35.00"));
    assertTrue(new Messages(Messages.FRENCH).describe(error).startsWith("Fonds insuffisants"));
  }

  @Test
  @DisplayName("A missing key shows as !key! instead of crashing")
  void missingKeyIsVisible() {
    assertEquals("!no.such.key!", new Messages(Messages.ENGLISH).get("no.such.key"));
  }
}
