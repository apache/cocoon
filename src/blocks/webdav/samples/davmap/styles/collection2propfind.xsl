<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:collection="http://apache.org/cocoon/collection/1.0"
  xmlns:ci="http://apache.org/cocoon/include/1.0"
  xmlns:D="DAV:">
  
  <xsl:param name="requestURI"></xsl:param>
  <xsl:variable name="adjustedRequestURI">
    <xsl:choose>
      <xsl:when test="substring($requestURI, string-length($requestURI),1)='/'"><xsl:value-of select="$requestURI"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$requestURI"/>/</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:template match="/">
    <D:multistatus>
      <xsl:apply-templates />
    </D:multistatus>
  </xsl:template>
  
  <xsl:template match="/collection:collection">
    <xsl:call-template name="collection">
      <xsl:with-param name="href" select="$adjustedRequestURI" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="collection:collection">
    <xsl:param name="parent-href" />
    <xsl:call-template name="collection">
      <xsl:with-param name="href">
        <xsl:value-of select="$parent-href" /><xsl:value-of select="@name"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="/collection:resource">
    <xsl:call-template name="resource">
      <xsl:with-param name="href" select="$adjustedRequestURI" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="collection:resource">
    <xsl:param name="parent-href" />
    <xsl:call-template name="resource">
      <xsl:with-param name="href">
        <xsl:value-of select="$parent-href" /><xsl:value-of select="@name"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="collection">
    <xsl:param name="href" />
    <D:response>
      <D:href><xsl:value-of select="$href"/></D:href>
      <D:propstat>
        <D:prop>
          <D:displayname><xsl:value-of select="@name"/></D:displayname>
          <D:getlastmodified><xsl:value-of select="@date"/></D:getlastmodified>
          <D:resourcetype><D:collection /></D:resourcetype>
        </D:prop>
        <D:status>HTTP/1.1 200 OK</D:status>
      </D:propstat>
    </D:response>
    <xsl:apply-templates>
      <xsl:with-param name="parent-href" select="$href" />
    </xsl:apply-templates>
  </xsl:template>
    
  <xsl:template name="resource">
    <xsl:param name="href" />
    <D:response>
      <D:href><xsl:value-of select="$href"/></D:href>
      <D:propstat>
        <D:prop>
          <D:displayname><xsl:value-of select="@name"/></D:displayname>
          <D:getlastmodified><xsl:value-of select="@date"/></D:getlastmodified>
          <D:getcontenttype>text/xml</D:getcontenttype>
          <D:getcontentlength><xsl:value-of select="@size" /></D:getcontentlength>
          <D:resourcetype />
        </D:prop>
        <D:status>HTTP/1.1 200 OK</D:status>
      </D:propstat>
    </D:response>
  </xsl:template>

</xsl:stylesheet>
