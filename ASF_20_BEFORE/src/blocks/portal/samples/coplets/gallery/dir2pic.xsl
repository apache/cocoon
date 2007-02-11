<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:dir="http://apache.org/cocoon/directory/2.0">

<xsl:param name="dirprefix"/>

<xsl:template match="/">
    <pictures>
        <xsl:for-each select="dir:directory/dir:file">
            <picture><xsl:value-of select="$dirprefix"/>/<xsl:value-of select="@name"/></picture>
        </xsl:for-each>
    </pictures>
</xsl:template>

</xsl:stylesheet>
