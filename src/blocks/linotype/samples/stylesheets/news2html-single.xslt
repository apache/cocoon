<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                              xmlns:n="http://www.betaversion.org/linotype/news/1.0" 
                              xmlns:h="http://www.w3.org/1999/xhtml"
                              xmlns="http://www.w3.org/1999/xhtml">

  <xsl:param name="home"/>

  <xsl:template match="n:news">
    <html xml:lang="en" lang="en">
      <head>
        <title>Stefano's Linotype ~ <xsl:value-of select="n:title"/></title>
        <meta name="DC.title" content="{n:title}"/>
        <meta name="DC.author.personalName" content="Stefano Mazzocchi"/>
        <meta name="keywords" lang="en" content="{n:keywords}"/>
        <meta name="DC.date.created" content="{@creation-date}"/>
        <meta name="DC.date.lastModified" content="{@creation-date}"/>
        <meta name="DC.identifier" content="http://www.betaversion.org/~stefano/linotype/"/>

        <link rel="stylesheet" href="{$home}/styles/main.css" type="text/css"/>

        <rdf:RDF xmlns="http://web.resource.org/cc/"
                 xmlns:dc="http://purl.org/dc/elements/1.1/"
                 xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
         <Work rdf:about="">
          <license rdf:resource="http://creativecommons.org/licenses/by-nc-sa/1.0/" />
         </Work>
         <License rdf:about="http://creativecommons.org/licenses/by-nc-sa/1.0/">
          <requires rdf:resource="http://web.resource.org/cc/Attribution" />
          <requires rdf:resource="http://web.resource.org/cc/ShareAlike" />
          <permits rdf:resource="http://web.resource.org/cc/Reproduction" />
          <permits rdf:resource="http://web.resource.org/cc/Distribution" />
          <permits rdf:resource="http://web.resource.org/cc/DerivativeWorks" />
          <prohibits rdf:resource="http://web.resource.org/cc/CommercialUse" />
          <requires rdf:resource="http://web.resource.org/cc/Notice" />
         </License>
        </rdf:RDF>
      </head>
      <body>

        <div id="bottombar">
          <a href="http://cocoon.apache.org" title="Apache Cocoon"><img alt="Powered by Cocoon" src="{$home}/images/cocoon.jpg"/></a>
        </div>

        <div id="sidebar">
          <a href="{$home}/private/"><img alt="Linotype" src="{$home}/images/linotype.jpg" width="156px" height="207px" /></a>
        </div>

        <div id="page">
          <div class="news">
            <h1><img src="{$home}/images/hand.jpg" alt=""/><xsl:value-of select="n:title"/></h1>
            <h2><xsl:value-of select="@creation-date"/> ~ <xsl:value-of select="@creation-time"/></h2>
            <div class="body">
              <xsl:apply-templates select="h:body"/>
            </div>
          </div>
        </div>

        <div id="footer">
          <a href="http://creativecommons.org/licenses/by-nc-sa/1.0/" title="Creative Commons: some rights reserved"><img alt="Creative Commons License" src="{$home}/images/cc.gif"/></a>
        </div>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
