<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:include="http://apache.org/cocoon/include/1.0"
  xmlns:n="http://www.betaversion.org/linotype/news/1.0"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#default include n h"
>

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

        <div id="path"> 
	     <a href="../../../">Home</a> &#187; <a href="../../">Blog</a> &#187; <xsl:value-of select="n:title"/>
	    </div>
	 
        <div id="page">
          <div class="news">
            <h1><img src="{$home}/images/hand.jpg" alt=""/><xsl:value-of select="n:title"/></h1>
            <h2><xsl:value-of select="@creation-date"/> ~ <xsl:value-of select="@creation-time"/></h2>
            <xsl:apply-templates select="h:body"/>
          </div>
        </div>

        <div id="footer">
          <a href="http://creativecommons.org/licenses/by-nc-sa/1.0/" title="Creative Commons: some rights reserved"><img alt="Creative Commons License" src="{$home}/images/cc.gif"/></a>
        </div>

        <div id="sidebar">
          <a href="{$home}/private/"><img alt="Linotype" src="{$home}/images/linotype.jpg" width="156px" height="207px" /></a>
        </div>

        <div id="bottombar">
          <a href="http://cocoon.apache.org" title="Apache Cocoon"><img alt="Powered by Cocoon" src="{$home}/images/cocoon.jpg"/></a>
        </div>
        
      </body>
    </html>
  </xsl:template>

  <xsl:template match="h:body">
   <div class="body">
    <xsl:apply-templates/>
   </div>
  </xsl:template>
  
  <xsl:template match="h:p[1]">
   <p>
    <span class="firstletter"><xsl:value-of select="substring(text()[1],1,1)"/></span>
    <xsl:value-of select="substring(text()[1],2)"/>
    <xsl:apply-templates select="text()[position() &gt; 1]|@*|*"/>
   </p>
  </xsl:template>

  <xsl:template match="hr">
    <div class="separator"><img src="images/separator2.jpg"/></div>
  </xsl:template>

  <xsl:template match="@*">
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <xsl:template match="*">
   <xsl:element name="{name()}">
    <xsl:apply-templates select="@*|node()"/>
   </xsl:element>
  </xsl:template>

  <xsl:template match="text()">
   <xsl:copy/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
