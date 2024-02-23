package org.metadatacenter.cedar.bridge;

import org.metadatacenter.cedar.bridge.resource.datacite.DataciteConstants;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.AlternateIdentifierElement.AlternateIdentifierField2;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.AlternateIdentifierElement.AlternateIdentifierTypeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.DateElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.DescriptionElement.DescriptionField2;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.DescriptionElement.DescriptionTypeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.FundingReferenceElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.GeoLocationBoxElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.GeoLocationPointElement.PointLatitudeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.GeoLocationPointElement.PointLongitudeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.RightsElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.SubjectElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.generateId;

/*
 * Generates a default instance for the DOI request form (URL, Year, Publisher, resourceType)
 */
public class GenerateMetadataInstanceTests {
  private static final String ELEMENT_IRI_PREFIX = "https://repo.metadatacenter.org/template-element-instances/";
  private static final String INSTANCE_IRI_PREFIX = "https://repo.metadatacenter.org/template-instances/";
  private static final String TEMPLATE_ID = "https://repo.metadatacenter.org/templates/5b6e0952-8a56-4f97-a35d-7ce784773b57";
  private static final String USER_ID = "https://metadatacenter.org/users/6124554b-9c83-443c-8207-241d75b82f44";
  private static final String PUBLISHER = DataciteConstants.PUBLISHER;
  private static final String publicationYear = Year.now().getValue() + "-01-01";
  private static final String nowAsString = Instant.ofEpochSecond(System.currentTimeMillis() / 1000).toString();

  private static String PREFIX = null;

