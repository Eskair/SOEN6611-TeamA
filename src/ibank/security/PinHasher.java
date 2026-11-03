package ibank.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Turns a PIN into a salted SHA-256 hash, so the PIN itself is never stored.
 *
 * <p>A stored value looks like {@code salt:hash}. To check a PIN, the same salt is
 * applied again and the two hashes are compared.
 */
public final class PinHasher {

  private static final int SALT_BYTES = 16;
  private static final SecureRandom RANDOM = new SecureRandom();

  private PinHasher() {
    // Utility class: no instances.
  }

  /** Hashes a PIN with a new random salt. */
  public static String hash(String pin) {
    byte[] salt = new byte[SALT_BYTES];
    RANDOM.nextBytes(salt);
    return encode(salt) + ":" + encode(digest(salt, pin));
  }

  /** Returns true if the PIN matches the stored {@code salt:hash} value. */
  public static boolean matches(String pin, String stored) {
    String[] parts = stored.split(":");
    if (parts.length != 2) {
      return false;
    }
    byte[] salt = Base64.getDecoder().decode(parts[0]);
    byte[] expected = Base64.getDecoder().decode(parts[1]);
    // Constant-time comparison, so timing does not reveal how many bytes matched.
    return MessageDigest.isEqual(expected, digest(salt, pin));
  }

  private static byte[] digest(byte[] salt, String pin) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      sha256.update(salt);
      return sha256.digest(pin.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      // Every Java platform is required to provide SHA-256.
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }

  private static String encode(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }
}
