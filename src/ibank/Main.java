package ibank;

import ibank.config.AtmConfig;
import ibank.device.CardholderCsvLoader;
import ibank.device.CashDispenser;
import ibank.device.SimulatedBankCore;
import ibank.gui.AtmFrame;
import ibank.persistence.StateStore;
import ibank.service.AuthenticationService;
import ibank.service.TransactionService;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.format.DateTimeParseException;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Starts the iBank ABM. Run it from the project folder so it finds the data files.
 *
 * <p>On the first run, iBank starts from {@code data/cardholders.csv} and a full
 * dispenser. After that, it continues from the state saved in {@code data/state/}.
 * Delete that folder to start again from the beginning.
 */
public final class Main {

  private static final Path DATA_FILE = Path.of("data", "cardholders.csv");
  private static final Path STATE_FOLDER = Path.of("data", "state");
  /** Notes in the machine on the very first run: 50 of each value ($9,250 in total). */
  private static final Map<Integer, Integer> STARTING_NOTES =
      Map.of(100, 50, 50, 50, 20, 50, 10, 50, 5, 50);
  private static final int TEXT_SIZE = 18;

  private Main() {
    // Entry point only.
  }

  /** Loads the data, restores the saved state and opens the ABM window. */
  public static void main(String[] args) {
    AtmConfig config = AtmConfig.montrealDefaults();
    StateStore store = new StateStore(STATE_FOLDER, config.getCurrency());
    SimulatedBankCore bankCore;
    CashDispenser dispenser;
    try {
      bankCore = new SimulatedBankCore(CardholderCsvLoader.load(DATA_FILE, config.getCurrency()));
      store.restore(bankCore);
      dispenser = new CashDispenser(config, store.savedNotes().orElse(STARTING_NOTES));
    } catch (IOException | IllegalArgumentException | DateTimeParseException e) {
      System.err.println("iBank could not start: " + e.getMessage()
          + "\nRun iBank from the project folder. If data/state is damaged, delete that folder.");
      System.exit(1);
      return;
    }
    Clock clock = Clock.systemDefaultZone();
    AuthenticationService auth = new AuthenticationService(bankCore, config, clock);
    TransactionService transactions = new TransactionService(bankCore, dispenser, config, clock);
    Runnable saveState = () -> {
      try {
        store.save(bankCore, dispenser);
      } catch (IOException e) {
        System.err.println("Warning: iBank could not save its state: " + e.getMessage());
      }
    };
    SwingUtilities.invokeLater(() -> {
      useLargeText();
      new AtmFrame(auth, transactions, dispenser, saveState).setVisible(true);
    });
  }

  /** Makes all text large enough to read comfortably at a walk-up machine. */
  private static void useLargeText() {
    // Copy the keys first: changing the settings while looping over them would fail.
    Object[] keys = UIManager.getLookAndFeelDefaults().keySet().toArray();
    for (Object key : keys) {
      Object value = UIManager.get(key);
      if (value instanceof FontUIResource) {
        Font font = (FontUIResource) value;
        UIManager.put(key, new FontUIResource(font.deriveFont((float) TEXT_SIZE)));
      }
    }
  }
}
