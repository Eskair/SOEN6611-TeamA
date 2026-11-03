package ibank.i18n;

import ibank.exception.AtmException;
import ibank.model.Money;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * All text shown to the cardholder, in English or French.
 *
 * <p>The text itself lives in {@code messages_en.properties} and
 * {@code messages_fr.properties}, so a translator can change wording without touching
 * Java code. Money values are formatted the way each language writes them: "$1,234.50"
 * in English and "1 234,50 $" in French.
 */
public final class Messages {

  /** English as used in Canada. */
  public static final Locale ENGLISH = Locale.CANADA;
  /** French as used in Quebec. */
  public static final Locale FRENCH = Locale.CANADA_FRENCH;

  private static final String BUNDLE = "ibank.i18n.messages";

  private final Locale locale;
  private final ResourceBundle bundle;
  private final NumberFormat moneyFormat;

  /** Loads the text for the given language. */
  public Messages(Locale locale) {
    this.locale = locale;
    this.bundle = ResourceBundle.getBundle(BUNDLE, locale);
    this.moneyFormat = dollarFormat(locale);
  }

  /**
   * The language's money format, always with the symbol "$". Without this, some Java
   * versions write French amounts as "1 234,50 $ CA" and others as "1 234,50 $", so the
   * ABM would look different depending on the Java installed.
   */
  private static NumberFormat dollarFormat(Locale locale) {
    NumberFormat format = NumberFormat.getCurrencyInstance(locale);
    if (format instanceof DecimalFormat) {
      DecimalFormat decimal = (DecimalFormat) format;
      DecimalFormatSymbols symbols = decimal.getDecimalFormatSymbols();
      symbols.setCurrencySymbol("$");
      decimal.setDecimalFormatSymbols(symbols);
    }
    return format;
  }

  /**
   * Returns the text for a key, with any values filled in. A missing key shows as
   * "!key!" so it is easy to spot during testing instead of crashing the ABM.
   */
  public String get(String key, Object... args) {
    String pattern;
    try {
      pattern = bundle.getString(key);
    } catch (MissingResourceException e) {
      return "!" + key + "!";
    }
    return new MessageFormat(pattern, locale).format(localize(args));
  }

  /** Returns the cardholder-facing message for an error. */
  public String describe(AtmException error) {
    return get(error.getMessageKey(), error.getMessageArgs());
  }

  /** Formats an amount in this language's style. */
  public String format(Money money) {
    return moneyFormat.format(money.getAmount());
  }

  /** Replaces Money values with their formatted text, so messages show "$35.00". */
  private Object[] localize(Object[] args) {
    Object[] result = args.clone();
    for (int i = 0; i < result.length; i++) {
      if (result[i] instanceof Money) {
        result[i] = format((Money) result[i]);
      }
    }
    return result;
  }
}
