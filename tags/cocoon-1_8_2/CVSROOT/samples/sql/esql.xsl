<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0"
>

<xsl:template match="/page">
  <html>
    <head>
      <title>Apache Cocoon - esql sample page</title>
    </head>
    <body>
      <p>
        <xsl:value-of select="header"/>
      </p>
      <table border="1">
        <xsl:for-each select="department">
          <tr>
            <td>
              <xsl:value-of select="id"/>
            </td>
            <td>
              <xsl:value-of select="name"/>
            </td>
            <td>
              <table border="1">
                <xsl:for-each select="user">
                  <tr>
                    <td>
                      <xsl:value-of select="."/>
                    </td>
                  </tr>
                </xsl:for-each>
              </table>
            </td>
          </tr>
        </xsl:for-each>
      </table>
      <p>
        <xsl:value-of select="footer"/>
      </p>
    </body>
  </html>
</xsl:template>

</xsl:stylesheet>
