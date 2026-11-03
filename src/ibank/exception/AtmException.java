package ibank.exception;

import java.util.Arrays;

/**
 * Base class for every error a cardholder can cause at the ABM.
 *
 * <p>Each error carries a message key and optional values instead of fixed text. The
 * GUI uses the key to show the message in the language the cardholder chose. These
 * are checked exceptions, so the compiler forces every caller to handle them.
 */
public abstract class AtmException extends Exception {

  private static final long serialVersionUID = 1L;

  private final String messageKey;
  private final transient Object[] messageArgs;

  /** Creates an error with a message key and the values to insert into the message. */
  protected AtmException(String messageKey, Object... messageArgs) {
    super(messageKey);
    this.messageKey = messageKey;
    this.messageArgs = messageArgs.clone();
  }

  /** Key for the message in the language files, for example "error.pin.wrong". */
  public String getMessageKey() {
    return messageKey;
  }

  /** Values to insert into the message, for example the number of attempts left. */
  public Object[] getMessageArgs() {
    return messageArgs.clone();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + messageKey + " " + Arrays.toString(messageArgs);
  }
}
