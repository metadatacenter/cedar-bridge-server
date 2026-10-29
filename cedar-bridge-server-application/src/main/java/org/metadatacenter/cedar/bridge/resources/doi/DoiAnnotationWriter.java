package org.metadatacenter.cedar.bridge.resources.doi;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.rest.context.CedarRequestContext;

/**
 * Sends the resource server's DOI command, which records a DOI on an artifact. The command refuses a
 * DOI other than the one the artifact carries and repeats harmlessly for the same one.
 */
@FunctionalInterface
public interface DoiAnnotationWriter {

  ClassicHttpResponse post(String url, CedarRequestContext context, String content) throws CedarProcessingException;
}
