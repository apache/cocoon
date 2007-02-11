<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="servletPath" select="string('/samples')"/>
  <xsl:param name="sitemapURI"/>
  <xsl:param name="file"/><!-- relative path to file or file suffix -->
  <xsl:param name="remove"/><!-- path to remove from servletPath -->
  <xsl:param name="contextPath" select="string('/cocoon')"/>

  <xsl:variable name="realpath">
    <xsl:choose>
      <xsl:when test="$remove=''">
        <xsl:value-of select="$servletPath"/>        
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="substring-before($servletPath,$remove)"/>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="path" select="concat($contextPath,'/samples/view-source?filename=')"/>
  <xsl:variable name="view-source" select="concat($realpath,$file)"/>
  <xsl:variable name="directory" select="substring-before($servletPath,$sitemapURI)"/>
  <!-- assume that sitemapURIs don't occur in servletPath more than once -->
  <xsl:variable name="sitemap" select="concat($directory,'sitemap.xmap')"/>

  <xsl:template match="page">
   <html>
     <link rel="stylesheet" href="{$contextPath}/styles/main.css" title="Default Style"/>
    <head>
     <title>
      <xsl:value-of select="title"/>
     </title>
    </head>
    <body>
      <xsl:call-template name="resources"/>
      <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template name="resources">
    <div class="resources">
      <table width="100%">
        <tbody>
          <tr>
            <td>
              <a target="_blank" href="{concat($contextPath,$servletPath,'?cocoon-view=content')}">Content View</a>
            </td>
            <td>
              <a target="_blank" href="{concat($path,$view-source)}">Source</a>
            </td>
            <td>
              <a target="_blank" href="{concat($path,$sitemap)}">Sitemap</a>
            </td>
            <xsl:for-each select="resources/resource">
              <td class="{@type}">
                <xsl:choose>
                  <xsl:when test="@type='file'">
                    <a target="_blank" href="{concat($path,$directory,@href)}">
                      <xsl:apply-templates/>
                    </a>
                  </xsl:when>
                  <xsl:when test="@type='doc'">
                    <a target="_blank" href="{concat($contextPath,'/docs/',@href)}">
                      <xsl:apply-templates/>
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a target="_blank" href="{concat($contextPath,'/',@href)}">
                      <xsl:apply-templates/>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </xsl:for-each>
          </tr>
        </tbody>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="resources"/>

  <xsl:template match="title">
   <h2>
     <xsl:apply-templates/>
   </h2>
  </xsl:template>
  
  <xsl:template match="content">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="para">
   <p>
     <xsl:apply-templates/>
   </p>
  </xsl:template>

  <xsl:template match="link">
   <a href="{@href}">
     <xsl:apply-templates/>
   </a>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
