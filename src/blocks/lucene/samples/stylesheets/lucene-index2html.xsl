<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:cinclude="http://apache.org/cocoon/include/1.0"
  xmlns:lucene="http://apache.org/cocoon/lucene/1.0" 
>
  <xsl:template match="lucene:index">
    <html>
      <head>
        <title>Lucene Index</title>
        <link title="Default Style" href="/styles/main.css" rel="stylesheet"/>
      </head>
      <body >
        <a href="http://jakarta.apache.org/lucene/">
          <img border="0" alt="Lucene Logo" src="images/lucene_green_300.gif"/>
        </a>
        <h1>Lucene-Index</h1>
        <p>
          <small>
            <a href="welcome">Welcome</a>
          </small>
        </p>
        <ul>
          <li>merge-factor - <xsl:value-of select="@merge-factor"/></li>
          <li>create - <xsl:value-of select="@create"/></li>
          <li>directory - <xsl:value-of select="@directory"/></li>
          <li>analyzer - <xsl:value-of select="@analyzer"/></li>
        </ul>

        <table>
          <tr><th>url</th><th>elapsed-time</th></tr>
          <xsl:apply-templates/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="lucene:document">
    <tr>
      <td><xsl:value-of select="@url"/></td>
      <td><xsl:value-of select="@elapsed-time"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1"></xsl:template>
  <xsl:template match="text()" priority="-1"></xsl:template>
</xsl:stylesheet> 