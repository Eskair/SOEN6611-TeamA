package ibank.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ibank.i18n.Messages;
import ibank.model.Cardholder;
import ibank.model.Money;
import ibank.service.AtmSession;
import ibank.service.WithdrawalResult;
import ibank.support.TestBank;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataAndReceiptTest {

  @Test
  @DisplayName("The sample data file loads 4 cardholders and 7 accounts")
  void sampleDataLoads() throws Exception {
    List<Cardholder> people =
        CardholderCsvLoader.load(Path.of("data", "cardholders.csv"), Money.CAD);
    assertEquals(4, people.size());
    assertEquals(7, people.stream().mapToInt(p -> p.getAccounts().size()).sum());
  }

  @Test
  @DisplayName("A malformed data line is reported with its line number")
  void badLineIsReported(@TempDir Path folder) throws Exception {
    Path file = folder.resolve("bad.csv");
    Files.writeString(file, "# header\n4500,1111,Name,100,CHEQUING\n");
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> CardholderCsvLoader.load(file, Money.CAD));
    assertTrue(error.getMessage().contains("line 2"));
  }

  @Test
  @DisplayName("A receipt shows masked numbers only, never the full card or account number")
  void receiptMasksNumbers() throws Exception {
    TestBank bank = new TestBank();
    AtmSession session = bank.auth.authenticate(TestBank.CARD, TestBank.PIN);
    WithdrawalResult result =
        bank.transactions.withdraw(session, TestBank.CHEQUING, Money.cad(60));
    String receipt = new ReceiptPrinter().print(
        result.getTransaction(), bank.main.getCard(), result.getNotes(),
        new Messages(Messages.ENGLISH));
    assertTrue(receipt.contains("*****1101"));
    assertFalse(receipt.contains(TestBank.CHEQUING));
    assertFalse(receipt.contains(TestBank.CARD));
    assertTrue(receipt.contains("1 x $50, 1 x $10"));
  }
}
