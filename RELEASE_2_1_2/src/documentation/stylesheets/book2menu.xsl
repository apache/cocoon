<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:param name="resource"/>

  <xsl:template match="book">
    <menu>
      <xsl:apply-templates/>
    </menu>
  </xsl:template>

  <xsl:template match="project">
  </xsl:template>

  <xsl:template match="menu[position()=1]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="menu">
     <tr>
      <td valign="top" bgcolor="#959595" background="images/label-background_b.gif">
       <img src="images/separator.gif" height="6" width="120"/><br/>
       <span class="menutitle"><xsl:value-of select="@label"/></span></td>
     </tr>
     <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="menu-item">
    <xsl:if test="not(@type) or @type!='hidden'">
     <tr>
      <td bgcolor="#959595" valign="top">
       <xsl:attribute name="background">images/label-background_a.gif</xsl:attribute>
       <xsl:choose>
         <xsl:when test="starts-with(@href, $resource)">
           <span class="menuselected"><xsl:value-of select="@label"/></span>
         </xsl:when>
         <xsl:otherwise>
           <a href="{@href}" class="menu"><xsl:value-of select="@label"/></a>
         </xsl:otherwise>
       </xsl:choose>
      </td>
     </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="external">
    <xsl:if test="not(@type) or @type!='hidden'">
     <tr><td bgcolor="#959595" background="images/label-background_a.gif" valign="top">
        <a href="{@href}" target="new" class="menu"><xsl:value-of select="@label"/></a>
	</td>
     </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1"/>
</xsl:stylesheet>

