<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:global="http://apache.org/DTD/XSP/global"
>
  <!-- *** XSPGlobal Templates *** -->

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


  <!-- global.getAttribute -->
  <xsl:template match="global:get-attribute">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPGlobalLibrary.getAttribute(
            global,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          XSPGlobalLibrary.getAttribute(
            global,
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- global.getAttributeNames -->
  <xsl:template match="global:get-attribute-names">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPGlobalLibrary.getAttributeNames(global, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPGlobalLibrary.getAttributeNames(global)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- global.removeAttribute -->
  <xsl:template match="global:remove-attribute">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsp:logic>
      global.removeAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>)
      );
    </xsp:logic>
  </xsl:template>

  <!-- global.setAttribute -->
  <xsl:template match="global:set-attribute">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Recursively evaluate nested attribute value -->
    <xsl:variable name="content">
      <xsl:call-template name="get-nested-content">
        <xsl:with-param name="content">
          <content><xsl:apply-templates/></content>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsp:logic>
      global.setAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>),
        <xsl:copy-of select="$content"/>
      );
    </xsp:logic>
  </xsl:template>

</xsl:stylesheet>
