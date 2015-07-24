<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="xs"
                version="2.0">
  <xsl:variable name="defaultConstant" select="'XXXX_nullValue_XXXX'"/>
  <xsl:param name="organisationName" select="$defaultConstant"/>
  <xsl:param name="organisationEmail" select="$defaultConstant"/>
  <xsl:param name="metadataModifiedDate" select="$defaultConstant"/>
  <xsl:param name="lineage" select="$defaultConstant"/>
  <xsl:param name="title" select="$defaultConstant"/>
  <xsl:param name="publicationDate" select="$defaultConstant"/>
  <xsl:param name="uuid" select="$defaultConstant"/>
  <xsl:param name="abstract" select="$defaultConstant"/>
  <xsl:param name="thumbnailUri" select="$defaultConstant"/>
  <xsl:param name="keywords" select="$defaultConstant"/>
  <xsl:param name="keywordSeparator" select="$defaultConstant"/>
  <xsl:param name="userLimitation" select="$defaultConstant"/>
  <xsl:param name="resolution" select="$defaultConstant"/>
  <xsl:param name="topics" select="$defaultConstant"/>
  <xsl:param name="topicSeparator" select="$defaultConstant"/>
  <xsl:param name="geographicIdentifier" select="$defaultConstant"/>
  <xsl:param name="bboxWestLongitude" select="$defaultConstant"/>
  <xsl:param name="bboxEastLongitude" select="$defaultConstant"/>
  <xsl:param name="bboxSouthLatitude" select="$defaultConstant"/>
  <xsl:param name="bboxNorthLatitude" select="$defaultConstant"/>
  <xsl:param name="format" select="$defaultConstant"/>
  <xsl:param name="downloadUri" select="$defaultConstant"/>
  <xsl:param name="fileName" select="$defaultConstant"/>
  <xsl:param name="license" select="$defaultConstant"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- UUID -->
  <xsl:template match="/gmd:MD_Metadata/gmd:fileIdentifier">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$uuid" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>
  <!-- Organisation name -->
  <xsl:template match="/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$organisationName"/></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Organisation email -->
  <xsl:template match="/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$organisationEmail" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>
  <!-- Metadata modified date -->
  <!-- // FIXME is this neccesary or is fixed by up updade-fixed-info.xsl? -->
  <!--
  <xsl:template match="/gmd:MD_Metadata/gmd:dateStamp">
     <gco:Date><xsl:value-of select="$metadataModifiedDate"></xsl:value-of></gco:Date>
  </xsl:template>
  -->
  <!-- Title -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$title" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Alternate title -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$title" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Publication date -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date[../gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='publication']]">
    <xsl:copy>
      <!-- Datum waarop de dataset is gepubliceerd -->
      <gco:DateTime><xsl:value-of select="$publicationDate"></xsl:value-of></gco:DateTime>
    </xsl:copy>
  </xsl:template>

  <!-- Creation date -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date[../gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='creation']]">
    <xsl:copy>
      <!-- Datum waarop de dataset is gepubliceerd -->
      <gco:DateTime><xsl:value-of select="$publicationDate"></xsl:value-of></gco:DateTime>
    </xsl:copy>
  </xsl:template>


  <!-- identifier -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code">
    <xsl:copy>
      <gco:CharacterString>http://geodatastore.pdok.nl/id/dataset/<xsl:value-of select="$uuid"/></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Abstract -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$abstract" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Organisation name Identification Info -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$organisationName" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Organisation email Identification Info -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$organisationEmail" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Thumbnail URI -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$thumbnailUri"/></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Descriptive keywords -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords">
    <xsl:copy>
      <xsl:variable name="keywordList" select="tokenize($keywords, $keywordSeparator)"/>
      <xsl:choose>
        <xsl:when test="count($keywordList) = 0">
          <gmd:keyword></gmd:keyword>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$keywordList">
            <gmd:keyword>
              <gco:CharacterString><xsl:value-of select="." /></gco:CharacterString>
            </gmd:keyword>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- User limitation -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$userLimitation"></xsl:value-of></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints[gmd:MD_LegalConstraints]">
    <xsl:copy>
      <gmd:MD_LegalConstraints>
        <gmd:useLimitation>
          <gco:CharacterString>Geen gebruiksbeperking</gco:CharacterString>
        </gmd:useLimitation>
        <gmd:accessConstraints>
          <gmd:MD_RestrictionCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode"
                                  codeListValue="otherRestrictions"/>
        </gmd:accessConstraints>
        <gmd:otherConstraints>
          <gco:CharacterString>Geen beperkingen</gco:CharacterString>
        </gmd:otherConstraints>
        <gmd:otherConstraints>
          <gco:CharacterString><xsl:value-of select="$license"/></gco:CharacterString>
        </gmd:otherConstraints>
        <xsl:if test="contains($license, 'by')">
          <gmd:otherConstraints>
            <gco:CharacterString> Naamsvermelding verplicht, <xsl:value-of select="$organisationName"/></gco:CharacterString>
          </gmd:otherConstraints>
        </xsl:if>
      </gmd:MD_LegalConstraints>



    </xsl:copy>
  </xsl:template>

  <!-- Resolution -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution">
    <xsl:copy>
      <gmd:MD_Resolution>
        <gmd:equivalentScale>
          <gmd:MD_RepresentativeFraction>
            <gmd:denominator>
              <gco:Integer><xsl:value-of select="$resolution"/></gco:Integer>
            </gmd:denominator>
          </gmd:MD_RepresentativeFraction>
        </gmd:equivalentScale>
      </gmd:MD_Resolution>
    </xsl:copy>
  </xsl:template>

  <!-- Topic categories -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory">
    <xsl:variable name="topicList" select="tokenize($topics, $topicSeparator)"/>
    <xsl:choose>
      <xsl:when test="count($topicList) = 0">
        <!-- do not add any topic category -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="$topicList">
          <gmd:topicCategory>
            <gmd:MD_TopicCategoryCode><xsl:value-of select="." /></gmd:MD_TopicCategoryCode>
          </gmd:topicCategory>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Geographic Identifier -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier">
    <xsl:variable name="identifierUri" select="concat('http://gazeteer.pdok.nl/', $geographicIdentifier)"/>
    <xsl:copy>
      <gmd:RS_Identifier>
        <xsl:attribute name="uuid" select="$identifierUri" />
        <gmd:code>
          <gco:CharacterString><xsl:value-of select="$geographicIdentifier"/></gco:CharacterString>
        </gmd:code>
        <gmd:codeSpace>
          <gco:CharacterString>PDOK</gco:CharacterString>
        </gmd:codeSpace>
      </gmd:RS_Identifier>
    </xsl:copy>
  </xsl:template>

  <!-- Bounding Box -->
  <xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
    <xsl:copy>
      <gmd:westBoundLongitude>
        <gco:Decimal><xsl:value-of select="$bboxWestLongitude"/></gco:Decimal>
      </gmd:westBoundLongitude>
      <gmd:eastBoundLongitude>
        <gco:Decimal><xsl:value-of select="$bboxEastLongitude"/></gco:Decimal>
      </gmd:eastBoundLongitude>
      <gmd:southBoundLatitude>
        <gco:Decimal><xsl:value-of select="$bboxSouthLatitude"/></gco:Decimal>
      </gmd:southBoundLatitude>
      <gmd:northBoundLatitude>
        <gco:Decimal><xsl:value-of select="$bboxNorthLatitude"/></gco:Decimal>
      </gmd:northBoundLatitude>
    </xsl:copy>
  </xsl:template>

  <!-- Format -->
  <xsl:template match="/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format">
    <xsl:copy>
      <gmd:name>
        <xsl:comment>
          Voor geharmoniseerde INSPIRE data naar het applicatie schema verwijzen, juiste benaming zie hoofdstuk 9.x in de dataspecificaties
        </xsl:comment>
        <gco:CharacterString><xsl:value-of select="$format"/></gco:CharacterString>
      </gmd:name>
      <gmd:version>
        <gco:CharacterString/>
      </gmd:version>
    </xsl:copy>
  </xsl:template>

  <!-- Distributor organisation -->
  <xsl:template match="/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$organisationName" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Distributor email -->
  <xsl:template match="/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$organisationEmail" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- CI_OnlineResource -->
<!--  <xsl:template match="/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
    <xsl:copy>
      <gmd:linkage>
        <gmd:URL><xsl:value-of select="$downloadUri"></xsl:value-of></gmd:URL>
      </gmd:linkage>
      <gmd:protocol>
        <gco:CharacterString>download</gco:CharacterString>
      </gmd:protocol>
      <gmd:name>
        <gco:CharacterString><xsl:value-of select="$fileName" /></gco:CharacterString>
      </gmd:name>
    </xsl:copy>
  </xsl:template>-->


  <!-- lineage -->
  <xsl:template match="/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement">
    <xsl:copy>
      <gco:CharacterString><xsl:value-of select="$lineage" /></gco:CharacterString>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>