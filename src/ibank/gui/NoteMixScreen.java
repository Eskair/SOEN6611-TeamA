package ibank.gui;

import ibank.device.NoteBundle;
import ibank.model.Account;
import ibank.model.Money;
import ibank.service.WithdrawalResult;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Withdraw Cash, second step: the cardholder chooses which notes to receive, as RBC
 * branch ATMs allow. The screen starts with the fewest-notes suggestion; each count
 * can be changed, up to what the machine holds. A running total shows whether the mix
 * adds up to the requested amount.
 */
public class NoteMixScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;
  private static final Color OK_COLOUR = new Color(0, 110, 40);
  private static final Color WRONG_COLOUR = new Color(170, 0, 0);

  private final transient Account account;
  private final transient Money amount;
  private final transient NoteBundle suggestion;
  private final Map<Integer, JSpinner> counts = new LinkedHashMap<>();
  private final JLabel total = new JLabel(" ");

  /** Builds the screen, starting from the fewest-notes suggestion. */
  public NoteMixScreen(AtmFrame frame, Account account, Money amount, NoteBundle suggestion) {
    super(frame, "notes.title", frame.text().format(amount));
    this.account = account;
    this.amount = amount;
    this.suggestion = suggestion;
    for (Map.Entry<Integer, Integer> stock : frame.dispenser().getStock().entrySet()) {
      addCountField(stock.getKey(), stock.getValue());
    }
    addLine(total);
    JButton ok = addButton("button.ok", this::withdraw);
    addButton("notes.fewest", this::useSuggestion);
    addButton("button.back", frame::showMenu);
    setEnterButton(ok);
    useSuggestion();
  }

  private void addCountField(int noteValue, int inMachine) {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, inMachine, 1));
    spinner.setPreferredSize(new Dimension(110, 40));
    spinner.setEnabled(inMachine > 0);
    spinner.addChangeListener(event -> updateTotal());
    counts.put(noteValue, spinner);
    JPanel compact = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Keeps the box small.
    compact.setOpaque(false);
    compact.add(spinner);
    addFieldText(text().get("notes.row", noteValue, inMachine), compact);
  }

  /** Puts the fewest-notes suggestion back into every count. */
  private void useSuggestion() {
    for (Map.Entry<Integer, JSpinner> entry : counts.entrySet()) {
      entry.getValue().setValue(suggestion.getNotes().getOrDefault(entry.getKey(), 0));
    }
    updateTotal();
  }

  private NoteBundle chosenNotes() {
    Map<Integer, Integer> chosen = new LinkedHashMap<>();
    for (Map.Entry<Integer, JSpinner> entry : counts.entrySet()) {
      chosen.put(entry.getKey(), (Integer) entry.getValue().getValue());
    }
    return new NoteBundle(chosen, amount.getCurrency());
  }

  private void updateTotal() {
    Money chosen = chosenNotes().getTotal();
    total.setText(text().get("notes.total", chosen, amount));
    total.setForeground(chosen.equals(amount) ? OK_COLOUR : WRONG_COLOUR);
  }

  private void withdraw() {
    frame.attempt(() -> {
      WithdrawalResult result = frame.transactions().withdraw(
          frame.session(), account.getNumber(), amount, chosenNotes());
      frame.showInfo(text().get("withdraw.done", result.getNotes()));
      frame.showScreen(new ReceiptScreen(frame, result.getTransaction(), result.getNotes()));
    });
  }
}
