package org.metadatacenter.cedar.bridge.resources.doi;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A DOI CEDAR chooses is written as DataCite writes the DOIs it generates. */
class DoiSuffixesTest {

  private static final Pattern DATACITE_FORM =
      Pattern.compile("[0-9a-hjkmnp-tv-z]{4}-[0-9a-hjkmnp-tv-z]{2}[0-9]{2}");

  /**
   * Two suffixes DataCite generated: production's {@code 10.82658/aqdn-5e14}, which the backend runbook
   * names, and {@code 10.14454/3w3z-sa82}, one of DataCite's own. Each is the number its first
   * six characters write, followed by that number's two ISO 7064 check digits.
   */
  @Test
  void writesANumberAsDataCiteWritesIt() {
    assertEquals("aqdn-5e14", DoiSuffixes.encode(360_109_230L));
    assertEquals("3w3z-sa82", DoiSuffixes.encode(130_154_282L));
    assertEquals("0000-0098", DoiSuffixes.encode(0L));
    assertEquals("zzzz-zz" + String.format("%02d", 98 - (((1L << 30) - 1) * 100) % 97), DoiSuffixes.encode((1L << 30) - 1));
  }

  @Test
  void refusesANumberOutsideTheRangeSixCharactersWrite() {
    assertThrows(IllegalArgumentException.class, () -> DoiSuffixes.encode(-1));
    assertThrows(IllegalArgumentException.class, () -> DoiSuffixes.encode(1L << 30));
  }

  @Test
  void eachNewSuffixHasDataCitesFormAndAValidCheck() {
    DoiSuffixes suffixes = new DoiSuffixes(new Random(20261028L));
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      String suffix = suffixes.get();
      assertTrue(DATACITE_FORM.matcher(suffix).matches(), suffix);
      long number = 0;
      for (char character : suffix.replace("-", "").substring(0, 6).toCharArray()) {
        number = number * 32 + "0123456789abcdefghjkmnpqrstvwxyz".indexOf(character);
      }
      assertEquals(suffix, DoiSuffixes.encode(number));
      seen.add(suffix);
    }
    assertTrue(seen.size() > 990, "suffixes repeat: " + seen.size());
  }
}
