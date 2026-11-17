package org.metadatacenter.cedar.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Map;

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
 * <p>
 * Each instance the conversion accepts has, under {@code datacite/expected}, the DataCite document it
 * must become: the {@code data} object of DataCite's REST API, with the attributes DataCite registers.
 * These were written from DataCite's JSON schema and the values each instance holds, not produced by the
 * conversion, so a value it drops, moves or alters shows as a difference. A CEE emission converts to the
 * document of the instance its form opened with. A property the instance holds no value for is left
 * out, and the four values that depend on the run are written as tokens: {@code {prefix}},
 * {@code {landingPage}}, {@code {event}} and {@code "{year}"}, which stands for the current year as a
 * number.
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

  /** The named instance under the given prefix. Each call reads a fresh copy. */
  public static ObjectNode instance(String name, String prefix) {
    ObjectNode instance = read("datacite/instances/" + name + ".json");
    replaceValue(instance, PREFIX, prefix);
    replaceValue(instance, PUBLICATION_YEAR, Year.now().getValue() + "-01-01");
    return instance;
  }

  /**
   * The DataCite document the named instance must convert to, for a DOI under the given prefix that
   * resolves to the given landing page and is registered with the given event.
   */
  public static ObjectNode expected(String name, String prefix, String landingPage, String event) {
    String document = text("datacite/expected/" + name + ".json")
        .replace("\"{year}\"", String.valueOf(Year.now().getValue()))
        .replace("{prefix}", prefix)
        .replace("{landingPage}", landingPage)
        .replace("{event}", event);
    try {
      return (ObjectNode) withoutNulls(JsonMapper.STRICT_MAPPER.readTree(document));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * The document as DataCite reads it, without the properties that hold null. The conversion names
   * every property of the attributes, null where the instance holds no value, and the expected
   * documents name only those that hold one. Every number becomes a double, so that 45 and 45.0, which DataCite reads
   * alike, compare equal however each side was parsed.
   */
  public static JsonNode withoutNulls(JsonNode document) {
    JsonNode copy = document.deepCopy();
    normalize(copy);
    return copy;
  }

  private static void normalize(JsonNode node) {
    if (node instanceof ObjectNode object) {
      object.remove(object.properties().stream().filter(property -> property.getValue().isNull())
          .map(Map.Entry::getKey).toList());
      object.properties().stream().filter(property -> property.getValue().isNumber()).toList()
          .forEach(property -> object.put(property.getKey(), property.getValue().doubleValue()));
    }
    node.forEach(DataCiteFixtures::normalize);
  }

  private static String text(String path) {
    try (InputStream in = DataCiteFixtures.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalArgumentException("No DataCite fixture " + path);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
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
