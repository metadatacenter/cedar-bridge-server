// Generated code.  Do not edit by hand.
package org.metadatacenter.cedar.bridge.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class Cedar {

    public static final String IRI_PREFIX = "https://repo.metadatacenter.org/template-element-instances/";

    public interface FieldNames {
        String prefix = "prefix";
        String url = "url";
        String creator = "creator";
        String creatorName = "creatorName";
        String nameType = "nameType";
        String givenName = "givenName";
        String familyName = "familyName";
        String affiliation = "affiliation";
        String name = "name";
        String affiliationIdentifier = "affiliationIdentifier";
        String affiliationIdentifierScheme = "affiliationIdentifierScheme";
        String schemeURI = "schemeURI";
        String nameIdentifier = "nameIdentifier";
        String nameIdentifierScheme = "nameIdentifierScheme";
        String title = "title";
        String titleType = "titleType";
        String publicationYear = "publicationYear";
        String publisher = "publisher";
        String resourceType = "resourceType";
        String subject = "subject";
        String subjectScheme = "subjectScheme";
        String valueURI = "valueURI";
        String classificationCode = "classificationCode";
        String contributor = "contributor";
        String contributorName = "contributorName";
        String contributorType = "contributorType";
        String date = "date";
        String dateType = "dateType";
        String dateInformation = "dateInformation";
        String language = "language";
        String alternateIdentifier = "alternateIdentifier";
        String alternateIdentifierType = "alternateIdentifierType";
        String relatedIdentifier = "relatedIdentifier";
        String relatedIdentifierType = "relatedIdentifierType";
        String relationType = "relationType";
        String relatedMetadataScheme = "relatedMetadataScheme";
        String schemeType = "schemeType";
        String resourceTypeGeneral = "resourceTypeGeneral";
        String size = "size";
        String format = "format";
        String version = "version";
        String rights = "rights";
        String rightsURI = "rightsURI";
        String rightsIdentifier = "rightsIdentifier";
        String rightsIdentifierScheme = "rightsIdentifierScheme";
        String description = "description";
        String descriptionType = "descriptionType";
        String geoLocation = "geoLocation";
        String geoLocationPlace = "geoLocationPlace";
        String geoLocationPoint = "geoLocationPoint";
        String pointLongitude = "pointLongitude";
        String pointLatitude = "pointLatitude";
        String geoLocationBox = "geoLocationBox";
        String westBoundLongitude = "westBoundLongitude";
        String eastBoundLongitude = "eastBoundLongitude";
        String southBoundLatitude = "southBoundLatitude";
        String northBoundLatitude = "northBoundLatitude";
        String fundingReference = "fundingReference";
        String funderName = "funderName";
        String funderIdentifier = "funderIdentifier";
        String funderIdentifierType = "funderIdentifierType";
        String awardNumber = "awardNumber";
        String awardURI = "awardURI";
        String awardTitle = "awardTitle";
        String relatedItem = "relatedItem";
        String relatedItemType = "relatedItemType";
        String relatedItemCreator = "relatedItemCreator";
        String number = "number";
        String numberType = "numberType";
        String volume = "volume";
        String issue = "issue";
        String firstPage = "firstPage";
        String lastPage = "lastPage";
        String edition = "edition";
        String relatedItemContributor = "relatedItemContributor";
    }

    public interface InstanceNode {
        @JsonIgnore
        boolean isEmpty();
    }

    public interface Artifact extends InstanceNode {
    }

    public interface Field extends Artifact {
    }

    public interface Element extends Artifact {
        @Override
        default boolean isEmpty() {
            return getArtifacts().allMatch(Artifact::isEmpty);
        }

        @JsonProperty("@id")
        String id();

        @JsonIgnore
        Stream<Artifact> getArtifacts();
    }

    public interface ArtifactList extends InstanceNode {
        @JsonValue
        List<Artifact> getArtifacts();

        default boolean isEmpty() {
            return getArtifacts().stream().allMatch(Artifact::isEmpty);
        }
    }

    public interface Compactable {
        String compact();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public interface LiteralField extends Field, Compactable {
        @JsonProperty("@value")
        String value();

        default String compact() {
            return this.value();
        }

        public static LiteralField of(java.lang.String value) {
            return new LiteralFieldImpl(value);
        }

        @JsonIgnore
        default boolean isEmpty() {
            return value() == null;
        }
    }

    public interface IriField extends Field, Compactable {
        @JsonProperty("@id")
        String id();

        @JsonProperty("rdfs:label")
        String label();

        default String compact() {
            return this.id();
        }

        @JsonIgnore
        default boolean isEmpty() {
            return id() == null;
        }
    }

    public record LiteralFieldImpl(
        @JsonProperty("@value") String value) implements LiteralField, Map<String, String> {

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return "@value".equals(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.value.equals(value);
        }

        @Override
        public String get(Object key) {
            return value;
        }

        @Override
        public String put(String key, String value) {
            return value;
        }

        @Override
        public String remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
        }

        @Override
        public void clear() {
        }

        @Override
        public Set<String> keySet() {
            return Collections.singleton("@value");
        }

        @Override
        public Collection<String> values() {
            return Collections.singleton(value);
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return Collections.singleton(new Entry<String, String>() {
                @Override
                public String getKey() {
                    return "@value";
                }

                @Override
                public String getValue() {
                    return value;
                }

                @Override
                public String setValue(String value) {
                    return null;
                }
            });
        }
    }

    public interface CoreView {
    }

    public static String generateId() {
        return IRI_PREFIX + UUID.randomUUID();
    }

    private static Stream<Artifact> streamArtifacts(Object... in) {
        return Arrays.stream(in).flatMap(o -> {
            if (o instanceof List l) {
                return l.stream();
            } else if (o instanceof Artifact) {
                return Stream.of(o);
            } else {
                return Stream.empty();
            }
        }).filter(o -> o instanceof Artifact).map(o -> (Artifact) o);
    }

    public record MetadataInstance(@JsonProperty("@id") String id,
                                   @JsonView(CoreView.class) @JsonProperty("schema:name") String schemaName,
                                   @JsonView(CoreView.class) @JsonProperty("schema:description") String schemaDescription,
                                   @JsonView(CoreView.class) @JsonProperty("schema:isBasedOn") String isBasedOn,
                                   @JsonView(CoreView.class) @JsonProperty("pav:createdOn") Instant pavCreatedOn,
                                   @JsonView(CoreView.class) @JsonProperty("pav:createdBy") String pavCreatedBy,
                                   @JsonView(CoreView.class) @JsonProperty("pav:lastUpdatedOn") Instant pavLastUpdatedOn,
                                   @JsonView(CoreView.class) @JsonProperty("oslc:modifiedBy") String oslcModifiedBy,
                                   @JsonView(CoreView.class) @JsonProperty("pav:derivedFrom") String pavDerivedFrom,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.prefix) PrefixField prefix,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.url) UrlField url,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.creator) CreatorElementList creator,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.title) TitleElementList title,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.publicationYear) PublicationYearField publicationYear,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.publisher) PublisherField publisher,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.resourceType) ResourceTypeField resourceType,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.subject) SubjectElementList subject,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.contributor) ContributorElementList contributor,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.date) DateElementList date,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.language) LanguageField language,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.alternateIdentifier) AlternateIdentifierElementList alternateIdentifier,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedIdentifier) RelatedIdentifierElementList relatedIdentifier,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.size) SizeFieldList size,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.format) FormatFieldList format,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.version) VersionField version,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.rights) RightsElementList rights,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.description) DescriptionElementList description,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.geoLocation) GeoLocationElementList geoLocation,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.fundingReference) FundingReferenceElementList fundingReference,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedItem) RelatedItemElementList relatedItem)
        implements
        Element {

        /**
         * Gets an empty MetadataInstance list.
         */
        public static MetadataInstance of() {
            return new MetadataInstance(generateId(), null, null, null, null, null, null, null, null, PrefixField.of(),
                UrlField.of(), CreatorElementList.of(), TitleElementList.of(), PublicationYearField.of(),
                PublisherField.of(), ResourceTypeField.of(), SubjectElementList.of(), ContributorElementList.of(),
                DateElementList.of(), LanguageField.of(), AlternateIdentifierElementList.of(),
                RelatedIdentifierElementList.of(), SizeFieldList.of(), FormatFieldList.of(), VersionField.of(),
                RightsElementList.of(), DescriptionElementList.of(), GeoLocationElementList.of(),
                FundingReferenceElementList.of(), RelatedItemElementList.of());
        }

        /**
         * Returns the child artifacts as a flat stream. Lists of children are flattened
         * out.
         */
        @JsonIgnore
        public Stream<Artifact> getArtifacts() {
            return streamArtifacts(prefix, url, creator, title, publicationYear, publisher, resourceType, subject,
                contributor, date, language, alternateIdentifier, relatedIdentifier, size, format, version, rights,
                description, geoLocation, fundingReference, relatedItem);
        }

        /**
         * Gets the JSON-LD context for this element. This is a fixed value and does not
         * depend upon the content of child elements/fields.
         */
        @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
        public Map<String, Object> context() {
            var contextMap = new LinkedHashMap<String, Object>();
            contextMap.put(FieldNames.prefix,
                "https://schema.metadatacenter.org/properties/c5c19810-b058-48cf-b0ae-de2e9f82a95e");
            contextMap.put(FieldNames.url,
                "https://schema.metadatacenter.org/properties/b9751716-653d-4e28-a0dc-ae0db993db01");
            contextMap.put(FieldNames.creator,
                "https://schema.metadatacenter.org/properties/cc36ca34-bc4d-4afc-9eb8-06512f052086");
            contextMap.put(FieldNames.title,
                "https://schema.metadatacenter.org/properties/4fffaeb2-416c-45b9-87d2-b6c1455ec896");
            contextMap.put(FieldNames.publicationYear,
                "https://schema.metadatacenter.org/properties/7fe11cba-9053-42b6-aa8e-3ed25e951600");
            contextMap.put(FieldNames.publisher,
                "https://schema.metadatacenter.org/properties/0bc8215e-6b07-42c0-9613-9209f5393b08");
            contextMap.put(FieldNames.resourceType,
                "https://schema.metadatacenter.org/properties/db8a76bc-f148-4cbf-b577-cd2dd4f3bbfe");
            contextMap.put(FieldNames.subject,
                "https://schema.metadatacenter.org/properties/bb0781d6-ef8c-4760-a5e9-00eaf9d16c63");
            contextMap.put(FieldNames.contributor,
                "https://schema.metadatacenter.org/properties/dc039c7c-04d1-4fa1-bf93-9edc5a817d6e");
            contextMap.put(FieldNames.date,
                "https://schema.metadatacenter.org/properties/ead63a8f-4995-4f78-84b7-696c03489966");
            contextMap.put(FieldNames.language,
                "https://schema.metadatacenter.org/properties/7dd35493-fdfb-4c50-8d06-7844d4df3039");
            contextMap.put(FieldNames.alternateIdentifier,
                "https://schema.metadatacenter.org/properties/d18a391b-c0af-47da-b188-afe5c97e50ab");
            contextMap.put(FieldNames.relatedIdentifier,
                "https://schema.metadatacenter.org/properties/eab1d09a-4235-4863-bbd5-4f43d273da85");
            contextMap.put(FieldNames.size,
                "https://schema.metadatacenter.org/properties/9d4bb27e-e036-4e90-8c00-3629943f1d7b");
            contextMap.put(FieldNames.format,
                "https://schema.metadatacenter.org/properties/77e2965e-26d7-4a57-a9dc-6f98b59eefab");
            contextMap.put(FieldNames.version,
                "https://schema.metadatacenter.org/properties/c19a0cb5-a3e0-4deb-abf8-c3042dedefa4");
            contextMap.put(FieldNames.rights,
                "https://schema.metadatacenter.org/properties/10ff1a36-73b8-4364-9edb-c30f8de4c7fe");
            contextMap.put(FieldNames.description,
                "https://schema.metadatacenter.org/properties/4eec3604-47d3-4d19-b976-459945630c4a");
            contextMap.put(FieldNames.geoLocation,
                "https://schema.metadatacenter.org/properties/0477e239-366c-4ba2-8f55-fe4611fbf58b");
            contextMap.put(FieldNames.fundingReference,
                "https://schema.metadatacenter.org/properties/18bca250-d1fb-48e2-ab7b-915e43d80bcb");
            contextMap.put(FieldNames.relatedItem,
                "https://schema.metadatacenter.org/properties/9454a8c9-9e97-4707-a816-76bafa70bacc");
            contextMap.put("schema", "http://schema.org/");
            contextMap.put("xsd", "http://www.w3.org/2001/XMLSchema#");
            contextMap.put("skos", "http://www.w3.org/2004/02/skos/core#");
            contextMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            contextMap.put("pav:createdOn", Map.of("@type", "xsd:dateTime"));
            contextMap.put("pav:createdBy", Map.of("@type", "@id"));
            contextMap.put("rdfs:label", Map.of("@type", "xsd:string"));
            contextMap.put("oslc:modifiedBy", Map.of("@type", "@id"));
            contextMap.put("pav:derivedFrom", Map.of("@type", "@id"));
            contextMap.put("skos:notation", Map.of("@type", "xsd:string"));
            contextMap.put("schema:isBasedOn", Map.of("@type", "@id"));
            contextMap.put("schema:description", Map.of("@type", "xsd:string"));
            contextMap.put("pav:lastUpdatedOn", Map.of("@type", "xsd:dateTime"));
            contextMap.put("schema:name", Map.of("@type", "xsd:string"));
            return contextMap;
        }
        /**
         */
        public record PrefixField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  PrefixField} record with a {@code  null}
             * value.
             *
             * @return An instance of the {@code  PrefixField} record.
             */
            public static PrefixField of() {
                return new PrefixField(null);
            }

            /**
             * Creates an instance of the {@code  PrefixField} record with the specified
             * value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  PrefixField} record.
             */
            @JsonCreator
            public static PrefixField of(@JsonProperty("@value") String value) {
                return new PrefixField(value);
            }
        }

        /**
         * The location of the landing page with more information about the resource.
         */
        public record UrlField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  UrlField} record with a {@code  null}
             * value.
             *
             * @return An instance of the {@code  UrlField} record.
             */
            public static UrlField of() {
                return new UrlField(null);
            }

            /**
             * Creates an instance of the {@code  UrlField} record with the specified value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  UrlField} record.
             */
            @JsonCreator
            public static UrlField of(@JsonProperty("@value") String value) {
                return new UrlField(value);
            }
        }

        public record CreatorElement(@JsonProperty("@id") String id,
                                     @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.creatorName) CreatorNameField creatorName,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameType) NameTypeField nameType,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.givenName) GivenNameField givenName,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.familyName) FamilyNameField familyName,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.affiliation) AffiliationElementList affiliation,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameIdentifier) NameIdentifierElementList nameIdentifier)
            implements
            Element {

            /**
             * Gets an empty CreatorElement list.
             */
            public static CreatorElement of() {
                return new CreatorElement(generateId(), CreatorNameField.of(), NameTypeField.of(), GivenNameField.of(),
                    FamilyNameField.of(), AffiliationElementList.of(), NameIdentifierElementList.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(creatorName, nameType, givenName, familyName, affiliation, nameIdentifier);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.creatorName,
                    "https://schema.metadatacenter.org/properties/8acde937-9aac-4830-901d-18827533d9ee");
                contextMap.put(FieldNames.nameType,
                    "https://schema.metadatacenter.org/properties/c5257a64-69a4-4262-93ff-b7624fd00900");
                contextMap.put(FieldNames.givenName,
                    "https://schema.metadatacenter.org/properties/6f29b11a-2c3e-4ebd-92f5-8b6ca863c484");
                contextMap.put(FieldNames.familyName,
                    "https://schema.metadatacenter.org/properties/8a8c322b-7f15-4a7a-b687-ad969e71defe");
                contextMap.put(FieldNames.affiliation,
                    "https://schema.metadatacenter.org/properties/2a2341d0-4936-4dce-bdf4-deb6a6544fa2");
                contextMap.put(FieldNames.nameIdentifier,
                    "https://schema.metadatacenter.org/properties/54d8ce77-6c50-4e77-b2b7-8bd33c846d1d");
                return contextMap;
            }
            /**
             * May be a corporate/institutional or personal name.
             */
            public record CreatorNameField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  CreatorNameField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  CreatorNameField} record.
                 */
                public static CreatorNameField of() {
                    return new CreatorNameField(null);
                }

                /**
                 * Creates an instance of the {@code  CreatorNameField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  CreatorNameField} record.
                 */
                @JsonCreator
                public static CreatorNameField of(@JsonProperty("@value") String value) {
                    return new CreatorNameField(value);
                }
            }

            /**
             * The type of name: Personal or Organizational
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record NameTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty NameTypeField instance, with null values for the id and
                 * label.
                 */
                public static NameTypeField of() {
                    return new NameTypeField(null, null);
                }

                /**
                 * Create an instance of NameTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static NameTypeField of(@JsonProperty("@id") String id,
                                               @JsonProperty("rdfs:label") String label) {
                    return new NameTypeField(id, label);
                }
            }

            /**
             * The personal or first name of the creator
             */
            public record GivenNameField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  GivenNameField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  GivenNameField} record.
                 */
                public static GivenNameField of() {
                    return new GivenNameField(null);
                }

                /**
                 * Creates an instance of the {@code  GivenNameField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  GivenNameField} record.
                 */
                @JsonCreator
                public static GivenNameField of(@JsonProperty("@value") String value) {
                    return new GivenNameField(value);
                }
            }

            /**
             * The surname or last name of the creator
             */
            public record FamilyNameField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  FamilyNameField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  FamilyNameField} record.
                 */
                public static FamilyNameField of() {
                    return new FamilyNameField(null);
                }

                /**
                 * Creates an instance of the {@code  FamilyNameField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  FamilyNameField} record.
                 */
                @JsonCreator
                public static FamilyNameField of(@JsonProperty("@value") String value) {
                    return new FamilyNameField(value);
                }
            }

            public record AffiliationElement(@JsonProperty("@id") String id,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.name) NameField name,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.affiliationIdentifier) AffiliationIdentifierField affiliationIdentifier,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.affiliationIdentifierScheme) AffiliationIdentifierSchemeField affiliationIdentifierScheme,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri)
                implements
                Element {

                /**
                 * Gets an empty AffiliationElement list.
                 */
                public static AffiliationElement of() {
                    return new AffiliationElement(generateId(), NameField.of(), AffiliationIdentifierField.of(),
                        AffiliationIdentifierSchemeField.of(), SchemeUriField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(name, affiliationIdentifier, affiliationIdentifierScheme, schemeUri);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.name,
                        "https://schema.metadatacenter.org/properties/b316245d-7a0b-4f1c-b6a2-8f08f1f3da1b");
                    contextMap.put(FieldNames.affiliationIdentifier,
                        "https://schema.metadatacenter.org/properties/1c405b8f-a36a-4b35-a7e5-52ad7828f5a4");
                    contextMap.put(FieldNames.affiliationIdentifierScheme,
                        "https://schema.metadatacenter.org/properties/ac3137bc-0bd2-4e50-ad4e-6592bf71e681");
                    contextMap.put(FieldNames.schemeURI,
                        "https://schema.metadatacenter.org/properties/3b1e62d4-f1b6-4874-b36e-d7740a7f7b05");
                    return contextMap;
                }
                /**
                 * The organizational or institutional affiliation of the creator
                 */
                public record NameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  NameField} record with a {@code  null}
                     * value.
                     *
                     * @return An instance of the {@code  NameField} record.
                     */
                    public static NameField of() {
                        return new NameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  NameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  NameField} record.
                     */
                    @JsonCreator
                    public static NameField of(@JsonProperty("@value") String value) {
                        return new NameField(value);
                    }
                }

                /**
                 * Uniquely identifies the organizational affiliation of the creator.
                 */
                public record AffiliationIdentifierField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  AffiliationIdentifierField} record.
                     */
                    public static AffiliationIdentifierField of() {
                        return new AffiliationIdentifierField(null);
                    }

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierField} record with
                     * the specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  AffiliationIdentifierField} record.
                     */
                    @JsonCreator
                    public static AffiliationIdentifierField of(@JsonProperty("@value") String value) {
                        return new AffiliationIdentifierField(value);
                    }
                }

                /**
                 * If Affiliation Identifier is used, Affiliation Identifier Scheme is
                 * mandatory.
                 */
                public record AffiliationIdentifierSchemeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierSchemeField} record
                     * with a {@code  null} value.
                     *
                     * @return An instance of the {@code  AffiliationIdentifierSchemeField} record.
                     */
                    public static AffiliationIdentifierSchemeField of() {
                        return new AffiliationIdentifierSchemeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierSchemeField} record
                     * with the specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  AffiliationIdentifierSchemeField} record.
                     */
                    @JsonCreator
                    public static AffiliationIdentifierSchemeField of(@JsonProperty("@value") String value) {
                        return new AffiliationIdentifierSchemeField(value);
                    }
                }

                /**
                 * The URI of the affiliation identifier scheme
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record SchemeUriField(String id) implements IriField {

                    /**
                     * Creates an empty SchemeUriField instance, with null values for the id and
                     * label.
                     */
                    public static SchemeUriField of() {
                        return new SchemeUriField(null);
                    }

                    /**
                     * Create an instance of SchemeUriField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     */
                    @JsonCreator
                    public static SchemeUriField of(@JsonProperty("@id") String id) {
                        return new SchemeUriField(id);
                    }

                    @Override
                    public String label() {
                        return "";
                    }
                }
            }

            public record AffiliationElementList(List<AffiliationElement> affiliationList) implements ArtifactList {

                public static AffiliationElementList of() {
                    return new AffiliationElementList(List.of());
                }

                public int minItems() {
                    return 0;
                }

                public int maxItems() {
                    return Integer.MAX_VALUE;
                }

                public boolean isCardinalitySatisfied() {
                    return affiliationList.size() >= minItems() && affiliationList.size() <= maxItems();
                }

                @JsonCreator
                public static AffiliationElementList of(List<AffiliationElement> affiliationList) {
                    return new AffiliationElementList(affiliationList);
                }

                public static AffiliationElementList of(AffiliationElement affiliation) {
                    return new AffiliationElementList(List.of(affiliation));
                }

                @Override
                public List<Artifact> getArtifacts() {
                    return new ArrayList<>(affiliationList);
                }
            }

            public record NameIdentifierElement(@JsonProperty("@id") String id,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.name) NameField name,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameIdentifierScheme) NameIdentifierSchemeField nameIdentifierScheme,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri)
                implements
                Element {

                /**
                 * Gets an empty NameIdentifierElement list.
                 */
                public static NameIdentifierElement of() {
                    return new NameIdentifierElement(generateId(), NameField.of(), NameIdentifierSchemeField.of(),
                        SchemeUriField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(name, nameIdentifierScheme, schemeUri);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.name,
                        "https://schema.metadatacenter.org/properties/0fd96dfa-0d23-4075-a6ca-6b12205e437e");
                    contextMap.put(FieldNames.nameIdentifierScheme,
                        "https://schema.metadatacenter.org/properties/ba910380-8046-4298-94bb-27a7f76d54a4");
                    contextMap.put(FieldNames.schemeURI,
                        "https://schema.metadatacenter.org/properties/9a319e93-6bd2-42a3-a1c2-5ec980395882");
                    return contextMap;
                }
                /**
                 * Uniquely identifies an individual or legal entity, according to various
                 * schemes.
                 */
                public record NameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  NameField} record with a {@code  null}
                     * value.
                     *
                     * @return An instance of the {@code  NameField} record.
                     */
                    public static NameField of() {
                        return new NameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  NameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  NameField} record.
                     */
                    @JsonCreator
                    public static NameField of(@JsonProperty("@value") String value) {
                        return new NameField(value);
                    }
                }

                /**
                 * If Name Identifier is used, Name Identifier Scheme is mandatory.
                 */
                public record NameIdentifierSchemeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  NameIdentifierSchemeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  NameIdentifierSchemeField} record.
                     */
                    public static NameIdentifierSchemeField of() {
                        return new NameIdentifierSchemeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  NameIdentifierSchemeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  NameIdentifierSchemeField} record.
                     */
                    @JsonCreator
                    public static NameIdentifierSchemeField of(@JsonProperty("@value") String value) {
                        return new NameIdentifierSchemeField(value);
                    }
                }

                /**
                 * The URI of the name identifier scheme
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record SchemeUriField(String id) implements IriField {

                    /**
                     * Creates an empty SchemeUriField instance, with null values for the id and
                     * label.
                     */
                    public static SchemeUriField of() {
                        return new SchemeUriField(null);
                    }

                    /**
                     * Create an instance of SchemeUriField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     */
                    @JsonCreator
                    public static SchemeUriField of(@JsonProperty("@id") String id) {
                        return new SchemeUriField(id);
                    }

                    @Override
                    public String label() {
                        return "";
                    }
                }
            }

            public record NameIdentifierElementList(
                List<NameIdentifierElement> nameIdentifierList) implements ArtifactList {

                public static NameIdentifierElementList of() {
                    return new NameIdentifierElementList(List.of());
                }

                public int minItems() {
                    return 0;
                }

                public int maxItems() {
                    return Integer.MAX_VALUE;
                }

                public boolean isCardinalitySatisfied() {
                    return nameIdentifierList.size() >= minItems() && nameIdentifierList.size() <= maxItems();
                }

                @JsonCreator
                public static NameIdentifierElementList of(List<NameIdentifierElement> nameIdentifierList) {
                    return new NameIdentifierElementList(nameIdentifierList);
                }

                public static NameIdentifierElementList of(NameIdentifierElement nameIdentifier) {
                    return new NameIdentifierElementList(List.of(nameIdentifier));
                }

                @Override
                public List<Artifact> getArtifacts() {
                    return new ArrayList<>(nameIdentifierList);
                }
            }
        }

        public record CreatorElementList(List<CreatorElement> creatorList) implements ArtifactList {

            public static CreatorElementList of() {
                return new CreatorElementList(List.of());
            }

            public int minItems() {
                return 1;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return creatorList.size() >= minItems() && creatorList.size() <= maxItems();
            }

            @JsonCreator
            public static CreatorElementList of(List<CreatorElement> creatorList) {
                return new CreatorElementList(creatorList);
            }

            public static CreatorElementList of(CreatorElement creator) {
                return new CreatorElementList(List.of(creator));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(creatorList);
            }
        }

        public record TitleElement(@JsonProperty("@id") String id,
                                   @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.title) TitleField2 title,
                                   @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.titleType) TitleTypeField titleType)
            implements
            Element {

            /**
             * Gets an empty TitleElement list.
             */
            public static TitleElement of() {
                return new TitleElement(generateId(), TitleField2.of(), TitleTypeField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(title, titleType);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.title,
                    "https://schema.metadatacenter.org/properties/c9a3f95f-d076-4a78-9b30-93ba8caf8d05");
                contextMap.put(FieldNames.titleType,
                    "https://schema.metadatacenter.org/properties/9b5eb75f-3355-4ddb-903d-c716f4816b12");
                return contextMap;
            }
            /**
             * A name or title by which a resource is known.
             */
            public record TitleField2(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  TitleField2} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  TitleField2} record.
                 */
                public static TitleField2 of() {
                    return new TitleField2(null);
                }

                /**
                 * Creates an instance of the {@code  TitleField2} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  TitleField2} record.
                 */
                @JsonCreator
                public static TitleField2 of(@JsonProperty("@value") String value) {
                    return new TitleField2(value);
                }
            }

            /**
             * The type of Title (other than the Main Title)
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record TitleTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty TitleTypeField instance, with null values for the id and
                 * label.
                 */
                public static TitleTypeField of() {
                    return new TitleTypeField(null, null);
                }

                /**
                 * Create an instance of TitleTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static TitleTypeField of(@JsonProperty("@id") String id,
                                                @JsonProperty("rdfs:label") String label) {
                    return new TitleTypeField(id, label);
                }
            }
        }

        public record TitleElementList(List<TitleElement> titleList) implements ArtifactList {

            public static TitleElementList of() {
                return new TitleElementList(List.of());
            }

            public int minItems() {
                return 1;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return titleList.size() >= minItems() && titleList.size() <= maxItems();
            }

            @JsonCreator
            public static TitleElementList of(List<TitleElement> titleList) {
                return new TitleElementList(titleList);
            }

            public static TitleElementList of(TitleElement title) {
                return new TitleElementList(List.of(title));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(titleList);
            }
        }

        /**
         */
        @JsonPropertyOrder({"@type", "@value"})
        public record PublicationYearField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  PublicationYearField} record with a
             * {@code  null} value.
             *
             * @return An instance of the {@code  PublicationYearField} record.
             */
            public static PublicationYearField of() {
                return new PublicationYearField(null);
            }

            /**
             * Creates an instance of the {@code  PublicationYearField} record with the
             * specified value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  PublicationYearField} record.
             */
            @JsonCreator
            public static PublicationYearField of(@JsonProperty("@value") String value) {
                return new PublicationYearField(value);
            }

            /**
             * Gets the datatype associated with the record.
             *
             * @return The datatype string.
             */
            @JsonView(CoreView.class)
            @JsonProperty("@type")
            public String getDatatype() {
                return "xsd:date";
            }
        }

        /**
         * The name of the entity that holds, archives, publishes prints, distributes,
         * releases, issues, or produces the resource.
         */
        public record PublisherField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  PublisherField} record with a
             * {@code  null} value.
             *
             * @return An instance of the {@code  PublisherField} record.
             */
            public static PublisherField of() {
                return new PublisherField(null);
            }

            /**
             * Creates an instance of the {@code  PublisherField} record with the specified
             * value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  PublisherField} record.
             */
            @JsonCreator
            public static PublisherField of(@JsonProperty("@value") String value) {
                return new PublisherField(value);
            }
        }

        /**
         * A description of the resource
         */
        public record ResourceTypeField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  ResourceTypeField} record with a
             * {@code  null} value.
             *
             * @return An instance of the {@code  ResourceTypeField} record.
             */
            public static ResourceTypeField of() {
                return new ResourceTypeField(null);
            }

            /**
             * Creates an instance of the {@code  ResourceTypeField} record with the
             * specified value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  ResourceTypeField} record.
             */
            @JsonCreator
            public static ResourceTypeField of(@JsonProperty("@value") String value) {
                return new ResourceTypeField(value);
            }
        }

        public record SubjectElement(@JsonProperty("@id") String id,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.subject) SubjectField2 subject,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.subjectScheme) SubjectSchemeField subjectScheme,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.valueURI) ValueUriField valueUri,
                                     @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.classificationCode) ClassificationCodeField classificationCode)
            implements
            Element {

            /**
             * Gets an empty SubjectElement list.
             */
            public static SubjectElement of() {
                return new SubjectElement(generateId(), SubjectField2.of(), SubjectSchemeField.of(),
                    SchemeUriField.of(), ValueUriField.of(), ClassificationCodeField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(subject, subjectScheme, schemeUri, valueUri, classificationCode);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.subject,
                    "https://schema.metadatacenter.org/properties/a9cee9cf-656d-4383-b2a1-9594d46dc7ea");
                contextMap.put(FieldNames.subjectScheme,
                    "https://schema.metadatacenter.org/properties/c029122f-b9b4-4176-b235-d3fff6f5167b");
                contextMap.put(FieldNames.schemeURI,
                    "https://schema.metadatacenter.org/properties/342428c4-c748-47bf-a3c1-e6b96a5e9e32");
                contextMap.put(FieldNames.valueURI,
                    "https://schema.metadatacenter.org/properties/7420f8aa-4a00-418f-92cb-8874c0c770cd");
                contextMap.put(FieldNames.classificationCode,
                    "https://schema.metadatacenter.org/properties/f9115bda-17ee-4073-b66b-886a256bc944");
                return contextMap;
            }
            /**
             * Subject, keyword, classification code, or key phrase describing the resource
             */
            public record SubjectField2(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  SubjectField2} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  SubjectField2} record.
                 */
                public static SubjectField2 of() {
                    return new SubjectField2(null);
                }

                /**
                 * Creates an instance of the {@code  SubjectField2} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  SubjectField2} record.
                 */
                @JsonCreator
                public static SubjectField2 of(@JsonProperty("@value") String value) {
                    return new SubjectField2(value);
                }
            }

            /**
             * The name of the subject scheme or classification code or authority if one is
             * used
             */
            public record SubjectSchemeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  SubjectSchemeField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  SubjectSchemeField} record.
                 */
                public static SubjectSchemeField of() {
                    return new SubjectSchemeField(null);
                }

                /**
                 * Creates an instance of the {@code  SubjectSchemeField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  SubjectSchemeField} record.
                 */
                @JsonCreator
                public static SubjectSchemeField of(@JsonProperty("@value") String value) {
                    return new SubjectSchemeField(value);
                }
            }

            /**
             * The URI of the subject identifier scheme
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record SchemeUriField(String id) implements IriField {

                /**
                 * Creates an empty SchemeUriField instance, with null values for the id and
                 * label.
                 */
                public static SchemeUriField of() {
                    return new SchemeUriField(null);
                }

                /**
                 * Create an instance of SchemeUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static SchemeUriField of(@JsonProperty("@id") String id) {
                    return new SchemeUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }

            /**
             * The URI of the subject term
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record ValueUriField(String id) implements IriField {

                /**
                 * Creates an empty ValueUriField instance, with null values for the id and
                 * label.
                 */
                public static ValueUriField of() {
                    return new ValueUriField(null);
                }

                /**
                 * Create an instance of ValueUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static ValueUriField of(@JsonProperty("@id") String id) {
                    return new ValueUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }

            /**
             * The classification code used for the subject term in the subject scheme
             */
            public record ClassificationCodeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  ClassificationCodeField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  ClassificationCodeField} record.
                 */
                public static ClassificationCodeField of() {
                    return new ClassificationCodeField(null);
                }

                /**
                 * Creates an instance of the {@code  ClassificationCodeField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  ClassificationCodeField} record.
                 */
                @JsonCreator
                public static ClassificationCodeField of(@JsonProperty("@value") String value) {
                    return new ClassificationCodeField(value);
                }
            }
        }

        public record SubjectElementList(List<SubjectElement> subjectList) implements ArtifactList {

            public static SubjectElementList of() {
                return new SubjectElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return subjectList.size() >= minItems() && subjectList.size() <= maxItems();
            }

            @JsonCreator
            public static SubjectElementList of(List<SubjectElement> subjectList) {
                return new SubjectElementList(subjectList);
            }

            public static SubjectElementList of(SubjectElement subject) {
                return new SubjectElementList(List.of(subject));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(subjectList);
            }
        }

        public record ContributorElement(@JsonProperty("@id") String id,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.contributorName) ContributorNameField contributorName,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameType) NameTypeField nameType,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.givenName) GivenNameField givenName,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.familyName) FamilyNameField familyName,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.contributorType) ContributorTypeField contributorType,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.affiliation) AffiliationElementList affiliation,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameIdentifier) NameIdentifierElementList nameIdentifier)
            implements
            Element {

            /**
             * Gets an empty ContributorElement list.
             */
            public static ContributorElement of() {
                return new ContributorElement(generateId(), ContributorNameField.of(), NameTypeField.of(),
                    GivenNameField.of(), FamilyNameField.of(), ContributorTypeField.of(),
                    AffiliationElementList.of(), NameIdentifierElementList.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(contributorName, nameType, givenName, familyName, contributorType, affiliation,
                    nameIdentifier);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.contributorName,
                    "https://schema.metadatacenter.org/properties/f9284b7a-1775-4a1f-b7f7-026198b9aa64");
                contextMap.put(FieldNames.nameType,
                    "https://schema.metadatacenter.org/properties/f3554aad-6be5-468a-b665-568a192cb9d1");
                contextMap.put(FieldNames.givenName,
                    "https://schema.metadatacenter.org/properties/fd548b4a-0670-4660-9c77-b4210526672b");
                contextMap.put(FieldNames.familyName,
                    "https://schema.metadatacenter.org/properties/a431cb7f-8675-46dd-b53c-d8461b433f8b");
                contextMap.put(FieldNames.contributorType,
                    "https://schema.metadatacenter.org/properties/ad2f2387-2231-4023-982d-ed3db7c31ac0");
                contextMap.put(FieldNames.affiliation,
                    "https://schema.metadatacenter.org/properties/ac1b30da-6f91-4b09-aa91-cecb68eef98d");
                contextMap.put(FieldNames.nameIdentifier,
                    "https://schema.metadatacenter.org/properties/348f45aa-f191-43e4-bfed-a3dfb69502ae");
                return contextMap;
            }
            /**
             * The full name of the contributor
             */
            public record ContributorNameField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  ContributorNameField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  ContributorNameField} record.
                 */
                public static ContributorNameField of() {
                    return new ContributorNameField(null);
                }

                /**
                 * Creates an instance of the {@code  ContributorNameField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  ContributorNameField} record.
                 */
                @JsonCreator
                public static ContributorNameField of(@JsonProperty("@value") String value) {
                    return new ContributorNameField(value);
                }
            }

            /**
             * Name Type
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record NameTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty NameTypeField instance, with null values for the id and
                 * label.
                 */
                public static NameTypeField of() {
                    return new NameTypeField(null, null);
                }

                /**
                 * Create an instance of NameTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static NameTypeField of(@JsonProperty("@id") String id,
                                               @JsonProperty("rdfs:label") String label) {
                    return new NameTypeField(id, label);
                }
            }

            /**
             */
            public record GivenNameField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  GivenNameField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  GivenNameField} record.
                 */
                public static GivenNameField of() {
                    return new GivenNameField(null);
                }

                /**
                 * Creates an instance of the {@code  GivenNameField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  GivenNameField} record.
                 */
                @JsonCreator
                public static GivenNameField of(@JsonProperty("@value") String value) {
                    return new GivenNameField(value);
                }
            }

            /**
             */
            public record FamilyNameField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  FamilyNameField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  FamilyNameField} record.
                 */
                public static FamilyNameField of() {
                    return new FamilyNameField(null);
                }

                /**
                 * Creates an instance of the {@code  FamilyNameField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  FamilyNameField} record.
                 */
                @JsonCreator
                public static FamilyNameField of(@JsonProperty("@value") String value) {
                    return new FamilyNameField(value);
                }
            }

            /**
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record ContributorTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty ContributorTypeField instance, with null values for the id
                 * and label.
                 */
                public static ContributorTypeField of() {
                    return new ContributorTypeField(null, null);
                }

                /**
                 * Create an instance of ContributorTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static ContributorTypeField of(@JsonProperty("@id") String id,
                                                      @JsonProperty("rdfs:label") String label) {
                    return new ContributorTypeField(id, label);
                }
            }

            public record AffiliationElement(@JsonProperty("@id") String id,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.name) NameField name,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.affiliationIdentifier) AffiliationIdentifierField affiliationIdentifier,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.affiliationIdentifierScheme) AffiliationIdentifierSchemeField affiliationIdentifierScheme,
                                             @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri)
                implements
                Element {

                /**
                 * Gets an empty AffiliationElement list.
                 */
                public static AffiliationElement of() {
                    return new AffiliationElement(generateId(), NameField.of(), AffiliationIdentifierField.of(),
                        AffiliationIdentifierSchemeField.of(), SchemeUriField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(name, affiliationIdentifier, affiliationIdentifierScheme, schemeUri);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.name,
                        "https://schema.metadatacenter.org/properties/b316245d-7a0b-4f1c-b6a2-8f08f1f3da1b");
                    contextMap.put(FieldNames.affiliationIdentifier,
                        "https://schema.metadatacenter.org/properties/1c405b8f-a36a-4b35-a7e5-52ad7828f5a4");
                    contextMap.put(FieldNames.affiliationIdentifierScheme,
                        "https://schema.metadatacenter.org/properties/ac3137bc-0bd2-4e50-ad4e-6592bf71e681");
                    contextMap.put(FieldNames.schemeURI,
                        "https://schema.metadatacenter.org/properties/3b1e62d4-f1b6-4874-b36e-d7740a7f7b05");
                    return contextMap;
                }
                /**
                 * The organizational or institutional affiliation of the creator
                 */
                public record NameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  NameField} record with a {@code  null}
                     * value.
                     *
                     * @return An instance of the {@code  NameField} record.
                     */
                    public static NameField of() {
                        return new NameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  NameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  NameField} record.
                     */
                    @JsonCreator
                    public static NameField of(@JsonProperty("@value") String value) {
                        return new NameField(value);
                    }
                }

                /**
                 * Uniquely identifies the organizational affiliation of the creator.
                 */
                public record AffiliationIdentifierField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  AffiliationIdentifierField} record.
                     */
                    public static AffiliationIdentifierField of() {
                        return new AffiliationIdentifierField(null);
                    }

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierField} record with
                     * the specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  AffiliationIdentifierField} record.
                     */
                    @JsonCreator
                    public static AffiliationIdentifierField of(@JsonProperty("@value") String value) {
                        return new AffiliationIdentifierField(value);
                    }
                }

                /**
                 * If Affiliation Identifier is used, Affiliation Identifier Scheme is
                 * mandatory.
                 */
                public record AffiliationIdentifierSchemeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierSchemeField} record
                     * with a {@code  null} value.
                     *
                     * @return An instance of the {@code  AffiliationIdentifierSchemeField} record.
                     */
                    public static AffiliationIdentifierSchemeField of() {
                        return new AffiliationIdentifierSchemeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  AffiliationIdentifierSchemeField} record
                     * with the specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  AffiliationIdentifierSchemeField} record.
                     */
                    @JsonCreator
                    public static AffiliationIdentifierSchemeField of(@JsonProperty("@value") String value) {
                        return new AffiliationIdentifierSchemeField(value);
                    }
                }

                /**
                 * The URI of the affiliation identifier scheme
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record SchemeUriField(String id) implements IriField {

                    /**
                     * Creates an empty SchemeUriField instance, with null values for the id and
                     * label.
                     */
                    public static SchemeUriField of() {
                        return new SchemeUriField(null);
                    }

                    /**
                     * Create an instance of SchemeUriField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     */
                    @JsonCreator
                    public static SchemeUriField of(@JsonProperty("@id") String id) {
                        return new SchemeUriField(id);
                    }

                    @Override
                    public String label() {
                        return "";
                    }
                }
            }

            public record AffiliationElementList(List<AffiliationElement> affiliationList) implements ArtifactList {

                public static AffiliationElementList of() {
                    return new AffiliationElementList(List.of());
                }

                public int minItems() {
                    return 0;
                }

                public int maxItems() {
                    return Integer.MAX_VALUE;
                }

                public boolean isCardinalitySatisfied() {
                    return affiliationList.size() >= minItems() && affiliationList.size() <= maxItems();
                }

                @JsonCreator
                public static AffiliationElementList of(List<AffiliationElement> affiliationList) {
                    return new AffiliationElementList(affiliationList);
                }

                public static AffiliationElementList of(AffiliationElement affiliation) {
                    return new AffiliationElementList(List.of(affiliation));
                }

                @Override
                public List<Artifact> getArtifacts() {
                    return new ArrayList<>(affiliationList);
                }
            }

            public record NameIdentifierElement(@JsonProperty("@id") String id,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.name) NameField name,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameIdentifierScheme) NameIdentifierSchemeField nameIdentifierScheme,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri)
                implements
                Element {

                /**
                 * Gets an empty NameIdentifierElement list.
                 */
                public static NameIdentifierElement of() {
                    return new NameIdentifierElement(generateId(), NameField.of(), NameIdentifierSchemeField.of(),
                        SchemeUriField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(name, nameIdentifierScheme, schemeUri);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.name,
                        "https://schema.metadatacenter.org/properties/0fd96dfa-0d23-4075-a6ca-6b12205e437e");
                    contextMap.put(FieldNames.nameIdentifierScheme,
                        "https://schema.metadatacenter.org/properties/ba910380-8046-4298-94bb-27a7f76d54a4");
                    contextMap.put(FieldNames.schemeURI,
                        "https://schema.metadatacenter.org/properties/9a319e93-6bd2-42a3-a1c2-5ec980395882");
                    return contextMap;
                }
                /**
                 * Uniquely identifies an individual or legal entity, according to various
                 * schemes.
                 */
                public record NameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  NameField} record with a {@code  null}
                     * value.
                     *
                     * @return An instance of the {@code  NameField} record.
                     */
                    public static NameField of() {
                        return new NameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  NameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  NameField} record.
                     */
                    @JsonCreator
                    public static NameField of(@JsonProperty("@value") String value) {
                        return new NameField(value);
                    }
                }

                /**
                 * If Name Identifier is used, Name Identifier Scheme is mandatory.
                 */
                public record NameIdentifierSchemeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  NameIdentifierSchemeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  NameIdentifierSchemeField} record.
                     */
                    public static NameIdentifierSchemeField of() {
                        return new NameIdentifierSchemeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  NameIdentifierSchemeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  NameIdentifierSchemeField} record.
                     */
                    @JsonCreator
                    public static NameIdentifierSchemeField of(@JsonProperty("@value") String value) {
                        return new NameIdentifierSchemeField(value);
                    }
                }

                /**
                 * The URI of the name identifier scheme
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record SchemeUriField(String id) implements IriField {

                    /**
                     * Creates an empty SchemeUriField instance, with null values for the id and
                     * label.
                     */
                    public static SchemeUriField of() {
                        return new SchemeUriField(null);
                    }

                    /**
                     * Create an instance of SchemeUriField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     */
                    @JsonCreator
                    public static SchemeUriField of(@JsonProperty("@id") String id) {
                        return new SchemeUriField(id);
                    }

                    @Override
                    public String label() {
                        return "";
                    }
                }
            }

            public record NameIdentifierElementList(
                List<NameIdentifierElement> nameIdentifierList) implements ArtifactList {

                public static NameIdentifierElementList of() {
                    return new NameIdentifierElementList(List.of());
                }

                public int minItems() {
                    return 0;
                }

                public int maxItems() {
                    return Integer.MAX_VALUE;
                }

                public boolean isCardinalitySatisfied() {
                    return nameIdentifierList.size() >= minItems() && nameIdentifierList.size() <= maxItems();
                }

                @JsonCreator
                public static NameIdentifierElementList of(List<NameIdentifierElement> nameIdentifierList) {
                    return new NameIdentifierElementList(nameIdentifierList);
                }

                public static NameIdentifierElementList of(NameIdentifierElement nameIdentifier) {
                    return new NameIdentifierElementList(List.of(nameIdentifier));
                }

                @Override
                public List<Artifact> getArtifacts() {
                    return new ArrayList<>(nameIdentifierList);
                }
            }
        }

        public record ContributorElementList(List<ContributorElement> contributorList) implements ArtifactList {

            public static ContributorElementList of() {
                return new ContributorElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return contributorList.size() >= minItems() && contributorList.size() <= maxItems();
            }

            @JsonCreator
            public static ContributorElementList of(List<ContributorElement> contributorList) {
                return new ContributorElementList(contributorList);
            }

            public static ContributorElementList of(ContributorElement contributor) {
                return new ContributorElementList(List.of(contributor));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(contributorList);
            }
        }

        public record DateElement(@JsonProperty("@id") String id,
                                  @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.date) DateField2 date,
                                  @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.dateType) DateTypeField dateType,
                                  @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.dateInformation) DateInformationField dateInformation)
            implements
            Element {

            /**
             * Gets an empty DateElement list.
             */
            public static DateElement of() {
                return new DateElement(generateId(), DateField2.of(), DateTypeField.of(), DateInformationField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(date, dateType, dateInformation);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.date,
                    "https://schema.metadatacenter.org/properties/85eb2413-48da-4dd9-904b-a828cd5d2b4d");
                contextMap.put(FieldNames.dateType,
                    "https://schema.metadatacenter.org/properties/77e6d4be-3c5f-4076-8517-46ad4759d315");
                contextMap.put(FieldNames.dateInformation,
                    "https://schema.metadatacenter.org/properties/ba5937f5-4b85-4770-be1c-5c62df8bb6c7");
                return contextMap;
            }
            /**
             * Different dates relevant to the work
             */
            @JsonPropertyOrder({"@type", "@value"})
            public record DateField2(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  DateField2} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  DateField2} record.
                 */
                public static DateField2 of() {
                    return new DateField2(null);
                }

                /**
                 * Creates an instance of the {@code  DateField2} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  DateField2} record.
                 */
                @JsonCreator
                public static DateField2 of(@JsonProperty("@value") String value) {
                    return new DateField2(value);
                }

                /**
                 * Gets the datatype associated with the record.
                 *
                 * @return The datatype string.
                 */
                @JsonView(CoreView.class)
                @JsonProperty("@type")
                public String getDatatype() {
                    return "xsd:date";
                }
            }

            /**
             * If Date is used, Date Type is mandatory.
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record DateTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty DateTypeField instance, with null values for the id and
                 * label.
                 */
                public static DateTypeField of() {
                    return new DateTypeField(null, null);
                }

                /**
                 * Create an instance of DateTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static DateTypeField of(@JsonProperty("@id") String id,
                                               @JsonProperty("rdfs:label") String label) {
                    return new DateTypeField(id, label);
                }
            }

            /**
             * Specific information about the date, if appropriate
             */
            public record DateInformationField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  DateInformationField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  DateInformationField} record.
                 */
                public static DateInformationField of() {
                    return new DateInformationField(null);
                }

                /**
                 * Creates an instance of the {@code  DateInformationField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  DateInformationField} record.
                 */
                @JsonCreator
                public static DateInformationField of(@JsonProperty("@value") String value) {
                    return new DateInformationField(value);
                }
            }
        }

        public record DateElementList(List<DateElement> dateList) implements ArtifactList {

            public static DateElementList of() {
                return new DateElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return dateList.size() >= minItems() && dateList.size() <= maxItems();
            }

            @JsonCreator
            public static DateElementList of(List<DateElement> dateList) {
                return new DateElementList(dateList);
            }

            public static DateElementList of(DateElement date) {
                return new DateElementList(List.of(date));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(dateList);
            }
        }

        /**
         * Enter a language tag
         */
        public record LanguageField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  LanguageField} record with a {@code  null}
             * value.
             *
             * @return An instance of the {@code  LanguageField} record.
             */
            public static LanguageField of() {
                return new LanguageField(null);
            }

            /**
             * Creates an instance of the {@code  LanguageField} record with the specified
             * value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  LanguageField} record.
             */
            @JsonCreator
            public static LanguageField of(@JsonProperty("@value") String value) {
                return new LanguageField(value);
            }
        }

        public record AlternateIdentifierElement(@JsonProperty("@id") String id,
                                                 @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.alternateIdentifier) AlternateIdentifierField2 alternateIdentifier,
                                                 @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.alternateIdentifierType) AlternateIdentifierTypeField alternateIdentifierType)
            implements
            Element {

            /**
             * Gets an empty AlternateIdentifierElement list.
             */
            public static AlternateIdentifierElement of() {
                return new AlternateIdentifierElement(generateId(), AlternateIdentifierField2.of(),
                    AlternateIdentifierTypeField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(alternateIdentifier, alternateIdentifierType);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.alternateIdentifier,
                    "https://schema.metadatacenter.org/properties/da9e58cf-9dcc-4ef9-9cf9-f4bc58936f8b");
                contextMap.put(FieldNames.alternateIdentifierType,
                    "https://schema.metadatacenter.org/properties/47a48fd6-97a2-4198-8c06-b9b96608db7e");
                return contextMap;
            }
            /**
             * An identifier other than the primary Identifier applied to the resource being
             * registered.
             */
            public record AlternateIdentifierField2(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  AlternateIdentifierField2} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  AlternateIdentifierField2} record.
                 */
                public static AlternateIdentifierField2 of() {
                    return new AlternateIdentifierField2(null);
                }

                /**
                 * Creates an instance of the {@code  AlternateIdentifierField2} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  AlternateIdentifierField2} record.
                 */
                @JsonCreator
                public static AlternateIdentifierField2 of(@JsonProperty("@value") String value) {
                    return new AlternateIdentifierField2(value);
                }
            }

            /**
             * The type of the Alternate Identifier
             */
            public record AlternateIdentifierTypeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  AlternateIdentifierTypeField} record with
                 * a {@code  null} value.
                 *
                 * @return An instance of the {@code  AlternateIdentifierTypeField} record.
                 */
                public static AlternateIdentifierTypeField of() {
                    return new AlternateIdentifierTypeField(null);
                }

                /**
                 * Creates an instance of the {@code  AlternateIdentifierTypeField} record with
                 * the specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  AlternateIdentifierTypeField} record.
                 */
                @JsonCreator
                public static AlternateIdentifierTypeField of(@JsonProperty("@value") String value) {
                    return new AlternateIdentifierTypeField(value);
                }
            }
        }

        public record AlternateIdentifierElementList(
            List<AlternateIdentifierElement> alternateIdentifierList) implements ArtifactList {

            public static AlternateIdentifierElementList of() {
                return new AlternateIdentifierElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return alternateIdentifierList.size() >= minItems() && alternateIdentifierList.size() <= maxItems();
            }

            @JsonCreator
            public static AlternateIdentifierElementList of(List<AlternateIdentifierElement> alternateIdentifierList) {
                return new AlternateIdentifierElementList(alternateIdentifierList);
            }

            public static AlternateIdentifierElementList of(AlternateIdentifierElement alternateIdentifier) {
                return new AlternateIdentifierElementList(List.of(alternateIdentifier));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(alternateIdentifierList);
            }
        }

        public record RelatedIdentifierElement(@JsonProperty("@id") String id,
                                               @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedIdentifier) RelatedIdentifierField2 relatedIdentifier,
                                               @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedIdentifierType) RelatedIdentifierTypeField relatedIdentifierType,
                                               @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relationType) RelationTypeField relationType,
                                               @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedMetadataScheme) RelatedMetadataSchemeField relatedMetadataScheme,
                                               @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri,
                                               @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeType) SchemeTypeField schemeType,
                                               @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.resourceTypeGeneral) ResourceTypeGeneralField resourceTypeGeneral)
            implements
            Element {

            /**
             * Gets an empty RelatedIdentifierElement list.
             */
            public static RelatedIdentifierElement of() {
                return new RelatedIdentifierElement(generateId(), RelatedIdentifierField2.of(),
                    RelatedIdentifierTypeField.of(), RelationTypeField.of(), RelatedMetadataSchemeField.of(),
                    SchemeUriField.of(), SchemeTypeField.of(), ResourceTypeGeneralField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(relatedIdentifier, relatedIdentifierType, relationType, relatedMetadataScheme,
                    schemeUri, schemeType, resourceTypeGeneral);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.relatedIdentifier,
                    "https://schema.metadatacenter.org/properties/6a6f9a9f-6824-4591-9582-d9f3aa5b8422");
                contextMap.put(FieldNames.relatedIdentifierType,
                    "https://schema.metadatacenter.org/properties/886dba05-0110-4768-82c8-f0ceee4944bb");
                contextMap.put(FieldNames.relationType,
                    "https://schema.metadatacenter.org/properties/24b03c68-f713-44e1-bb63-f39e7a006d61");
                contextMap.put(FieldNames.relatedMetadataScheme,
                    "https://schema.metadatacenter.org/properties/3eb9d332-a035-46ae-8559-834028a50980");
                contextMap.put(FieldNames.schemeURI,
                    "https://schema.metadatacenter.org/properties/a985716f-3b3d-4b32-8267-3badd7353c29");
                contextMap.put(FieldNames.schemeType,
                    "https://schema.metadatacenter.org/properties/9abe3743-9af2-4520-b85a-79b9292c3c39");
                contextMap.put(FieldNames.resourceTypeGeneral,
                    "https://schema.metadatacenter.org/properties/e7e7821a-f1cc-4243-b817-638276fa33fb");
                return contextMap;
            }
            /**
             * Identifiers of related resources. These must be globally unique identifiers.
             */
            public record RelatedIdentifierField2(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  RelatedIdentifierField2} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  RelatedIdentifierField2} record.
                 */
                public static RelatedIdentifierField2 of() {
                    return new RelatedIdentifierField2(null);
                }

                /**
                 * Creates an instance of the {@code  RelatedIdentifierField2} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  RelatedIdentifierField2} record.
                 */
                @JsonCreator
                public static RelatedIdentifierField2 of(@JsonProperty("@value") String value) {
                    return new RelatedIdentifierField2(value);
                }
            }

            /**
             * The type of the RelatedIdentifier. If relatedIdentifier is used,
             * relatedIdentifierType is mandatory.
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record RelatedIdentifierTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty RelatedIdentifierTypeField instance, with null values for
                 * the id and label.
                 */
                public static RelatedIdentifierTypeField of() {
                    return new RelatedIdentifierTypeField(null, null);
                }

                /**
                 * Create an instance of RelatedIdentifierTypeField with the specified id and
                 * label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static RelatedIdentifierTypeField of(@JsonProperty("@id") String id,
                                                            @JsonProperty("rdfs:label") String label) {
                    return new RelatedIdentifierTypeField(id, label);
                }
            }

            /**
             * Description of the relationship of the resource being registered (A) and the
             * related resource (B).
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record RelationTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty RelationTypeField instance, with null values for the id and
                 * label.
                 */
                public static RelationTypeField of() {
                    return new RelationTypeField(null, null);
                }

                /**
                 * Create an instance of RelationTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static RelationTypeField of(@JsonProperty("@id") String id,
                                                   @JsonProperty("rdfs:label") String label) {
                    return new RelationTypeField(id, label);
                }
            }

            /**
             * The name of the scheme
             */
            public record RelatedMetadataSchemeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  RelatedMetadataSchemeField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  RelatedMetadataSchemeField} record.
                 */
                public static RelatedMetadataSchemeField of() {
                    return new RelatedMetadataSchemeField(null);
                }

                /**
                 * Creates an instance of the {@code  RelatedMetadataSchemeField} record with
                 * the specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  RelatedMetadataSchemeField} record.
                 */
                @JsonCreator
                public static RelatedMetadataSchemeField of(@JsonProperty("@value") String value) {
                    return new RelatedMetadataSchemeField(value);
                }
            }

            /**
             * The URI of the Related Metadata Scheme
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record SchemeUriField(String id) implements IriField {

                /**
                 * Creates an empty SchemeUriField instance, with null values for the id and
                 * label.
                 */
                public static SchemeUriField of() {
                    return new SchemeUriField(null);
                }

                /**
                 * Create an instance of SchemeUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static SchemeUriField of(@JsonProperty("@id") String id) {
                    return new SchemeUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }

            /**
             * The type of the Related Metadata Scheme, linked with the Scheme URI.
             */
            public record SchemeTypeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  SchemeTypeField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  SchemeTypeField} record.
                 */
                public static SchemeTypeField of() {
                    return new SchemeTypeField(null);
                }

                /**
                 * Creates an instance of the {@code  SchemeTypeField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  SchemeTypeField} record.
                 */
                @JsonCreator
                public static SchemeTypeField of(@JsonProperty("@value") String value) {
                    return new SchemeTypeField(value);
                }
            }

            /**
             * The general type of the related resource
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record ResourceTypeGeneralField(String id, String label) implements IriField {

                /**
                 * Creates an empty ResourceTypeGeneralField instance, with null values for the
                 * id and label.
                 */
                public static ResourceTypeGeneralField of() {
                    return new ResourceTypeGeneralField(null, null);
                }

                /**
                 * Create an instance of ResourceTypeGeneralField with the specified id and
                 * label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static ResourceTypeGeneralField of(@JsonProperty("@id") String id,
                                                          @JsonProperty("rdfs:label") String label) {
                    return new ResourceTypeGeneralField(id, label);
                }
            }
        }

        public record RelatedIdentifierElementList(
            List<RelatedIdentifierElement> relatedIdentifierList) implements ArtifactList {

            public static RelatedIdentifierElementList of() {
                return new RelatedIdentifierElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return relatedIdentifierList.size() >= minItems() && relatedIdentifierList.size() <= maxItems();
            }

            @JsonCreator
            public static RelatedIdentifierElementList of(List<RelatedIdentifierElement> relatedIdentifierList) {
                return new RelatedIdentifierElementList(relatedIdentifierList);
            }

            public static RelatedIdentifierElementList of(RelatedIdentifierElement relatedIdentifier) {
                return new RelatedIdentifierElementList(List.of(relatedIdentifier));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(relatedIdentifierList);
            }
        }

        /**
         * Size (e.g., bytes, pages, inches, etc.) or duration (extent), e.g., hours,
         * minutes, days, etc., of a resource.
         */
        public record SizeField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  SizeField} record with a {@code  null}
             * value.
             *
             * @return An instance of the {@code  SizeField} record.
             */
            public static SizeField of() {
                return new SizeField(null);
            }

            /**
             * Creates an instance of the {@code  SizeField} record with the specified
             * value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  SizeField} record.
             */
            @JsonCreator
            public static SizeField of(@JsonProperty("@value") String value) {
                return new SizeField(value);
            }
        }

        public record SizeFieldList(List<SizeField> sizeList) implements ArtifactList {

            public static SizeFieldList of() {
                return new SizeFieldList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return sizeList.size() >= minItems() && sizeList.size() <= maxItems();
            }

            @JsonCreator
            public static SizeFieldList of(List<SizeField> sizeList) {
                return new SizeFieldList(sizeList);
            }

            public static SizeFieldList of(SizeField size) {
                return new SizeFieldList(List.of(size));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(sizeList);
            }
        }

        /**
         * Technical format of the resource. Use file extension or MIME type where
         * possible, e.g., PDF, XML, MPG or application/pdf, text/xml, video/mpeg.
         */
        public record FormatField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  FormatField} record with a {@code  null}
             * value.
             *
             * @return An instance of the {@code  FormatField} record.
             */
            public static FormatField of() {
                return new FormatField(null);
            }

            /**
             * Creates an instance of the {@code  FormatField} record with the specified
             * value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  FormatField} record.
             */
            @JsonCreator
            public static FormatField of(@JsonProperty("@value") String value) {
                return new FormatField(value);
            }
        }

        public record FormatFieldList(List<FormatField> formatList) implements ArtifactList {

            public static FormatFieldList of() {
                return new FormatFieldList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return formatList.size() >= minItems() && formatList.size() <= maxItems();
            }

            @JsonCreator
            public static FormatFieldList of(List<FormatField> formatList) {
                return new FormatFieldList(formatList);
            }

            public static FormatFieldList of(FormatField format) {
                return new FormatFieldList(List.of(format));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(formatList);
            }
        }

        /**
         * The version number of the resource.
         */
        public record VersionField(String value) implements LiteralField {

            /**
             * Creates an instance of the {@code  VersionField} record with a {@code  null}
             * value.
             *
             * @return An instance of the {@code  VersionField} record.
             */
            public static VersionField of() {
                return new VersionField(null);
            }

            /**
             * Creates an instance of the {@code  VersionField} record with the specified
             * value.
             *
             * @param value
             *            The value to set for the record.
             * @return An instance of the {@code  VersionField} record.
             */
            @JsonCreator
            public static VersionField of(@JsonProperty("@value") String value) {
                return new VersionField(value);
            }
        }

        public record RightsElement(@JsonProperty("@id") String id,
                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.rights) RightsField2 rights,
                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.rightsURI) RightsUriField rightsUri,
                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.rightsIdentifier) RightsIdentifierField rightsIdentifier,
                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.rightsIdentifierScheme) RightsIdentifierSchemeField rightsIdentifierScheme,
                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri)
            implements
            Element {

            /**
             * Gets an empty RightsElement list.
             */
            public static RightsElement of() {
                return new RightsElement(generateId(), RightsField2.of(), RightsUriField.of(),
                    RightsIdentifierField.of(), RightsIdentifierSchemeField.of(), SchemeUriField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(rights, rightsUri, rightsIdentifier, rightsIdentifierScheme, schemeUri);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.rights,
                    "https://schema.metadatacenter.org/properties/4e328407-a40d-4393-8d9b-cc3923afe5f8");
                contextMap.put(FieldNames.rightsURI,
                    "https://schema.metadatacenter.org/properties/3e8de9e1-2649-42e2-95c5-717247d0060e");
                contextMap.put(FieldNames.rightsIdentifier,
                    "https://schema.metadatacenter.org/properties/d4acc73e-f030-4902-9ec8-3cf166040679");
                contextMap.put(FieldNames.rightsIdentifierScheme,
                    "https://schema.metadatacenter.org/properties/f1195818-b92e-4916-a5c8-f326b0a546f0");
                contextMap.put(FieldNames.schemeURI,
                    "https://schema.metadatacenter.org/properties/9b429a92-5dc3-4ff3-995c-24deaabc9c15");
                return contextMap;
            }
            /**
             * Any rights information for this resource.
             */
            public record RightsField2(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  RightsField2} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  RightsField2} record.
                 */
                public static RightsField2 of() {
                    return new RightsField2(null);
                }

                /**
                 * Creates an instance of the {@code  RightsField2} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  RightsField2} record.
                 */
                @JsonCreator
                public static RightsField2 of(@JsonProperty("@value") String value) {
                    return new RightsField2(value);
                }
            }

            /**
             * The URI of the license
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record RightsUriField(String id) implements IriField {

                /**
                 * Creates an empty RightsUriField instance, with null values for the id and
                 * label.
                 */
                public static RightsUriField of() {
                    return new RightsUriField(null);
                }

                /**
                 * Create an instance of RightsUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static RightsUriField of(@JsonProperty("@id") String id) {
                    return new RightsUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }

            /**
             * A short, standardized version of the license name. Example: CC-BY-3.0 A list
             * of identifiers for commonly-used licenses may be found here:
             * (https://spdx.org/licenses/).
             */
            public record RightsIdentifierField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  RightsIdentifierField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  RightsIdentifierField} record.
                 */
                public static RightsIdentifierField of() {
                    return new RightsIdentifierField(null);
                }

                /**
                 * Creates an instance of the {@code  RightsIdentifierField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  RightsIdentifierField} record.
                 */
                @JsonCreator
                public static RightsIdentifierField of(@JsonProperty("@value") String value) {
                    return new RightsIdentifierField(value);
                }
            }

            /**
             * The name of the scheme.
             */
            public record RightsIdentifierSchemeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  RightsIdentifierSchemeField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  RightsIdentifierSchemeField} record.
                 */
                public static RightsIdentifierSchemeField of() {
                    return new RightsIdentifierSchemeField(null);
                }

                /**
                 * Creates an instance of the {@code  RightsIdentifierSchemeField} record with
                 * the specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  RightsIdentifierSchemeField} record.
                 */
                @JsonCreator
                public static RightsIdentifierSchemeField of(@JsonProperty("@value") String value) {
                    return new RightsIdentifierSchemeField(value);
                }
            }

            /**
             * The URI of the Rights Identifier Scheme
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record SchemeUriField(String id) implements IriField {

                /**
                 * Creates an empty SchemeUriField instance, with null values for the id and
                 * label.
                 */
                public static SchemeUriField of() {
                    return new SchemeUriField(null);
                }

                /**
                 * Create an instance of SchemeUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static SchemeUriField of(@JsonProperty("@id") String id) {
                    return new SchemeUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }
        }

        public record RightsElementList(List<RightsElement> rightsList) implements ArtifactList {

            public static RightsElementList of() {
                return new RightsElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return rightsList.size() >= minItems() && rightsList.size() <= maxItems();
            }

            @JsonCreator
            public static RightsElementList of(List<RightsElement> rightsList) {
                return new RightsElementList(rightsList);
            }

            public static RightsElementList of(RightsElement rights) {
                return new RightsElementList(List.of(rights));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(rightsList);
            }
        }

        public record DescriptionElement(@JsonProperty("@id") String id,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.description) DescriptionField2 description,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.descriptionType) DescriptionTypeField descriptionType)
            implements
            Element {

            /**
             * Gets an empty DescriptionElement list.
             */
            public static DescriptionElement of() {
                return new DescriptionElement(generateId(), DescriptionField2.of(), DescriptionTypeField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(description, descriptionType);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.description,
                    "https://schema.metadatacenter.org/properties/828a6904-4440-4f16-9369-4883f795f983");
                contextMap.put(FieldNames.descriptionType,
                    "https://schema.metadatacenter.org/properties/ba34a97c-9400-4e79-9581-b9218651231d");
                return contextMap;
            }
            /**
             * All additional information that does not fit in any of the other categories.
             * May be used for technical information.
             */
            public record DescriptionField2(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  DescriptionField2} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  DescriptionField2} record.
                 */
                public static DescriptionField2 of() {
                    return new DescriptionField2(null);
                }

                /**
                 * Creates an instance of the {@code  DescriptionField2} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  DescriptionField2} record.
                 */
                @JsonCreator
                public static DescriptionField2 of(@JsonProperty("@value") String value) {
                    return new DescriptionField2(value);
                }
            }

            /**
             * The type of the Description. If Description is used, descriptionType is
             * mandatory.
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record DescriptionTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty DescriptionTypeField instance, with null values for the id
                 * and label.
                 */
                public static DescriptionTypeField of() {
                    return new DescriptionTypeField(null, null);
                }

                /**
                 * Create an instance of DescriptionTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static DescriptionTypeField of(@JsonProperty("@id") String id,
                                                      @JsonProperty("rdfs:label") String label) {
                    return new DescriptionTypeField(id, label);
                }
            }
        }

        public record DescriptionElementList(List<DescriptionElement> descriptionList) implements ArtifactList {

            public static DescriptionElementList of() {
                return new DescriptionElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return descriptionList.size() >= minItems() && descriptionList.size() <= maxItems();
            }

            @JsonCreator
            public static DescriptionElementList of(List<DescriptionElement> descriptionList) {
                return new DescriptionElementList(descriptionList);
            }

            public static DescriptionElementList of(DescriptionElement description) {
                return new DescriptionElementList(List.of(description));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(descriptionList);
            }
        }

        public record GeoLocationElement(@JsonProperty("@id") String id,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.geoLocationPlace) GeoLocationPlaceField geoLocationPlace,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.geoLocationPoint) GeoLocationPointElement geoLocationPoint,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.geoLocationBox) GeoLocationBoxElement geoLocationBox)
            implements
            Element {

            /**
             * Gets an empty GeoLocationElement list.
             */
            public static GeoLocationElement of() {
                return new GeoLocationElement(generateId(), GeoLocationPlaceField.of(), GeoLocationPointElement.of(),
                    GeoLocationBoxElement.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(geoLocationPlace, geoLocationPoint, geoLocationBox);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.geoLocationPlace,
                    "https://schema.metadatacenter.org/properties/37cbffe7-b904-472d-bd36-887b003177ab");
                contextMap.put(FieldNames.geoLocationPoint,
                    "https://schema.metadatacenter.org/properties/bc661527-448d-455c-8871-9e19d6233b49");
                contextMap.put(FieldNames.geoLocationBox,
                    "https://schema.metadatacenter.org/properties/00a33d1e-f500-42dd-9d13-1e09f02be601");
                return contextMap;
            }
            /**
             */
            public record GeoLocationPlaceField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  GeoLocationPlaceField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  GeoLocationPlaceField} record.
                 */
                public static GeoLocationPlaceField of() {
                    return new GeoLocationPlaceField(null);
                }

                /**
                 * Creates an instance of the {@code  GeoLocationPlaceField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  GeoLocationPlaceField} record.
                 */
                @JsonCreator
                public static GeoLocationPlaceField of(@JsonProperty("@value") String value) {
                    return new GeoLocationPlaceField(value);
                }
            }

            public record GeoLocationPointElement(@JsonProperty("@id") String id,
                                                  @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.pointLongitude) PointLongitudeField pointLongitude,
                                                  @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.pointLatitude) PointLatitudeField pointLatitude)
                implements
                Element {

                /**
                 * Gets an empty GeoLocationPointElement list.
                 */
                public static GeoLocationPointElement of() {
                    return new GeoLocationPointElement(generateId(), PointLongitudeField.of(), PointLatitudeField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(pointLongitude, pointLatitude);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.pointLongitude,
                        "https://schema.metadatacenter.org/properties/89e06703-c7b7-420e-b24a-48969fb58ccb");
                    contextMap.put(FieldNames.pointLatitude,
                        "https://schema.metadatacenter.org/properties/9f74832e-0eb9-4b3b-bb73-8c9e4f4fa52a");
                    return contextMap;
                }
                /**
                 * Latitudinal dimension of point. If In Polygon Point is used pointLongitude is
                 * mandatory. Longitude of the geographic point expressed in decimal degrees
                 * (positive east).
                 */
                @JsonPropertyOrder({"@type", "@value"})
                public record PointLongitudeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  PointLongitudeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  PointLongitudeField} record.
                     */
                    public static PointLongitudeField of() {
                        return new PointLongitudeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  PointLongitudeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  PointLongitudeField} record.
                     */
                    @JsonCreator
                    public static PointLongitudeField of(@JsonProperty("@value") String value) {
                        return new PointLongitudeField(value);
                    }

                    /**
                     * Gets the datatype associated with the record.
                     *
                     * @return The datatype string.
                     */
                    @JsonView(CoreView.class)
                    @JsonProperty("@type")
                    public String getDatatype() {
                        return "xsd:decimal";
                    }
                }

                /**
                 * Latitudinal dimension of point. If inPolygonPoint is used, pointLatitude is
                 * mandatory. Latitude of the geographic point expressed in decimal degrees
                 * (positive north).
                 */
                @JsonPropertyOrder({"@type", "@value"})
                public record PointLatitudeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  PointLatitudeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  PointLatitudeField} record.
                     */
                    public static PointLatitudeField of() {
                        return new PointLatitudeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  PointLatitudeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  PointLatitudeField} record.
                     */
                    @JsonCreator
                    public static PointLatitudeField of(@JsonProperty("@value") String value) {
                        return new PointLatitudeField(value);
                    }

                    /**
                     * Gets the datatype associated with the record.
                     *
                     * @return The datatype string.
                     */
                    @JsonView(CoreView.class)
                    @JsonProperty("@type")
                    public String getDatatype() {
                        return "xsd:decimal";
                    }
                }
            }

            public record GeoLocationBoxElement(@JsonProperty("@id") String id,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.westBoundLongitude) WestBoundLongitudeField westBoundLongitude,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.eastBoundLongitude) EastBoundLongitudeField eastBoundLongitude,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.southBoundLatitude) SouthBoundLatitudeField southBoundLatitude,
                                                @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.northBoundLatitude) NorthBoundLatitudeField northBoundLatitude)
                implements
                Element {

                /**
                 * Gets an empty GeoLocationBoxElement list.
                 */
                public static GeoLocationBoxElement of() {
                    return new GeoLocationBoxElement(generateId(), WestBoundLongitudeField.of(),
                        EastBoundLongitudeField.of(), SouthBoundLatitudeField.of(), NorthBoundLatitudeField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(westBoundLongitude, eastBoundLongitude, southBoundLatitude,
                        northBoundLatitude);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.westBoundLongitude,
                        "https://schema.metadatacenter.org/properties/dd27b92a-04d1-435c-bab1-b6acdc1df9eb");
                    contextMap.put(FieldNames.eastBoundLongitude,
                        "https://schema.metadatacenter.org/properties/9cee2101-38ad-4ae9-922c-55a89e049741");
                    contextMap.put(FieldNames.southBoundLatitude,
                        "https://schema.metadatacenter.org/properties/a4171ee9-162e-4e32-89dd-cad62d60f4d9");
                    contextMap.put(FieldNames.northBoundLatitude,
                        "https://schema.metadatacenter.org/properties/5f41ba88-3fb0-4871-88ef-23d0a7f1326e");
                    return contextMap;
                }
                /**
                 * Western longitudinal dimension of box. Longitude of the geographic point
                 * expressed in decimal degrees (positive east). Domain: -180.00 ≤
                 * westBoundLongitude ≤ 180.00
                 */
                @JsonPropertyOrder({"@type", "@value"})
                public record WestBoundLongitudeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  WestBoundLongitudeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  WestBoundLongitudeField} record.
                     */
                    public static WestBoundLongitudeField of() {
                        return new WestBoundLongitudeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  WestBoundLongitudeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  WestBoundLongitudeField} record.
                     */
                    @JsonCreator
                    public static WestBoundLongitudeField of(@JsonProperty("@value") String value) {
                        return new WestBoundLongitudeField(value);
                    }

                    /**
                     * Gets the datatype associated with the record.
                     *
                     * @return The datatype string.
                     */
                    @JsonView(CoreView.class)
                    @JsonProperty("@type")
                    public String getDatatype() {
                        return "xsd:decimal";
                    }
                }

                /**
                 * Eastern longitudinal dimension of box. Longitude of the geographic point
                 * expressed in decimal degrees (positive east) Domain: -180.00 ≤
                 * eastBoundLongitude ≤ 180.00.
                 */
                @JsonPropertyOrder({"@type", "@value"})
                public record EastBoundLongitudeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  EastBoundLongitudeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  EastBoundLongitudeField} record.
                     */
                    public static EastBoundLongitudeField of() {
                        return new EastBoundLongitudeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  EastBoundLongitudeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  EastBoundLongitudeField} record.
                     */
                    @JsonCreator
                    public static EastBoundLongitudeField of(@JsonProperty("@value") String value) {
                        return new EastBoundLongitudeField(value);
                    }

                    /**
                     * Gets the datatype associated with the record.
                     *
                     * @return The datatype string.
                     */
                    @JsonView(CoreView.class)
                    @JsonProperty("@type")
                    public String getDatatype() {
                        return "xsd:decimal";
                    }
                }

                /**
                 * Southern latitudinal dimension of box. Latitude of the geographic point
                 * expressed in decimal degrees (positive north). Domain: -90.00 ≤
                 * southBoundingLatitude ≤ 90.00
                 */
                @JsonPropertyOrder({"@type", "@value"})
                public record SouthBoundLatitudeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  SouthBoundLatitudeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  SouthBoundLatitudeField} record.
                     */
                    public static SouthBoundLatitudeField of() {
                        return new SouthBoundLatitudeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  SouthBoundLatitudeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  SouthBoundLatitudeField} record.
                     */
                    @JsonCreator
                    public static SouthBoundLatitudeField of(@JsonProperty("@value") String value) {
                        return new SouthBoundLatitudeField(value);
                    }

                    /**
                     * Gets the datatype associated with the record.
                     *
                     * @return The datatype string.
                     */
                    @JsonView(CoreView.class)
                    @JsonProperty("@type")
                    public String getDatatype() {
                        return "xsd:decimal";
                    }
                }

                /**
                 * Northern latitudinal dimension of box. Latitude of the geographic point
                 * expressed in decimal degrees (positive north). Domain: -90.00 ≤
                 * northBoundingLatitude ≤ 90.00.
                 */
                @JsonPropertyOrder({"@type", "@value"})
                public record NorthBoundLatitudeField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  NorthBoundLatitudeField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  NorthBoundLatitudeField} record.
                     */
                    public static NorthBoundLatitudeField of() {
                        return new NorthBoundLatitudeField(null);
                    }

                    /**
                     * Creates an instance of the {@code  NorthBoundLatitudeField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  NorthBoundLatitudeField} record.
                     */
                    @JsonCreator
                    public static NorthBoundLatitudeField of(@JsonProperty("@value") String value) {
                        return new NorthBoundLatitudeField(value);
                    }

                    /**
                     * Gets the datatype associated with the record.
                     *
                     * @return The datatype string.
                     */
                    @JsonView(CoreView.class)
                    @JsonProperty("@type")
                    public String getDatatype() {
                        return "xsd:decimal";
                    }
                }
            }
        }

        public record GeoLocationElementList(List<GeoLocationElement> geoLocationList) implements ArtifactList {

            public static GeoLocationElementList of() {
                return new GeoLocationElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return geoLocationList.size() >= minItems() && geoLocationList.size() <= maxItems();
            }

            @JsonCreator
            public static GeoLocationElementList of(List<GeoLocationElement> geoLocationList) {
                return new GeoLocationElementList(geoLocationList);
            }

            public static GeoLocationElementList of(GeoLocationElement geoLocation) {
                return new GeoLocationElementList(List.of(geoLocation));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(geoLocationList);
            }
        }

        public record FundingReferenceElement(@JsonProperty("@id") String id,
                                              @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.funderName) FunderNameField funderName,
                                              @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.funderIdentifier) FunderIdentifierField funderIdentifier,
                                              @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.funderIdentifierType) FunderIdentifierTypeField funderIdentifierType,
                                              @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri,
                                              @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.awardNumber) AwardNumberField awardNumber,
                                              @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.awardURI) AwardUriField awardUri,
                                              @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.awardTitle) AwardTitleField awardTitle)
            implements
            Element {

            /**
             * Gets an empty FundingReferenceElement list.
             */
            public static FundingReferenceElement of() {
                return new FundingReferenceElement(generateId(), FunderNameField.of(), FunderIdentifierField.of(),
                    FunderIdentifierTypeField.of(), SchemeUriField.of(), AwardNumberField.of(), AwardUriField.of(),
                    AwardTitleField.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(funderName, funderIdentifier, funderIdentifierType, schemeUri, awardNumber,
                    awardUri, awardTitle);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.funderName,
                    "https://schema.metadatacenter.org/properties/18666fcd-6f8e-4886-90f7-153d1193985c");
                contextMap.put(FieldNames.funderIdentifier,
                    "https://schema.metadatacenter.org/properties/67927c8d-4572-4ba5-b5da-0c1d89f77c7c");
                contextMap.put(FieldNames.funderIdentifierType,
                    "https://schema.metadatacenter.org/properties/a666abd0-8f05-43d7-9aea-dab007d4454a");
                contextMap.put(FieldNames.schemeURI,
                    "https://schema.metadatacenter.org/properties/b6061a0d-d09c-46ad-a9da-813034ce6b9f");
                contextMap.put(FieldNames.awardNumber,
                    "https://schema.metadatacenter.org/properties/3aa01f27-0495-43e9-9bf4-b89c8ce8ea32");
                contextMap.put(FieldNames.awardURI,
                    "https://schema.metadatacenter.org/properties/30a02941-ea11-433d-b145-c1694997e152");
                contextMap.put(FieldNames.awardTitle,
                    "https://schema.metadatacenter.org/properties/9afc27b4-407a-4079-98c5-e7669f012d69");
                return contextMap;
            }
            /**
             * Name of the funding provider
             */
            public record FunderNameField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  FunderNameField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  FunderNameField} record.
                 */
                public static FunderNameField of() {
                    return new FunderNameField(null);
                }

                /**
                 * Creates an instance of the {@code  FunderNameField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  FunderNameField} record.
                 */
                @JsonCreator
                public static FunderNameField of(@JsonProperty("@value") String value) {
                    return new FunderNameField(value);
                }
            }

            /**
             * Uniquely identifies a funding entity, according to various types. Example:
             * https://doi.org/10.13039/100000936
             */
            public record FunderIdentifierField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  FunderIdentifierField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  FunderIdentifierField} record.
                 */
                public static FunderIdentifierField of() {
                    return new FunderIdentifierField(null);
                }

                /**
                 * Creates an instance of the {@code  FunderIdentifierField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  FunderIdentifierField} record.
                 */
                @JsonCreator
                public static FunderIdentifierField of(@JsonProperty("@value") String value) {
                    return new FunderIdentifierField(value);
                }
            }

            /**
             * The type of the funderIdentifier
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record FunderIdentifierTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty FunderIdentifierTypeField instance, with null values for the
                 * id and label.
                 */
                public static FunderIdentifierTypeField of() {
                    return new FunderIdentifierTypeField(null, null);
                }

                /**
                 * Create an instance of FunderIdentifierTypeField with the specified id and
                 * label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static FunderIdentifierTypeField of(@JsonProperty("@id") String id,
                                                           @JsonProperty("rdfs:label") String label) {
                    return new FunderIdentifierTypeField(id, label);
                }
            }

            /**
             * The URI of the funder identifier scheme
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record SchemeUriField(String id) implements IriField {

                /**
                 * Creates an empty SchemeUriField instance, with null values for the id and
                 * label.
                 */
                public static SchemeUriField of() {
                    return new SchemeUriField(null);
                }

                /**
                 * Create an instance of SchemeUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static SchemeUriField of(@JsonProperty("@id") String id) {
                    return new SchemeUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }

            /**
             * The code assigned by the funder to a sponsored award (grant).
             */
            public record AwardNumberField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  AwardNumberField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  AwardNumberField} record.
                 */
                public static AwardNumberField of() {
                    return new AwardNumberField(null);
                }

                /**
                 * Creates an instance of the {@code  AwardNumberField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  AwardNumberField} record.
                 */
                @JsonCreator
                public static AwardNumberField of(@JsonProperty("@value") String value) {
                    return new AwardNumberField(value);
                }
            }

            /**
             * The URI leading to a page provided by the funder for more information about
             * the award (grant).
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record AwardUriField(String id) implements IriField {

                /**
                 * Creates an empty AwardUriField instance, with null values for the id and
                 * label.
                 */
                public static AwardUriField of() {
                    return new AwardUriField(null);
                }

                /**
                 * Create an instance of AwardUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static AwardUriField of(@JsonProperty("@id") String id) {
                    return new AwardUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }

            /**
             * The human readable title or name of the award (grant).
             */
            public record AwardTitleField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  AwardTitleField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  AwardTitleField} record.
                 */
                public static AwardTitleField of() {
                    return new AwardTitleField(null);
                }

                /**
                 * Creates an instance of the {@code  AwardTitleField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  AwardTitleField} record.
                 */
                @JsonCreator
                public static AwardTitleField of(@JsonProperty("@value") String value) {
                    return new AwardTitleField(value);
                }
            }
        }

        public record FundingReferenceElementList(
            List<FundingReferenceElement> fundingReferenceList) implements ArtifactList {

            public static FundingReferenceElementList of() {
                return new FundingReferenceElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return fundingReferenceList.size() >= minItems() && fundingReferenceList.size() <= maxItems();
            }

            @JsonCreator
            public static FundingReferenceElementList of(List<FundingReferenceElement> fundingReferenceList) {
                return new FundingReferenceElementList(fundingReferenceList);
            }

            public static FundingReferenceElementList of(FundingReferenceElement fundingReference) {
                return new FundingReferenceElementList(List.of(fundingReference));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(fundingReferenceList);
            }
        }

        public record RelatedItemElement(@JsonProperty("@id") String id,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedItemType) RelatedItemTypeField relatedItemType,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relationType) RelationTypeField relationType,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedIdentifier) RelatedIdentifierField relatedIdentifier,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedIdentifierType) RelatedIdentifierTypeField relatedIdentifierType,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedMetadataScheme) RelatedMetadataSchemeField relatedMetadataScheme,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeURI) SchemeUriField schemeUri,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.schemeType) SchemeTypeField schemeType,
                                         @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.title) TitleElementList title,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedItemCreator) RelatedItemCreatorElementList relatedItemCreator,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.number) NumberField number,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.numberType) NumberTypeField numberType,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.volume) VolumeField volume,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.issue) IssueField issue,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.firstPage) FirstPageField firstPage,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.lastPage) LastPageField lastPage,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.publicationYear) PublicationYearField publicationYear,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.publisher) PublisherField publisher,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.edition) EditionField edition,
                                         @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.relatedItemContributor) RelatedItemContributorElementList relatedItemContributor)
            implements
            Element {

            /**
             * Gets an empty RelatedItemElement list.
             */
            public static RelatedItemElement of() {
                return new RelatedItemElement(generateId(), RelatedItemTypeField.of(), RelationTypeField.of(),
                    RelatedIdentifierField.of(), RelatedIdentifierTypeField.of(), RelatedMetadataSchemeField.of(),
                    SchemeUriField.of(), SchemeTypeField.of(), TitleElementList.of(),
                    RelatedItemCreatorElementList.of(), NumberField.of(), NumberTypeField.of(), VolumeField.of(),
                    IssueField.of(), FirstPageField.of(), LastPageField.of(), PublicationYearField.of(),
                    PublisherField.of(), EditionField.of(), RelatedItemContributorElementList.of());
            }

            /**
             * Returns the child artifacts as a flat stream. Lists of children are flattened
             * out.
             */
            @JsonIgnore
            public Stream<Artifact> getArtifacts() {
                return streamArtifacts(relatedItemType, relationType, relatedIdentifier, relatedIdentifierType,
                    relatedMetadataScheme, schemeUri, schemeType, title, relatedItemCreator, number, numberType,
                    volume, issue, firstPage, lastPage, publicationYear, publisher, edition,
                    relatedItemContributor);
            }

            /**
             * Gets the JSON-LD context for this element. This is a fixed value and does not
             * depend upon the content of child elements/fields.
             */
            @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
            public Map<String, Object> context() {
                var contextMap = new LinkedHashMap<String, Object>();
                contextMap.put(FieldNames.relatedItemType,
                    "https://schema.metadatacenter.org/properties/234f4448-88d6-461c-9ea0-fd465cdbbbf1");
                contextMap.put(FieldNames.relationType,
                    "https://schema.metadatacenter.org/properties/3975c4a4-e8be-45e9-9d6d-fc51812d9d4c");
                contextMap.put(FieldNames.relatedIdentifier,
                    "https://schema.metadatacenter.org/properties/767e3ad6-099d-4093-a319-540c12543b65");
                contextMap.put(FieldNames.relatedIdentifierType,
                    "https://schema.metadatacenter.org/properties/90e2b96c-98fe-4841-ba09-fc968ad76ad7");
                contextMap.put(FieldNames.relatedMetadataScheme,
                    "https://schema.metadatacenter.org/properties/cfb49da2-d22e-4820-9138-c3ea327162f9");
                contextMap.put(FieldNames.schemeURI,
                    "https://schema.metadatacenter.org/properties/852ca099-7cac-4d3a-bd66-5dd08e977ea0");
                contextMap.put(FieldNames.schemeType,
                    "https://schema.metadatacenter.org/properties/6ec28104-3ec3-436b-9290-a76d261e3191");
                contextMap.put(FieldNames.title,
                    "https://schema.metadatacenter.org/properties/60a7b270-006a-4964-aea8-e314131c19b4");
                contextMap.put(FieldNames.relatedItemCreator,
                    "https://schema.metadatacenter.org/properties/cbf702a6-dd73-434b-97d7-6bdb2e30b2ed");
                contextMap.put(FieldNames.number,
                    "https://schema.metadatacenter.org/properties/fbf488d1-8dd3-4eac-bf7a-d27999756900");
                contextMap.put(FieldNames.numberType,
                    "https://schema.metadatacenter.org/properties/96efcb35-06cd-4c37-b12c-6247def3bf76");
                contextMap.put(FieldNames.volume,
                    "https://schema.metadatacenter.org/properties/9bdeae88-1488-468a-b94a-e15470e2a9cf");
                contextMap.put(FieldNames.issue,
                    "https://schema.metadatacenter.org/properties/e402cccc-d8b2-41a3-baf3-516a296e780c");
                contextMap.put(FieldNames.firstPage,
                    "https://schema.metadatacenter.org/properties/344171db-4639-4c95-9785-70a0182b3cba");
                contextMap.put(FieldNames.lastPage,
                    "https://schema.metadatacenter.org/properties/4f8e526e-98bd-46c6-b42f-a569c5cc093b");
                contextMap.put(FieldNames.publicationYear,
                    "https://schema.metadatacenter.org/properties/ffedb4fa-e352-416d-a7cb-8757cb7bfcea");
                contextMap.put(FieldNames.publisher,
                    "https://schema.metadatacenter.org/properties/6709e7aa-d79b-4f0b-b3b2-e2c4aa84e9f2");
                contextMap.put(FieldNames.edition,
                    "https://schema.metadatacenter.org/properties/97dd4fbb-b5c2-4d80-b410-b48315470d9c");
                contextMap.put(FieldNames.relatedItemContributor,
                    "https://schema.metadatacenter.org/properties/c7ef98c4-bd86-4f97-a052-224bfdf80974");
                return contextMap;
            }
            /**
             * The type of the related item, e.g., journal article; book or chapter
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record RelatedItemTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty RelatedItemTypeField instance, with null values for the id
                 * and label.
                 */
                public static RelatedItemTypeField of() {
                    return new RelatedItemTypeField(null, null);
                }

                /**
                 * Create an instance of RelatedItemTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static RelatedItemTypeField of(@JsonProperty("@id") String id,
                                                      @JsonProperty("rdfs:label") String label) {
                    return new RelatedItemTypeField(id, label);
                }
            }

            /**
             * Description of the relationship of the resource being registered (A) and the
             * related resource (B).
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record RelationTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty RelationTypeField instance, with null values for the id and
                 * label.
                 */
                public static RelationTypeField of() {
                    return new RelationTypeField(null, null);
                }

                /**
                 * Create an instance of RelationTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static RelationTypeField of(@JsonProperty("@id") String id,
                                                   @JsonProperty("rdfs:label") String label) {
                    return new RelationTypeField(id, label);
                }
            }

            /**
             * Identifiers of related resources. These must be globally unique identifiers.
             */
            public record RelatedIdentifierField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  RelatedIdentifierField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  RelatedIdentifierField} record.
                 */
                public static RelatedIdentifierField of() {
                    return new RelatedIdentifierField(null);
                }

                /**
                 * Creates an instance of the {@code  RelatedIdentifierField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  RelatedIdentifierField} record.
                 */
                @JsonCreator
                public static RelatedIdentifierField of(@JsonProperty("@value") String value) {
                    return new RelatedIdentifierField(value);
                }
            }

            /**
             * The type of the RelatedIdentifier. If relatedIdentifier is used,
             * relatedIdentifierType is mandatory.
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record RelatedIdentifierTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty RelatedIdentifierTypeField instance, with null values for
                 * the id and label.
                 */
                public static RelatedIdentifierTypeField of() {
                    return new RelatedIdentifierTypeField(null, null);
                }

                /**
                 * Create an instance of RelatedIdentifierTypeField with the specified id and
                 * label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static RelatedIdentifierTypeField of(@JsonProperty("@id") String id,
                                                            @JsonProperty("rdfs:label") String label) {
                    return new RelatedIdentifierTypeField(id, label);
                }
            }

            /**
             * The name of the scheme
             */
            public record RelatedMetadataSchemeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  RelatedMetadataSchemeField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  RelatedMetadataSchemeField} record.
                 */
                public static RelatedMetadataSchemeField of() {
                    return new RelatedMetadataSchemeField(null);
                }

                /**
                 * Creates an instance of the {@code  RelatedMetadataSchemeField} record with
                 * the specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  RelatedMetadataSchemeField} record.
                 */
                @JsonCreator
                public static RelatedMetadataSchemeField of(@JsonProperty("@value") String value) {
                    return new RelatedMetadataSchemeField(value);
                }
            }

            /**
             * The URI of the Related Metadata Scheme
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record SchemeUriField(String id) implements IriField {

                /**
                 * Creates an empty SchemeUriField instance, with null values for the id and
                 * label.
                 */
                public static SchemeUriField of() {
                    return new SchemeUriField(null);
                }

                /**
                 * Create an instance of SchemeUriField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 */
                @JsonCreator
                public static SchemeUriField of(@JsonProperty("@id") String id) {
                    return new SchemeUriField(id);
                }

                @Override
                public String label() {
                    return "";
                }
            }

            /**
             * The type of the Related Metadata Scheme, linked with the Scheme URI.
             */
            public record SchemeTypeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  SchemeTypeField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  SchemeTypeField} record.
                 */
                public static SchemeTypeField of() {
                    return new SchemeTypeField(null);
                }

                /**
                 * Creates an instance of the {@code  SchemeTypeField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  SchemeTypeField} record.
                 */
                @JsonCreator
                public static SchemeTypeField of(@JsonProperty("@value") String value) {
                    return new SchemeTypeField(value);
                }
            }

            public record TitleElement(@JsonProperty("@id") String id,
                                       @Nonnull @JsonView(CoreView.class) @JsonProperty(FieldNames.title) TitleField2 title,
                                       @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.titleType) TitleTypeField titleType)
                implements
                Element {

                /**
                 * Gets an empty TitleElement list.
                 */
                public static TitleElement of() {
                    return new TitleElement(generateId(), TitleField2.of(), TitleTypeField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(title, titleType);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.title,
                        "https://schema.metadatacenter.org/properties/c9a3f95f-d076-4a78-9b30-93ba8caf8d05");
                    contextMap.put(FieldNames.titleType,
                        "https://schema.metadatacenter.org/properties/9b5eb75f-3355-4ddb-903d-c716f4816b12");
                    return contextMap;
                }
                /**
                 * A name or title by which a resource is known.
                 */
                public record TitleField2(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  TitleField2} record with a {@code  null}
                     * value.
                     *
                     * @return An instance of the {@code  TitleField2} record.
                     */
                    public static TitleField2 of() {
                        return new TitleField2(null);
                    }

                    /**
                     * Creates an instance of the {@code  TitleField2} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  TitleField2} record.
                     */
                    @JsonCreator
                    public static TitleField2 of(@JsonProperty("@value") String value) {
                        return new TitleField2(value);
                    }
                }

                /**
                 * The type of Title (other than the Main Title)
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record TitleTypeField(String id, String label) implements IriField {

                    /**
                     * Creates an empty TitleTypeField instance, with null values for the id and
                     * label.
                     */
                    public static TitleTypeField of() {
                        return new TitleTypeField(null, null);
                    }

                    /**
                     * Create an instance of TitleTypeField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     * @param label
                     *            The rdfs:label for the specified id.
                     */
                    @JsonCreator
                    public static TitleTypeField of(@JsonProperty("@id") String id,
                                                    @JsonProperty("rdfs:label") String label) {
                        return new TitleTypeField(id, label);
                    }
                }
            }

            public record TitleElementList(List<TitleElement> titleList) implements ArtifactList {

                public static TitleElementList of() {
                    return new TitleElementList(List.of());
                }

                public int minItems() {
                    return 0;
                }

                public int maxItems() {
                    return Integer.MAX_VALUE;
                }

                public boolean isCardinalitySatisfied() {
                    return titleList.size() >= minItems() && titleList.size() <= maxItems();
                }

                @JsonCreator
                public static TitleElementList of(List<TitleElement> titleList) {
                    return new TitleElementList(titleList);
                }

                public static TitleElementList of(TitleElement title) {
                    return new TitleElementList(List.of(title));
                }

                @Override
                public List<Artifact> getArtifacts() {
                    return new ArrayList<>(titleList);
                }
            }

            public record RelatedItemCreatorElement(@JsonProperty("@id") String id,
                                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.creatorName) CreatorNameField creatorName,
                                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameType) NameTypeField nameType,
                                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.givenName) GivenNameField givenName,
                                                    @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.familyName) FamilyNameField familyName)
                implements
                Element {

                /**
                 * Gets an empty RelatedItemCreatorElement list.
                 */
                public static RelatedItemCreatorElement of() {
                    return new RelatedItemCreatorElement(generateId(), CreatorNameField.of(), NameTypeField.of(),
                        GivenNameField.of(), FamilyNameField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(creatorName, nameType, givenName, familyName);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.creatorName,
                        "https://schema.metadatacenter.org/properties/bfe3ce47-a7dc-43d6-85d7-74aad2cf2193");
                    contextMap.put(FieldNames.nameType,
                        "https://schema.metadatacenter.org/properties/7797c67e-e0fa-4b84-a8d8-1d4e4af1528d");
                    contextMap.put(FieldNames.givenName,
                        "https://schema.metadatacenter.org/properties/7ed14f4e-e78e-4ccc-a5a7-986ad2ad5d87");
                    contextMap.put(FieldNames.familyName,
                        "https://schema.metadatacenter.org/properties/9c23ca96-f662-4651-8f71-8a964a6070a0");
                    return contextMap;
                }
                /**
                 * The full name of the related item creator
                 */
                public record CreatorNameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  CreatorNameField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  CreatorNameField} record.
                     */
                    public static CreatorNameField of() {
                        return new CreatorNameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  CreatorNameField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  CreatorNameField} record.
                     */
                    @JsonCreator
                    public static CreatorNameField of(@JsonProperty("@value") String value) {
                        return new CreatorNameField(value);
                    }
                }

                /**
                 * Name Type
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record NameTypeField(String id, String label) implements IriField {

                    /**
                     * Creates an empty NameTypeField instance, with null values for the id and
                     * label.
                     */
                    public static NameTypeField of() {
                        return new NameTypeField(null, null);
                    }

                    /**
                     * Create an instance of NameTypeField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     * @param label
                     *            The rdfs:label for the specified id.
                     */
                    @JsonCreator
                    public static NameTypeField of(@JsonProperty("@id") String id,
                                                   @JsonProperty("rdfs:label") String label) {
                        return new NameTypeField(id, label);
                    }
                }

                /**
                 * The personal or first name of the creator
                 */
                public record GivenNameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  GivenNameField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  GivenNameField} record.
                     */
                    public static GivenNameField of() {
                        return new GivenNameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  GivenNameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  GivenNameField} record.
                     */
                    @JsonCreator
                    public static GivenNameField of(@JsonProperty("@value") String value) {
                        return new GivenNameField(value);
                    }
                }

                /**
                 * The surname or last name of the creator
                 */
                public record FamilyNameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  FamilyNameField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  FamilyNameField} record.
                     */
                    public static FamilyNameField of() {
                        return new FamilyNameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  FamilyNameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  FamilyNameField} record.
                     */
                    @JsonCreator
                    public static FamilyNameField of(@JsonProperty("@value") String value) {
                        return new FamilyNameField(value);
                    }
                }
            }

            public record RelatedItemCreatorElementList(
                List<RelatedItemCreatorElement> relatedItemCreatorList) implements ArtifactList {

                public static RelatedItemCreatorElementList of() {
                    return new RelatedItemCreatorElementList(List.of());
                }

                public int minItems() {
                    return 0;
                }

                public int maxItems() {
                    return Integer.MAX_VALUE;
                }

                public boolean isCardinalitySatisfied() {
                    return relatedItemCreatorList.size() >= minItems() && relatedItemCreatorList.size() <= maxItems();
                }

                @JsonCreator
                public static RelatedItemCreatorElementList of(List<RelatedItemCreatorElement> relatedItemCreatorList) {
                    return new RelatedItemCreatorElementList(relatedItemCreatorList);
                }

                public static RelatedItemCreatorElementList of(RelatedItemCreatorElement relatedItemCreator) {
                    return new RelatedItemCreatorElementList(List.of(relatedItemCreator));
                }

                @Override
                public List<Artifact> getArtifacts() {
                    return new ArrayList<>(relatedItemCreatorList);
                }
            }

            /**
             * Type of the related item’s number, e.g., report number or article number.
             */
            public record NumberField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  NumberField} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  NumberField} record.
                 */
                public static NumberField of() {
                    return new NumberField(null);
                }

                /**
                 * Creates an instance of the {@code  NumberField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  NumberField} record.
                 */
                @JsonCreator
                public static NumberField of(@JsonProperty("@value") String value) {
                    return new NumberField(value);
                }
            }

            /**
             * Type of the related item’s number, e.g., report number or article number
             */
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public record NumberTypeField(String id, String label) implements IriField {

                /**
                 * Creates an empty NumberTypeField instance, with null values for the id and
                 * label.
                 */
                public static NumberTypeField of() {
                    return new NumberTypeField(null, null);
                }

                /**
                 * Create an instance of NumberTypeField with the specified id and label.
                 *
                 * @param id
                 *            The id. This is an IRI.
                 * @param label
                 *            The rdfs:label for the specified id.
                 */
                @JsonCreator
                public static NumberTypeField of(@JsonProperty("@id") String id,
                                                 @JsonProperty("rdfs:label") String label) {
                    return new NumberTypeField(id, label);
                }
            }

            /**
             * Volume of the related item.
             */
            public record VolumeField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  VolumeField} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  VolumeField} record.
                 */
                public static VolumeField of() {
                    return new VolumeField(null);
                }

                /**
                 * Creates an instance of the {@code  VolumeField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  VolumeField} record.
                 */
                @JsonCreator
                public static VolumeField of(@JsonProperty("@value") String value) {
                    return new VolumeField(value);
                }
            }

            /**
             * Issue number or name of the related item
             */
            public record IssueField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  IssueField} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  IssueField} record.
                 */
                public static IssueField of() {
                    return new IssueField(null);
                }

                /**
                 * Creates an instance of the {@code  IssueField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  IssueField} record.
                 */
                @JsonCreator
                public static IssueField of(@JsonProperty("@value") String value) {
                    return new IssueField(value);
                }
            }

            /**
             */
            public record FirstPageField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  FirstPageField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  FirstPageField} record.
                 */
                public static FirstPageField of() {
                    return new FirstPageField(null);
                }

                /**
                 * Creates an instance of the {@code  FirstPageField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  FirstPageField} record.
                 */
                @JsonCreator
                public static FirstPageField of(@JsonProperty("@value") String value) {
                    return new FirstPageField(value);
                }
            }

            /**
             * Last page of the related item, e.g., of the chapter, article, or conference
             * paper in proceedings
             */
            public record LastPageField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  LastPageField} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  LastPageField} record.
                 */
                public static LastPageField of() {
                    return new LastPageField(null);
                }

                /**
                 * Creates an instance of the {@code  LastPageField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  LastPageField} record.
                 */
                @JsonCreator
                public static LastPageField of(@JsonProperty("@value") String value) {
                    return new LastPageField(value);
                }
            }

            /**
             */
            @JsonPropertyOrder({"@type", "@value"})
            public record PublicationYearField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  PublicationYearField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  PublicationYearField} record.
                 */
                public static PublicationYearField of() {
                    return new PublicationYearField(null);
                }

                /**
                 * Creates an instance of the {@code  PublicationYearField} record with the
                 * specified value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  PublicationYearField} record.
                 */
                @JsonCreator
                public static PublicationYearField of(@JsonProperty("@value") String value) {
                    return new PublicationYearField(value);
                }

                /**
                 * Gets the datatype associated with the record.
                 *
                 * @return The datatype string.
                 */
                @JsonView(CoreView.class)
                @JsonProperty("@type")
                public String getDatatype() {
                    return "xsd:date";
                }
            }

            /**
             * The name of the entity that holds, archives, publishes prints, distributes,
             * releases, issues, or produces the resource.
             */
            public record PublisherField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  PublisherField} record with a
                 * {@code  null} value.
                 *
                 * @return An instance of the {@code  PublisherField} record.
                 */
                public static PublisherField of() {
                    return new PublisherField(null);
                }

                /**
                 * Creates an instance of the {@code  PublisherField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  PublisherField} record.
                 */
                @JsonCreator
                public static PublisherField of(@JsonProperty("@value") String value) {
                    return new PublisherField(value);
                }
            }

            /**
             */
            public record EditionField(String value) implements LiteralField {

                /**
                 * Creates an instance of the {@code  EditionField} record with a {@code  null}
                 * value.
                 *
                 * @return An instance of the {@code  EditionField} record.
                 */
                public static EditionField of() {
                    return new EditionField(null);
                }

                /**
                 * Creates an instance of the {@code  EditionField} record with the specified
                 * value.
                 *
                 * @param value
                 *            The value to set for the record.
                 * @return An instance of the {@code  EditionField} record.
                 */
                @JsonCreator
                public static EditionField of(@JsonProperty("@value") String value) {
                    return new EditionField(value);
                }
            }

            public record RelatedItemContributorElement(@JsonProperty("@id") String id,
                                                        @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.contributorType) ContributorTypeField contributorType,
                                                        @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.contributorName) ContributorNameField contributorName,
                                                        @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.nameType) NameTypeField nameType,
                                                        @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.givenName) GivenNameField givenName,
                                                        @Nullable @JsonView(CoreView.class) @JsonProperty(FieldNames.familyName) FamilyNameField familyName)
                implements
                Element {

                /**
                 * Gets an empty RelatedItemContributorElement list.
                 */
                public static RelatedItemContributorElement of() {
                    return new RelatedItemContributorElement(generateId(), ContributorTypeField.of(),
                        ContributorNameField.of(), NameTypeField.of(), GivenNameField.of(), FamilyNameField.of());
                }

                /**
                 * Returns the child artifacts as a flat stream. Lists of children are flattened
                 * out.
                 */
                @JsonIgnore
                public Stream<Artifact> getArtifacts() {
                    return streamArtifacts(contributorType, contributorName, nameType, givenName, familyName);
                }

                /**
                 * Gets the JSON-LD context for this element. This is a fixed value and does not
                 * depend upon the content of child elements/fields.
                 */
                @JsonProperty(value = "@context", access = JsonProperty.Access.READ_ONLY)
                public Map<String, Object> context() {
                    var contextMap = new LinkedHashMap<String, Object>();
                    contextMap.put(FieldNames.contributorType,
                        "https://schema.metadatacenter.org/properties/b44ecdc0-b566-4125-bb14-16323bfe7040");
                    contextMap.put(FieldNames.contributorName,
                        "https://schema.metadatacenter.org/properties/7fedc824-9cf4-49c9-8c99-2cc49b291074");
                    contextMap.put(FieldNames.nameType,
                        "https://schema.metadatacenter.org/properties/acbdae1a-e966-4e79-b6fb-1dfb549ba5b0");
                    contextMap.put(FieldNames.givenName,
                        "https://schema.metadatacenter.org/properties/e8598641-9631-414d-9179-350eae3895b0");
                    contextMap.put(FieldNames.familyName,
                        "https://schema.metadatacenter.org/properties/e26a346a-b2f4-4f0d-a7ff-1ed02badc3be");
                    return contextMap;
                }
                /**
                 * The type of contributor of the resource
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record ContributorTypeField(String id, String label) implements IriField {

                    /**
                     * Creates an empty ContributorTypeField instance, with null values for the id
                     * and label.
                     */
                    public static ContributorTypeField of() {
                        return new ContributorTypeField(null, null);
                    }

                    /**
                     * Create an instance of ContributorTypeField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     * @param label
                     *            The rdfs:label for the specified id.
                     */
                    @JsonCreator
                    public static ContributorTypeField of(@JsonProperty("@id") String id,
                                                          @JsonProperty("rdfs:label") String label) {
                        return new ContributorTypeField(id, label);
                    }
                }

                /**
                 * The full name of the contributor
                 */
                public record ContributorNameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  ContributorNameField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  ContributorNameField} record.
                     */
                    public static ContributorNameField of() {
                        return new ContributorNameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  ContributorNameField} record with the
                     * specified value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  ContributorNameField} record.
                     */
                    @JsonCreator
                    public static ContributorNameField of(@JsonProperty("@value") String value) {
                        return new ContributorNameField(value);
                    }
                }

                /**
                 * Name Type
                 */
                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                public record NameTypeField(String id, String label) implements IriField {

                    /**
                     * Creates an empty NameTypeField instance, with null values for the id and
                     * label.
                     */
                    public static NameTypeField of() {
                        return new NameTypeField(null, null);
                    }

                    /**
                     * Create an instance of NameTypeField with the specified id and label.
                     *
                     * @param id
                     *            The id. This is an IRI.
                     * @param label
                     *            The rdfs:label for the specified id.
                     */
                    @JsonCreator
                    public static NameTypeField of(@JsonProperty("@id") String id,
                                                   @JsonProperty("rdfs:label") String label) {
                        return new NameTypeField(id, label);
                    }
                }

                /**
                 * The personal or first name of the contributor
                 */
                public record GivenNameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  GivenNameField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  GivenNameField} record.
                     */
                    public static GivenNameField of() {
                        return new GivenNameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  GivenNameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  GivenNameField} record.
                     */
                    @JsonCreator
                    public static GivenNameField of(@JsonProperty("@value") String value) {
                        return new GivenNameField(value);
                    }
                }

                /**
                 * The surname or last name of the contributor
                 */
                public record FamilyNameField(String value) implements LiteralField {

                    /**
                     * Creates an instance of the {@code  FamilyNameField} record with a
                     * {@code  null} value.
                     *
                     * @return An instance of the {@code  FamilyNameField} record.
                     */
                    public static FamilyNameField of() {
                        return new FamilyNameField(null);
                    }

                    /**
                     * Creates an instance of the {@code  FamilyNameField} record with the specified
                     * value.
                     *
                     * @param value
                     *            The value to set for the record.
                     * @return An instance of the {@code  FamilyNameField} record.
                     */
                    @JsonCreator
                    public static FamilyNameField of(@JsonProperty("@value") String value) {
                        return new FamilyNameField(value);
                    }
                }
            }

            public record RelatedItemContributorElementList(
                List<RelatedItemContributorElement> relatedItemContributorList) implements ArtifactList {

                public static RelatedItemContributorElementList of() {
                    return new RelatedItemContributorElementList(List.of());
                }

                public int minItems() {
                    return 0;
                }

                public int maxItems() {
                    return Integer.MAX_VALUE;
                }

                public boolean isCardinalitySatisfied() {
                    return relatedItemContributorList.size() >= minItems()
                        && relatedItemContributorList.size() <= maxItems();
                }

                @JsonCreator
                public static RelatedItemContributorElementList of(
                    List<RelatedItemContributorElement> relatedItemContributorList) {
                    return new RelatedItemContributorElementList(relatedItemContributorList);
                }

                public static RelatedItemContributorElementList of(
                    RelatedItemContributorElement relatedItemContributor) {
                    return new RelatedItemContributorElementList(List.of(relatedItemContributor));
                }

                @Override
                public List<Artifact> getArtifacts() {
                    return new ArrayList<>(relatedItemContributorList);
                }
            }
        }

        public record RelatedItemElementList(List<RelatedItemElement> relatedItemList) implements ArtifactList {

            public static RelatedItemElementList of() {
                return new RelatedItemElementList(List.of());
            }

            public int minItems() {
                return 0;
            }

            public int maxItems() {
                return Integer.MAX_VALUE;
            }

            public boolean isCardinalitySatisfied() {
                return relatedItemList.size() >= minItems() && relatedItemList.size() <= maxItems();
            }

            @JsonCreator
            public static RelatedItemElementList of(List<RelatedItemElement> relatedItemList) {
                return new RelatedItemElementList(relatedItemList);
            }

            public static RelatedItemElementList of(RelatedItemElement relatedItem) {
                return new RelatedItemElementList(List.of(relatedItem));
            }

            @Override
            public List<Artifact> getArtifacts() {
                return new ArrayList<>(relatedItemList);
            }
        }
    }

    public record MetadataInstanceList(List<MetadataInstance> cedarList) implements ArtifactList {

        public static MetadataInstanceList of() {
            return new MetadataInstanceList(List.of());
        }

        public int minItems() {
            return 0;
        }

        public int maxItems() {
            return Integer.MAX_VALUE;
        }

        public boolean isCardinalitySatisfied() {
            return cedarList.size() >= minItems() && cedarList.size() <= maxItems();
        }

        @JsonCreator
        public static MetadataInstanceList of(List<MetadataInstance> cedarList) {
            return new MetadataInstanceList(cedarList);
        }

        public static MetadataInstanceList of(MetadataInstance cedar) {
            return new MetadataInstanceList(List.of(cedar));
        }

        @Override
        public List<Artifact> getArtifacts() {
            return new ArrayList<>(cedarList);
        }
    }
}