  public static MetadataInstance getInstanceRequiredOnly() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required Only Test",
        "Required properties only",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRichMetadata() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Rich Metadata Test",
        "Has data in each element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(List.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("John"),
                CreatorElement.FamilyNameField.of("Doe"),
                CreatorElement.AffiliationElementList.of(List.of(
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of("affiliation name"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/04wxnsj81"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of()),
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of(),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/05dxps055"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri2"))
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                )),
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("Potter Summer"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("Potter"),
                CreatorElement.FamilyNameField.of("Summer"),
                CreatorElement.AffiliationElementList.of(
                    new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.AffiliationElement.NameField.of(),
                        CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/05dxps055"),
                        CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        CreatorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri2")
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                ))
        )),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Rich Metadata Test"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other"))
        ),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(List.of(
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("000")),
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("111"))
        )),
        ContributorElementList.of(List.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Starr, Joan"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Joan"),
                ContributorElement.FamilyNameField.of("Starr"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                )),
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Chou, Jimmy"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Jimmy"),
                ContributorElement.FamilyNameField.of("Chou"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataManager", "DataManager"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCID18"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                ))
        )),
        DateElementList.of(List.of(
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-12"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Created", "Created"),
                DateInformationField.of()),
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-08"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Issued", "Issued"),
                DateInformationField.of())
        )),
        LanguageField.of("ar"),
        AlternateIdentifierElementList.of(List.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.4.xml"),
                AlternateIdentifierTypeField.of("URL")),
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.0.xml"),
                AlternateIdentifierTypeField.of("URL"))
        )),
        RelatedIdentifierElementList.of(List.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of("https://github.com/citation-style-language/schema/raw/master/csl-data.json"),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of("http://purl.org/datacite/v4.4/Book", "Book")),
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        )),
        SizeFieldList.of(List.of(
            new SizeField("10KB"),
            new SizeField("10 Pages")
        )),
        FormatFieldList.of(List.of(
            new FormatField("video"),
            new FormatField("PDF")
        )),
        VersionField.of("1.0.0"),
        RightsElementList.of(List.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("Creative Commons Zero v1.0 Universal"),
                RightsUriField.of("https://creativecommons.org/publicdomain/zero/1.0/legalcode"),
                RightsIdentifierField.of("cc0-1.0"),
                RightsIdentifierSchemeField.of("SPDX"),
                RightsElement.SchemeUriField.of("https://spdx.org/licenses/")),
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights 2"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        )),
        DescriptionElementList.of(List.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the first description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/TableOfContents", "TableOfContents")),
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the second description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/Abstract", "Abstract"))
        )),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("45"),
                    PointLatitudeField.of("60")
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of("30"),
                    EastBoundLongitudeField.of("32"),
                    SouthBoundLatitudeField.of("60"),
                    NorthBoundLatitudeField.of("-50")
                )
            )),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Mark Ten"),
                FunderIdentifierField.of("05dxps055"),
                FunderIdentifierTypeField.of("http://purl.obolibrary.org/obo/BE_ROR", "ROR"),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of("CBET-106"),
                AwardUriField.of("https://www.moore.org/grants/list/GBMF3859.01"),
                AwardTitleField.of("Full DataCite XML Example"))
        ),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of("Kelly"),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of("Spring"))),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of("issue"),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of("2003"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/Editor", "Editor"),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of("Stanford"),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceAllUnderRequiredElements() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "All Properties Under Required Element",
        "Has value in fields under required element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(List.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("John"),
                CreatorElement.FamilyNameField.of("Doe"),
                CreatorElement.AffiliationElementList.of(List.of(
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of("affiliation name"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/04wxnsj81"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of())
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                ))
        )),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Rich Metadata Test"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other"))
        ),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndSubject() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Subject",
        "Required properties and Subject element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(List.of(
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("000")),
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("111"))
        )),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndContributor() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Contributor",
        "Required properties and Contributor element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(List.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Starr, Joan"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Joan"),
                ContributorElement.FamilyNameField.of("Starr"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                )),
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Chou, Jimmy"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Jimmy"),
                ContributorElement.FamilyNameField.of("Chou"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataManager", "DataManager"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCID18"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                ))
        )),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndDate() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Date",
        "Required properties and Date element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(List.of(
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-12"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Created", "Created"),
                DateInformationField.of()),
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-08"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Issued", "Issued"),
                DateInformationField.of())
        )),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndLang() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Lang",
        "Required properties and Language field",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of("en"),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndAlternateIdentifier() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Alternate Identifier",
        "Required properties and AlternateIdentifier element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(List.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.4.xml"),
                AlternateIdentifierTypeField.of("URL")),
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.0.xml"),
                AlternateIdentifierTypeField.of("URL"))
        )),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndRelatedIdentifier() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Related Identifier",
        "Required properties and RelatedIdentifier element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(List.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of("https://github.com/citation-style-language/schema/raw/master/csl-data.json"),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of("http://purl.org/datacite/v4.4/Book", "Book")),
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        )),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndSizeFormatVersion() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Size Format Version",
        "Required properties, and Size, Format and Version Elements",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(List.of(
            new SizeField("10KB"),
            new SizeField("10 Pages")
        )),
        FormatFieldList.of(List.of(
            new FormatField("video"),
            new FormatField("PDF")
        )),
        VersionField.of("1.0.0"),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndRights() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Rights",
        "Required properties and Rights element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(List.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("Creative Commons Zero v1.0 Universal"),
                RightsUriField.of("https://creativecommons.org/publicdomain/zero/1.0/legalcode"),
                RightsIdentifierField.of("cc0-1.0"),
                RightsIdentifierSchemeField.of("SPDX"),
                RightsElement.SchemeUriField.of("https://spdx.org/licenses/")),
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights 2"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        )),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndDescription() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Description",
        "Required properties and Description element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(List.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the first description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/TableOfContents", "TableOfContents")),
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the second description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/Abstract", "Abstract"))
        )),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndFundingRef() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And FundingReference",
        "Required properties and FundingReference element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Mark Ten"),
                FunderIdentifierField.of("05dxps055"),
                FunderIdentifierTypeField.of("http://purl.obolibrary.org/obo/BE_ROR", "ROR"),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of("CBET-106"),
                AwardUriField.of("https://www.moore.org/grants/list/GBMF3859.01"),
                AwardTitleField.of("Full DataCite XML Example"))
        ),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndGeoLocation() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required Only And GeoLocation",
        "Required properties and GeoLocation element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("45"),
                    PointLatitudeField.of("60")
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of("30"),
                    EastBoundLongitudeField.of("32"),
                    SouthBoundLatitudeField.of("60"),
                    NorthBoundLatitudeField.of("-50")
                )
            )),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRequiredAndRelatedItem() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required Only And RelatedItem",
        "Required properties and RelatedItem element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of("Kelly"),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of("Spring"))),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of("issue"),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of("2003"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/Editor", "Editor"),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of("Stanford"),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceRequiredAndRandomFields() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required Only Test",
        "Required properties only",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(List.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Starr, Joan"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Joan"),
                ContributorElement.FamilyNameField.of("Starr"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                )),
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Chou, Jimmy"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of(),
                ContributorElement.FamilyNameField.of(),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of())
                ),
                ContributorElement.NameIdentifierElementList.of())
        )),
        DateElementList.of(),
        LanguageField.of("ar"),
        AlternateIdentifierElementList.of(List.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.4.xml"),
                AlternateIdentifierTypeField.of("URL")),
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.0.xml"),
                AlternateIdentifierTypeField.of("URL"))
        )),
        RelatedIdentifierElementList.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of(),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        ),
        SizeFieldList.of(
            new SizeField("10 Pages")
        ),
        FormatFieldList.of(
            new FormatField("video")
        ),
        VersionField.of("1.0.0"),
        RightsElementList.of(List.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("the rights"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        )),
        DescriptionElementList.of(List.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the first description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/Abstract", "Abstract")))),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("45"),
                    PointLatitudeField.of("60")
                ),
                GeoLocationBoxElement.of()
            )),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Mark Ten"),
                FunderIdentifierField.of(),
                FunderIdentifierTypeField.of("http://purl.obolibrary.org/obo/BE_ROR", "ROR"),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of(),
                AwardUriField.of(),
                AwardTitleField.of())
        ),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of(),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of(),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of(),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of())),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of(),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of(),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of(),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/Editor", "Editor"),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of("Stanford"),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceRequiredAndEmptyString() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And Empty String",
        "Required properties and properties with empty string",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(""),
                CreatorElement.FamilyNameField.of(""),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Starr, Joan"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of(""),
                ContributorElement.FamilyNameField.of(""),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of(""))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of(""))
                )
            )),
        DateElementList.of(),
        LanguageField.of(""),
        AlternateIdentifierElementList.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of(""),
                AlternateIdentifierTypeField.of("URL"))
        ),
        RelatedIdentifierElementList.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of(),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        ),
        SizeFieldList.of(
            new SizeField("")
        ),
        FormatFieldList.of(
            new FormatField("")
        ),
        VersionField.of(""),
        RightsElementList.of(List.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights"),
                RightsUriField.of("https://creativecommons.org/publicdomain/zero/1.0/legalcode"),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        )),
        DescriptionElementList.of(),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("45"),
                    PointLatitudeField.of("60")
                ),
                GeoLocationBoxElement.of()
            )),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Mark Ten"),
                FunderIdentifierField.of(),
                FunderIdentifierTypeField.of("http://purl.obolibrary.org/obo/BE_ROR", "ROR"),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of(),
                AwardUriField.of(),
                AwardTitleField.of())
        ),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of(),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of(),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of(),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of())),
                RelatedItemElement.NumberField.of(""),
                RelatedItemElement.NumberTypeField.of(),
                RelatedItemElement.VolumeField.of(""),
                RelatedItemElement.IssueField.of(),
                RelatedItemElement.FirstPageField.of(""),
                RelatedItemElement.LastPageField.of(""),
                RelatedItemElement.PublicationYearField.of(),
                RelatedItemElement.PublisherField.of(""),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/Editor", "Editor"),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of("Stanford"),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceRequiredAndAllEmptyFields() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required And All Empty Fields",
        "All elements are expanded but empty except for the requires",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(
                    new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.AffiliationElement.NameField.of(),
                        CreatorElement.AffiliationElement.AffiliationIdentifierField.of(),
                        CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of(),
                        CreatorElement.AffiliationElement.SchemeUriField.of())
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of(),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of(),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of())
                ))
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required And All Empty Fields"),
                TitleElement.TitleTypeField.of())
        ),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of(),
                SubjectSchemeField.of(),
                SubjectElement.SchemeUriField.of(),
                ValueUriField.of(),
                ClassificationCodeField.of())
        ),
        ContributorElementList.of(List.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of(),
                ContributorElement.NameTypeField.of(),
                ContributorElement.GivenNameField.of(),
                ContributorElement.FamilyNameField.of(),
                ContributorElement.ContributorTypeField.of(),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of(),
                        ContributorElement.AffiliationElement.SchemeUriField.of())
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of(),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of(),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                ))
        )),
        DateElementList.of(
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of(),
                DateTypeField.of(),
                DateInformationField.of())
        ),
        LanguageField.of(),
        AlternateIdentifierElementList.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of(),
                AlternateIdentifierTypeField.of())
        ),
        RelatedIdentifierElementList.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of(),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of(),
                RelatedIdentifierElement.RelationTypeField.of(),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of(),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        ),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(List.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of(),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of()),
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of(),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        )),
        DescriptionElementList.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of(),
                DescriptionTypeField.of())
        ),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of(),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of(),
                    PointLatitudeField.of()
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of(),
                    EastBoundLongitudeField.of(),
                    SouthBoundLatitudeField.of(),
                    NorthBoundLatitudeField.of()
                )
            )),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of(),
                FunderIdentifierField.of(),
                FunderIdentifierTypeField.of(),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of(),
                AwardUriField.of(),
                AwardTitleField.of())
        ),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of(),
                RelatedItemElement.RelationTypeField.of(),
                RelatedItemElement.RelatedIdentifierField.of(),
                RelatedItemElement.RelatedIdentifierTypeField.of(),
                RelatedItemElement.RelatedMetadataSchemeField.of(),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of(),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of(),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of(),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of(),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of())),
                RelatedItemElement.NumberField.of(),
                RelatedItemElement.NumberTypeField.of(),
                RelatedItemElement.VolumeField.of(),
                RelatedItemElement.IssueField.of(),
                RelatedItemElement.FirstPageField.of(),
                RelatedItemElement.LastPageField.of(),
                RelatedItemElement.PublicationYearField.of(),
                RelatedItemElement.PublisherField.of(),
                RelatedItemElement.EditionField.of(),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of(),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceMissingAllFields() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing All Required Properties",
        "",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "",
        PrefixField.of(),
        UrlField.of(generateId()),
        CreatorElementList.of(),
        TitleElementList.of(),
        PublicationYearField.of(),
        PublisherField.of(),
        ResourceTypeField.of(),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingPrefix() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing Prefix",
        "Missing required property Prefix",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingPublisher() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing Publisher",
        "Missing required property Publisher",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing Publisher"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingPublicationYear() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing PublicationYear",
        "Missing required property PublicationYear",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing PublicationYear"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingMultipleRequiredFields() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing Multiple Required Fields",
        "Missing Multiple Required Fields",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing Multiple Required Fields"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of(),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingContributorName() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing ContributorName",
        "Missing sub-required property ContributorName",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of(),
        SubjectElementList.of(),
        ContributorElementList.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of(),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of(""),
                ContributorElement.FamilyNameField.of(""),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of(""))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of(""))
                )
            )),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingContributorType() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing ContributorType",
        "Missing sub-required property ContributorType",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Required properties only"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of(),
        SubjectElementList.of(),
        ContributorElementList.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Mark Shan"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of(""),
                ContributorElement.FamilyNameField.of(""),
                ContributorElement.ContributorTypeField.of(),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of(""))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of(""))
                )
            )),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingRelatedIdentifierType() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing RelatedIdentifierType",
        "Missing sub-required property RelatedIdentifierType",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing RelatedIdentifierType Test"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of(),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of("https://github.com/citation-style-language/schema/raw/master/csl-data.json"),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of("http://purl.org/datacite/v4.4/Book", "Book"))
        ),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingRelationType() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing RelationType",
        "Missing sub-required property RelationType",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing RelationType Test"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of(),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of("https://github.com/citation-style-language/schema/raw/master/csl-data.json"),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of("http://purl.org/datacite/v4.4/Book", "Book"))
        ),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingFunderName() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing FunderName",
        "Missing sub-required property FunderName",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing FunderName Test"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of(),
                FunderIdentifierField.of("05dxps055"),
                FunderIdentifierTypeField.of("http://purl.obolibrary.org/obo/BE_ROR", "ROR"),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of("CBET-106"),
                AwardUriField.of("https://www.moore.org/grants/list/GBMF3859.01"),
                AwardTitleField.of("Full DataCite XML Example"))
        ),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceMissingRelatedItemType() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing RelatedItemType",
        "Missing sub-required property relatedItemType field",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing RelatedItemType Test"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
