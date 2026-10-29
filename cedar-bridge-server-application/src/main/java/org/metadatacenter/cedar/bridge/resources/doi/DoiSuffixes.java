package org.metadatacenter.cedar.bridge.resources.doi;

import java.security.SecureRandom;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Random DOI suffixes in the form DataCite gives the suffixes it generates, such as {@code aqdn-5e14}.
 *
 * <p>CEDAR chooses each DOI itself, before it asks DataCite to register it, so that the DOI is recorded
 * before any request that could time out. A suffix is a random number below 2<sup>30</sup>, written as
 * six characters of Crockford's base 32 in lower case and followed by two ISO 7064 MOD 97-10 check
 * digits, in two groups of four. That is how DataCite writes the suffixes it generates, so a DOI CEDAR
 * chooses looks like one DataCite chose.
 */
public final class DoiSuffixes implements Supplier<String> {

  private static final char[] ALPHABET = "0123456789abcdefghjkmnpqrstvwxyz".toCharArray();
  private static final int UPPER_LIMIT = 1_073_741_823;
  private static final int CHARACTERS = 6;

  private final RandomGenerator random;

  public DoiSuffixes() {
    this(new SecureRandom());
  }

  public DoiSuffixes(RandomGenerator random) {
    this.random = random;
  }

  /** A new random suffix. */
  @Override
  public String get() {
    return encode(random.nextInt(UPPER_LIMIT));
  }

  /** The suffix that writes this number, which must lie between 0 and 2<sup>30</sup> - 1. */
  static String encode(long number) {
    if (number < 0 || number >= (1L << (5 * CHARACTERS))) {
      throw new IllegalArgumentException("A DOI suffix writes a number below 2^30, not " + number);
    }
    char[] characters = new char[CHARACTERS];
    long rest = number;
    for (int i = CHARACTERS - 1; i >= 0; i--) {
      characters[i] = ALPHABET[(int) (rest % 32)];
      rest /= 32;
    }
    long check = 98 - (number * 100) % 97;
    String written = new String(characters) + String.format("%02d", check);
    return written.substring(0, 4) + "-" + written.substring(4);
  }
}
