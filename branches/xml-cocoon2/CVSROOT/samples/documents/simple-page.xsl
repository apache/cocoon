<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp-request="http://xml.apache.org/xsp/request"
  xmlns:xsp-response="http://xml.apache.org/xsp/response"
>
  <xsl:template match="page">
    <html>
      <head>
        <title><xsl:value-of select="title"/></title>
      </head>
      <body bgcolor="white" alink="red" link="blue" vlink="blue">
        <h3 style="color: navy; text-align: center">
         <xsl:copy-of select="title/*|title/text()"/>
       </h3>

       <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="xsp-request:uri">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>

  <xsl:template match="xsp-request:parameter">
    <i>
      <xsl:value-of select="@name"/>
    </i>:

    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>

  <xsl:template match="xsp-request:parameter-values">
    <p>
      Parameter Values for "<xsl:value-of select="@name"/>":
    </p>

    <ul>
      <xsl:for-each select="xsp-request:value">
        <li>
	  <xsl:value-of select="."/>
	</li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="xsp-request:parameter-names">
    <p>
      All Parameter Names:
    </p>

    <ul>
      <xsl:for-each select="xsp-request:name">
        <li>
	  <xsl:value-of select="."/>
	</li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="xsp-request:headers">
    <p>Headers:</p>
    <ul>
      <xsl:for-each select="xsp-request:header">
	<li>
          <i><xsl:value-of select="@name"/></i>:
          <b><xsl:value-of select="."/></b>
	</li>
      </xsl:for-each>
    </ul>
    <br/>
  </xsl:template>

  <xsl:template match="xsp-request:header">
    <i>
      <xsl:value-of select="@name"/>
    </i>:

    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>


  <xsl:template match="xsp-request:header-names">
    <p>
      All Header names:
    </p>

    <ul>
      <xsl:for-each select="xsp-request:name">
        <li>
	  <xsl:value-of select="."/>
	</li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="title"/>

  <xsl:template match="@*|*|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