//                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelatedItemTypeField.of(),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of("Kelly"),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of("Spring"))),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of("issue"),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of("2003"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/Editor", "Editor"),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of("Stanford"),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceMissingRelatedItemRelationType() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Missing RelatedItemType",
        "Missing sub-required property relatedItemType field",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Missing RelatedItemType Test"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of(),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of(),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of("Kelly"),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of("Spring"))),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of("issue"),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of("2003"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceWrongNameType() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Wrong NameType",
        "Wrong NameType Value",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(List.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Person"),
                CreatorElement.GivenNameField.of("John"),
                CreatorElement.FamilyNameField.of("Doe"),
                CreatorElement.AffiliationElementList.of(List.of(
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of("affiliation name"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/04wxnsj81"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of())
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                ))
        )),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Wrong NameType Test"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other"))
        ),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceDataOutOfRange() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Data in GeoLocation Out Of Range",
        "Data in GeoLocation Out Of Range",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Data in GeoLocation Out Of Range Test"),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(),
        ContributorElementList.of(),
        DateElementList.of(),
        LanguageField.of(),
        AlternateIdentifierElementList.of(),
        RelatedIdentifierElementList.of(),
        SizeFieldList.of(),
        FormatFieldList.of(),
        VersionField.of(),
        RightsElementList.of(),
        DescriptionElementList.of(),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("200"),
                    PointLatitudeField.of("-120")
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of("200"),
                    EastBoundLongitudeField.of("-200"),
                    SouthBoundLatitudeField.of("-100"),
                    NorthBoundLatitudeField.of("100")
                )
            )),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRichMetadataRichUpdate() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Draft DOI Rich Metadata Update Test",
        "Has data in each element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("John"),
                CreatorElement.FamilyNameField.of("Doe"),
                CreatorElement.AffiliationElementList.of(List.of(
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of("affiliation name"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/04wxnsj81"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of()),
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of(),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/05dxps055"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri2"))
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                ))
        ),
        TitleElementList.of(List.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Draft DOI Rich Metadata Update Test"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other")),
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Rich Metadata Test"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other"))
        )),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("111"))
        ),
        ContributorElementList.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Starr, Joan"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Joan"),
                ContributorElement.FamilyNameField.of("Starr"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(List.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of()),
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 2"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                )))
        ),
        DateElementList.of(
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-12"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Created", "Created"),
                DateInformationField.of())
        ),
        LanguageField.of("ar"),
        AlternateIdentifierElementList.of(List.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.4.xml"),
                AlternateIdentifierTypeField.of("URL")),
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.0.xml"),
                AlternateIdentifierTypeField.of("URL"))
        )),
        RelatedIdentifierElementList.of(List.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of("https://github.com/citation-style-language/schema/raw/master/csl-data.json"),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of("http://purl.org/datacite/v4.4/Book", "Book")),
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of("https://github.com/citation-style-language/schema/raw/master/csl-data.json"),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of("http://purl.org/datacite/v4.4/Book", "Book"))
        )),
        SizeFieldList.of(List.of(
            new SizeField("10KB"),
            new SizeField("10 Pages")
        )),
        FormatFieldList.of(
            new FormatField("PDF")
        ),
        VersionField.of("1.0.1"),
        RightsElementList.of(List.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("Creative Commons Zero v1.0 Universal"),
                RightsUriField.of("https://creativecommons.org/publicdomain/zero/1.0/legalcode"),
                RightsIdentifierField.of("cc0-1.0"),
                RightsIdentifierSchemeField.of("SPDX"),
                RightsElement.SchemeUriField.of("https://spdx.org/licenses/")),
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights 2"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        )),
        DescriptionElementList.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the first description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/TableOfContents", "TableOfContents"))
        ),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("45"),
                    PointLatitudeField.of("60")
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of("30"),
                    EastBoundLongitudeField.of("32"),
                    SouthBoundLatitudeField.of("60"),
                    NorthBoundLatitudeField.of("-50")
                )
            )),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Mark Ten"),
                FunderIdentifierField.of("05dxps055"),
                FunderIdentifierTypeField.of("http://purl.obolibrary.org/obo/BE_ROR", "ROR"),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of("CBET-106"),
                AwardUriField.of("https://www.moore.org/grants/list/GBMF3859.01"),
                AwardTitleField.of("Full DataCite XML Example"))
        ),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of("Kelly"),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of("Spring"))),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of("issue"),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of("2003"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/Editor", "Editor"),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of("Stanford"),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                ))
        )
    );
  }

  public static MetadataInstance getInstanceRichMetadataAddNewInstance() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Update Rich Metadata with New Added Instances of Each Element",
        "Update Rich Metadata with New Added Instances of Each Element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(List.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("John"),
                CreatorElement.FamilyNameField.of("Doe"),
                CreatorElement.AffiliationElementList.of(List.of(
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of("affiliation name"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/04wxnsj81"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of()),
                        new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                            CreatorElement.AffiliationElement.NameField.of(),
                            CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/05dxps055"),
                            CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                            CreatorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri2"))
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                )),
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("Potter Summer"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("Potter"),
                CreatorElement.FamilyNameField.of("Summer"),
                CreatorElement.AffiliationElementList.of(
                    new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.AffiliationElement.NameField.of(),
                        CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/05dxps055"),
                        CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        CreatorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri2")
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                )),
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("Joel Haggard"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("Joel"),
                CreatorElement.FamilyNameField.of("Haggard"),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                ))
        )),
        TitleElementList.of(List.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Update Rich Metadata with New Added Instances of Each Element"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other")),
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Rich Metadata Test"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other"))
        )),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(List.of(
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("000")),
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("111")),
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("222"))
        )),
        ContributorElementList.of(List.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Starr, Joan"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Joan"),
                ContributorElement.FamilyNameField.of("Starr"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector", "DataCollector"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                )),
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Chou, Jimmy"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Jimmy"),
                ContributorElement.FamilyNameField.of("Chou"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataManager", "DataManager"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCID18"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                )),
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Cao, Ming"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Ming"),
                ContributorElement.FamilyNameField.of("Cao"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataManager", "DataManager"),
                ContributorElement.AffiliationElementList.of(),
                ContributorElement.NameIdentifierElementList.of())
        )),
        DateElementList.of(List.of(
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-12"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Created", "Created"),
                DateInformationField.of()),
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-08"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Issued", "Issued"),
                DateInformationField.of()),
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-5-08"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Created", "Created"),
                DateInformationField.of())
        )),
        LanguageField.of("ar"),
        AlternateIdentifierElementList.of(List.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.4.xml"),
                AlternateIdentifierTypeField.of("URL")),
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.0.xml"),
                AlternateIdentifierTypeField.of("URL"))
        )),
        RelatedIdentifierElementList.of(List.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of("https://github.com/citation-style-language/schema/raw/master/csl-data.json"),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of("http://purl.org/datacite/v4.4/Book", "Book")),
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of()),
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/DOI", "DOI"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        )),
        SizeFieldList.of(List.of(
            new SizeField("10KB"),
            new SizeField("10 Pages"),
            new SizeField("2 Articles")
        )),
        FormatFieldList.of(List.of(
            new FormatField("video"),
            new FormatField("PDF"),
            new FormatField("Journal")
        )),
        VersionField.of("1.0.0"),
        RightsElementList.of(List.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("Creative Commons Zero v1.0 Universal"),
                RightsUriField.of("https://creativecommons.org/publicdomain/zero/1.0/legalcode"),
                RightsIdentifierField.of("cc0-1.0"),
                RightsIdentifierSchemeField.of("SPDX"),
                RightsElement.SchemeUriField.of("https://spdx.org/licenses/")),
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights 2"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of()),
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights 3"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        )),
        DescriptionElementList.of(List.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the first description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/TableOfContents", "TableOfContents")),
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the second description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/Abstract", "Abstract")),
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the third description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/Methods", "Methods"))
        )),
        GeoLocationElementList.of(List.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("45"),
                    PointLatitudeField.of("60")
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of("30"),
                    EastBoundLongitudeField.of("32"),
                    SouthBoundLatitudeField.of("60"),
                    NorthBoundLatitudeField.of("-50")
                )),
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("21"),
                    PointLatitudeField.of("50")
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of("100"),
                    EastBoundLongitudeField.of("60"),
                    SouthBoundLatitudeField.of("20"),
                    NorthBoundLatitudeField.of("-50")
                ))
        )),
        FundingReferenceElementList.of(List.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Mark Ten"),
                FunderIdentifierField.of("05dxps055"),
                FunderIdentifierTypeField.of("http://purl.obolibrary.org/obo/BE_ROR", "ROR"),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of("CBET-106"),
                AwardUriField.of("https://www.moore.org/grants/list/GBMF3859.01"),
                AwardTitleField.of("Full DataCite XML Example")),
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Stanford"),
                FunderIdentifierField.of(),
                FunderIdentifierTypeField.of(),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of(),
                AwardUriField.of(),
                AwardTitleField.of())
        )),
        RelatedItemElementList.of(List.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(
                    new RelatedItemElement.RelatedItemCreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemCreatorElement.CreatorNameField.of("Kelly Spring"),
                        RelatedItemElement.RelatedItemCreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                        RelatedItemElement.RelatedItemCreatorElement.GivenNameField.of("Kelly"),
                        RelatedItemElement.RelatedItemCreatorElement.FamilyNameField.of("Spring"))),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of("issue"),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of("2003"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("5"),
                RelatedItemElement.RelatedItemContributorElementList.of(
                    new RelatedItemElement.RelatedItemContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.RelatedItemContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/Editor", "Editor"),
                        RelatedItemElement.RelatedItemContributorElement.ContributorNameField.of("Stanford"),
                        RelatedItemElement.RelatedItemContributorElement.NameTypeField.of("http://www.w3.org/2006/vcard/ns#Organizational", "Organizational"),
                        RelatedItemElement.RelatedItemContributorElement.GivenNameField.of(),
                        RelatedItemElement.RelatedItemContributorElement.FamilyNameField.of())
                )),
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title 2"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("776"),
                RelatedItemElement.IssueField.of("issue"),
                RelatedItemElement.FirstPageField.of("249"),
                RelatedItemElement.LastPageField.of("264"),
                RelatedItemElement.PublicationYearField.of("2018"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of(),
                RelatedItemElement.RelatedItemContributorElementList.of())
        ))
    );
  }

  public static MetadataInstance getInstanceRichMetadataDeleteInstance() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Update Rich Metadata with Deleted Instances of Each Element",
        "Update Rich Metadata with Deleted Instances of Each Element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("Potter Summer"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("Potter"),
                CreatorElement.FamilyNameField.of("Summer"),
                CreatorElement.AffiliationElementList.of(
                    new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.AffiliationElement.NameField.of(),
                        CreatorElement.AffiliationElement.AffiliationIdentifierField.of("https://ror.org/05dxps055"),
                        CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        CreatorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri2")
                    )
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("https://orcid.org/0000-0001-5000-0007"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCIID"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of("https://orcid.org"))
                ))
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Rich Metadata Test"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Other"))
        ),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("111"))
        ),
        ContributorElementList.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Chou, Jimmy"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Jimmy"),
                ContributorElement.FamilyNameField.of("Chou"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataManager", "DataManager"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 1"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 1"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCID18"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                ))
        ),
        DateElementList.of(
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-08"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Issued", "Issued"),
                DateInformationField.of())
        ),
        LanguageField.of("ar"),
        AlternateIdentifierElementList.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.0.xml"),
                AlternateIdentifierTypeField.of("URL"))
        ),
        RelatedIdentifierElementList.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        ),
        SizeFieldList.of(
            new SizeField("10KB")
        ),
        FormatFieldList.of(
            new FormatField("PDF")
        ),
        VersionField.of("1.0.0"),
        RightsElementList.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights 2"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        ),
        DescriptionElementList.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the second description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/Abstract", "Abstract"))
        ),
        GeoLocationElementList.of(),
        FundingReferenceElementList.of(),
        RelatedItemElementList.of()
    );
  }

  public static MetadataInstance getInstanceRichMetadataDeleteAndAddInstance() {
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Update Rich Metadata with Deleted And New Added Instances of Each Element",
        "Update Rich Metadata with Deleted And New Added Instances of Each Element",
        TEMPLATE_ID,
        nowAsString,
        USER_ID,
        nowAsString,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of(generateId()),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("Potter Winner"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("Potter"),
                CreatorElement.FamilyNameField.of("Winner"),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of("Update Rich Metadata with Deleted And New Added Instances of Each Element"),
                TitleElement.TitleTypeField.of("http://purl.org/datacite/v4.4/OtherTitle", "Subtitle"))
        ),
        MetadataInstance.PublicationYearField.of(publicationYear),
        PublisherField.of(PUBLISHER),
        ResourceTypeField.of("Template"),
        SubjectElementList.of(
            new SubjectElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                SubjectField2.of("computer science"),
                SubjectSchemeField.of("dewey"),
                SubjectElement.SchemeUriField.of("http://dewey.info/"),
                ValueUriField.of("http://value.uri/"),
                ClassificationCodeField.of("333"))
        ),
        ContributorElementList.of(
            new ContributorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                ContributorElement.ContributorNameField.of("Mark, Ten"),
                ContributorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                ContributorElement.GivenNameField.of("Mark"),
                ContributorElement.FamilyNameField.of("Ten"),
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/ProjectLeader", "ProjectLeader"),
                ContributorElement.AffiliationElementList.of(
                    new ContributorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.AffiliationElement.NameField.of(),
                        ContributorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier 2"),
                        ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("ROR"),
                        ContributorElement.AffiliationElement.SchemeUriField.of("http://affiliation_identification_scheme_uri"))
                ),
                ContributorElement.NameIdentifierElementList.of(
                    new ContributorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        ContributorElement.NameIdentifierElement.NameField.of("name identifier 2"),
                        ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ORCID18"),
                        ContributorElement.NameIdentifierElement.SchemeUriField.of())
                ))
        ),
        DateElementList.of(
            new DateElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DateField2.of("2023-6-30"),
                DateTypeField.of("http://purl.org/datacite/v4.4/Issued", "Issued"),
                DateInformationField.of())
        ),
        LanguageField.of("ar"),
        AlternateIdentifierElementList.of(
            new AlternateIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                AlternateIdentifierField2.of("https://schema.datacite.org/meta/kernel-4.4/example/datacite-example-full-v4.0.xml"),
                AlternateIdentifierTypeField.of("URL"))
        ),
        RelatedIdentifierElementList.of(
            new RelatedIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedIdentifierElement.RelatedIdentifierField2.of("https://data.datacite.org/application/citeproc+json/10.5072/example-full"),
                RelatedIdentifierElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedIdentifierElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Compiles", "Compiles"),
                RelatedIdentifierElement.RelatedMetadataSchemeField.of("json"),
                RelatedIdentifierElement.SchemeUriField.of(),
                RelatedIdentifierElement.SchemeTypeField.of(),
                RelatedIdentifierElement.ResourceTypeGeneralField.of())
        ),
        SizeFieldList.of(
            new SizeField("30KB")
        ),
        FormatFieldList.of(
            new FormatField("PDF")
        ),
        VersionField.of("4.4"),
        RightsElementList.of(
            new RightsElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RightsField2.of("rights 3"),
                RightsUriField.of(),
                RightsIdentifierField.of(),
                RightsIdentifierSchemeField.of(),
                RightsElement.SchemeUriField.of())
        ),
        DescriptionElementList.of(
            new DescriptionElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                DescriptionField2.of("the third description"),
                DescriptionTypeField.of("http://purl.org/datacite/v4.4/Abstract", "Abstract"))
        ),
        GeoLocationElementList.of(
            new GeoLocationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                GeoLocationPlaceField.of("Stanford"),
                new GeoLocationPointElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    PointLongitudeField.of("21"),
                    PointLatitudeField.of("50")
                ),
                new GeoLocationBoxElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                    WestBoundLongitudeField.of("100"),
                    EastBoundLongitudeField.of("60"),
                    SouthBoundLatitudeField.of("20"),
                    NorthBoundLatitudeField.of("-50")
                ))
        ),
        FundingReferenceElementList.of(
            new FundingReferenceElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                FunderNameField.of("Stanford"),
                FunderIdentifierField.of(),
                FunderIdentifierTypeField.of(),
                FundingReferenceElement.SchemeUriField.of(),
                AwardNumberField.of(),
                AwardUriField.of(),
                AwardTitleField.of())
        ),
        RelatedItemElementList.of(
            new RelatedItemElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                RelatedItemElement.RelatedItemTypeField.of("http://purl.org/datacite/v4.4/Audiovisual", "Audiovisual"),
                RelatedItemElement.RelationTypeField.of("http://purl.org/datacite/v4.4/Continues", "Continues"),
                RelatedItemElement.RelatedIdentifierField.of("0370-2693"),
                RelatedItemElement.RelatedIdentifierTypeField.of("http://purl.org/datacite/v4.4/ARK", "ARK"),
                RelatedItemElement.RelatedMetadataSchemeField.of("related metadata scheme"),
                RelatedItemElement.SchemeUriField.of(),
                RelatedItemElement.SchemeTypeField.of("DOI"),
                RelatedItemElement.TitleElementList.of(
                    new RelatedItemElement.TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        RelatedItemElement.TitleElement.TitleField2.of("related item title 3"),
                        RelatedItemElement.TitleElement.TitleTypeField.of())),
                RelatedItemElement.RelatedItemCreatorElementList.of(),
                RelatedItemElement.NumberField.of("10"),
                RelatedItemElement.NumberTypeField.of("http://purl.org/ontology/bibo/Chapter", "Chapter"),
                RelatedItemElement.VolumeField.of("56"),
                RelatedItemElement.IssueField.of(),
                RelatedItemElement.FirstPageField.of(),
                RelatedItemElement.LastPageField.of(),
                RelatedItemElement.PublicationYearField.of("2018"),
                RelatedItemElement.PublisherField.of("Stanford"),
                RelatedItemElement.EditionField.of("4"),
                RelatedItemElement.RelatedItemContributorElementList.of())
        )
    );
  }

}
