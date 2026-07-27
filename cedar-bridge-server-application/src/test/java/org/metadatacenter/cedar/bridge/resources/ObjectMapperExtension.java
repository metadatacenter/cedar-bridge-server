package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ObjectMapperExtension implements BeforeEachCallback, AfterEachCallback {
  private ObjectMapper objectMapper;

  public ObjectMapperExtension() {
    objectMapper = new ObjectMapper();
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    objectMapper = new ObjectMapper();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    objectMapper = null;
  }
}
