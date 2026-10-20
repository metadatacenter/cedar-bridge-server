package org.metadatacenter.cedar.bridge.resource.datacite;

import org.metadatacenter.cedar.bridge.resource.datacite.form.Attributes;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteAffiliation;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteAlternateIdentifier;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteContributor;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteCreator;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteDate;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteDescription;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteFundingReference;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteGeoLocation;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteGeoLocationBox;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteGeoLocationPoint;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteNameIdentifier;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteRelatedIdentifier;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteRelatedItem;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteRelatedItemContributor;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteRelatedItemIdentifier;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteRights;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSubject;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteTitle;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteType;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The DataCite metadata template, field by field, against the DataCite property each field fills.
 * <p>
 * Each entry identifies a child of the template by the name the template gives it, nested as the
 * template nests it. Both conversions read this table. {@link CedarInstanceParser} walks it over an
 * instance to fill DataCite's attributes, and {@link DataCiteMetadataParser} walks it over DataCite's
 * attributes to build an instance. Everything else about the template comes from the template: its
 * property IRIs, the kind of each field, and the terms a controlled field allows.
 * <p>
 * Most entries copy a value unchanged. The few that do more say what they do beside the entry.
 */
public final class DataCiteMapping {

  private static final String RESOURCE_TYPE_GENERAL = "Other";

  private DataCiteMapping() {
  }

  /** Where a field value carries the content DataCite holds. */
  public enum Part {
    /** The literal in {@code @value}. */
    VALUE,
    /** The label of a controlled term, in {@code rdfs:label}. DataCite identifies its terms by label. */
    LABEL,
    /** The IRI of a link, in {@code @id}. */
    IRI
  }

  /** A child of the template: a field or an element, once or repeated. */
  public sealed interface Entry<T> permits Value, Values, Elements, Element {
    /** The child's name in the template. */
    String field();
  }

  /** Reads the content of one field from a DataCite object. */
  @FunctionalInterface
  public interface Reader<T> {
    String read(T dataCite, Context context);
  }

  /** Writes the content of one field, read from an instance, into a DataCite object. */
  @FunctionalInterface
  public interface Writer<T> {
    void write(T dataCite, String content, Context context) throws DataCiteInstanceValidationException;
  }

  /**
   * A field that holds one value. When {@code requiredAs} is present, an instance that leaves the
   * field blank is refused, and the refusal identifies the field by that phrase.
   */
  public record Value<T>(String field, Part part, Reader<T> reader, Writer<T> writer, String requiredAs)
      implements Entry<T> {
    Value<T> required(String phrase) {
      return new Value<>(field, part, reader, writer, phrase);
    }
  }

  /** A repeated literal field, which DataCite holds as a list of strings. */
  public record Values<T>(String field, Function<T, List<String>> get, BiConsumer<T, List<String>> set)
      implements Entry<T> {
  }

  /**
   * A repeated element, which DataCite holds as a list of objects. When {@code requiredAs} is
   * present, an instance that holds no entry of it is refused, as for a field.
   */
  public record Elements<T, C>(String field, Function<T, List<C>> get, BiConsumer<T, List<C>> set,
                               Supplier<C> create, List<Entry<C>> children, String requiredAs)
      implements Entry<T> {
    Elements<T, C> required(String phrase) {
      return new Elements<>(field, get, set, create, children, phrase);
    }
  }

  /** An element that occurs once, which DataCite holds as one object. */
  public record Element<T, C>(String field, Function<T, C> get, BiConsumer<T, C> set, Supplier<C> create,
                              List<Entry<C>> children) implements Entry<T> {
  }

  /**
   * What a conversion needs beyond the document it converts. Both directions need the DOI prefix this
   * deployment registers under. The conversion to DataCite also needs the landing page and the
   * resource type of the artifact the DOI is for, and collects the problems it finds.
   */
  public record Context(String prefix, String landingPage, String resourceType, Problems problems) {
    static Context forInstance(String prefix) {
      return new Context(prefix, null, null, null);
    }
  }

