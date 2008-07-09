<?xml version="1.0" encoding="UTF-8"?>
<!--
 Licensed to the Outerthought bvba and Schaubroeck NV under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information regarding
 copyright ownership.  Outerthought bvba and Schaubroeck NV license
 this file to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://outerx.org/daisy/1.0#publisher"
                xmlns:ns="http://outerx.org/daisy/1.0"
                xmlns:d="http://outerx.org/daisy/1.0"
                exclude-result-prefixes="p d ns">

  <xsl:output method="xml"/>
  <xsl:output omit-xml-declaration="no"/>

  <xsl:preserve-space elements="pre"/>

  <xsl:param name="author"/>
  <xsl:param name="documentName"/>
  <xsl:param name="editUrl"/>

  <xsl:template match="/">
    <xsl:variable name="doc"
      select="/p:publisherResponse/p:document/p:preparedDocuments/p:preparedDocument[1]/p:publisherResponse/d:document"/>
    <document>
      <properties>
        <title>
          <xsl:value-of select="$doc/@name"/>
        </title>
        <author>
          <xsl:value-of select="$author"/>
        </author>
      </properties>
      <body>
        <xsl:apply-templates select="$doc"/>
      </body>
    </document>
  </xsl:template>

  <!--+
      | CocoonDocument (typeId=5)
      | SimpleDocument (typeId=2)
      | NewsItem       (typeId=13)
      | - copy the content of the SimpleDocumentContent part (typeId=2)
      +-->
  <xsl:template match="d:document[@typeId='5'] | d:document[@typeId='2'] | d:document[@typeId='13'] |
    d:document[@typeId='14']">
    <xsl:choose>
      <xsl:when test="@typeId = '14'">
        <h1>
          <xsl:value-of select="@name"/>
        </h1>
        <xsl:apply-templates select="d:parts/d:part[@typeId='2']/html/body/*"/>
      </xsl:when>
      <xsl:otherwise>
        <div id="contentBody">
          <div id="bodyText">
            <h1 class="docTitle">
              <xsl:value-of select="@name"/>
            </h1>
            <xsl:apply-templates select="d:parts/d:part[@typeId='2']/html/body/*"/>
          </div>
          <xsl:call-template name="addEditUrl"/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Sitemap elements -->
  <xsl:template match="d:document[@typeId='10']">
    <div id="contentBody">
      <div id="bodyText">
        <h1 class="docTitle">
          <xsl:value-of select="@name"/>
        </h1>
        <xsl:apply-templates select="d:parts/d:part[@typeId='19']/html/body/*"/>
        <h1>Attributes</h1>
        <xsl:apply-templates select="d:parts/d:part[@typeId='16']/html/body/*"/>
        <h1>Occurence</h1>
        <h2>Children</h2>
        <p>This element can have the following children:</p>
        <ul>
          <xsl:for-each select="d:fields/d:field[@name='SitemapElementChildren']/d:link">
            <li><xsl:value-of select="@valueFormatted"/></li>
          </xsl:for-each>
        </ul>
      </div>
      <xsl:call-template name="addEditUrl"/>
    </div>
  </xsl:template>

  <!-- Sitemap components -->
  <xsl:template match="d:document[@typeId='12']">
    <div id="contentBody">
      <div id="bodyText">
        <h1 class="docTitle">
          <xsl:value-of select="@name"/>
        </h1>

        <h1>Summary</h1>
        <xsl:if test="not(d:parts/d:part[@typeId='17']/html/body/*)">
          <p>No summary available. The summary needs to be defined using the
            <tt>@cocoon.sitemap.component.documentation</tt> annotation in the Java source file for this component.</p>
        </xsl:if>
        <xsl:apply-templates select="d:parts/d:part[@typeId='17']/html/body/*"/>

        <h1>Basic information</h1>
        <table>
          <tr>
            <th>Component type</th>
            <td>
              <xsl:value-of select="d:fields/d:field[@name='CocoonComponentReference']/d:string/@valueFormatted"/>
            </td>
          </tr>
          <tr>
            <th>Cocoon block</th>
            <td>
              <xsl:value-of select="d:fields/d:field[@name='CocoonBlock']/d:string/@valueFormatted"/>
            </td>
          </tr>
          <tr>
            <th>Java class</th>
            <td>
              <xsl:value-of select="d:fields/d:field[@name='JavaClassName']/d:string/@valueFormatted"/>
            </td>
          </tr>
          <xsl:variable name="scm" select="d:fields/d:field[@name='SitemapComponentName']/d:string/@valueFormatted"></xsl:variable>
          <xsl:if test="not($scm = '')">
            <tr>
              <th>Name in Sitemap</th>
              <td>
                <xsl:value-of select="$scm"/>
              </td>
            </tr>
          </xsl:if>
          <tr>
            <th>Cacheable</th>
            <td>
              <xsl:value-of select="d:fields/d:field[@name='SitemapComponentCacheabilityInfo']/d:string/@valueFormatted"/>
            </td>
          </tr>
        </table>

        <h1>Documentation</h1>
        <xsl:if test="not(d:parts/d:part[@typeId='18']/html/body/*)">
          <p>No documentation available yet.</p>
        </xsl:if>
        <xsl:apply-templates select="d:parts/d:part[@typeId='18']/html/body/*"/>
      </div>
      <xsl:call-template name="addEditUrl"/>
    </div>
  </xsl:template>

  <!--+
      | WebpageWithSidebar (typeId=14)
      | - Sidebar (part Id=19)
      | - SimpleDocumentContent (part Id=2)
      |
      | The markup here is so specific that it only works for the home page, so
      | we need to make sure that it is only used for that page.
      |
      | It also depends on special settings in the Daisy page
      +-->
  <xsl:template match="d:document[@typeId='14']">
    <div id="intro">
      <div>
        <xsl:apply-templates select="d:parts/d:part[@typeId='19']/html/body/*"/>
      </div>
    </div>
    <div id="contentBody" class="withSidebar">
      <div id="bodyText">
        <div id="getting">
          <div id="gettingStarted">
            <div>
              <!-- this is required to be a div, a p is removed from the end result -->
              <img alt="Getting Started" src="images/getting-started.gif"/>
            </div>
            <h2>Getting Started</h2>
            <ul>
              <xsl:apply-templates
                select="d:parts/d:part[@typeId='2']/html/body//ul[contains(@id,'gettingStartedList')]/*"/>
            </ul>
          </div>
          <div id="gettingBetter">
            <div>
              <!-- this is required to be a div, a p is removed from the end result -->
              <img alt="Getting Better" src="images/getting-better.gif"/>
            </div>
            <h2>Getting Better</h2>
            <ul>
              <xsl:apply-templates
                select="d:parts/d:part[@typeId='2']/html/body//ul[contains(@id,'gettingBetterList')]/*"/>
            </ul>
          </div>
          <div id="gettingInvolved">
            <div>
              <!-- this is required to be a div, a p is removed from the end result -->
              <img alt="Getting Involved" src="images/getting-involved.gif"/>
            </div>
            <h2>Getting Involved</h2>
            <ul>
              <xsl:apply-templates
                select="d:parts/d:part[@typeId='2']/html/body//ul[contains(@id,'gettingInvolvedList')]/*"/>
            </ul>
          </div>
          <div id="gettingDownload">
            <div class="downloadVersion">
              <xsl:apply-templates select="d:parts/d:part[@typeId='2']/html/body//p[contains(@id,'gettingDownloadText1')]"/>
            </div>
            <div class="moreDownload">
              <xsl:apply-templates select="d:parts/d:part[@typeId='2']/html/body//p[contains(@id,'gettingDownloadText2')]"/>
            </div>
          </div>
        </div>
        <xsl:apply-templates select="d:parts/d:part[@typeId='2']/html/body/*[not(contains(@id, 'getting'))]"/>
        <xsl:call-template name="addEditUrl"/>
      </div>
    </div>
  </xsl:template>
  
  <!--+
    | MultiMediaObject (typeId=9) (supports flash only!)
    +-->
  <xsl:template match="d:document[@typeId='9']">
    <object>
      <xsl:apply-templates select="d:fields/d:field[(@name = 'MultiMediaObjectHeight') or (@name = 'MultiMediaObjectWidth')]" mode="object"/>
      <xsl:apply-templates select="d:parts/d:part[@name = 'MultiMediaData']" mode="object">
        <xsl:with-param name="filePath" select="concat('flash/', @id, '_', @branchId, '_', @languageId, '.swf')"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="d:fields/d:field[starts-with(@name, 'MultiMediaObject') and
        ((@name != 'MultiMediaObjectHeight') and (@name != 'MultiMediaObjectWidth')) ]" mode="object"/>
      <embed>
        <xsl:apply-templates select="d:parts/d:part[@name = 'MultiMediaData']" mode="embed">
          <xsl:with-param name="filePath" select="concat('flash/', @id, '_', @branchId, '_', @languageId, '.swf')"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="d:fields/d:field[starts-with(@name, 'MultiMediaObject')]" mode="embed"/>
      </embed>
    </object>
    <br/>
  </xsl:template>
  
  <!--+
    | MultiMediaData part (typeId=13)
    +-->
  <xsl:template match="d:part" mode="object">
    <xsl:param name="filePath"/>
    <xsl:attribute name="type">
      <xsl:value-of select="@mimeType" />
    </xsl:attribute>
    <xsl:choose>
      <xsl:when test="@mimeType = 'application/x-shockwave-flash'">
        <xsl:attribute name="classid">clsid:D27CDB6E-AE6D-11cf-96B8-444553540000</xsl:attribute>
        <xsl:attribute name="codebase">http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,19,0</xsl:attribute>
        <param name="movie" value="{$filePath}"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="d:part" mode="embed">
    <xsl:param name="filePath"/>
    <xsl:attribute name="src">
      <xsl:value-of select="$filePath"/>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="d:field" mode="object">
    <xsl:variable name="fieldName" select="substring-after(@name,'MultiMediaObject')"/>
    <xsl:choose>
      <xsl:when test="(($fieldName = 'Height') or ($fieldName = 'Width'))">
        <xsl:attribute name="{$fieldName}">
          <xsl:value-of select="@valueFormatted"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <param name="{$fieldName}" value="{@valueFormatted}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="d:field" mode="embed">
    <xsl:variable name="fieldName" select="substring-after(@name,'MultiMediaObject')"/>
    
    <xsl:attribute name="{$fieldName}">
      <xsl:value-of select="@valueFormatted"/>
    </xsl:attribute>
  </xsl:template>

  <!--+
      | Add link to Daisy page at the bottom of each page
      +-->
  <xsl:template name="addEditUrl">
    <div class="editUrl">
      <div>
        <!-- this is required to be a div, a p is removed from the end result -->
        <em>Errors and Improvements?</em> If you see any errors or potential improvements in this document please help
        us: <a href="{$editUrl}">View, Edit or comment</a> on the latest development version (registration required).
      </div>
    </div>
  </xsl:template>

  <!--+
      | Work-around for tables as the Maven site plugin changes the attributes of
      | table, tr and td.
      +-->
  <xsl:template match="table[@daisy-table-type]">
    <div id="table-{@daisy-table-type}">
      <table>
        <!-- if there's a thead available copy it -->
        <xsl:if test="thead">
          <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
          </xsl:copy>
        </xsl:if>
        <!-- if there's not a thead, but there are th tags, add the thead -->
        <xsl:if test="tr/th">
          <thead>
            <xsl:copy>
              <xsl:apply-templates select="tr[th]"/>
            </xsl:copy>
          </thead>
        </xsl:if>
        <tbody>
          <xsl:copy>
            <xsl:apply-templates select="@*"/>
          </xsl:copy>
          <xsl:for-each select="tr">
            <tr>
              <xsl:copy>
                <xsl:apply-templates select="@*"/>
              </xsl:copy>
              <xsl:if test="position() mod 2 = 0">
                <xsl:attribute name="class">alt <xsl:value-of select="@class"/></xsl:attribute>
              </xsl:if>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
    </div>
  </xsl:template>

  <!--+
      | Includes
      |
      | Mark them with an enclosing div so we can restyle the main headings such as h1.docTitle
      +-->
  <xsl:template match="p:daisyPreparedInclude">
    <xsl:variable name="id" select="@id"/>
    <div class="includedDoc">
      <xsl:apply-templates select="/p:publisherResponse/p:document/p:preparedDocuments/p:preparedDocument[@id =
        $id]/p:publisherResponse/d:document"/>
    </div>
  </xsl:template>

  <!-- images -->
  <xsl:template match="img">
    <img src="{@src}" width="{@p:imageWidth}" height="{@p:imageHeight}" name="{p:linkInfo/@documentName}"
      alt="{p:linkInfo/@documentName}"/>
  </xsl:template>

  <!-- links: remove p:linkInfo -->
  <xsl:template match="a/p:linkInfo"/>
  <xsl:template match="pre">
    <!-- only to remove namespaces -->
    <pre>
        <xsl:apply-templates/>
      </pre>
  </xsl:template>

  <!-- surround all p elements that have an attribute to some div equivalent -->
  <xsl:template match="p[@class='warning']">
    <div class="warning">
      <div>
        <strong>Warning: </strong>
        <xsl:apply-templates/>
      </div>
    </div>
  </xsl:template>
  <xsl:template match="p[@class='note']">
    <div class="note">
      <div>
        <strong>Note: </strong>
        <xsl:apply-templates/>
      </div>
    </div>
  </xsl:template>
  <xsl:template match="p[@class='fixme']">
    <div class="fixme">
      <div>
        <strong>Fixme: </strong>
        <xsl:apply-templates/>
      </div>
    </div>
  </xsl:template>

  <!-- format search results -->
  <xsl:template match="d:searchResult[@styleHint='news']">
    <div class="news">
      <xsl:for-each select="d:rows/d:row">
        <dl class="news">
          <dt>
            <xsl:value-of select="d:value[1]"/>
          </dt>
          <dd>
            <xsl:value-of select="d:value[2]"/> [<a href="daisy:{@documentId}@{@branchId}:{@languageId}">more</a>] </dd>
          <dd class="newsMeta">submitted by <xsl:value-of select="d:value[4]"/>, <xsl:value-of select="d:value[3]"
          /></dd>
        </dl>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template match="d:searchResult[@styleHint='sitemap-components']">
    <xsl:call-template name="createComponentTable">
      <xsl:with-param name="type" select="'Generator'"/>
    </xsl:call-template>
    <xsl:call-template name="createComponentTable">
      <xsl:with-param name="type" select="'Transformer'"/>
    </xsl:call-template>
    <xsl:call-template name="createComponentTable">
      <xsl:with-param name="type" select="'Serializer'"/>
    </xsl:call-template>
    <xsl:call-template name="createComponentTable">
      <xsl:with-param name="type" select="'Matcher'"/>
    </xsl:call-template>
    <xsl:call-template name="createComponentTable">
      <xsl:with-param name="type" select="'Selector'"/>
    </xsl:call-template>
    <xsl:call-template name="createComponentTable">
      <xsl:with-param name="type" select="'Action'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="createComponentTable">
    <xsl:param name="type"/>
    <h1><xsl:value-of select="$type"/>s</h1>
    <table>
      <tr>
        <th>Component Name</th>
        <th>Name in Sitemap</th>
        <th>Block</th>
      </tr>
      <xsl:for-each select="d:rows/d:row[d:value[1]=$type]">
        <tr>
          <td>
            <a href="daisy:{@documentId}"><xsl:value-of select="./d:value[3]"/></a>
          </td>
          <td>
            <xsl:value-of select="./d:value[4]"/>
          </td>
          <td>
            <xsl:value-of select="./d:value[2]"/>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="d:searchResult">
    <div class="warning">
      <strong style="color:red;font-weight:bold">Warning: There is no styling for this query available.</strong>
    </div>
  </xsl:template>
  
  <!-- Suppress any p:* elements from the output but still process child elements -->
  <xsl:template match="p:*">
    <xsl:apply-templates/>
  </xsl:template>

  <!--+
       | default templates
       +-->
  <xsl:template match="*|@*|node()" priority="-2">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="text()" priority="-1">
    <xsl:value-of select="."/>
  </xsl:template>

</xsl:stylesheet>
