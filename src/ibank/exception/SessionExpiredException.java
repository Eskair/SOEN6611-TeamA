package ibank.exception;

/** Thrown when a session has been idle for too long and has ended. */
public class SessionExpiredException extends AtmException {

  private static final long serialVersionUID = 1L;

  /** Creates the error. */
  public SessionExpiredException() {
    super("error.session.expired");
  }
}
