<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:collection="http://apache.org/cocoon/collection/1.0"
                xmlns:D="DAV:">

<xsl:param name="requestURI"></xsl:param>
<xsl:param name="directory"></xsl:param>
<xsl:param name="file"></xsl:param>

<xsl:template match="/collection:collection">
  <D:multistatus>
    <xsl:apply-templates select="collection:resource[@name=$file]"/>
  </D:multistatus>

</xsl:template>

<xsl:template match="collection:resource[@name=$file]">
  <D:response>
    <D:href><xsl:value-of select="$requestURI"/>/</D:href>
    <D:propstat>
      <D:prop>
        <D:current-user-privilege-set/>
        <D:displayname><xsl:value-of select="@name"/></D:displayname>
        <D:getlastmodified><xsl:value-of select="@lastModified"/></D:getlastmodified>
        <D:getcontentlength><xsl:value-of select="@size"/></D:getcontentlength>
        <D:getcontenttype>text/xml</D:getcontenttype>
        <D:resourcetype/>
      </D:prop>
      <D:status>HTTP/1.1 200 OK</D:status>
      <!--D:status>HTTP/1.1 404 Not Found</D:status-->
    </D:propstat>
  </D:response>
</xsl:template>

</xsl:stylesheet>
