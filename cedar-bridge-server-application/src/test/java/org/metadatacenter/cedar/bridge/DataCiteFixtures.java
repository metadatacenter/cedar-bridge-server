package org.metadatacenter.cedar.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.config.environment.CedarEnvironmentSource;
import org.metadatacenter.config.environment.CedarEnvironmentVariable;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Year;

/**
 * The DataCite metadata template, and the instances of it that the DOI tests convert, read from
 * {@code datacite} in the test resources.
 * <p>
 * The template is production's, as read back from it on 2026-09-26: "DataCite V4.4",
 * {@code https://repo.metadatacenter.org/templates/f2c2d19d-d94c-420e-80bd-77a9d65e5dc7}. It is
 * published, so it does not change in place.
 * <p>
 * Each document is an instance as the DOI form posts it. Two of its values depend on the run rather
 * than on the case, and are set as the document is read: the DOI prefix, which must be the one the
 * conversion is configured with, and the publication year, which must be the current year. A
 * document that leaves either value empty keeps it empty, because that is the case it tests.
 * <p>
 * A document whose name starts {@code cee-} is CEE's own output. The instance the DOI form opens
 * with for the document of the rest of the name, or for no draft at all in {@code cee-default}, was
 * loaded into CEE 2.0.26 with the template on 2026-10-19, and what CEE emitted is the document. So
 * those are what the form posts, rather than what this repository expects it to post.
 */
public final class DataCiteFixtures {

  private static final String PREFIX = "prefix";
  private static final String PUBLICATION_YEAR = "publicationYear";
  private static final String VALUE = "@value";

  private DataCiteFixtures() {
  }

  /** The DataCite metadata template as JSON. */
  public static ObjectNode templateDocument() {
    return read("datacite/template.json");
  }

  /** The DataCite metadata template, read into the model. */
  public static TemplateSchemaArtifact template() {
    return new JsonArtifactReader().readTemplateSchemaArtifact(templateDocument());
  }

  /** The named instance under the prefix this environment configures, as the live suite needs. */
  public static ObjectNode instance(String name) {
    return instance(name,
        CedarEnvironmentSource.get(CedarEnvironmentVariable.CEDAR_DATACITE_REPOSITORY_PREFIX.getName()));
  }

  /** The named instance under the given prefix. Each call reads a fresh copy. */
  public static ObjectNode instance(String name, String prefix) {
    ObjectNode instance = read("datacite/instances/" + name + ".json");
    replaceValue(instance, PREFIX, prefix);
    replaceValue(instance, PUBLICATION_YEAR, Year.now().getValue() + "-01-01");
    return instance;
  }

  private static ObjectNode read(String path) {
    try (InputStream in = DataCiteFixtures.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalArgumentException("No DataCite fixture " + path);
      }
      return (ObjectNode) JsonMapper.STRICT_MAPPER.readTree(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void replaceValue(ObjectNode instance, String field, String value) {
    JsonNode node = instance.get(field);
    if (node instanceof ObjectNode fieldValue && fieldValue.hasNonNull(VALUE)) {
      fieldValue.put(VALUE, value);
    }
  }
}