  /** The problems a conversion to DataCite finds in an instance, so that one refusal lists them all. */
  public static final class Problems {
    private final Set<String> missing = new LinkedHashSet<>();
    private final List<String> longitudes = new ArrayList<>();
    private final List<String> latitudes = new ArrayList<>();

    void missing(String phrase) {
      missing.add(phrase);
    }

    void throwIfOutOfRange() throws DataCiteInstanceValidationException {
      if (longitudes.isEmpty() && latitudes.isEmpty()) {
        return;
      }
      String message = "";
      if (!longitudes.isEmpty()) {
        message = String.join(", ", longitudes) + " should be in the range of [-180, 180].\n";
      }
      if (!latitudes.isEmpty()) {
        message += " " + String.join(", ", latitudes) + " should be in the range of [-90, 90].";
      }
      throw new DataCiteInstanceValidationException(message);
    }

    void throwIfMissing() throws DataCiteInstanceValidationException {
      if (missing.isEmpty()) {
        return;
      }
      StringBuilder message = new StringBuilder("The following fields are required:\n");
      for (String phrase : missing) {
        message.append(phrase).append("\n");
      }
      throw new DataCiteInstanceValidationException(message.toString());
    }
  }

  /**
   * The template's children, nested as the template nests them. They are listed in the template's
   * order, except that a box checks east before west, which is the order its refusal names them in.
   */
  public static final List<Entry<Attributes>> TEMPLATE = List.of(
      // DOIs are minted under the prefix this deployment holds, and registering under another is not
      // CEDAR's to do. An instance is refused at once if its prefix is any other.
      new Value<>("prefix", Part.VALUE, (attributes, context) -> context.prefix(), DataCiteMapping::writePrefix,
          null),
      // The landing page is the artifact's page in OpenView, whatever URL the instance gives.
      new Value<>("url", Part.IRI, (attributes, context) -> attributes.getUrl(),
          (attributes, url, context) -> attributes.setUrl(context.landingPage()), null),
      elements("creator", Attributes::getCreators, Attributes::setCreators, DataCiteCreator::new,
          text("creatorName", DataCiteCreator::getName, DataCiteCreator::setName)
              .required("Creator Name under Creators"),
          term("nameType", DataCiteCreator::getNameType, DataCiteCreator::setNameType),
          text("givenName", DataCiteCreator::getGivenName, DataCiteCreator::setGivenName),
          text("familyName", DataCiteCreator::getFamilyName, DataCiteCreator::setFamilyName),
          affiliations("Creators", DataCiteCreator::getAffiliations, DataCiteCreator::setAffiliations),
          nameIdentifiers("Creators", DataCiteCreator::getNameIdentifiers, DataCiteCreator::setNameIdentifiers))
          .required("Creator Name under Creators"),
      elements("title", Attributes::getTitles, Attributes::setTitles, DataCiteTitle::new, titleFields("Titles"))
          .required("Title under Titles"),
      text("publisher", Attributes::getPublisher, Attributes::setPublisher).required("Publisher"),
      // The template holds a date, and DataCite the year alone. DataCite is sent only the current year.
      new Value<>("publicationYear", Part.VALUE,
          (attributes, context) -> firstOfJanuary(attributes.getPublicationYear()),
          DataCiteMapping::writePublicationYear, "Publication Year"),
      // DataCite is told the kind of artifact the DOI is for, which the instance must also state.
      new Value<>("resourceType", Part.VALUE, (attributes, context) -> resourceType(attributes.getTypes()),
          (attributes, type, context) -> attributes.setTypes(types(context.resourceType())), "Resource Type"),
      elements("subject", Attributes::getSubjects, Attributes::setSubjects, DataCiteSubject::new,
          text("subject", DataCiteSubject::getSubject, DataCiteSubject::setSubject),
          text("subjectScheme", DataCiteSubject::getSubjectScheme, DataCiteSubject::setSubjectScheme),
          link("schemeURI", DataCiteSubject::getSchemeUri, DataCiteSubject::setSchemeUri),
          link("valueURI", DataCiteSubject::getValueUri, DataCiteSubject::setValueUri),
          text("classificationCode", DataCiteSubject::getClassificationCode, DataCiteSubject::setClassificationCode)),
      elements("contributor", Attributes::getContributors, Attributes::setContributors, DataCiteContributor::new,
          text("contributorName", DataCiteContributor::getName, DataCiteContributor::setName)
              .required("Contributor Name under Contributors"),
          term("nameType", DataCiteContributor::getNameType, DataCiteContributor::setNameType),
          text("givenName", DataCiteContributor::getGivenName, DataCiteContributor::setGivenName),
          text("familyName", DataCiteContributor::getFamilyName, DataCiteContributor::setFamilyName),
          term("contributorType", DataCiteContributor::getContributorType, DataCiteContributor::setContributorType)
              .required("Contributor Type under Contributors"),
          affiliations("Contributors", DataCiteContributor::getAffiliations, DataCiteContributor::setAffiliations),
          nameIdentifiers("Contributors", DataCiteContributor::getNameIdentifiers,
              DataCiteContributor::setNameIdentifiers)),
      elements("date", Attributes::getDates, Attributes::setDates, DataCiteDate::new,
          text("date", DataCiteDate::getDate, DataCiteDate::setDate),
          term("dateType", DataCiteDate::getDateType, DataCiteDate::setDateType).required("Date Type under Date"),
          text("dateInformation", DataCiteDate::getDateInformation, DataCiteDate::setDateInformation)),
      text("language", Attributes::getLanguage, Attributes::setLanguage),
      elements("alternateIdentifier", Attributes::getAlternateIdentifiers, Attributes::setAlternateIdentifiers,
          DataCiteAlternateIdentifier::new,
          text("alternateIdentifier", DataCiteAlternateIdentifier::getAlternateIdentifier,
              DataCiteAlternateIdentifier::setAlternateIdentifier),
          text("alternateIdentifierType", DataCiteAlternateIdentifier::getAlternateIdentifierType,
              DataCiteAlternateIdentifier::setAlternateIdentifierType)
              .required("Alternate Identifier Type under Alternate Identifiers")),
      elements("relatedIdentifier", Attributes::getRelatedIdentifiers, Attributes::setRelatedIdentifiers,
          DataCiteRelatedIdentifier::new,
          text("relatedIdentifier", DataCiteRelatedIdentifier::getRelatedIdentifier,
              DataCiteRelatedIdentifier::setRelatedIdentifier),
          term("relatedIdentifierType", DataCiteRelatedIdentifier::getRelatedIdentifierType,
              DataCiteRelatedIdentifier::setRelatedIdentifierType)
              .required("Related Identifier Type under Related Identifiers"),
          term("relationType", DataCiteRelatedIdentifier::getRelationType, DataCiteRelatedIdentifier::setRelationType)
              .required("Relation Type under Related Identifiers"),
          text("relatedMetadataScheme", DataCiteRelatedIdentifier::getRelatedMetadataScheme,
              DataCiteRelatedIdentifier::setRelatedMetadataScheme),
          link("schemeURI", DataCiteRelatedIdentifier::getSchemeUri, DataCiteRelatedIdentifier::setSchemeUri),
          text("schemeType", DataCiteRelatedIdentifier::getSchemeType, DataCiteRelatedIdentifier::setSchemeType),
          term("resourceTypeGeneral", DataCiteRelatedIdentifier::getResourceTypeGeneral,
              DataCiteRelatedIdentifier::setResourceTypeGeneral)),
      texts("size", Attributes::getSizes, Attributes::setSizes),
      texts("format", Attributes::getFormats, Attributes::setFormats),
      text("version", Attributes::getVersion, Attributes::setVersion),
      elements("rights", Attributes::getRightsList, Attributes::setRightsList, DataCiteRights::new,
          text("rights", DataCiteRights::getRights, DataCiteRights::setRights),
          link("rightsURI", DataCiteRights::getRightsUri, DataCiteRights::setRightsUri),
          text("rightsIdentifier", DataCiteRights::getRightsIdentifier, DataCiteRights::setRightsIdentifier),
          text("rightsIdentifierScheme", DataCiteRights::getRightsIdentifierScheme,
              DataCiteRights::setRightsIdentifierScheme),
          link("schemeURI", DataCiteRights::getSchemeUri, DataCiteRights::setSchemeUri)),
      elements("description", Attributes::getDescriptions, Attributes::setDescriptions, DataCiteDescription::new,
          text("description", DataCiteDescription::getDescription, DataCiteDescription::setDescription),
          term("descriptionType", DataCiteDescription::getDescriptionType, DataCiteDescription::setDescriptionType)
              .required("Description Type under Descriptions")),
      // A point or a box with any coordinate needs all of its coordinates, and each must be in range.
      elements("geoLocation", Attributes::getGeoLocations, Attributes::setGeoLocations, DataCiteGeoLocation::new,
          text("geoLocationPlace", DataCiteGeoLocation::getGeoLocationPlace, DataCiteGeoLocation::setGeoLocationPlace),
          element("geoLocationPoint", DataCiteGeoLocation::getGeoLocationPoint,
              DataCiteGeoLocation::setGeoLocationPoint, DataCiteGeoLocationPoint::new,
              longitude("pointLongitude", "Point Longitude", DataCiteGeoLocationPoint::getPointLongitude,
                  DataCiteGeoLocationPoint::setPointLongitude),
              latitude("pointLatitude", "Point Latitude", DataCiteGeoLocationPoint::getPointLatitude,
                  DataCiteGeoLocationPoint::setPointLatitude)),
          element("geoLocationBox", DataCiteGeoLocation::getGeoLocationBox, DataCiteGeoLocation::setGeoLocationBox,
              DataCiteGeoLocationBox::new,
              longitude("eastBoundLongitude", "East Bound Longitude", DataCiteGeoLocationBox::getEastBoundLongitude,
                  DataCiteGeoLocationBox::setEastBoundLongitude),
              longitude("westBoundLongitude", "West Bound Longitude", DataCiteGeoLocationBox::getWestBoundLongitude,
                  DataCiteGeoLocationBox::setWestBoundLongitude),
              latitude("southBoundLatitude", "South Bound Latitude", DataCiteGeoLocationBox::getSouthBoundLatitude,
                  DataCiteGeoLocationBox::setSouthBoundLatitude),
              latitude("northBoundLatitude", "North Bound Latitude", DataCiteGeoLocationBox::getNorthBoundLatitude,
                  DataCiteGeoLocationBox::setNorthBoundLatitude))),
      elements("fundingReference", Attributes::getFundingReferences, Attributes::setFundingReferences,
          DataCiteFundingReference::new,
          text("funderName", DataCiteFundingReference::getFunderName, DataCiteFundingReference::setFunderName)
              .required("Funder Name under Funding Reference"),
          text("funderIdentifier", DataCiteFundingReference::getFunderIdentifier,
              DataCiteFundingReference::setFunderIdentifier),
          term("funderIdentifierType", DataCiteFundingReference::getFunderIdentifierType,
              DataCiteFundingReference::setFunderIdentifierType),
          link("schemeURI", DataCiteFundingReference::getSchemeUri, DataCiteFundingReference::setSchemeUri),
          text("awardNumber", DataCiteFundingReference::getAwardNumber, DataCiteFundingReference::setAwardNumber),
          link("awardURI", DataCiteFundingReference::getAwardUri, DataCiteFundingReference::setAwardUri),
          text("awardTitle", DataCiteFundingReference::getAwardTitle, DataCiteFundingReference::setAwardTitle)),
      elements("relatedItem", Attributes::getRelatedItems, Attributes::setRelatedItems, DataCiteRelatedItem::new,
          term("relatedItemType", DataCiteRelatedItem::getRelatedItemType, DataCiteRelatedItem::setRelatedItemType)
              .required("Related Item Type under Related Items"),
          term("relationType", DataCiteRelatedItem::getRelationType, DataCiteRelatedItem::setRelationType)
              .required("Relation Type under Related Items"),
          // DataCite holds the related item's identifier in an object of its own.
          text("relatedIdentifier", (DataCiteRelatedItem item) -> identifier(item).getRelatedItemIdentifier(),
              (DataCiteRelatedItem item, String value) -> identifier(item).setRelatedItemIdentifier(value)),
          term("relatedIdentifierType", (DataCiteRelatedItem item) -> identifier(item).getRelatedItemIdentifierType(),
              (DataCiteRelatedItem item, String value) -> identifier(item).setRelatedItemIdentifierType(value)),
          text("relatedMetadataScheme", (DataCiteRelatedItem item) -> identifier(item).getRelatedMetadataScheme(),
              (DataCiteRelatedItem item, String value) -> identifier(item).setRelatedMetadataScheme(value)),
          link("schemeURI", (DataCiteRelatedItem item) -> identifier(item).getSchemeUri(),
              (DataCiteRelatedItem item, String value) -> identifier(item).setSchemeUri(value)),
          text("schemeType", (DataCiteRelatedItem item) -> identifier(item).getSchemeType(),
              (DataCiteRelatedItem item, String value) -> identifier(item).setSchemeType(value)),
          elements("title", DataCiteRelatedItem::getTitles, DataCiteRelatedItem::setTitles, DataCiteTitle::new,
              titleFields("Related Items"))
              .required("Title under Related Items"),
          elements("relatedItemCreator", DataCiteRelatedItem::getCreators, DataCiteRelatedItem::setCreators,
              DataCiteCreator::new,
              text("creatorName", DataCiteCreator::getName, DataCiteCreator::setName)
                  .required("Creator Name under Related Items"),
              term("nameType", DataCiteCreator::getNameType, DataCiteCreator::setNameType),
              text("givenName", DataCiteCreator::getGivenName, DataCiteCreator::setGivenName),
              text("familyName", DataCiteCreator::getFamilyName, DataCiteCreator::setFamilyName)),
          text("number", DataCiteRelatedItem::getNumber, DataCiteRelatedItem::setNumber),
          term("numberType", DataCiteRelatedItem::getNumberType, DataCiteRelatedItem::setNumberType),
          text("volume", DataCiteRelatedItem::getVolume, DataCiteRelatedItem::setVolume),
          text("issue", DataCiteRelatedItem::getIssue, DataCiteRelatedItem::setIssue),
          text("firstPage", DataCiteRelatedItem::getFirstPage, DataCiteRelatedItem::setFirstPage),
          text("lastPage", DataCiteRelatedItem::getLastPage, DataCiteRelatedItem::setLastPage),
          // As for the resource itself, the template holds a date and DataCite the year alone.
          new Value<DataCiteRelatedItem>("publicationYear", Part.VALUE,
              (item, context) -> firstOfJanuary(item.getPublicationYear()),
              (item, date, context) -> item.setPublicationYear(yearOf(date)), null),
          text("publisher", DataCiteRelatedItem::getPublisher, DataCiteRelatedItem::setPublisher),
          text("edition", DataCiteRelatedItem::getEdition, DataCiteRelatedItem::setEdition),
          elements("relatedItemContributor", DataCiteRelatedItem::getContributors,
              DataCiteRelatedItem::setContributors, DataCiteRelatedItemContributor::new,
              term("contributorType", DataCiteRelatedItemContributor::getContributorType,
                  DataCiteRelatedItemContributor::setContributorType)
                  .required("Contributor Type under Related Items"),
              text("contributorName", DataCiteRelatedItemContributor::getName,
                  DataCiteRelatedItemContributor::setName)
                  .required("Contributor Name under Related Items"),
              term("nameType", DataCiteRelatedItemContributor::getNameType,
                  DataCiteRelatedItemContributor::setNameType),
              text("givenName", DataCiteRelatedItemContributor::getGivenName,
                  DataCiteRelatedItemContributor::setGivenName),
              text("familyName", DataCiteRelatedItemContributor::getFamilyName,
                  DataCiteRelatedItemContributor::setFamilyName))));

