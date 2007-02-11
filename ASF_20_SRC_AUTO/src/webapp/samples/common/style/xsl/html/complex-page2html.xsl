<?xml version="1.0"?>

<!--+
    | Covert samples file to the HTML page. Uses styles/main.css stylesheet.
    |
    | Author: Nicola Ken Barozzi "nicolaken@apache.org"
    | Author: Vadim Gritsenko "vgritsenko@apache.org"
    | Author: Christian Haul "haul@apache.org"
    | CVS $Id: complex-page2html.xsl,v 1.3 2003/12/09 21:07:03 vgritsenko Exp $
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:param name="contextPath" select="string('/cocoon')"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Apache Cocoon @version@</title>
        <link rel="SHORTCUT ICON" href="favicon.ico"/>
        <link href="{$contextPath}/styles/main.css" type="text/css" rel="stylesheet"/>
        <xsl:apply-templates select="document/header/style"/>
        <xsl:apply-templates select="document/header/script"/>
      </head>
      <body>
        <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
          <tr>
            <td width="*">The Apache Software Foundation is proud to present...</td>
            <td width="40%" align="center"><img border="0" src="{$contextPath}/images/cocoon.gif"/></td>
            <td width="30%" align="center">Version: <b>@version@</b></td>
          </tr>
        </table>

        <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
          <tr>
            <td width="50%">
              <h2><xsl:value-of select="document/header/title"/></h2>
            </td>
            <td width="25%">
              <xsl:apply-templates select="document/header/tab"/>
            </td>
            <td nowrap="nowrap" align="right">
              Orthogonal views:
              <a href="?cocoon-view=content">Content</a>
              &#160;
              <a href="?cocoon-view=pretty-content">Pretty content</a>
              &#160;
              <a href="?cocoon-view=links">Links</a>
            </td>
          </tr>
        </table>

        <p>
          <xsl:choose>
            <xsl:when test="document/body/row">
              <table width="100%">
                <xsl:apply-templates select="document/body/*"/>
              </table>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="document/body/*"/>
            </xsl:otherwise>
          </xsl:choose>
        </p>

        <p class="copyright">
          Copyright &#169; @year@ <a href="http://www.apache.org/">The Apache Software Foundation</a>.
          All rights reserved.
        </p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="style">
    <link type="text/css" rel="stylesheet" href="{@href}"/>
  </xsl:template>
 
  <xsl:template match="script">
    <script type="text/javascript" src="{@href}"/>
  </xsl:template>
 
  <xsl:template match="tab">
    <a href="{@href}"><i><xsl:value-of select="@title"/></i></a>&#160;
  </xsl:template>
 
  <xsl:template match="row">
    <tr>
      <xsl:apply-templates select="column"/>
    </tr>
  </xsl:template>
 
  <xsl:template match="column">
    <td valign="top">
      <h4 class="samplesGroup"><xsl:value-of select="@title"/></h4>
      <p class="samplesText"><xsl:apply-templates/></p>
    </td> 
  </xsl:template>

  <xsl:template match="section">
    <xsl:choose> <!-- stupid test for the hirachy deep -->
      <xsl:when test="../../../section">
        <h5><xsl:value-of select="title"/></h5>
      </xsl:when>
      <xsl:when test="../../section">
        <h4><xsl:value-of select="title"/></h4>
      </xsl:when>
      <xsl:when test="../section">
        <h4 class="samplesGroup"><xsl:value-of select="title"/></h4>
      </xsl:when>
    </xsl:choose>
    <p>
      <xsl:apply-templates select="*[name()!='title']"/>
    </p>
  </xsl:template>

  <xsl:template match="source">
    <div style="background: #b9d3ee; border: thin; border-color: black; border-style: solid; padding-left: 0.8em; 
                padding-right: 0.8em; padding-top: 0px; padding-bottom: 0px; margin: 0.5ex 0px; clear: both;">
      <pre>
        <xsl:value-of select="."/>
      </pre>
    </div>
  </xsl:template>
 
  <xsl:template match="link">
    <a href="{@href}">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
 
  <xsl:template match="strong">
    <b>
      <xsl:apply-templates/>
    </b>
  </xsl:template>
 
  <xsl:template match="anchor">
    <a name="{@name}">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
 
  <xsl:template match="para">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="*|@*|node()|text()" priority="-1">
    <xsl:copy><xsl:apply-templates select="*|@*|node()|text()"/></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
