<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/page">
  <html>
    <body>
      <a href="./">Back to directory listing</a>
      <br/>
      <form method="get">
        <xsl:attribute name="action">${continuation.id}.kont</xsl:attribute>
        <p>Filename:<br />
        <input name="filename" type="text" size="30" maxlength="30" value="" />
        </p>
        <p>Title:<br />
        <input name="title" type="text" size="30" maxlength="30" value="{page/title}" />
        </p>
        <xsl:apply-templates select="metapage"/>
        <xsl:apply-templates select="page/content/para"/>
        <input type="submit" value="Submit" />
        <input type="reset" value="Reset" />
      </form>
    </body>
  </html>
</xsl:template>

<xsl:template match="page/content/para">
        <p>para:<br />
        <textarea name="para" cols="50" rows="10">
        <!--xsl:value-of select="normalize-space(.)"/-->
        <xsl:copy-of select="node()"/>
        </textarea>
        </p>
</xsl:template>

<xsl:template match="metapage">
  <p>Author:<br />
    <input name="author" type="text" size="30" maxlength="30" value="{author}" />
  </p>
  <p>Category:<br />
    <input name="category" type="text" size="30" maxlength="30" value="{category}" />
  </p>
  <p>State:<br />
    <input name="state" type="text" size="30" maxlength="30" value="{state}" />
  </p>
</xsl:template>

</xsl:stylesheet>
