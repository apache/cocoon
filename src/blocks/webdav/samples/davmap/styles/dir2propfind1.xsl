<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:collection="http://apache.org/cocoon/collection/1.0"
                xmlns:D="DAV:">

<xsl:param name="requestURI"></xsl:param>
<xsl:variable name="adjustedRequestURI">
  <xsl:choose>
    <xsl:when test="substring($requestURI, string-length($requestURI),1)='/'"><xsl:value-of select="$requestURI"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="$requestURI"/>/</xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<xsl:template match="/collection:collection">
  <D:multistatus>
    <xsl:apply-templates select="collection:resource"/>
    <xsl:apply-templates select="collection:collection"/>
  </D:multistatus>
</xsl:template>

<xsl:template match="collection:resource">
  <D:response>
    <D:href><xsl:value-of select="$adjustedRequestURI"/><xsl:value-of select="@name"/></D:href>
    <D:propstat>
      <D:prop>
        <D:displayname><xsl:value-of select="@name"/></D:displayname>
        <D:getlastmodified><xsl:value-of select="@lastModified"/></D:getlastmodified>
        <D:getcontentlength><xsl:value-of select="@size"/></D:getcontentlength>
        <D:resourcetype/>
      </D:prop>
      <D:status>HTTP/1.1 200 OK</D:status>
    </D:propstat>
  </D:response>
</xsl:template>

<xsl:template match="collection:collection">
  <D:response>
    <D:href><xsl:value-of select="$adjustedRequestURI"/><xsl:value-of select="@name"/>/</D:href>
    <D:propstat>
      <D:prop>
        <D:displayname><xsl:value-of select="@name"/></D:displayname>
        <D:getlastmodified><xsl:value-of select="@lastModified"/></D:getlastmodified>
        <D:getcontenttype>httpd/unix-directory</D:getcontenttype>
        <D:resourcetype><D:collection/></D:resourcetype>
      </D:prop>
      <D:status>HTTP/1.1 200 OK</D:status>
    </D:propstat>
  </D:response>
</xsl:template>

</xsl:stylesheet>
