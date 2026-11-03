package ibank.device;

import ibank.config.AtmConfig;
import ibank.exception.CashUnavailableException;
import ibank.exception.InvalidAmountException;
import ibank.exception.InvalidNoteMixException;
import ibank.model.Money;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * The simulated cash dispenser (a secondary actor in the D1 use case model).
 *
 * <p>It holds a stock of each note value and works out how to pay an amount using the
 * fewest notes it actually has.
 */
public class CashDispenser {

  private final List<Integer> noteValues;
  private final Map<Integer, Integer> stock = new TreeMap<>(Collections.reverseOrder());
  private final AtmConfig config;

  /**
   * Creates a dispenser.
   *
   * @param initialStock note value in dollars to number of notes loaded
   */
  public CashDispenser(AtmConfig config, Map<Integer, Integer> initialStock) {
    this.config = config;
    this.noteValues = config.getNoteValues();
    for (int value : noteValues) {
      stock.put(value, initialStock.getOrDefault(value, 0));
    }
  }

  /**
   * Works out which notes to pay out for an amount, using the fewest notes. Nothing is
   * taken from the stock; call {@link #dispense(NoteBundle)} for that.
   *
   * @throws InvalidAmountException if no combination of the note values can ever make
   *     the amount, for example $7
   * @throws CashUnavailableException if the amount is valid but the notes currently in
   *     the machine cannot make it
   */
  public NoteBundle plan(Money amount) throws InvalidAmountException, CashUnavailableException {
    if (!amount.isPositive()) {
      throw InvalidAmountException.notPositive();
    }
    int dollars = toWholeDollars(amount);
    if (dollars < 0 || dollars % smallestStep() != 0) {
      throw InvalidAmountException.notDispensable(amount, describeNoteValues());
    }
    int[] best = search(dollars);
    if (best == null) {
      throw new CashUnavailableException();
    }
    Map<Integer, Integer> notes = new HashMap<>();
    for (int i = 0; i < noteValues.size(); i++) {
      notes.put(noteValues.get(i), best[i]);
    }
    return new NoteBundle(notes, config.getCurrency());
  }

  /**
   * Checks a note mix the cardholder chose: it must add up to the requested amount, and
   * the machine must hold enough of each note.
   *
   * @throws InvalidNoteMixException if the total is wrong or a note runs short
   */
  public void checkMix(NoteBundle chosen, Money requested) throws InvalidNoteMixException {
    if (!chosen.getTotal().equals(requested)) {
      throw InvalidNoteMixException.wrongTotal(chosen.getTotal(), requested);
    }
    for (Map.Entry<Integer, Integer> entry : chosen.getNotes().entrySet()) {
      int available = stock.getOrDefault(entry.getKey(), 0);
      if (entry.getValue() > available) {
        throw InvalidNoteMixException.notEnough(entry.getKey(), available);
      }
    }
  }

  /** Takes the notes out of the stock. Call only after {@link #plan} or {@link #checkMix}. */
  public void dispense(NoteBundle bundle) {
    for (Map.Entry<Integer, Integer> entry : bundle.getNotes().entrySet()) {
      int left = stock.get(entry.getKey()) - entry.getValue();
      if (left < 0) {
        throw new IllegalStateException("Not enough $" + entry.getKey() + " notes");
      }
      stock.put(entry.getKey(), left);
    }
  }

  /** Current number of notes of each value, largest first. */
  public Map<Integer, Integer> getStock() {
    return Collections.unmodifiableMap(stock);
  }

  /** Total value of all notes in the machine. */
  public Money getTotalCash() {
    return new NoteBundle(stock, config.getCurrency()).getTotal();
  }

  /** Returns the note values as text, for example "$100, $50, $20, $10, $5". */
  public String describeNoteValues() {
    StringJoiner joiner = new StringJoiner(", ");
    for (int value : noteValues) {
      joiner.add("$" + value);
    }
    return joiner.toString();
  }

  // ---------------------------------------------------------------- search

  /**
   * Finds the note counts that make {@code dollars} with the fewest notes, or returns
   * {@code null} if the stock cannot make it.
   *
   * <p>Why not simply "largest note first"? Because that fails when a note runs out.
   * Example: $60 with one $50, three $20 and no $10 or $5. Taking the $50 leaves $10,
   * which cannot be paid; 3 x $20 works. So the search tries fewer of each large note
   * when a choice leads nowhere, and keeps the answer with the fewest notes.
   */
  private int[] search(int dollars) {
    SearchState state = new SearchState(noteValues.size());
    tryNotes(0, dollars, 0, state);
    return state.best;
  }

  private void tryNotes(int index, int remaining, int notesSoFar, SearchState state) {
    if (remaining == 0) {
      state.recordIfBetter(notesSoFar);
      return;
    }
    if (index == noteValues.size() || notesSoFar >= state.bestCount) {
      return; // No notes left to try, or already worse than the best answer found.
    }
    int value = noteValues.get(index);
    int most = Math.min(stock.get(value), remaining / value);
    for (int count = most; count >= 0; count--) {
      state.current[index] = count;
      tryNotes(index + 1, remaining - count * value, notesSoFar + count, state);
    }
    state.current[index] = 0;
  }

  /** The working arrays of the search, kept together so methods stay short. */
  private static final class SearchState {
    final int[] current;
    int[] best;
    int bestCount = Integer.MAX_VALUE;

    SearchState(int size) {
      current = new int[size];
    }

    void recordIfBetter(int count) {
      if (count < bestCount) {
        bestCount = count;
        best = current.clone();
      }
    }
  }

  // ---------------------------------------------------------------- helpers

  /** The smallest step any amount must be a multiple of: the GCD of the note values. */
  private int smallestStep() {
    int gcd = 0;
    for (int value : noteValues) {
      gcd = gcd(gcd, value);
    }
    return gcd;
  }

  private static int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
  }

  /** Returns the amount in whole dollars, or -1 if it has cents. */
  private static int toWholeDollars(Money amount) {
    BigDecimal value = amount.getAmount();
    if (value.stripTrailingZeros().scale() > 0
        || value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
      return -1;
    }
    return value.intValueExact();
  }
}
