<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:collection="http://apache.org/cocoon/collection/1.0">
  
  <xsl:param name="footer" />
  <xsl:param name="requestURI"></xsl:param>
  <xsl:variable name="adjustedRequestURI">
    <xsl:choose>
      <xsl:when test="substring($requestURI, string-length($requestURI),1)='/'"><xsl:value-of select="$requestURI"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$requestURI"/>/</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:template match="/collection:collection">
    <html>
      <head>
        <title>
          <xsl:value-of select="@name"/>
        </title>
      </head>
      <body bgcolor="#ffffff">
        <table width="90%" cellspacing="0" cellpadding="5" align="center">
          <tr>
            <td colspan="3">
              <font size="+2">
                <strong>
                  Directory Listing For <xsl:value-of select="@name"/>
                </strong>
              </font>
            </td>
          </tr>
          <tr>
            <td colspan="3">&#160;</td>
          </tr>
          <xsl:call-template name="collection-header" />
          <xsl:apply-templates>
            <xsl:with-param name="href" select="$adjustedRequestURI" />
          </xsl:apply-templates>
          <tr>
            <td colspan="3" bgcolor="#cccccc">
              <font size="-1"><xsl:value-of select="$footer" /></font>
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="collection-header">
    <tr bgcolor="#cccccc">
      <td align="left">
        <font size="+1">
          <strong>Filename</strong>
        </font>
      </td>
      <td align="right">
        <font size="+1">
          <strong>Size</strong>
        </font>
      </td>
      <td align="right">
        <font size="+1">
          <strong>Last Modified</strong>
        </font>
      </td>
    </tr>
  </xsl:template>
	
  <xsl:template match="collection:collection">
    <xsl:param name="href" />
    <tr>
      <xsl:if test="position() mod 2 = 0">
        <xsl:attribute name="bgcolor">#eeeeee</xsl:attribute>
      </xsl:if>
      <td align="left">&#160;&#160;<a href="{$href}{@name}">
        <tt><xsl:value-of select="@name"/></tt></a>
      </td>
      <td align="right">
        <tt>&#160;</tt>
      </td>
      <td align="right">
        <tt>
          <xsl:value-of select="@date"/>
        </tt>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:template match="collection:resource">
    <xsl:param name="href" />
    <tr>
      <xsl:if test="position() mod 2 = 0">
        <xsl:attribute name="bgcolor">#eeeeee</xsl:attribute>
      </xsl:if>
      <td align="left">&#160;&#160;<a href="{$href}{@name}">
        <tt><xsl:value-of select="@name"/></tt></a>
      </td>
      <td align="right">
        <tt><xsl:value-of select="@size"/></tt>
      </td>
      <td align="right">
        <tt><xsl:value-of select="@date"/></tt>
      </td>
    </tr>
  </xsl:template>
  
  <!-- ignore everything else -->
  <xsl:template match="node()|@*" />
  
</xsl:stylesheet>
