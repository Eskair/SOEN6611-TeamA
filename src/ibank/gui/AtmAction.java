package ibank.gui;

import ibank.exception.AtmException;

/**
 * A step the cardholder asked for, which may fail with an iBank error. Screens pass
 * these to {@link AtmFrame#attempt(AtmAction)}, so every error is handled in one place.
 */
@FunctionalInterface
public interface AtmAction {

  /** Performs the action. */
  void run() throws AtmException;
}
