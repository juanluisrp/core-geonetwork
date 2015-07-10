# language: en
Feature: Download dataset(s)
  The download action is handled by an API, so it is disconnected from the actual location of the file

  Scenario: Download published dataset valid uuid
    Given a file named "NW_PDOK_2015_04_13.zip" has been uploaded in the geodatastore.
    And the file has been given an identifier "60bb2696-2090-4649-9695-19f4e9a0a52e"
    When a browser performs a Http GET to "/id/dataset/60bb2696-2090-4649-9695-19f4e9a0a52e"
    Then The server redirects with a 302 to the physical resource location.

  Scenario: Download published thumbnail
    Given a thumbnail named "rdinfo-stations.png" has been uploaded as a thumbnail
    And The thumbnail has been attached to an identifier "60bb2696-2090-4649-9695-19f4e9a0a52e"
    When a browser performs a Http GET to /geodatastore/id/thumbnail/60bb2696-2090-4649-9695-19f4e9a0a52e streams the thumbnail back.
    Then The server redirects with a 302 to the physical resource location.

  Scenario: Download published  doc
    Given The following metadatarecord has been attached to identifier "60bb2696-2090-4649-9695-19f4e9a0a52e"
    """
<?xml version="1.0" encoding="UTF-8"?>
<gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:geonet="http://www.fao.org/geonetwork" xsi:schemaLocation="  http://www.isotc211.org/2005/gmd   http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd  http://www.isotc211.org/2005/srv   http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd  http://www.isotc211.org/2005/gmd  http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd">
  <gmd:fileIdentifier>
    <gco:CharacterString>60bb2696-2090-4649-9695-19f4e9a0a52e</gco:CharacterString>
  </gmd:fileIdentifier>
  <gmd:language>
    <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="dut" />
  </gmd:language>
  <gmd:characterSet>
    <gmd:MD_CharacterSetCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_CharacterSetCode" codeListValue="utf8" />
  </gmd:characterSet>
  <gmd:hierarchyLevel>
    <gmd:MD_ScopeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode" codeListValue="service" />
  </gmd:hierarchyLevel>
  <gmd:contact>
    <gmd:CI_ResponsibleParty>
      <gmd:individualName>
        <gco:CharacterString>Beheer PDOK</gco:CharacterString>
      </gmd:individualName>
      <gmd:organisationName>
        <gco:CharacterString>Beheer PDOK</gco:CharacterString>
      </gmd:organisationName>
      <gmd:contactInfo>
        <gmd:CI_Contact>
          <gmd:address>
            <gmd:CI_Address>
              <gmd:electronicMailAddress>
                <gco:CharacterString>beheerPDOK@kadaster.nl</gco:CharacterString>
              </gmd:electronicMailAddress>
            </gmd:CI_Address>
          </gmd:address>
        </gmd:CI_Contact>
      </gmd:contactInfo>
      <gmd:role>
        <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="pointOfContact" />
      </gmd:role>
    </gmd:CI_ResponsibleParty>
  </gmd:contact>
  <gmd:dateStamp>
    <gco:Date>2015-05-18</gco:Date>
  </gmd:dateStamp>
  <gmd:metadataStandardName>
    <gco:CharacterString>ISO 19119</gco:CharacterString>
  </gmd:metadataStandardName>
  <gmd:metadataStandardVersion>
    <gco:CharacterString>Nederlands metadata profiel op ISO 19119 voor services 1.2</gco:CharacterString>
  </gmd:metadataStandardVersion>
  <gmd:identificationInfo>
    <srv:SV_ServiceIdentification>
      <gmd:citation>
        <gmd:CI_Citation>
          <gmd:title>
            <gco:CharacterString>RD-punten WMS</gco:CharacterString>
          </gmd:title>
          <gmd:date>
            <gmd:CI_Date>
              <gmd:date>
                <gco:Date>2012-12-06</gco:Date>
              </gmd:date>
              <gmd:dateType>
                <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_DateTypeCode" codeListValue="creation" />
              </gmd:dateType>
            </gmd:CI_Date>
          </gmd:date>
        </gmd:CI_Citation>
      </gmd:citation>
      <gmd:abstract>
        <gco:CharacterString>Overzicht van de ligging van de RD-punten, GNSS-referentiestations en GNSS-kernnetpunten in Nederland met de bijbehorende omschrijvingen en coordinaten in het stelsel van de Rijksdriehoeksmeting (RD) en het Europese stelsel ETRS-89.

Op http://www.kadaster.nl/web/artikel/download/Beschrijving-velden-RDinfo-PDOK-1.htm is uitleg beschikbaar over de inhoud van de velden van deze dataset.</gco:CharacterString>
      </gmd:abstract>
      <gmd:pointOfContact>
        <gmd:CI_ResponsibleParty>
          <gmd:organisationName>
            <gco:CharacterString>Beheer PDOK</gco:CharacterString>
          </gmd:organisationName>
          <gmd:contactInfo>
            <gmd:CI_Contact>
              <gmd:address>
                <gmd:CI_Address>
                  <gmd:electronicMailAddress>
                    <gco:CharacterString>beheerPDOK@kadaster.nl</gco:CharacterString>
                  </gmd:electronicMailAddress>
                </gmd:CI_Address>
              </gmd:address>
            </gmd:CI_Contact>
          </gmd:contactInfo>
          <gmd:role>
            <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="distributor" />
          </gmd:role>
        </gmd:CI_ResponsibleParty>
      </gmd:pointOfContact>
      <gmd:graphicOverview>
        <gmd:MD_BrowseGraphic>
          <gmd:fileName>
            <gco:CharacterString>http://geodata.nationaalgeoregister.nl/rdinfo/wms?LAYERS=stations&amp;FORMAT=image%2Fpng&amp;TRANSPARENT=TRUE&amp;SERVICE=WMS&amp;VERSION=1.1.1&amp;REQUEST=GetMap&amp;STYLES=&amp;SRS=EPSG%3A28992&amp;BBOX=109545.92,559566.4,174380.48,599483.2&amp;WIDTH=200&amp;HEIGHT=150</gco:CharacterString>
          </gmd:fileName>
        </gmd:MD_BrowseGraphic>
      </gmd:graphicOverview>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>RD-punten</gco:CharacterString>
          </gmd:keyword>
          <gmd:thesaurusName>
            <gmd:CI_Citation>
              <gmd:title />
              <gmd:date />
            </gmd:CI_Citation>
          </gmd:thesaurusName>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword />
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>infoMapAccessService</gco:CharacterString>
          </gmd:keyword>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:resourceConstraints>
        <gmd:MD_Constraints>
          <gmd:useLimitation>
            <gco:CharacterString>Geen gebruiksbeperking</gco:CharacterString>
          </gmd:useLimitation>
        </gmd:MD_Constraints>
      </gmd:resourceConstraints>
      <gmd:resourceConstraints>
        <gmd:MD_SecurityConstraints>
          <gmd:classification>
            <gmd:MD_ClassificationCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ClassificationCode" codeListValue="unclassified" />
          </gmd:classification>
        </gmd:MD_SecurityConstraints>
      </gmd:resourceConstraints>
      <gmd:resourceConstraints>
        <gmd:MD_LegalConstraints>
          <gmd:accessConstraints>
            <gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions" />
          </gmd:accessConstraints>
          <gmd:otherConstraints>
            <gco:CharacterString>geen beperkingen</gco:CharacterString>
          </gmd:otherConstraints>
          <gmd:otherConstraints>
            <gco:CharacterString>http://creativecommons.org/publicdomain/zero/1.0/</gco:CharacterString>
          </gmd:otherConstraints>
        </gmd:MD_LegalConstraints>
      </gmd:resourceConstraints>
      <srv:serviceType>
        <gco:LocalName>view</gco:LocalName>
      </srv:serviceType>
      <srv:extent>
        <gmd:EX_Extent>
          <gmd:geographicElement>
            <gmd:EX_GeographicBoundingBox>
              <gmd:westBoundLongitude>
                <gco:Decimal>3.37087</gco:Decimal>
              </gmd:westBoundLongitude>
              <gmd:eastBoundLongitude>
                <gco:Decimal>7.21097</gco:Decimal>
              </gmd:eastBoundLongitude>
              <gmd:southBoundLatitude>
                <gco:Decimal>50.7539</gco:Decimal>
              </gmd:southBoundLatitude>
              <gmd:northBoundLatitude>
                <gco:Decimal>53.4658</gco:Decimal>
              </gmd:northBoundLatitude>
            </gmd:EX_GeographicBoundingBox>
          </gmd:geographicElement>
          <gmd:temporalElement>
            <gmd:EX_TemporalExtent>
              <gmd:extent>
                <gml:TimePeriod gml:id="N10259">
                  <gml:begin>
                    <gml:TimeInstant gml:id="N1025E">
                      <gml:timePosition>1996-01-01</gml:timePosition>
                    </gml:TimeInstant>
                  </gml:begin>
                  <gml:end>
                    <gml:TimeInstant gml:id="N10267">
                      <gml:timePosition>2015-04-16</gml:timePosition>
                    </gml:TimeInstant>
                  </gml:end>
                </gml:TimePeriod>
              </gmd:extent>
            </gmd:EX_TemporalExtent>
          </gmd:temporalElement>
        </gmd:EX_Extent>
      </srv:extent>
      <srv:coupledResource>
        <srv:SV_CoupledResource>
          <srv:operationName>
            <gco:CharacterString>GetMap</gco:CharacterString>
          </srv:operationName>
          <srv:identifier>
            <gco:CharacterString>06b6c650-cdb1-11dd-ad8b-0800200c9a81</gco:CharacterString>
          </srv:identifier>
          <gco:ScopedName>punten</gco:ScopedName>
        </srv:SV_CoupledResource>
      </srv:coupledResource>
      <srv:coupledResource>
        <srv:SV_CoupledResource>
          <srv:operationName>
            <gco:CharacterString>GetFeatureInfo</gco:CharacterString>
          </srv:operationName>
          <srv:identifier>
            <gco:CharacterString>06b6c650-cdb1-11dd-ad8b-0800200c9a81</gco:CharacterString>
          </srv:identifier>
          <gco:ScopedName>punten</gco:ScopedName>
        </srv:SV_CoupledResource>
      </srv:coupledResource>
      <srv:coupledResource>
        <srv:SV_CoupledResource>
          <srv:operationName>
            <gco:CharacterString>GetMap</gco:CharacterString>
          </srv:operationName>
          <srv:identifier>
            <gco:CharacterString>06b6c650-cdb1-11dd-ad8b-0800200c9a81</gco:CharacterString>
          </srv:identifier>
          <gco:ScopedName>stations</gco:ScopedName>
        </srv:SV_CoupledResource>
      </srv:coupledResource>
      <srv:coupledResource>
        <srv:SV_CoupledResource>
          <srv:operationName>
            <gco:CharacterString>GetFeatureInfo</gco:CharacterString>
          </srv:operationName>
          <srv:identifier>
            <gco:CharacterString>06b6c650-cdb1-11dd-ad8b-0800200c9a81</gco:CharacterString>
          </srv:identifier>
          <gco:ScopedName>stations</gco:ScopedName>
        </srv:SV_CoupledResource>
      </srv:coupledResource>
      <srv:couplingType>
        <srv:SV_CouplingType codeList="http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#SV_CouplingType" codeListValue="tight" />
      </srv:couplingType>
      <srv:containsOperations>
        <srv:SV_OperationMetadata>
          <srv:operationName>
            <gco:CharacterString>GetCapabilities</gco:CharacterString>
          </srv:operationName>
          <srv:DCP>
            <srv:DCPList codeList="http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#DCPList" codeListValue="WebServices" />
          </srv:DCP>
          <srv:connectPoint>
            <gmd:CI_OnlineResource>
              <gmd:linkage>
                <gmd:URL>http://geodata.nationaalgeoregister.nl/rdinfo/ows?SERVICE=WMS&amp;</gmd:URL>
              </gmd:linkage>
              <gmd:protocol>
                <gco:CharacterString>OGC:WMS</gco:CharacterString>
              </gmd:protocol>
              <gmd:description>
                <gco:CharacterString>OGC:WMS, operation: GetCapabilities</gco:CharacterString>
              </gmd:description>
            </gmd:CI_OnlineResource>
          </srv:connectPoint>
        </srv:SV_OperationMetadata>
      </srv:containsOperations>
      <srv:containsOperations>
        <srv:SV_OperationMetadata>
          <srv:operationName>
            <gco:CharacterString>GetMap</gco:CharacterString>
          </srv:operationName>
          <srv:DCP>
            <srv:DCPList codeList="http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#DCPList" codeListValue="WebServices" />
          </srv:DCP>
          <srv:connectPoint>
            <gmd:CI_OnlineResource>
              <gmd:linkage>
                <gmd:URL>http://geodata.nationaalgeoregister.nl/rdinfo/ows?SERVICE=WMS&amp;</gmd:URL>
              </gmd:linkage>
              <gmd:protocol>
                <gco:CharacterString>OGC:WMS</gco:CharacterString>
              </gmd:protocol>
              <gmd:description>
                <gco:CharacterString>OGC:WMS, operation: GetMap</gco:CharacterString>
              </gmd:description>
            </gmd:CI_OnlineResource>
          </srv:connectPoint>
        </srv:SV_OperationMetadata>
      </srv:containsOperations>
      <srv:containsOperations>
        <srv:SV_OperationMetadata>
          <srv:operationName>
            <gco:CharacterString>GetFeatureInfo</gco:CharacterString>
          </srv:operationName>
          <srv:DCP>
            <srv:DCPList codeList="http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#DCPList" codeListValue="WebServices" />
          </srv:DCP>
          <srv:connectPoint>
            <gmd:CI_OnlineResource>
              <gmd:linkage>
                <gmd:URL>http://geodata.nationaalgeoregister.nl/rdinfo/ows?SERVICE=WMS&amp;</gmd:URL>
              </gmd:linkage>
              <gmd:protocol>
                <gco:CharacterString>OGC:WMS</gco:CharacterString>
              </gmd:protocol>
              <gmd:description>
                <gco:CharacterString>OGC:WMS, operation: GetFeatureInfo</gco:CharacterString>
              </gmd:description>
            </gmd:CI_OnlineResource>
          </srv:connectPoint>
        </srv:SV_OperationMetadata>
      </srv:containsOperations>
      <srv:operatesOn uuidref="06b6c650-cdb1-11dd-ad8b-0800200c9a81" xlink:href="http://nationaalgeoregister.nl/geonetwork/srv/en/csw?service=CSW&amp;version=2.0.2&amp;request=GetRecordById&amp;outputschema=http://www.isotc211.org/2005/gmd&amp;elementsetname=full&amp;id=29c17585-e702-463f-a5dc-99d34b17d333" />
    </srv:SV_ServiceIdentification>
  </gmd:identificationInfo>
  <gmd:distributionInfo>
    <gmd:MD_Distribution>
      <gmd:transferOptions>
        <gmd:MD_DigitalTransferOptions>
          <gmd:onLine>
            <gmd:CI_OnlineResource>
              <gmd:linkage>
                <gmd:URL>http://geodata.nationaalgeoregister.nl/rdinfo/ows?SERVICE=WMS&amp;</gmd:URL>
              </gmd:linkage>
              <gmd:protocol>
                <gco:CharacterString>OGC:WMS</gco:CharacterString>
              </gmd:protocol>
              <gmd:name>
                <gco:CharacterString>punten</gco:CharacterString>
              </gmd:name>
              <gmd:description>
                <gco:CharacterString>punten</gco:CharacterString>
              </gmd:description>
            </gmd:CI_OnlineResource>
          </gmd:onLine>
          <gmd:onLine>
            <gmd:CI_OnlineResource>
              <gmd:linkage>
                <gmd:URL>http://geodata.nationaalgeoregister.nl/rdinfo/ows?SERVICE=WMS&amp;</gmd:URL>
              </gmd:linkage>
              <gmd:protocol>
                <gco:CharacterString>OGC:WMS</gco:CharacterString>
              </gmd:protocol>
              <gmd:name>
                <gco:CharacterString>stations</gco:CharacterString>
              </gmd:name>
              <gmd:description>
                <gco:CharacterString>stations</gco:CharacterString>
              </gmd:description>
            </gmd:CI_OnlineResource>
          </gmd:onLine>
        </gmd:MD_DigitalTransferOptions>
      </gmd:transferOptions>
    </gmd:MD_Distribution>
  </gmd:distributionInfo>
  <gmd:dataQualityInfo>
    <gmd:DQ_DataQuality>
      <gmd:scope>
        <gmd:DQ_Scope>
          <gmd:level>
            <gmd:MD_ScopeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode" codeListValue="service" />
          </gmd:level>
        </gmd:DQ_Scope>
      </gmd:scope>
    </gmd:DQ_DataQuality>
  </gmd:dataQualityInfo>
</gmd:MD_Metadata>
    """
    And the metadata has been published to
Http GET to /geodatastore/doc/dataset/{identifier} streams the metadata defined in the format of the response header, the possibilities are text/html, application/json, application/ISO19139+xml or application/rdf+xml.

We still have some discussion on the fact if the service call includes the filename, the resources.get function in GeoNetwork requires this param to find the proper file in the metadata. Also when you use a filename you could also bypass the API and grab the file from a directory called /id/dataset/{uuid}.

This service should use some functionality of resource.get (translate a UUID to a file path and stream the file). We have to make sure, this call does take as least resources as possible, because potentially this call is done a lot (for example don’t create a session for a new user, use lucene only, no database). If multiple files (not being a thumbnail), use the first file or the file flagged as ‘main dataset’.

Service should return status 404 if file is not available, and write a msg in logfile (or send notification)
