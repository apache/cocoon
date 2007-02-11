<?xml version="1.0"?>
<!-- $Id: picture.xsl,v 1.2 2003/12/12 13:46:13 cziegeler Exp $ 

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- The current picture to display -->
<xsl:param name="pic"/>

<xsl:template match="pictures" xmlns:cl="http://apache.org/cocoon/portal/coplet/1.0">
    <xsl:choose>
        <xsl:when test="$pic=''">
            <p>Please choose a picture in the gallery.</p>
        </xsl:when>
        <xsl:otherwise>
            <img src="{$pic}"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
