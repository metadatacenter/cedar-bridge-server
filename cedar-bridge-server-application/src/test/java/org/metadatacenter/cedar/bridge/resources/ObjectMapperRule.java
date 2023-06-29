package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


public class ObjectMapperRule implements TestRule {
  private ObjectMapper objectMapper;

  public ObjectMapperRule() {
    objectMapper = new ObjectMapper();
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        objectMapper = new ObjectMapper();
        try {
          base.evaluate();
        } finally {
          objectMapper = null;
        }
      }
    };
  }
}