  // The elements that occur in more than one place

  private static <T> Elements<T, DataCiteAffiliation> affiliations(
      String where, Function<T, List<DataCiteAffiliation>> get, BiConsumer<T, List<DataCiteAffiliation>> set) {
    return elements("affiliation", get, set, DataCiteAffiliation::new,
        text("name", DataCiteAffiliation::getName, DataCiteAffiliation::setName),
        text("affiliationIdentifier", DataCiteAffiliation::getAffiliationIdentifier,
            DataCiteAffiliation::setAffiliationIdentifier),
        text("affiliationIdentifierScheme", DataCiteAffiliation::getAffiliationIdentifierScheme,
            DataCiteAffiliation::setAffiliationIdentifierScheme)
            .required("Affiliation Identifier Scheme under " + where),
        link("schemeURI", DataCiteAffiliation::getAffiliationSchemeURI, DataCiteAffiliation::setAffiliationSchemeURI));
  }

  private static <T> Elements<T, DataCiteNameIdentifier> nameIdentifiers(
      String where, Function<T, List<DataCiteNameIdentifier>> get, BiConsumer<T, List<DataCiteNameIdentifier>> set) {
    return elements("nameIdentifier", get, set, DataCiteNameIdentifier::new,
        text("name", DataCiteNameIdentifier::getNameIdentifier, DataCiteNameIdentifier::setNameIdentifier),
        text("nameIdentifierScheme", DataCiteNameIdentifier::getNameIdentifierScheme,
            DataCiteNameIdentifier::setNameIdentifierScheme)
            .required("Name Identifier Scheme under " + where),
        link("schemeURI", DataCiteNameIdentifier::getSchemeUri, DataCiteNameIdentifier::setSchemeUri));
  }

