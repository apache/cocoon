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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:p="http://outerx.org/daisy/1.0#publisher"
  xmlns:d="http://outerx.org/daisy/1.0"
  exclude-result-prefixes="p d">
  
  <xsl:output method="xml"/>
  <xsl:output omit-xml-declaration="no"/>
  <xsl:preserve-space elements="pre"/>
  
  <xsl:param name="author"/>
  <xsl:param name="documentName"/>  
  <xsl:param name="editUrl"/>  
  
  <xsl:template match="/">
    <document>
      <properties>
        <title><xsl:value-of select="@name"/></title>
        <author><xsl:value-of select="$author"/></author>
      </properties>
      <body>
        <xsl:variable name="doc" select="/p:publisherResponse/p:document/p:preparedDocuments/p:preparedDocument[1]/p:publisherResponse/d:document"/>
        <xsl:apply-templates select="$doc"/>
        <div class="editUrl">
          <p>
            <i>Errors and Improvements?</i> If you see any errors or potential improvements in this document please help us:  
            <a href="{$editUrl}">View, Edit or comment</a> on  the latest development version (registration required).
          </p>
        </div>
      </body>
    </document>        
    
  </xsl:template>
  
  <!--+
      | CocoonDocument (typeId=5)
      | - copy the content of the SimpleDocumentContent part (typeId=2)
      +-->
  <xsl:template match="d:document[@typeId='5'] | d:document[@typeId='2'] | d:document[@typeId='13'] | d:document[@typeId='14']">
    <xsl:if test="not(@typeId = '14')">
      <h1><xsl:value-of select="@name"/></h1>      
    </xsl:if>
    <xsl:apply-templates select="d:parts/d:part[@typeId='2']/html/body/*"/>
  </xsl:template>
  
  
  <!--+
      | WebpageWithSidebar (typeId=14)
      +-->
  <xsl:template match="d:document[@typeId='14']">
    <div id="downloadbox">
      <xsl:apply-templates select="d:parts/d:part[@typeId='19']/html/body/*"/>
    </div>   
    <div id="c"> 
      <xsl:apply-templates select="d:parts/d:part[@typeId='2']/html/body/*"/>
    </div>
  </xsl:template>  
  
  <!--+
      | Work-around for tables as the Maven site plugin changes the attributes of 
      | table, tr and td.
      +-->
  <xsl:template match="table[@daisy-table-type]">
    <div id="table-{@daisy-table-type}">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </div>
  </xsl:template>
      
  
  <!--+
      | Includes
      +-->
  <xsl:template match="p:daisyPreparedInclude">
    <xsl:variable name="id" select="@id"/>
    <xsl:apply-templates select="/p:publisherResponse/p:document/p:preparedDocuments/p:preparedDocument[@id = $id]/p:publisherResponse/d:document"/>
  </xsl:template>
  
  <!-- images -->
  <xsl:template match="img">
    <img src="{@src}" width="{@p:imageWidth}" height="{@p:imageHeight}" name="{p:linkInfo/@documentName}" alt="{p:linkInfo/@documentName}"/>
  </xsl:template>
  
  <!-- links: remove p:linkInfo -->
  <xsl:template match="a/p:linkInfo"/>
  
  <!-- increase the heading level by one -->
  <xsl:template match="h1">
    <h2>
      <xsl:apply-templates/>
    </h2>
  </xsl:template>
  <xsl:template match="h2">
    <h3>
      <xsl:apply-templates/>
    </h3>
  </xsl:template>  
  <xsl:template match="h4">
    <h5>
      <xsl:apply-templates/>
    </h5>
  </xsl:template> 
  
  <!-- surround all p elements that have an attribute to some div equivalent -->
  <xsl:template match="p[@class='warning']">
    <div class="warning">
      <strong style="color:red;font-weight:bold">Warning: </strong>      
      <xsl:apply-templates/>
    </div>
  </xsl:template>   
  <xsl:template match="p[@class='note']">
    <div class="note">
      <strong>Note: </strong>
      <xsl:apply-templates/>
    </div>
  </xsl:template>     
  <xsl:template match="p[@class='fixme']">
    <strong>Fixme: </strong>    
    <div class="fixme" style="color:blue">
      <xsl:apply-templates/>
    </div>
  </xsl:template>   
  
  <!-- format search results -->
  <xsl:template match="d:searchResult[@styleHint='news']">
    <div class="news">
      <ul>
        <xsl:for-each select="d:rows/d:row">
          <li>
            <strong><xsl:value-of select="d:value[1]"/></strong>
            <br/>
            <xsl:value-of select="d:value[2]"/> [<a href="daisy:{@documentId}@{@branchId}:{@languageId}">more</a>]
            <br/>
            <div class="news-footer">submitted by <xsl:value-of select="d:value[4]"/>, <xsl:value-of select="d:value[3]"/></div>
          </li>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:template>
  
  <xsl:template match="d:searchResult">
    <div class="warning">
      <strong style="color:red;font-weight:bold">Warning: There is no styling for this query available.</strong>      
    </div>
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
