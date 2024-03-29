package org.metadatacenter.cedar.bridge.resource.datacite;

import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.id.CedarFQResourceId;

import java.time.Instant;
import java.time.Year;

import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.generateId;

/*
 * Generates a default instance for the DOI request form (URL, Year, Publisher, resourceType)
 */
public class GenerateMetadataInstance {

  public static MetadataInstance getDefaultInstance(String sourceArtifactId, String userID, String templateId, CedarConfig cedarConfig) {
    String openViewUrl = GenerateOpenViewUrl.getOpenViewUrl(sourceArtifactId, cedarConfig);
    String resourceType = CedarFQResourceId.build(sourceArtifactId).getType().getValue();
    String capitalizedResourceType = resourceType.substring(0, 1).toUpperCase() + resourceType.substring(1);
    String prefix = cedarConfig.getBridgeConfig().getDataCite().getPrefix();
    String publicationYear = Year.now().getValue() + "-01-01";
    String nowAsString = Instant.ofEpochSecond(System.currentTimeMillis() / 1000).toString();
    return new MetadataInstance(
        null,
        "Default DataCite Instance",
        "A new DataCite Instance with default values",
        templateId,
        nowAsString,
        userID,
        nowAsString,
        userID,
        templateId,
        PrefixField.of(prefix),
        UrlField.of(openViewUrl),
        CreatorElementList.of(
            new CreatorElement(generateId(),
                CreatorElement.CreatorNameField.of(),
                CreatorElement.NameTypeField.of(),
                CreatorElement.GivenNameField.of(),
                CreatorElement.FamilyNameField.of(),
                CreatorElement.AffiliationElementList.of(),
                CreatorElement.NameIdentifierElementList.of())
        ),
        TitleElementList.of(
            new TitleElement(generateId(),
                TitleElement.TitleField2.of(),
                TitleElement.TitleTypeField.of())
        ),
        PublicationYearField.of(publicationYear),
        PublisherField.of(DataciteConstants.PUBLISHER),
        ResourceTypeField.of(capitalizedResourceType),
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


}
