<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:session="http://apache.org/DTD/XSP/session"
>
  <!-- *** ServletSession Templates *** -->

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


  <!-- session.getAttribute -->
  <xsl:template match="session:get-attribute">
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
          XSPSessionLibrary.getAttribute(
            session,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          XSPSessionLibrary.getAttribute(
            session,
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- session.getAttributeNames -->
  <xsl:template match="session:get-attribute-names">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getAttributeNames(session, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPSessionLibrary.getAttributeNames(session)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- session.getCreationTime -->
  <xsl:template match="session:get-creation-time">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getCreationTime(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(session.getCreationTime())
        </xsl:when>
        <xsl:when test="$as = 'long'">
          session.getCreationTime()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- session.getId -->
  <xsl:template match="session:get-id">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getId(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          session.getId()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- session.getLastAccessedTime -->
  <xsl:template match="session:get-last-accessed-time">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getLastAccessedTime(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(session.getLastAccessedTime())
        </xsl:when>
        <xsl:when test="$as = 'long'">
          session.getLastAccessedTime()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- session.getMaxInactiveInterval -->
  <xsl:template match="session:get-max-inactive-interval">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getMaxInactiveInterval(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(session.getMaxInactiveInterval())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          session.getMaxInactiveInterval()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- session.invalidate -->
  <xsl:template match="session:invalidate">
    <xsp:logic>
      session.invalidate()
    </xsp:logic>
  </xsl:template>

  <!-- session.isNew -->
  <xsl:template match="session:is-new">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.isNew(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(session.isNew())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          session.isNew()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- session.removeAttribute -->
  <xsl:template match="session:remove-attribute">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsp:logic>
      session.removeAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>)
      );
    </xsp:logic>
  </xsl:template>

  <!-- session.setAttribute -->
  <xsl:template match="session:set-attribute">
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
      session.setAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>),
        <xsl:copy-of select="$content"/>
      );
    </xsp:logic>
  </xsl:template>

  <!-- session.setInactiveInterval -->
  <xsl:template match="session:set-max-inactive-interval">
    <!-- Get "interval" parameter as either attribute or nested element -->
    <xsl:variable name="interval">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@interval">"<xsl:value-of select="@interval"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="interval">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="interval"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      session.setInactiveInterval(
        Integer.parseInt(
	  String.valueOf(
	    <xsl:copy-of select="$interval"/>
	  )
	)
      );
    </xsp:logic>
  </xsl:template>

</xsl:stylesheet>
