<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">
 
  <xsl:template match="page">
    <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
    <HTML>
    <HEAD>
      <TITLE><xsl:value-of select="title"/></TITLE>
    </HEAD>
    <BODY BGCOLOR="white">
      <H1 ALIGN="center"><xsl:value-of select="title"/></H1>
      <xsl:apply-templates/>
    </BODY>
    </HTML>
  </xsl:template>

  <xsl:template match="title">
   <!-- remove -->
  </xsl:template>

  <xsl:template match="p">
   <P>
    <xsl:apply-templates/>
   </P>
  </xsl:template>

  <xsl:template match="em">
    <STRONG><xsl:apply-templates/></STRONG>
  </xsl:template>

  <xsl:template match="parameters">
    <P>The following is the list of parameters for this request:</P>

    <TABLE BORDER="1">
      <CAPTION><I><B>Parameters</B></I></CAPTION>
      <TR>
        <TH>Name</TH>
	    <TH>Value(s)</TH>
      </TR>
      <xsl:apply-templates/>
    </TABLE>
  </xsl:template>

  <xsl:template match="parameter">
    <TR>
      <TD VALIGN="top"><xsl:value-of select="@name"/></TD>
      <TD VALIGN="top">
	    <TABLE>
          <xsl:apply-templates/>
        </TABLE>
      </TD>
    </TR>
  </xsl:template>
 
  <xsl:template match="parameter-value">
    <TR>
      <TD>
        <xsl:apply-templates/>
      </TD>
    </TR>
  </xsl:template>
</xsl:stylesheet>
