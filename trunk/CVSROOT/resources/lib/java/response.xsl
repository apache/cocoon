<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:response="http://apache.org/DTD/XSP/response"
>
  <!-- *** ServletResponse Templates *** -->

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


  <!-- response.getCharacterEncoding -->
  <xsl:template match="response:get-character-encoding">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getCharacterEncoding(response, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.getCharacterEncoding()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>


  <!-- response.getLocale -->
  <xsl:template match="response:get-locale">
    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getLocale(response, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.getLocale(response).toString()
        </xsl:when>
        <xsl:when test="$as = 'object'">
          response.getLocale(response)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- response.setContentType -->
  <xsl:template match="response:set-content-type">
    <!-- Get "type" parameter as either attribute or nested element -->
    <xsl:variable name="type">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@type">"<xsl:value-of select="@type"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="type">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="type"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      response.setContentType(
        String.valueOf(
          <xsl:copy-of select="$type"/>
	)
      );
    </xsp:logic>
  </xsl:template>

  <!-- response.setLocale -->
  <xsl:template match="response:set-locale">
    <xsp:logic>
      response.setLocale(
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="locale"/>
        </xsl:call-template>
      );
    </xsp:logic>
  </xsl:template>


  <!-- *** HttpServletResponse *** -->

  <!-- response.addCookie -->
  <xsl:template match="response:add-cookie">
    <xsp:logic>
      response.addCookie(
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="*[1]"/>
        </xsl:call-template>
      );
    </xsp:logic>
  </xsl:template>

  <!-- response.addDateHeader -->
  <xsl:template match="response:add-date-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Get "date" parameter as either attribute or nested element -->
    <xsl:variable name="date">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@date">"<xsl:value-of select="@date"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="date">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="date"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <!-- Get "format" parameter as either attribute or nested element -->
    <!-- This optional parameter only applies for dates expressed as String -->
    <xsl:variable name="format">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="format">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="format"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$format">
          XSPResponseLibrary.addDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
	    String.valueOf(
              <xsl:call-template name="get-nested-content">
                <xsl:with-param name="content" select="$date"/>
              </xsl:call-template>
	    ),
	    String.valueOf(<xsl:copy-of select="$format"/>)
          );
        </xsl:when>
        <xsl:otherwise>
          XSPResponseLibrary.addDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:call-template name="get-nested-content">
              <xsl:with-param name="content" select="$date"/>
            </xsl:call-template>
          );
        </xsl:otherwise>
      </xsl:choose>
    </xsp:logic>
  </xsl:template>

  <!-- response.addHeader -->
  <xsl:template match="response:add-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

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
      response.addHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       String.valueOf(<xsl:copy-of select="$value"/>)
     );
    </xsp:logic>
  </xsl:template>

  <!-- response.addIntHeader -->
  <xsl:template match="response:add-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

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
      response.addIntHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       Integer.parseInt(
         String.valueOf(<xsl:copy-of select="$value"/>)
       )
     );
    </xsp:logic>
  </xsl:template>

  <!-- response.containsHeader -->
  <xsl:template match="response:contains-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPResponseLibrary.containsHeader(
            response,
            String.valueOf(<xsl:copy-of select="$value"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(
            response.containsHeader(
              String.valueOf(<xsl:copy-of select="$value"/>),
            )
          )
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          response.containsHeader(
            String.valueOf(<xsl:copy-of select="$value"/>),
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- response.encodeRedirectURL -->
  <xsl:template match="response:encode-redirect-url">
    <!-- Get "url" parameter as either attribute or nested element -->
    <xsl:variable name="url">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@url">"<xsl:value-of select="@url"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="url">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="url"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
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
          XSPResponseLibrary.encodeRedirectURL(
            response,
            String.valueOf(<xsl:copy-of select="$url"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.encodeRedirectURL(
            String.valueOf(<xsl:copy-of select="$url"/>),
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- response.encodeURL -->
  <xsl:template match="response:encode-url">
    <!-- Get "url" parameter as either attribute or nested element -->
    <xsl:variable name="url">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@url">"<xsl:value-of select="@url"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="url">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="url"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPResponseLibrary.encodeURL(
            response,
            String.valueOf(<xsl:copy-of select="$url"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.encodeURL(
            String.valueOf(<xsl:copy-of select="$url"/>),
          )
        </xsl:when>
      </xsl:choose>
    </xsp:logic>
  </xsl:template>

  <!-- response.sendRedirect -->
  <xsl:template match="response:send-redirect">
    <!-- Get "location" parameter as either attribute or nested element -->
    <xsl:variable name="location">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@location">"<xsl:value-of select="@location"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="location">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="location"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      response.sendRedirect(
       String.valueOf(<xsl:copy-of select="$location"/>)
     );
    </xsp:logic>
  </xsl:template>

  <!-- response.setDateHeader -->
  <xsl:template match="response:set-date-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Get "date" parameter as either attribute or nested element -->
    <xsl:variable name="date">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@date">"<xsl:value-of select="@date"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="date">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="date"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <!-- Get "format" parameter as either attribute or nested element -->
    <!-- This optional parameter only applies for dates expressed as String -->
    <xsl:variable name="format">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="format">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="format"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$format">
          XSPResponseLibrary.setDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
	    String.valueOf(
              <xsl:call-template name="get-nested-content">
                <xsl:with-param name="content" select="$date"/>
              </xsl:call-template>
	    ),
	    String.valueOf(<xsl:copy-of select="$format"/>)
          );
        </xsl:when>
        <xsl:otherwise>
          XSPResponseLibrary.setDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:call-template name="get-nested-content">
              <xsl:with-param name="content" select="$date"/>
            </xsl:call-template>
          );
        </xsl:otherwise>
      </xsl:choose>
    </xsp:logic>
  </xsl:template>

  <!-- response.setHeader -->
  <xsl:template match="response:set-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

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
      response.setHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       String.valueOf(<xsl:copy-of select="$value"/>)
     );
    </xsp:logic>
  </xsl:template>

  <!-- response.setIntHeader -->
  <xsl:template match="response:set-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

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
      response.setIntHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       Integer.parseInt(
         String.valueOf(<xsl:copy-of select="$value"/>)
       )
     );
    </xsp:logic>
  </xsl:template>

</xsl:stylesheet>
