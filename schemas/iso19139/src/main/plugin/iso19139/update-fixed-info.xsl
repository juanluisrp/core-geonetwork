<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:java="java:org.fao.geonet.util.XslUtil" exclude-result-prefixes="#all">

	<xsl:include href="../iso19139/convert/functions.xsl"/>
	<xsl:include href="../iso19139/convert/thesaurus-transformation.xsl"/>

  <xsl:variable name="serviceUrl" select="/root/env/siteURL" />
  
	<!-- ================================================================= -->

	<xsl:template match="/root">
		<xsl:apply-templates select="*:MD_Metadata"/>
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
		<xsl:message>IN gmd:dateStamp 2</xsl:message>
		<xsl:message>IN gmd:dateStamp 2 <xsl:value-of select="/root/env/changeDate" /></xsl:message>
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

	<xsl:template match="gmd:CI_OnlineResource[(matches(gmd:protocol/gco:CharacterString,'^WWW:DOWNLOAD-.*-http--download.*')
	                                              or gmd:protocol/gco:CharacterString='download') 
	                                               and gmd:name]">
		<xsl:variable name="fname" select="gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType"/>
		<xsl:variable name="mimeType">
			<xsl:call-template name="getMimeTypeFile">
				<xsl:with-param name="datadir" select="/root/env/datadir"/>
				<xsl:with-param name="fname" select="$fname"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="serverUrl" select="java:getBaseUrl()" />


		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<gmd:linkage>
				<gmd:URL>
					<xsl:choose>
						<xsl:when test="/root/env/system/downloadservice/simple='true'">
							<!--<xsl:value-of select="concat($serviceUrl,'resources.get?uuid=',/root/env/uuid,'&amp;fname=',$fname,'&amp;access=public&amp;gds=simple')"/>-->
							<xsl:value-of select="concat($serverUrl, '/id/dataset/', /root/env/uuid)"/>
						</xsl:when>
						<xsl:when test="/root/env/system/downloadservice/withdisclaimer='true'">
							<!--<xsl:value-of select="concat($serviceUrl,'file.disclaimer?uuid=',/root/env/uuid,'&amp;fname=',$fname,'&amp;access=public&amp;gds=withdisclaimer')"/>-->
							<xsl:value-of select="concat($serverUrl, '/id/dataset/', /root/env/uuid)"/>
						</xsl:when>
						<xsl:otherwise> <!-- /root/env/config/downloadservice/leave='true' -->
							<xsl:value-of select="gmd:linkage/gmd:URL"/>
						</xsl:otherwise>
					</xsl:choose>
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
	<!-- online resources: link-to-downloadable data etc -->
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

	<!-- ================================================================= -->

  <xsl:template match="gmx:FileName[name(..)!='gmd:contactInstructions']">
    <xsl:copy>
			<xsl:attribute name="src">
				<xsl:choose>
					<xsl:when test="/root/env/system/downloadservice/simple='true'">
						<xsl:value-of select="concat($serviceUrl,'/resources.get?uuid=',/root/env/uuid,'&amp;fname=',.,'&amp;access=private')"/>
					</xsl:when>
					<xsl:when test="/root/env/system/downloadservice/withdisclaimer='true'">
						<xsl:value-of select="concat($serviceUrl,'/file.disclaimer?uuid=',/root/env/uuid,'&amp;fname=',.,'&amp;access=private')"/>
					</xsl:when>
					<xsl:otherwise> <!-- /root/env/config/downloadservice/leave='true' -->
						<xsl:value-of select="@src"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:value-of select="."/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

	<!-- Do not allow to expand operatesOn sub-elements 
		and constrain users to use uuidref attribute to link
		service metadata to datasets. This will avoid to have
		error on XSD validation. -->

	<xsl:template match="srv:operatesOn|gmd:featureCatalogueCitation">
        <xsl:copy>
        <xsl:copy-of select="@uuidref"/>
        <xsl:if test="@uuidref">
            <xsl:choose>
                <xsl:when test="not(string(@xlink:href)) or starts-with(@xlink:href, $serviceUrl)">
                    <xsl:attribute name="xlink:href">
                        <xsl:value-of select="concat($serviceUrl,'csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=',@uuidref)"/>
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="@xlink:href"/>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:if>
        </xsl:copy>

	</xsl:template>

	<!-- Thumbnail may not contains full URL for the one updated to the catalog -->
	<xsl:template match="gmd:MD_BrowseGraphic[
					gmd:fileDescription/gco:CharacterString = 'thumbnail' or
					gmd:fileDescription/gco:CharacterString = 'large_thumbnail']/
						gmd:fileName[gco:CharacterString != '' and not(starts-with(gco:CharacterString, 'http'))]">
			<gmd:fileName>
				<gco:CharacterString>
					<xsl:value-of select="concat(
					    $serviceUrl, 'resources.get?',
					    'uuid=', /root/env/uuid,
					    '&amp;fname=', gco:CharacterString)"/>
				</gco:CharacterString>
			</gmd:fileName>
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

	<!-- ==================== geodatastore =====================-->
	<!-- Dutch profile require dateStamp to be a gco:Date -->
	<xsl:template match="gmd:dateStamp" priority="99">
		<xsl:choose>
			<xsl:when test="/root/env/changeDate">
				<xsl:copy>
					<gco:Date>
						<xsl:value-of select="tokenize(/root/env/changeDate,'T')[1]" />
					</gco:Date>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!-- ================================================================= -->
	<!-- copy everything else as is -->
	
	<xsl:template match="@*|node()">
	    <xsl:copy>
	        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
	</xsl:template>

</xsl:stylesheet>
