<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:uuid="java:java.util.UUID"
  xmlns:java="java:org.fao.geonet.util.XslUtil" exclude-result-prefixes="#all">

	<xsl:include href="../iso19139/convert/functions.xsl"/>
	<xsl:include href="../iso19139/convert/thesaurus-transformation.xsl"/>

  <xsl:variable name="serviceUrl" select="/root/env/siteURL" />
	<xsl:variable name="schemaTranslationsDir" select="/root/env/schemaTranslationsDir" />
	<xsl:variable name="labelsFile" select="document(concat('file:///', $schemaTranslationsDir, '/labels.xml'))"/>

	<!-- ================================================================= -->

	<xsl:template match="/root">
		<xsl:apply-templates select="*:MD_Metadata"/>
	</xsl:template>

	<!-- ================================================================= -->

  <!-- Fix schemaLocation if not complete -->
  <xsl:template match="@xsi:schemaLocation">

    <xsl:variable name="isService" select="count(//srv:SV_ServiceIdentification) > 0" />

    <xsl:variable name="schemaLocationInfo">
      <xsl:choose>
        <xsl:when test="not(contains(., 'http://www.isotc211.org/2005/gmd')) or
                        not(contains(., 'http://www.isotc211.org/2005/gmx')) or
                        ($isService and not(contains(., 'http://www.isotc211.org/2005/srv')))">
          <xsl:value-of select="." />
          <xsl:if test="not(contains(., 'http://www.isotc211.org/2005/gmd'))"><xsl:text> </xsl:text>http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd</xsl:if>
          <xsl:if test="not(contains(., 'http://www.isotc211.org/2005/gmx'))"><xsl:text> </xsl:text>http://www.isotc211.org/2005/gmx http://www.isotc211.org/2005/gmx/gmx.xsd</xsl:if>
          <xsl:if test="($isService and not(contains(., 'http://www.isotc211.org/2005/srv')))"><xsl:text> </xsl:text>http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd</xsl:if>
        </xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="string(normalize-space($schemaLocationInfo))">
        <xsl:attribute name="xsi:schemaLocation"><xsl:value-of select="normalize-space($schemaLocationInfo)" /></xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="." />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="gmd:MD_Metadata">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>

			<gmd:fileIdentifier>
				<gco:CharacterString>
					<xsl:value-of select="/root/env/uuid"/>
				</gco:CharacterString>
			</gmd:fileIdentifier>

			<xsl:apply-templates select="gmd:language"/>
			<xsl:apply-templates select="gmd:characterSet"/>

			<xsl:choose>
				<xsl:when test="/root/env/parentUuid!=''">
					<gmd:parentIdentifier>
						<gco:CharacterString>
							<xsl:value-of select="/root/env/parentUuid"/>
						</gco:CharacterString>
					</gmd:parentIdentifier>
				</xsl:when>
				<xsl:when test="gmd:parentIdentifier">
					<xsl:copy-of select="gmd:parentIdentifier"/>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="node()[not(self::gmd:language) and not(self::gmd:characterSet)]"/>
		</xsl:copy>
	</xsl:template>


	<!-- ================================================================= -->
	<!-- Do not process MD_Metadata header generated by previous template  -->

	<xsl:template match="gmd:MD_Metadata/gmd:fileIdentifier|gmd:MD_Metadata/gmd:parentIdentifier" priority="10"/>

	<!-- ================================================================= -->

	<xsl:template match="gmd:dateStamp">
    <xsl:choose>
        <xsl:when test="/root/env/changeDate">
            <xsl:copy>
                    <gco:DateTime>
                        <xsl:value-of select="/root/env/changeDate"/>
                    </gco:DateTime>
            </xsl:copy>
        </xsl:when>
        <xsl:otherwise>
            <xsl:copy-of select="."/>
        </xsl:otherwise>
    </xsl:choose>
	</xsl:template>

	<!-- ================================================================= -->

	<!-- Only set metadataStandardName and metadataStandardVersion
	if not set. -->
	<xsl:template match="gmd:metadataStandardName[@gco:nilReason='missing' or gco:CharacterString='']" priority="10">
		<xsl:copy>
			<gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="gmd:metadataStandardVersion[@gco:nilReason='missing' or gco:CharacterString='']" priority="10">
		<xsl:copy>
			<gco:CharacterString>1.0</gco:CharacterString>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

	<xsl:template match="@gml:id">
		<xsl:choose>
			<xsl:when test="normalize-space(.)=''">
				<xsl:attribute name="gml:id">
					<xsl:value-of select="generate-id(.)"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ==================================================================== -->
	<!-- Fix srsName attribute generate CRS:84 (EPSG:4326 with long/lat
	     ordering) by default -->

	<xsl:template match="@srsName">
		<xsl:choose>
			<xsl:when test="normalize-space(.)=''">
				<xsl:attribute name="srsName">
					<xsl:text>CRS:84</xsl:text>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

  <!-- Add required gml attributes if missing -->
  <xsl:template match="gml:Polygon[not(@gml:id) and not(@srsName)]">
    <xsl:copy>
      <xsl:attribute name="gml:id">
        <xsl:value-of select="generate-id(.)"/>
      </xsl:attribute>
      <xsl:attribute name="srsName">
        <xsl:text>urn:x-ogc:def:crs:EPSG:6.6:4326</xsl:text>
      </xsl:attribute>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="*"/>
    </xsl:copy>
  </xsl:template>

	<!-- ================================================================= -->

	<xsl:template match="*[gco:CharacterString]">
		<xsl:copy>
			<xsl:apply-templates select="@*[not(name()='gco:nilReason')]"/>
			<xsl:choose>
				<xsl:when test="normalize-space(gco:CharacterString)=''">
					<xsl:attribute name="gco:nilReason">
						<xsl:choose>
							<xsl:when test="@gco:nilReason"><xsl:value-of select="@gco:nilReason"/></xsl:when>
							<xsl:otherwise>missing</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="@gco:nilReason!='missing' and normalize-space(gco:CharacterString)!=''">
					<xsl:copy-of select="@gco:nilReason"/>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- codelists: set @codeList path -->
	<!-- ================================================================= -->
	<xsl:template match="gmd:LanguageCode[@codeListValue]" priority="10">
		<gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/">
			<xsl:apply-templates select="@*[name(.)!='codeList']"/>
		</gmd:LanguageCode>
	</xsl:template>


  <xsl:template match="gmd:*[@codeListValue]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="codeList">
			  <xsl:value-of select="concat('http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#',local-name(.))"/>
			</xsl:attribute>
		</xsl:copy>
	</xsl:template>

	<!-- can't find the location of the 19119 codelists - so we make one up -->

	<xsl:template match="srv:*[@codeListValue]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="codeList">
				<xsl:value-of select="concat('http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#',local-name(.))"/>
			</xsl:attribute>
		</xsl:copy>
	</xsl:template>
	<!-- ================================================================= -->
	<!-- online resources: download -->
	<!-- ================================================================= -->

	<xsl:template match="gmd:CI_OnlineResource[matches(gmd:protocol/gco:CharacterString,'^WWW:DOWNLOAD-.*-http--download.*') and gmd:name]">
		<xsl:variable name="fname" select="gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType"/>
		<xsl:variable name="mimeType">
			<xsl:call-template name="getMimeTypeFile">
				<xsl:with-param name="datadir" select="/root/env/datadir"/>
				<xsl:with-param name="fname" select="$fname"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<gmd:linkage>
				<gmd:URL>
					<xsl:value-of select="gmd:linkage/gmd:URL"/>
				</gmd:URL>
			</gmd:linkage>
			<xsl:copy-of select="gmd:protocol"/>
			<xsl:copy-of select="gmd:applicationProfile"/>
			<gmd:name>
				<gmx:MimeFileType type="{$mimeType}">
					<xsl:value-of select="$fname"/>
				</gmx:MimeFileType>
			</gmd:name>
			<xsl:copy-of select="gmd:description"/>
			<xsl:copy-of select="gmd:function"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- Add mime type for downloadable online resources -->
	<!-- ================================================================= -->

	<xsl:template match="gmd:CI_OnlineResource[starts-with(gmd:protocol/gco:CharacterString,'WWW:LINK-') and contains(gmd:protocol/gco:CharacterString,'http--download')]">
		<xsl:variable name="mimeType">
			<xsl:call-template name="getMimeTypeUrl">
				<xsl:with-param name="linkage" select="gmd:linkage/gmd:URL"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of select="gmd:linkage"/>
			<xsl:copy-of select="gmd:protocol"/>
			<xsl:copy-of select="gmd:applicationProfile"/>
			<gmd:name>
				<gmx:MimeFileType type="{$mimeType}"/>
			</gmd:name>
			<xsl:copy-of select="gmd:description"/>
			<xsl:copy-of select="gmd:function"/>
		</xsl:copy>
	</xsl:template>

  <xsl:template match="gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation">
    <xsl:copy>
      <xsl:copy-of select="@*" />

      <xsl:apply-templates select="gmd:title" />
      <xsl:apply-templates select="gmd:alternateTitle" />
      <xsl:apply-templates select="gmd:date" />
      <xsl:apply-templates select="gmd:edition" />
      <xsl:apply-templates select="gmd:editionDate" />

      <xsl:choose>
        <!-- record of type dataset or dataset series are created we shall automatically add a UUID for resource-identifier -->
        <xsl:when test="(count(//gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='dataset']) > 0) or
              (count(//gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='series']) > 0)">

          <xsl:choose>
            <!-- Identifier doesn't exists - Add it -->
            <xsl:when test="not(gmd:identifier)">
              <gmd:identifier>
                <gmd:MD_Identifier>
                  <gmd:code>
                    <gco:CharacterString><xsl:value-of select="uuid:randomUUID()"/></gco:CharacterString>
                  </gmd:code>
                </gmd:MD_Identifier>
              </gmd:identifier>
            </xsl:when>

            <!-- Identifier incomplete doesn't exists - Add it -->
            <xsl:when test="count(gmd:identifier) = 1 and not(gmd:identifier/gmd:MD_Identifier)">
              <xsl:for-each select="gmd:identifier">
                <xsl:copy>
                  <xsl:copy-of select="@*" />

                  <gmd:MD_Identifier>
                    <gmd:code>
                      <gco:CharacterString><xsl:value-of select="uuid:randomUUID()"/></gco:CharacterString>
                    </gmd:code>
                  </gmd:MD_Identifier>

                </xsl:copy>
              </xsl:for-each>
            </xsl:when>

            <!-- Process identifiers to check at least 1 has a code -->
            <xsl:otherwise>

              <xsl:choose>
                <!-- No identifier with code value - Add it -->
                <xsl:when test="count(gmd:identifier[string(gmd:MD_Identifier/gmd:code/gco:CharacterString)]) = 0">
                  <xsl:for-each select="gmd:identifier[gmd:MD_Identifier]">
                    <xsl:choose>
                      <!-- Add to first element -->
                      <xsl:when test="position() = 1">
                        <xsl:copy>
                          <xsl:copy-of select="@*" />

                          <xsl:for-each select="gmd:MD_Identifier">
                            <xsl:copy>
                              <xsl:copy-of select="@*" />
                              <gmd:code>
                                <gco:CharacterString><xsl:value-of select="uuid:randomUUID()"/></gco:CharacterString>
                              </gmd:code>
                            </xsl:copy>
                          </xsl:for-each>
                        </xsl:copy>
                      </xsl:when>

                      <!-- Copy the rest -->
                      <xsl:otherwise>
                        <xsl:copy-of select="." />
                      </xsl:otherwise>
                    </xsl:choose>

                  </xsl:for-each>
                </xsl:when>

                <!-- identifiers with code - process identifiers -->
                <xsl:otherwise>
                  <xsl:apply-templates select="gmd:identifier" />
                </xsl:otherwise>
              </xsl:choose>

            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <!-- other type fo records - process identifiers -->
        <xsl:otherwise>
          <xsl:apply-templates select="gmd:identifier" />
        </xsl:otherwise>
      </xsl:choose>



      <xsl:apply-templates select="gmd:citedResponsibleParty" />
      <xsl:apply-templates select="gmd:presentationForm" />
      <xsl:apply-templates select="gmd:series" />
      <xsl:apply-templates select="gmd:otherCitationDetails" />
      <xsl:apply-templates select="gmd:collectiveTitle" />
      <xsl:apply-templates select="gmd:ISBN" />
      <xsl:apply-templates select="gmd:ISSN" />
    </xsl:copy>
  </xsl:template>

	<!-- ================================================================= -->

	<!-- Do not allow to expand operatesOn sub-elements
		and constrain users to use uuidref attribute to link
		service metadata to datasets. This will avoid to have
		error on XSD validation. -->

  <xsl:template match="srv:operatesOn|gmd:featureCatalogueCitation">
    <xsl:copy>
      <xsl:choose>
        <!-- Do not expand operatesOn sub-elements when using uuidref
             to link service metadata to datasets or datasets to iso19110.
         -->
        <xsl:when test="@uuidref">
          <xsl:copy-of select="@uuidref"/>

          <xsl:choose>
            <xsl:when test="not(string(@xlink:href)) or starts-with(@xlink:href, $serviceUrl)">
              <xsl:attribute name="xlink:href">
                <xsl:value-of
                  select="concat($serviceUrl,'csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=',@uuidref)"/>
              </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="@xlink:href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <xsl:otherwise>
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates select="node()" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>


  <!-- ================================================================= -->
	<!-- Set local identifier to the first 3 letters of iso code. Locale ids
		are used for multilingual charcterString using #iso2code for referencing.
	-->
	<xsl:template match="gmd:PT_Locale">
		<xsl:element name="gmd:{local-name()}">
			<xsl:variable name="id" select="upper-case(java:twoCharLangCode(gmd:languageCode/gmd:LanguageCode/@codeListValue))"/>

			<xsl:apply-templates select="@*"/>
			<xsl:if test="normalize-space(@id)='' or normalize-space(@id)!=$id">
				<xsl:attribute name="id">
					<xsl:value-of select="$id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>

	<!-- Apply same changes as above to the gmd:LocalisedCharacterString -->
	<xsl:variable name="language" select="//gmd:PT_Locale" /> <!-- Need list of all locale -->
	<xsl:template  match="gmd:LocalisedCharacterString">
		<xsl:element name="gmd:{local-name()}">
			<xsl:variable name="currentLocale" select="upper-case(replace(normalize-space(@locale), '^#', ''))"/>
			<xsl:variable name="ptLocale" select="$language[upper-case(replace(normalize-space(@id), '^#', ''))=string($currentLocale)]"/>
			<xsl:variable name="id" select="upper-case(java:twoCharLangCode($ptLocale/gmd:languageCode/gmd:LanguageCode/@codeListValue))"/>
			<xsl:apply-templates select="@*"/>
			<xsl:if test="$id != '' and ($currentLocale='' or @locale!=concat('#', $id)) ">
				<xsl:attribute name="locale">
					<xsl:value-of select="concat('#',$id)"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>

  <!-- Remove attribute indeterminatePosition having empty
  value which is not a valid facet for it. -->
  <xsl:template match="@indeterminatePosition[. = '']" priority="2"/>

  <xsl:template match="gmd:descriptiveKeywords[@xlink:href]" priority="10">
    <xsl:variable name="isAllThesaurus" select="contains(@xlink:href, 'thesaurus=external.none.allThesaurus')" />
    <xsl:variable name="allThesaurusFinished" select="count(preceding-sibling::gmd:descriptiveKeywords[contains(@xlink:href, 'thesaurus=external.none.allThesaurus')]) > 0" />

    <xsl:choose>
      <xsl:when test="$isAllThesaurus and not($allThesaurusFinished)">
        <xsl:variable name="allThesaurusEl" select="../gmd:descriptiveKeywords[contains(@xlink:href, 'thesaurus=external.none.allThesaurus')]" />
        <xsl:variable name="ids">
            <xsl:for-each select="$allThesaurusEl/tokenize(replace(@xlink:href, '.+id=([^&amp;]+).*', '$1'), ',')">
              <keyword>
                <thes><xsl:value-of select="replace(., 'http://org.fao.geonet.thesaurus.all/(.+)@@@.+', '$1')"/></thes>
                <id><xsl:value-of select="replace(., 'http://org.fao.geonet.thesaurus.all/.+@@@(.+)', '$1')"/></id>
              </keyword>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="hrefPrefix" select="replace(@xlink:href, '(.+\?).*', '$1')"/>
        <xsl:variable name="hrefQuery" select="replace(@xlink:href, '.+\?(.*)', '$1')"/>
        <xsl:variable name="params">
            <xsl:for-each select="$allThesaurusEl/tokenize($hrefQuery, '\?|&amp;')">
              <param>
                <key><xsl:value-of select="tokenize(., '=')[1]"/></key>
                <val><xsl:value-of select="tokenize(., '=')[2]"/></val>
              </param>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="uniqueParams" select="distinct-values($params//key[. != 'id' and . != 'thesaurus' and . != 'multiple']/text())"/>
        <xsl:variable name="queryString">
          <xsl:for-each select="$uniqueParams">
            <xsl:variable name="p" select="." />
            <xsl:value-of select="concat('&amp;', ., '=', $params/param[key/text() = $p]/val)" />
          </xsl:for-each>
        </xsl:variable>


        <xsl:variable name="thesaurusNames" select="distinct-values($ids//thes)" />
        <xsl:variable name="context" select="."/>
        <xsl:variable name="root" select="/"/>
        <xsl:for-each select="$thesaurusNames" >
          <xsl:variable name="thesaurusName" select="."/>

          <xsl:variable name="finalIds">
            <xsl:value-of separator="," select="$ids/keyword[thes/text() = $thesaurusName]/id" />
          </xsl:variable>

          <gmd:descriptiveKeywords
              xlink:href="{concat($hrefPrefix, 'thesaurus=', $thesaurusName, '&amp;id=', $finalIds, '&amp;multiple=true',$queryString)}"
              xlink:show="{$context/@xlink:show}">
          </gmd:descriptiveKeywords>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$isAllThesaurus and $allThesaurusFinished">
        <!--Do nothing-->
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gmd:descriptiveKeywords[not(@xlink:href)]" priority="10">
    <xsl:variable name="isAllThesaurus" select="count(gmd:MD_Keywords/gmd:keyword[starts-with(@gco:nilReason,'thesaurus::')]) > 0" />
    <xsl:variable name="allThesaurusFinished" select="count(preceding-sibling::gmd:descriptiveKeywords[not(@xlink:href)]/gmd:MD_Keywords/gmd:keyword[starts-with(@gco:nilReason,'thesaurus::')]) > 0" />
    <xsl:choose>
      <xsl:when test="$isAllThesaurus and not($allThesaurusFinished)">
        <xsl:variable name="thesaurusNames" select="distinct-values(../gmd:descriptiveKeywords[not(@xlink:href)]/gmd:MD_Keywords/gmd:keyword/@gco:nilReason[starts-with(.,'thesaurus::')])" />
        <xsl:variable name="context" select="."/>
        <xsl:variable name="root" select="/"/>
        <xsl:for-each select="$thesaurusNames" >
          <xsl:variable name="thesaurusName" select="."/>
          <xsl:variable name="keywords" select="$context/../gmd:descriptiveKeywords[not(@xlink:href)]/gmd:MD_Keywords/gmd:keyword[@gco:nilReason = $thesaurusName]"/>

          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="$keywords">
                <gmd:keyword>
                  <xsl:copy-of select="./node()"/>
                </gmd:keyword>
              </xsl:for-each>

              <xsl:copy-of select="geonet:add-thesaurus-info(substring-after(., 'thesaurus::'), true(), $root/root/env/thesauri, true())" />

            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$isAllThesaurus and $allThesaurusFinished">
        <!--Do nothing-->
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

	<xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification">
		<xsl:copy>
			<xsl:copy-of select="@*" />

			<xsl:apply-templates select="gmd:citation" />
			<xsl:apply-templates select="gmd:abstract" />
			<xsl:apply-templates select="gmd:purpose" />
			<xsl:apply-templates select="gmd:credit" />
			<xsl:apply-templates select="gmd:status" />
			<xsl:apply-templates select="gmd:pointOfContact" />
			<xsl:apply-templates select="gmd:resourceMaintenance" />

      <!-- Copy only non-empty gmd:graphicOverview -->
      <xsl:for-each select="gmd:graphicOverview">
        <xsl:if test="string(gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString) or
                      string(gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString) or
                      string(gmd:MD_BrowseGraphic/gmd:fileType/gco:CharacterString)">
          <xsl:apply-templates select="." />
        </xsl:if>
      </xsl:for-each>

			<xsl:apply-templates select="gmd:resourceFormat" />
			<xsl:apply-templates select="gmd:descriptiveKeywords" />
			<xsl:apply-templates select="gmd:resourceSpecificUsage" />
			<xsl:apply-templates select="gmd:resourceConstraints" />
			<xsl:apply-templates select="gmd:aggregationInfo" />
			<xsl:apply-templates select="gmd:spatialRepresentationType" />

			<!-- Remove spatial resolutions with empty values -->
			<xsl:for-each select="gmd:spatialResolution">
				<xsl:choose>
					<xsl:when test="gmd:MD_Resolution/gmd:equivalentScale">
						<xsl:if test="string(gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer)">
							<xsl:apply-templates select="." />
						</xsl:if>
					</xsl:when>
					<xsl:when test="gmd:MD_Resolution/gmd:distance">
						<xsl:if test="string(gmd:MD_Resolution/gmd:distance/gco:Distance)">
							<xsl:apply-templates select="." />
						</xsl:if>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>

			<xsl:apply-templates select="gmd:language" />
			<xsl:apply-templates select="gmd:characterSet" />
			<xsl:apply-templates select="gmd:topicCategory" />
			<xsl:apply-templates select="gmd:environmentDescription" />
			<xsl:apply-templates select="gmd:extent" />
			<xsl:apply-templates select="gmd:supplementalInformation" />
		</xsl:copy>
	</xsl:template>

  <xsl:template match="gmd:identificationInfo/srv:SV_ServiceIdentification">
    <xsl:copy>
      <xsl:copy-of select="@*" />


      <xsl:apply-templates select="gmd:citation" />
      <xsl:apply-templates select="gmd:abstract" />
      <xsl:apply-templates select="gmd:purpose" />
      <xsl:apply-templates select="gmd:credit" />
      <xsl:apply-templates select="gmd:status" />
      <xsl:apply-templates select="gmd:pointOfContact" />

      <xsl:variable name="isSDS" select="//gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName = 'other'" />
      <xsl:if test="$isSDS = true()">
        <xsl:variable name="existsCustodian" select="count(gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue = 'custodian']) > 0" />
        <xsl:variable name="existsDistributorCustodian" select="count(//gmd:distributorContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue = 'custodian']) > 0" />

        <xsl:if test="$existsCustodian = false() and $existsDistributorCustodian = true()">
          <gmd:pointOfContact>
            <xsl:copy-of select="//gmd:distributorContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue = 'custodian']/gmd:CI_ResponsibleParty" />
          </gmd:pointOfContact>
        </xsl:if>
      </xsl:if>

      <xsl:apply-templates select="gmd:resourceMaintenance" />
      <xsl:apply-templates select="gmd:graphicOverview" />
      <xsl:apply-templates select="gmd:resourceFormat" />
      <xsl:apply-templates select="gmd:descriptiveKeywords" />
      <xsl:apply-templates select="gmd:resourceSpecificUsage" />
      <xsl:apply-templates select="gmd:resourceConstraints" />
      <xsl:apply-templates select="gmd:aggregationInfo" />

      <xsl:apply-templates select="srv:serviceType" />
      <xsl:apply-templates select="srv:serviceTypeVersion" />
      <xsl:apply-templates select="srv:accessProperties" />
      <xsl:apply-templates select="srv:restrictions" />
      <xsl:apply-templates select="srv:keywords" />
      <xsl:apply-templates select="srv:extent" />
      <xsl:apply-templates select="srv:coupledResource" />
      <xsl:apply-templates select="srv:couplingType" />
      <xsl:apply-templates select="srv:containsOperations" />
      <xsl:apply-templates select="srv:operatesOn" />
    </xsl:copy>
  </xsl:template>

	<xsl:template match="gmd:MD_Distributor">
		<xsl:copy>
			<xsl:copy-of select="@*" />

			<xsl:apply-templates select="gmd:distributorContact" />
			<xsl:apply-templates select="gmd:distributionOrderProcess" />

      <!-- Copy only non-empty distributor formats -->
			<xsl:for-each select="gmd:distributorFormat">
				<xsl:variable name="hasInfo" select="string(gmd:MD_Format/gmd:name/*) or
																						 string(gmd:MD_Format/gmd:version/*) or
																						 string(gmd:MD_Format/gmd:amendmentNumber/*) or
																						 string(gmd:MD_Format/gmd:specification/*) or
																						 string(gmd:MD_Format/gmd:fileDecompressionTechnique/*) or
																						 gmd:MD_Format/gmd:formatDistributor" />

				<xsl:if test="$hasInfo">
					<xsl:apply-templates select="." />
				</xsl:if>
			</xsl:for-each>

			<xsl:apply-templates select="gmd:distributorTransferOptions" />
		</xsl:copy>
	</xsl:template>


  <xsl:template match="gmd:MD_DigitalTransferOptions">
    <xsl:copy>
      <xsl:copy-of select="@*" />

      <xsl:apply-templates select="gmd:unitsOfDistribution" />
      <xsl:apply-templates select="gmd:transferSize" />

      <!-- Copy only non-empty online resources -->
      <xsl:for-each select="gmd:onLine">
        <xsl:variable name="hasInfo" select="string(gmd:CI_OnlineResource/gmd:linkage/*) or
																						 string(gmd:CI_OnlineResource/gmd:protocol/*) or
																						 string(gmd:CI_OnlineResource/gmd:applicationProfile/*) or
																						 string(gmd:CI_OnlineResource/gmd:name /*) or
																						 string(gmd:CI_OnlineResource/gmd:description/*) or
																						 string(gmd:CI_OnlineResource/gmd:function/*)" />

        <xsl:if test="$hasInfo">
          <xsl:apply-templates select="." />
        </xsl:if>
      </xsl:for-each>

      <xsl:apply-templates select="gmd:offLine" />

    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->
	<!-- Adjust the namespace declaration - In some cases name() is used to get the
		element. The assumption is that the name is in the format of  <ns:element>
		however in some cases it is in the format of <element xmlns=""> so the
		following will convert them back to the expected value. This also corrects the issue
		where the <element xmlns=""> loose the xmlns="" due to the exclude-result-prefixes="#all" -->
	<!-- Note: Only included prefix gml, gmd and gco for now. -->
	<!-- TODO: Figure out how to get the namespace prefix via a function so that we don't need to hard code them -->
	<!-- ================================================================= -->

	<xsl:template name="correct_ns_prefix">
		<xsl:param name="element" />
		<xsl:param name="prefix" />
		<xsl:choose>
			<xsl:when test="local-name($element)=name($element) and $prefix != '' ">
				<xsl:element name="{$prefix}:{local-name($element)}">
					<xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*|node()"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="gmd:*">
		<xsl:call-template name="correct_ns_prefix">
			<xsl:with-param name="element" select="."/>
			<xsl:with-param name="prefix" select="'gmd'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="gco:*">
		<xsl:call-template name="correct_ns_prefix">
			<xsl:with-param name="element" select="."/>
			<xsl:with-param name="prefix" select="'gco'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="gml:*">
		<xsl:call-template name="correct_ns_prefix">
			<xsl:with-param name="element" select="."/>
			<xsl:with-param name="prefix" select="'gml'"/>
		</xsl:call-template>
	</xsl:template>


	<!-- If the value is defined in the labels helper (contains the value in the helper) and contains
			 a title value (related attribute), it's stored as gmx:Anchor. Otherwise is stored as gco:CharacterString
	-->
	<xsl:template match="gmd:otherConstraints[not(contains(gmx:Anchor/@xlink:href, 'ConditionsApplyingToAccessAndUse')) and
	                                          not(contains(gmx:Anchor/@xlink:href, 'LimitationsOnPublicAccess')) and
	                                           not(contains(gmx:Anchor/@xlink:href, 'anvandningsrestriktioner.xml')) and
	                                           not(contains(gmx:Anchor/@xlink:href, 'atkomstrestriktioner.xml'))]" priority="1000">
		<xsl:variable name="value" select="*/text()" />

		<xsl:variable name="valueInHelper" select="$labelsFile/labels/element[@name='gmd:otherConstraints']/helper/option[contains($value, @value)]/@title" />

		<!--<xsl:message>value: <xsl:value-of select="$value" /></xsl:message>
		<xsl:message>valueInHelper: <xsl:value-of select="$valueInHelper" /></xsl:message>-->

		<xsl:choose>
			<xsl:when test="string($valueInHelper)">
				<gmd:otherConstraints>
					<xsl:copy-of select="@*" />
					<gmx:Anchor xlink:href="{$valueInHelper}"><xsl:value-of select="$value" /></gmx:Anchor>
				</gmd:otherConstraints>

			</xsl:when>
			<xsl:otherwise>
        <gmd:otherConstraints>
          <xsl:copy-of select="@*" />
          <gco:CharacterString><xsl:value-of select="$value" /></gco:CharacterString>
        </gmd:otherConstraints>
      </xsl:otherwise>
		</xsl:choose>
	</xsl:template>

  <!-- Fix the date for conformance reports -->
  <xsl:template match="gmd:specification/gmd:CI_Citation[gmd:title/gmx:Anchor/@xlink:href='http://data.europa.eu/eli/reg/2010/1089' or
                                                         gmd:title/gmx:Anchor/@xlink:href='http://data.europa.eu/eli/reg/2009/976']" priority="1000">
    <xsl:copy>
      <xsl:copy-of select="@*" />

      <xsl:apply-templates select="gmd:title" />
      <xsl:apply-templates select="gmd:alternateTitle" />

      <!-- Fix the date -->
      <xsl:choose>
        <xsl:when test="gmd:title/gmx:Anchor/@xlink:href='http://data.europa.eu/eli/reg/2010/1089'">
          <gmd:date>
            <gmd:CI_Date>
              <gmd:date>
                <gco:Date>2010-12-08</gco:Date>
              </gmd:date>
              <gmd:dateType>
                <gmd:CI_DateTypeCode
                  codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode"
                  codeListValue="publication"/>
              </gmd:dateType>
            </gmd:CI_Date>
          </gmd:date>
        </xsl:when>
        <xsl:when test="gmd:title/gmx:Anchor/@xlink:href='http://data.europa.eu/eli/reg/2009/976'">
          <gmd:date>
            <gmd:CI_Date>
              <gmd:date>
                <gco:Date>2009-10-20</gco:Date>
              </gmd:date>
              <gmd:dateType>
                <gmd:CI_DateTypeCode
                  codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode"
                  codeListValue="publication"/>
              </gmd:dateType>
            </gmd:CI_Date>
          </gmd:date>
        </xsl:when>
      </xsl:choose>

      <xsl:apply-templates select="gmd:edition" />
      <xsl:apply-templates select="gmd:editionDate" />
      <xsl:apply-templates select="gmd:identifier" />
      <xsl:apply-templates select="gmd:citedResponsibleParty" />
      <xsl:apply-templates select="gmd:presentationForm" />
      <xsl:apply-templates select="gmd:series" />
      <xsl:apply-templates select="gmd:otherCitationDetails" />
      <xsl:apply-templates select="gmd:collectiveTitle" />
      <xsl:apply-templates select="gmd:ISBN" />
      <xsl:apply-templates select="gmd:ISSN" />
    </xsl:copy>
  </xsl:template>

  <!-- Remove temporal extent if empty beginPosition and endPosition -->
  <xsl:template match="gmd:temporalElement[gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition]">
    <xsl:choose>
      <xsl:when test="not(string(gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition)) and
                      not(string(gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition))">
        <!-- Remove element if empty values in beginPosition and endPosition -->
      </xsl:when>

      <xsl:otherwise>
        <xsl:copy>
          <xsl:copy-of select="@*" />
          <xsl:apply-templates select="*" />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Fix metadata contact role to pointOfContact -->
  <xsl:template match="gmd:contact/gmd:CI_ResponsibleParty/gmd:role" priority="10">
    <gmd:role>
      <gmd:CI_RoleCode codeListValue="pointOfContact"
                       codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode"/>
    </gmd:role>
  </xsl:template>

<!-- ================================================================= -->
	<!-- copy everything else as is -->

	<xsl:template match="@*|node()">
	    <xsl:copy>
	        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
	</xsl:template>

</xsl:stylesheet>
