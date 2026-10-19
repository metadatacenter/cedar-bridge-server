package org.metadatacenter.cedar.bridge.resources;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.util.http.CedarError;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * The rule the DOI and external-authority routes share, for every status a service can answer with.
 *
 * <p>Only a refusal of the request itself keeps the service's status. Everything else concerns the
 * deployment or the service, and is a 502. No status is answered as a 401, so no answer carries
 * CEDAR's authentication challenge.
 */
class UpstreamRefusalTest {

  @Test
  void onlyARefusalOfTheRequestItselfKeepsTheServicesStatus() {
    for (int status = 100; status <= 999; status++) {
      UpstreamRefusal refusal = new UpstreamRefusal("A service", status);
      UpstreamRefusal.Kind kind;
      if (status < 400) {
        kind = UpstreamRefusal.Kind.UNUSABLE;
      } else if (status >= 500) {
        kind = UpstreamRefusal.Kind.FAILED;
      } else if (status == 401 || status == 403) {
        kind = UpstreamRefusal.Kind.ACCESS_REFUSED;
      } else if (status == 429) {
        kind = UpstreamRefusal.Kind.RATE_LIMITED;
      } else {
        kind = UpstreamRefusal.Kind.REQUEST_REFUSED;
      }
      assertEquals(kind, refusal.kind(), "HTTP " + status);
      assertEquals(kind == UpstreamRefusal.Kind.REQUEST_REFUSED ? status : 502, refusal.answerStatus(),
          "HTTP " + status);

      Response answer = refusal.answer("It went wrong.", "").build();
      assertEquals(refusal.answerStatus(), answer.getStatus(), "HTTP " + status);
      assertFalse(answer.getHeaders().containsKey(HttpHeaders.WWW_AUTHENTICATE), "HTTP " + status);
    }
  }

  @Test
  void theAnswerIsTheSharedErrorBodyNamingTheServiceAndItsStatus() {
    Response answer = new UpstreamRefusal("A service", 401).answer("It refused.", "first\nsecond").build();

    CedarError error = assertInstanceOf(CedarError.class, answer.getEntity());
    assertEquals(502, error.statusCode);
    assertEquals(CedarErrorKey.UPSTREAM_SERVER_ERROR.getValue(), error.errorKey);
    assertEquals("It refused.\nfirst\nsecond", error.message);
    assertEquals(Map.of("upstreamStatusCode", 401, "upstreamService", "A service"), error.parameters);
    assertEquals(Map.of(), error.objects);

    CedarError withoutReasons = assertInstanceOf(CedarError.class,
        new UpstreamRefusal("A service", 404).answer("It holds none.", "").build().getEntity());
    assertEquals(404, withoutReasons.statusCode);
    assertEquals("It holds none.", withoutReasons.message);
  }
}
