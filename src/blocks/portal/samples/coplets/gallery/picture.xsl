<?xml version="1.0"?>
<!-- $Id: picture.xsl,v 1.1 2003/12/11 16:05:01 cziegeler Exp $ 

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- The current picture (index) to display -->
<xsl:param name="pic"/>
<!-- Is this full screen? -->
<xsl:param name="fullscreen"/>
<!-- Should we print next/prev links? -->
<xsl:param name="navigation"/>

<xsl:template match="pictures" xmlns:cl="http://apache.org/cocoon/portal/coplet/1.0">
<xsl:variable name="maxp" select="count(picture)"/>

<xsl:choose>
<xsl:when test="$fullscreen='true'">
  <!-- This is the two column version: 
  <table>
    <xsl:for-each select="picture">
        <xsl:if test="position() mod 2 = 1">
            <tr>        
                <td><img src="{.}"/></td>
                <xsl:choose>
                    <xsl:when test="position() = last()">
                        <td>&#160;</td>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="p" select="position()+1"/>
                        <td><img src="{//picture[position()=$p]}"/></td>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>
        </xsl:if>
    </xsl:for-each>
  </table>
  -->
  <!-- And this is the simple version -->
      <table>
          <tr width="100%">
              <td>
                <xsl:for-each select="picture">
                   <img src="{.}"/><xsl:text> </xsl:text>
                </xsl:for-each>
              </td>
          </tr>
      </table>
</xsl:when>
<xsl:otherwise>
  <xsl:variable name="picn">
    <xsl:choose>
      <xsl:when test="$pic=$maxp">1</xsl:when>
      <xsl:when test="$pic=''">2</xsl:when>
      <xsl:otherwise><xsl:value-of select="$pic+1"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="picp">
    <xsl:choose>
      <xsl:when test="$pic=1 or $pic=''"><xsl:value-of select="$maxp"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$pic - 1"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="showpicindex">
    <xsl:choose>
      <xsl:when test="$pic=1 or $pic=''">1</xsl:when>
      <xsl:otherwise><xsl:value-of select="$pic"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:if test="$navigation!='false'">
  <p>Picture <xsl:value-of select="$showpicindex"/> of <xsl:value-of select="$maxp"/>
    <xsl:if test="$showpicindex &gt; 1">
      - <cl:link path="attributes/picture" value="{$picp}">&lt;Previous&gt;</cl:link>
    </xsl:if>
    <xsl:if test="$showpicindex &lt; $maxp">
      - <cl:link path="attributes/picture" value="{$picn}">&lt;Next&gt;</cl:link>
    </xsl:if>
    </p>
    <p><cl:link path="attributes/picture" value="{$showpicindex}" coplet="GalleryViewer-1">Push to Viewer</cl:link></p>
  </xsl:if>
    <img src="{picture[position()=$showpicindex]}"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>
