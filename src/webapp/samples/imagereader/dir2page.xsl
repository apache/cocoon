<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:dir="http://apache.org/cocoon/directory/2.0">
 
  <xsl:template match="/">
    <html><head><title>Image Directory Generator demonstration</title></head>
    <body>
      <h1>Image Directory Generator demonstration</h1>
      <xsl:apply-templates/>
      <p>
        See other <a href=".">image reader</a> samples.
      </p>
      <p>
        See documentation for
        <a href="../../docs/userdocs/generators/imagedirectory-generator.html">Image
        Directory Generator</a> and
        <a href="../../docs/userdocs/readers/image-reader.html">Image Reader</a>
      </p>
    </body></html>
  </xsl:template>

  <xsl:template match="dir:directory">
    <table border="1" cellpadding="3" cellspacing="3">
      <tr>
        <th>Image</th>
        <th>Name</th>
        <th>Width</th>
        <th>Height</th>
        <th colspan="2">Scale Size</th>
      </tr>
      <xsl:apply-templates select="dir:file|dir:directory"/>
    </table>
  </xsl:template>

  <xsl:template match="dir:file">
    <xsl:variable name="basename" select="substring-before(@name,'.jpg')"/>
    <tr>
      <td valign="top">
        <img src="full-{$basename}" alt="{@name} (full size)"
          title="{@name} (full size)"
          width="{@width}" height="{@height}"
        />
      </td>
      <td valign="top"><xsl:value-of select="$basename"/></td>
      <td valign="top"><xsl:value-of select="@width"/></td>
      <td valign="top"><xsl:value-of select="@height"/></td>
      <td valign="top">
        <a href="scale-{@width*2}-{@height*2}-{$basename}"
           title="{@name} (double size)">2x</a>
      </td>
      <td valign="top">
        <a href="scale-{@width*3}-{@height*3}-{$basename}"
           title="{@name} (triple size)">3x</a>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
