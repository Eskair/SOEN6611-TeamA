package ibank.device;

import ibank.model.Money;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * A set of notes to pay out, for example 1 x $50 + 1 x $20. Immutable.
 *
 * <p>Keys are note values in dollars; values are how many of that note.
 */
public final class NoteBundle {

  private final Map<Integer, Integer> notes;
  private final Currency currency;

  /** Creates a bundle; note values with a count of zero are left out. */
  public NoteBundle(Map<Integer, Integer> notes, Currency currency) {
    Map<Integer, Integer> copy = new TreeMap<>(Collections.reverseOrder());
    for (Map.Entry<Integer, Integer> entry : notes.entrySet()) {
      if (entry.getValue() > 0) {
        copy.put(entry.getKey(), entry.getValue());
      }
    }
    this.notes = Collections.unmodifiableMap(copy);
    this.currency = currency;
  }

  /** Note value to count, largest note first. */
  public Map<Integer, Integer> getNotes() {
    return notes;
  }

  /** The total number of notes in the bundle. */
  public int getNoteCount() {
    int count = 0;
    for (int n : notes.values()) {
      count += n;
    }
    return count;
  }

  /** The total value of the notes. */
  public Money getTotal() {
    long dollars = 0;
    for (Map.Entry<Integer, Integer> entry : notes.entrySet()) {
      dollars += (long) entry.getKey() * entry.getValue();
    }
    return Money.of(Long.toString(dollars), currency);
  }

  /** Plain form such as "1 x $50, 1 x $20". */
  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ");
    for (Map.Entry<Integer, Integer> entry : notes.entrySet()) {
      joiner.add(entry.getValue() + " x $" + entry.getKey());
    }
    return joiner.toString();
  }
}
