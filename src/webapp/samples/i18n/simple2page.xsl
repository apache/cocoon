<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="locale"/>
  <xsl:param name="page"/>

  <xsl:template match="root">
    <document>
      <header>
        <title>Internationalization (i18n) and Localization (l10n)</title>
      </header>
      <body>
        <row>
          <column title="Menu">
            <xsl:apply-templates select="book"/>
          </column>
          <column title="{document/title}">
            <xsl:apply-templates select="document"/>
          </column>
        </row>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="document">
    <h2>
      <font color="navy">
        <xsl:value-of select="title"/>
      </font>
      <xsl:apply-templates select="form"/>
    </h2>
    <h5>
      <xsl:value-of select="sub-title"/>
    </h5>
    <hr align="left" noshade="noshade" size="1"/>
    <small>
      <font color="red">
        <i>
          <xsl:apply-templates select="annotation"/>
        </i>
      </font>
    </small>
    <xsl:apply-templates select="content"/>
    <hr align="left" noshade="noshade" size="1"/>
    <xsl:apply-templates select="bottom"/>
  </xsl:template>

  <xsl:template match="book">
    <xsl:apply-templates select="menu"/>
  </xsl:template>

  <xsl:template match="para">
    <p>
      <font color="navy">
        <b>
          <xsl:number format="0. "/>
          <xsl:value-of select="@name"/>
        </b>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@title"/>
      </font>
      <br/>
      <xsl:apply-templates select="text() | strong | i"/>
    </p>
  </xsl:template>

  <!-- Current (open) menu -->
  <xsl:template match="menu">
    <xsl:if test="@icon">
      <img src="{@icon}" align="middle"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <h3>
      <xsl:value-of select="@label"/>
    </h3>
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <!-- Display a link to a page -->
  <xsl:template match="menu-item[substring-after(@href, 'locale=') = $locale or @href=$page or (@href='' and $locale='')]">
    <li class="current" title="{@href}">
      <xsl:if test="@icon">
        <img src="{@icon}" align="middle"/>
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="@label"/>
    </li>
  </xsl:template>

  <xsl:template match="menu-item | external">
    <li class="page">
      <xsl:if test="@icon">
        <img src="{@icon}" align="middle"/>
        <xsl:text> </xsl:text>
      </xsl:if>
      <a href="{@href}" class="page">
        <xsl:value-of select="@label"/>
      </a>
    </li>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
