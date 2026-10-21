package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * What a registry answered, read the same way by every authority that asks one.
 *
 * <p>The six authorities that proxy a registry each read its answer for themselves, and they
 * disagreed about everything that can go wrong. Four wrapped a registry they could not reach in a
 * RuntimeException, so the 503 the transport had raised went out as a 500. Two parsed the body
 * before looking at the status, so a registry's HTML error page became a 500 rather than the
 * registry's own status. Three answered "not found" for an identifier the registry had failed to
 * look up, and PubMed answered a search it could not put to NCBI with an empty page.
 *
 * <p>There are three outcomes. A registry that does not answer is an outage, which {@link ProxyUtil}
 * already raises as a 503 and which the authority's circuit breaker counts. A registry that answers
 * with a status other than 200 has refused, and that status is the authority's answer. A registry
 * that answers 200 with a body that cannot be read has failed as a gateway's upstream fails, which
 * is a 502.
 */
final class RegistryReply {

  private final int status;
  private final String body;

  private RegistryReply(int status, String body) {
    this.status = status;
    this.body = body;
  }

  static RegistryReply get(String url, Map<String, String> headers) throws CedarProcessingException {
    return of(ProxyUtil.proxyGet(sendable(url), headers));
  }

  static RegistryReply post(String url, Map<String, String> headers, String content) throws CedarProcessingException {
    return of(ProxyUtil.proxyPost(sendable(url), headers, content));
  }

  /**
   * The URL, when it can be sent.
   *
   * <p>The HTTP client accepts a URL it cannot parse and fails only when it comes to send it, in the
   * way it fails when a registry cannot be reached, so the request was answered 503 and counted by
   * the authority's circuit breaker against a registry it never reached. A request this server built
   * wrongly is this server's failure, a 500, which the breaker does not count against the registry.
   */
  private static String sendable(String url) throws CedarProcessingException {
    try {
      new URI(url);
      return url;
    } catch (URISyntaxException e) {
      throw new CedarProcessingException("The request to the registry could not be built", e);
    }
  }

  private static RegistryReply of(ClassicHttpResponse response) throws CedarProcessingException {
    try {
      String body = response.getEntity() == null ? null
          : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
      return new RegistryReply(response.getCode(), body);
    } catch (IOException | ParseException e) {
      throw unreadable(e);
    }
  }

  int status() {
    return status;
  }

  boolean ok() {
    return status == HttpConstants.OK;
  }

  /** The answer's JSON, which a 200 must carry. */
  JsonNode json() throws CedarProcessingException {
    try {
      JsonNode parsed = body == null ? null : JsonMapper.STRICT_MAPPER.readTree(body);
      if (parsed == null || parsed.isMissingNode()) {
        throw new IOException("The answer has no body");
      }
      return parsed;
    } catch (IOException e) {
      throw unreadable(e);
    }
  }

  /**
   * What a refusal said, when it said it in JSON, and null when it did not. A refusal is answered
   * according to the registry's status whatever its body holds, so an HTML error page is no reason to
   * fail.
   */
  JsonNode refusal() {
    try {
      return body == null ? null : JsonMapper.STRICT_MAPPER.readTree(body);
    } catch (IOException e) {
      return null;
    }
  }

  /** An answer that could not be read, which is a 502: the registry, not this server, failed. */
  static CedarProcessingException unreadable(Exception cause) {
    CedarProcessingException failure = new UnreadableAnswer(cause);
    failure.getErrorPack().status(CedarResponseStatus.BAD_GATEWAY);
    return failure;
  }

  /** Whether a failure is, or was caused by, an answer the registry gave that could not be read. */
  static boolean unreadableAnswer(Throwable failure) {
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      if (cause instanceof UnreadableAnswer) {
        return true;
      }
    }
    return false;
  }

  /** The failure {@link #unreadable} raises, told apart from one that happened before any answer. */
  private static final class UnreadableAnswer extends CedarProcessingException {
    UnreadableAnswer(Exception cause) {
      super("The registry's answer could not be read", cause);
    }
  }
}
