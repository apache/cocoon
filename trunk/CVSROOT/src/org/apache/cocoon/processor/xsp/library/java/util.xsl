<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:util="http://www.plenix.com/dtd/xsp/util"
>
  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
      </xsl:copy>

      <xsp:structure>
        <xsp:include>java.net.URL</xsp:include>
        <xsp:include>java.util.Date</xsp:include>
        <xsp:include>java.text.SimpleDateFormat</xsp:include>
      </xsp:structure>

      <xsp:logic>
        /* Util Class Level */

        private static int count = 0;
        private static synchronized int getCount() {
          return ++count;
        }
        private static synchronized int getSessionCount(HttpSession session) {
          Integer integer = (Integer) session.getValue("util.counter");
          if (integer == null) {
            integer = new Integer(0);
          }
          int cnt = integer.intValue() + 1;
          session.putValue("util.counter", new Integer(cnt));
          return cnt;
        }
        private static String formatDate(Date date, String pattern) {
          if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy/MM/dd hh:mm:ss aa";
          }
          return (new SimpleDateFormat(pattern)).format(date);
        }
      </xsp:logic>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="util:embed">
  <!-- Retrieve "uri" parameter as either attribute or element -->
    <xsl:variable name="uri">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@uri">"<xsl:value-of select="@uri"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="util:uri">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="util:uri"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic> {
      String embedURI = String.valueOf(<xsl:copy-of select="$uri"/>);

      try {
        URL url = new URL(embedURI);
        InputSource is = new InputSource(url.openStream());
        is.setSystemId(url.toExternalForm());

        xspCurrentNode.appendChild(
          XSPUtil.cloneNode(
            this.xspParser.parse(is).getDocumentElement(),
            document
          )
        );
      } catch (Exception e) {
        xspCurrentNode.appendChild(
	  document.createTextNode(
	    "{" +
	      "Unable to embed: " +
	      embedURI +
	    "}"
	  )
	);
      }
    } </xsp:logic>
  </xsl:template>

  <xsl:template match="util:counter">
    <xsl:choose>
      <xsl:when test="@scope = 'session'">
        <xsp:expr>getSessionCount(session)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>getCount()</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="util:time">
    <xsp:expr>
      formatDate(new Date(), "<xsl:value-of select="@format"/>")
    </xsp:expr>
  </xsl:template>

  <xsl:template match="util:include">
    <xsp:logic>
      xspCurrentNode.appendChild(
        XSPUtil.cloneNode(
          this.xspParser.parse(
            new InputSource(
              new FileReader(
                XSPUtil.relativeFilename(
                  "<xsl:value-of select="@file"/>",
                  request
                )
              )
            )
          ).getDocumentElement(),
          document
        )
      );
    </xsp:logic>
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

  <!-- Default copy-over -->
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
