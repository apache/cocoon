<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:cookie="http://apache.org/DTD/XSP/cookie"
>
  <!-- *** Cookie Templates *** -->

  <!-- Import Global XSP Templates -->
  <!-- <xsl:import href="base-library.xsl"/> -->
  <!-- Default copy-over's -->
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

  <!-- *** Utility Templates *** -->
  <!-- Retrieve "name" parameter as either attribute or element -->
  <xsl:template name="value-for-name">
    <xsl:choose>
      <!-- As attribute (String constant) -->
      <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
      <!-- As nested (presumably dynamic) element -->
      <xsl:when test="name">
        <!-- Recursively evaluate nested expression -->
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="name"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Return nested element content as expression or constant -->
  <xsl:template name="get-nested-content">
    <xsl:choose>
      <!-- Nested element -->
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <!-- Plain Text -->
      <xsl:otherwise>"<xsl:value-of select="normalize($content)"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Ensure attribute "as" has a value -->
  <xsl:template name="value-for-as">
    <xsl:choose>
      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- cookie.clone -->
  <xsl:template match="cookie:clone">
    <xsp:expr>
      cookie.clone()
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getComment -->
  <xsl:template match="cookie:get-comment">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getComment(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getComment()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getDomain -->
  <xsl:template match="cookie:get-domain">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getDomain(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getDomain()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getMaxAge -->
  <xsl:template match="cookie:get-max-age">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getMaxAge(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(cookie.getMaxAge())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          cookie.getMaxAge()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getName -->
  <xsl:template match="cookie:get-name">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getName(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getName()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getPath -->
  <xsl:template match="cookie:get-path">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getPath(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getPath()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getSecure -->
  <xsl:template match="cookie:get-secure">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getSecure(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(cookie.getSecure())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          cookie.getSecure()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getValue -->
  <xsl:template match="cookie:get-value">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getValue(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getValue()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.getVersion -->
  <xsl:template match="cookie:get-version">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getVersion(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(cookie.getVersion())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          cookie.getVersion()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- cookie.setComment -->
  <xsl:template match="cookie:set-comment">
    <!-- Get "purpose" parameter as either attribute or nested element -->
    <xsl:variable name="purpose">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@purpose">"<xsl:value-of select="@purpose"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="purpose">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="purpose"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setComment(
        String.valueOf(
          <xsl:copy-of select="$purpose"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <!-- cookie.setDomain -->
  <xsl:template match="cookie:set-domain">
    <!-- Get "pattern" parameter as either attribute or nested element -->
    <xsl:variable name="pattern">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@pattern">"<xsl:value-of select="@pattern"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="pattern">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="pattern"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setDomain(
        String.valueOf(
          <xsl:copy-of select="$pattern"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <!-- cookie.setMaxAge -->
  <xsl:template match="cookie:set-max-age">
    <!-- Get "expiry" parameter as either attribute or nested element -->
    <xsl:variable name="expiry">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@expiry">"<xsl:value-of select="@expiry"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="expiry">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="expiry"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setMaxAge(
        Integer.ParseInt(
          String.valueOf(
            <xsl:copy-of select="$expiry"/>
          )
        )
      );
    </xsp:logic>
  </xsl:template>

  <!-- cookie.setPath -->
  <xsl:template match="cookie:set-path">
    <!-- Get "path" parameter as either attribute or nested element -->
    <xsl:variable name="path">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@path">"<xsl:value-of select="@path"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="path">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="path"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setPath(
        String.valueOf(
          <xsl:copy-of select="$path"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <!-- cookie.setSecure -->
  <xsl:template match="cookie:set-secure">
    <!-- Get "flag" parameter as either attribute or nested element -->
    <xsl:variable name="flag">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@flag">"<xsl:value-of select="@flag"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="flag">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="flag"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setSecure(
        new Boolean(
          String.valueOf(
            <xsl:copy-of select="$flag"/>
          )
        ).booleanValue()
      );
    </xsp:logic>
  </xsl:template>

  <!-- cookie.setValue -->
  <xsl:template match="cookie:set-value">
    <!-- Get "new-value" parameter as either attribute or nested element -->
    <xsl:variable name="new-value">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@new-value">"<xsl:value-of select="@new-value"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="new-value">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="new-value"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setValue(
        String.valueOf(
          <xsl:copy-of select="$new-value"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <!-- cookie.setVersion -->
  <xsl:template match="cookie:set-version">
    <!-- Get "value" parameter as either attribute or nested element -->
    <xsl:variable name="value">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="value">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="value"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setVersion(
        Integer.ParseInt(
          String.valueOf(
            <xsl:copy-of select="$value"/>
          )
        )
      );
    </xsp:logic>
  </xsl:template>
</xsl:stylesheet>
