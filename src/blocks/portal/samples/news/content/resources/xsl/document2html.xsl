<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="isfaq"/>
  <xsl:template match="document">
  	<!-- Allready done by portal!
    <td colspan="2" valign="top" class="content"-->
      <xsl:if test="normalize-space(header/title)!=''">
        <h1>
          <xsl:value-of select="header/title"/>
        </h1>
      </xsl:if>
      <xsl:if test="normalize-space(header/subtitle)!=''">
        <h3>
          <xsl:value-of select="header/subtitle"/>
        </h3>
      </xsl:if>
      <xsl:if test="header/authors">
        <p>
          <font size="-2">
            <xsl:for-each select="header/authors/person">
              <xsl:choose>
                <xsl:when test="position()=1">by&#160;</xsl:when>
                <xsl:otherwise>,&#160;</xsl:otherwise>
              </xsl:choose>
              <xsl:value-of select="@name"/>
            </xsl:for-each>
          </font>
        </p>
      </xsl:if>
      <xsl:apply-templates select="body"/>
    <!--/td  see above!-->
  </xsl:template>
  <xsl:template match="body">
    <xsl:if test="section and not($isfaq='true')">
      <ul class="minitoc">
        <xsl:for-each select="section">
          <li>
            <a href="#{generate-id()}">
              <xsl:value-of select="title"/>
            </a>
            <xsl:if test="section">
              <ul class="minitoc">
                <xsl:for-each select="section">
                  <li>
                    <a href="#{generate-id()}">
                      <xsl:value-of select="title"/>
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
            </xsl:if>
          </li>
        </xsl:for-each>
      </ul>
    </xsl:if>
    <xsl:apply-templates/>
  </xsl:template>
<!--  section handling
  - <a name/> anchors are added if the id attribute is specified
  - generated anchors are still included for TOC - what should we do about this?
  - FIXME: provide a generic facility to process section irrelevant to their
    nesting depth
-->
  <xsl:template match="section">
    <a name="{generate-id()}"/>
    <xsl:if test="normalize-space(@id)!=''">
      <a name="{@id}"/>
    </xsl:if>
    <h3>
      <xsl:value-of select="title"/>
    </h3>
    <xsl:apply-templates select="*[not(self::title)]"/>
  </xsl:template>
  <xsl:template match="section/section">
    <a name="{generate-id()}"/>
    <xsl:if test="normalize-space(@id)!=''">
      <a name="{@id}"/>
    </xsl:if>
    <h4>
      <xsl:value-of select="title"/>
    </h4>
    <xsl:apply-templates select="*[not(self::title)]"/>
  </xsl:template>
  <xsl:template match="note | warning | fixme">
    <div class="frame {local-name()}">
      <div class="label">
        <xsl:choose>
          <xsl:when test="local-name() = 'note'">Note</xsl:when>
          <xsl:when test="local-name() = 'warning'">Warning</xsl:when>
          <xsl:otherwise>Fixme (
               <xsl:value-of select="@author"/>

               )</xsl:otherwise>
        </xsl:choose>
      </div>
      <div class="content">
        <xsl:apply-templates/>
      </div>
    </div>
  </xsl:template>
  <xsl:template match="link">
    <a href="{@href}">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  <xsl:template match="jump">
    <a href="{@href}" target="_top">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  <xsl:template match="fork">
    <a href="{@href}" target="_blank">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  <xsl:template match="source">
    <pre class="code">
      <xsl:apply-templates/>
    </pre>
  </xsl:template>
  <xsl:template match="anchor">
    <a name="{@id}"/>
  </xsl:template>
  <xsl:template match="icon">
    <img src="{@src}" alt="{@alt}">
      <xsl:if test="@height">
        <xsl:attribute name="height"><xsl:value-of select="@height"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="@width">
        <xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
      </xsl:if>
    </img>
  </xsl:template>
  <xsl:template match="figure">
    <div align="center">
      <img src="{@src}" alt="{@alt}" class="figure">
        <xsl:if test="@height">
          <xsl:attribute name="height"><xsl:value-of select="@height"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@width">
          <xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
        </xsl:if>
      </img>
    </div>
  </xsl:template>
  <xsl:template match="table">
    <table class="table" cellpadding="4" cellspacing="1">
      <xsl:apply-templates/>
    </table>
  </xsl:template>
  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
