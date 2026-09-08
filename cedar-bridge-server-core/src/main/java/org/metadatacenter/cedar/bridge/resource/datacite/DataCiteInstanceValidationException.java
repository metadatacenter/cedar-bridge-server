package org.metadatacenter.cedar.bridge.resource.datacite;

import org.metadatacenter.exception.CedarException;
import org.metadatacenter.http.CedarResponseStatus;

/**
 * The metadata instance the caller submitted for a DOI does not satisfy the DataCite schema. That is
 * the caller's to fix, so the exception decides on 400 rather than leaving the error pack to fall
 * through to its 500 default.
 */
public class DataCiteInstanceValidationException extends CedarException {

  public DataCiteInstanceValidationException(String message) {
    super(message);
    errorPack.status(CedarResponseStatus.BAD_REQUEST);
  }

}