  private static List<Entry<DataCiteTitle>> titleFields(String where) {
    return List.of(
        text("title", DataCiteTitle::getTitle, DataCiteTitle::setTitle).required("Title under " + where),
        term("titleType", DataCiteTitle::getTitleType, DataCiteTitle::setTitleType));
  }

  // The rules that do more than copy a value

  private static void writePrefix(Attributes attributes, String prefix, Context context)
      throws DataCiteInstanceValidationException {
    if (prefix == null) {
      throw new DataCiteInstanceValidationException(
          "The 'Prefix of DOI' is required, please provide your valid prefix");
    }
    if (!prefix.equals(context.prefix())) {
      throw new DataCiteInstanceValidationException(
          "The 'Prefix of DOI' is incorrect, please provide your valid prefix");
    }
    attributes.setPrefix(prefix);
  }

  private static void writePublicationYear(Attributes attributes, String date, Context context)
      throws DataCiteInstanceValidationException {
    if (date == null || date.isEmpty()) {
      return;
    }
    String currentYear = String.valueOf(Year.now().getValue());
    if (!date.startsWith(currentYear)) {
      throw new DataCiteInstanceValidationException(
          "The 'Publication Year' should be the current year: " + currentYear);
    }
    attributes.setPublicationYear(Integer.parseInt(currentYear));
  }

