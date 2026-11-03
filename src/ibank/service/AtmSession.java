package ibank.service;

import ibank.exception.SessionExpiredException;
import ibank.model.Cardholder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * One cardholder's visit to the ABM, from a correct PIN until the session ends.
 *
 * <p>A session ends when the cardholder chooses End Session, or automatically after a
 * period without activity, so a cardholder who walks away is not left logged in. The
 * clock is passed in so tests can move time forward without waiting.
 */
public class AtmSession {

  private final Cardholder cardholder;
  private final Clock clock;
  private final Duration timeout;
  private Instant lastActivity;
  private boolean ended;

  /** Starts a session now, which ends after the given idle time. */
  public AtmSession(Cardholder cardholder, Clock clock, Duration timeout) {
    this.cardholder = cardholder;
    this.clock = clock;
    this.timeout = timeout;
    this.lastActivity = clock.instant();
  }

  public Cardholder getCardholder() {
    return cardholder;
  }

  /**
   * Checks that the session can still be used and records new activity.
   *
   * @throws SessionExpiredException if the session was ended or has been idle too long
   */
  public void ensureActive() throws SessionExpiredException {
    if (!ended && isIdleTooLong()) {
      ended = true;
    }
    if (ended) {
      throw new SessionExpiredException();
    }
    lastActivity = clock.instant();
  }

  /** True if the session has ended or timed out. Does not count as activity. */
  public boolean isEnded() {
    return ended || isIdleTooLong();
  }

  /** Ends the session (End Session use case). */
  public void end() {
    ended = true;
  }

  private boolean isIdleTooLong() {
    return Duration.between(lastActivity, clock.instant()).compareTo(timeout) > 0;
  }
}
