package ibank.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/** A clock that tests can move forward, so timeouts and "next day" need no waiting. */
public class MutableClock extends Clock {

  private static final ZoneId MONTREAL = ZoneId.of("America/Toronto");
  private Instant now;

  /** Creates a clock that shows the given time until it is moved. */
  public MutableClock(Instant start) {
    this.now = start;
  }

  /** 3 November 2026, 10:00 in Montreal. */
  public static MutableClock startOfTestDay() {
    return new MutableClock(Instant.parse("2026-11-03T15:00:00Z"));
  }

  /** Moves the clock forward. */
  public void advance(Duration duration) {
    now = now.plus(duration);
  }

  @Override
  public Instant instant() {
    return now;
  }

  @Override
  public ZoneId getZone() {
    return MONTREAL;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }
}