  /** The date the template holds for a year DataCite holds. DataCite's attributes give 0 for no year. */
  private static String firstOfJanuary(Integer year) {
    return year == null || year == 0 ? null : String.format(Locale.ROOT, "%04d-01-01", year);
  }

  private static Integer yearOf(String date) {
    return date == null || date.isEmpty() ? null : Integer.parseInt(date.substring(0, 4));
  }

  private static String resourceType(DataCiteType types) {
    return types == null ? null : types.getResourceType();
  }

  private static DataCiteType types(String resourceType) {
    DataCiteType types = new DataCiteType();
    types.setResourceTypeGeneral(RESOURCE_TYPE_GENERAL);
    types.setResourceType(resourceType);
    return types;
  }

  private static DataCiteRelatedItemIdentifier identifier(DataCiteRelatedItem item) {
    if (item.getRelatedItemIdentifier() == null) {
      item.setRelatedItemIdentifier(new DataCiteRelatedItemIdentifier());
    }
    return item.getRelatedItemIdentifier();
  }

  private static <T> Value<T> longitude(String field, String name, Function<T, Float> get,
                                        BiConsumer<T, Float> set) {
    return coordinate(field, name, get, set, true);
  }

  private static <T> Value<T> latitude(String field, String name, Function<T, Float> get,
                                       BiConsumer<T, Float> set) {
    return coordinate(field, name, get, set, false);
  }

