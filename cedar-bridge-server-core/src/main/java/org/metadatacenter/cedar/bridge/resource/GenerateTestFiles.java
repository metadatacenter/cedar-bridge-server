package org.metadatacenter.cedar.bridge.resource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.metadatacenter.cedar.bridge.resource.Cedar.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.AlternateIdentifierElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.DateElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.DescriptionElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.FundingReferenceElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.GeoLocationElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.GeoLocationElement.GeoLocationBoxElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.GeoLocationElement.GeoLocationPointElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.RightsElement.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.SubjectElement.*;

public class GenerateTestFiles {
  private static final String ELEMENT_IRI_PREFIX = "https://repo.metadatacenter.org/template-element-instances/";
  private static final String INSTANCE_IRI_PREFIX = "https://repo.metadatacenter.org/template-instances/";
  private static final String TEMPLATE_ID = "https://repo.metadatacenter.org/templates/5b6e0952-8a56-4f97-a35d-7ce784773b57";
  private static final String USER_ID = "https://metadatacenter.org/users/6124554b-9c83-443c-8207-241d75b82f44";
  private static final String PREFIX = "10.82658";
  private static final String PUBLICATION_YEAR = "2023";
  private static final String PUBLISHER = "CEDAR";

  private static final Instant now = Instant.ofEpochSecond(System.currentTimeMillis() / 1000);
  public static MetadataInstance getRequiredOnlyMetadata(){
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Required Only Test",
        "Only has values that are required",
        TEMPLATE_ID,
        now,
        USER_ID,
        now,
        USER_ID,
        "Derived from",
        PrefixField.of(PREFIX),
        UrlField.of("the url"),
        CreatorElementList.of(
            new CreatorElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                CreatorElement.CreatorNameField.of("John Doe"),
                CreatorElement.NameTypeField.of("http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal", "Personal"),
                CreatorElement.GivenNameField.of("John"),
                CreatorElement.FamilyNameField.of("Doe"),
                CreatorElement.AffiliationElementList.of(
                    new CreatorElement.AffiliationElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.AffiliationElement.NameField.of("affiliation name"),
                        CreatorElement.AffiliationElement.AffiliationIdentifierField.of("affiliation identifier"),
                        CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of("scheme"),
                        CreatorElement.AffiliationElement.SchemeUriField.of())
                ),
                CreatorElement.NameIdentifierElementList.of(
                    new CreatorElement.NameIdentifierElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                        CreatorElement.NameIdentifierElement.NameField.of("name identifier"),
                        CreatorElement.NameIdentifierElement.NameIdentifierSchemeField.of("ROR"),
                        CreatorElement.NameIdentifierElement.SchemeUriField.of())
                ))
        ),
        TitleElementList.of(
            new TitleElement(ELEMENT_IRI_PREFIX + UUID.randomUUID(),
                TitleElement.TitleField2.of(),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(PUBLICATION_YEAR),
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

  public static MetadataInstance getRichMetadata(){
    return new MetadataInstance(
        INSTANCE_IRI_PREFIX + UUID.randomUUID(),
        "Rich Metadata Test",
        "Has data in each element",
        TEMPLATE_ID,
        now,
        USER_ID,
        now,
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
        MetadataInstance.PublicationYearField.of(PUBLICATION_YEAR),
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
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataCollector","DataCollector"),
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
                ContributorElement.ContributorTypeField.of("http://purl.org/datacite/v4.4/DataManager","DataManager"),
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
        LanguageField.of( "ar"),
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
            new FormatField("videl"),
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

}