  private static <T> Value<T> coordinate(String field, String name, Function<T, Float> get,
                                         BiConsumer<T, Float> set, boolean isLongitude) {
    return new Value<>(field, Part.VALUE,
        (dataCite, context) -> get.apply(dataCite) == null ? null : String.valueOf(get.apply(dataCite)),
        (dataCite, content, context) -> {
          if (content == null || content.isEmpty()) {
            return;
          }
          boolean outOfRange = isLongitude
              ? CheckValueRange.longitudeOutOfBound(content)
              : CheckValueRange.latitudeOutOfBound(content);
          if (outOfRange) {
            (isLongitude ? context.problems().longitudes : context.problems().latitudes).add(name);
          } else {
            set.accept(dataCite, Float.valueOf(content));
          }
        },
        name + " under Geographic Locations");
  }

  // The plain entries

  private static <T> Value<T> text(String field, Function<T, String> get, BiConsumer<T, String> set) {
    return copy(field, Part.VALUE, get, set);
  }

  private static <T> Value<T> term(String field, Function<T, String> get, BiConsumer<T, String> set) {
    return copy(field, Part.LABEL, get, set);
  }

  private static <T> Value<T> link(String field, Function<T, String> get, BiConsumer<T, String> set) {
    return copy(field, Part.IRI, get, set);
  }

  private static <T> Value<T> copy(String field, Part part, Function<T, String> get, BiConsumer<T, String> set) {
    return new Value<>(field, part, (dataCite, context) -> get.apply(dataCite),
        (dataCite, content, context) -> set.accept(dataCite, content), null);
  }

  private static <T> Values<T> texts(String field, Function<T, List<String>> get, BiConsumer<T, List<String>> set) {
    return new Values<>(field, get, set);
  }

  @SafeVarargs
  private static <T, C> Elements<T, C> elements(String field, Function<T, List<C>> get, BiConsumer<T, List<C>> set,
                                                Supplier<C> create, Entry<C>... children) {
    return elements(field, get, set, create, List.of(children));
  }

  private static <T, C> Elements<T, C> elements(String field, Function<T, List<C>> get, BiConsumer<T, List<C>> set,
                                                Supplier<C> create, List<Entry<C>> children) {
    return new Elements<>(field, get, set, create, children, null);
  }

  @SafeVarargs
  private static <T, C> Element<T, C> element(String field, Function<T, C> get, BiConsumer<T, C> set,
                                              Supplier<C> create, Entry<C>... children) {
    return new Element<>(field, get, set, create, List.of(children));
  }
}
