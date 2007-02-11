<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="window">

<xsl:variable name="bgColor">
  <xsl:choose>
    <xsl:when test="@bgColor">
        <xsl:value-of select="@bgColor" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>#CCCCCC</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<table border="2" cellSpacing="0" cellpadding="0" width="100%">
	<tr vAlign="top">
		<td bgColor="{$bgColor}" valign="middle">
			<font>
				<xsl:attribute name="color">#ffffff</xsl:attribute>
				<xsl:attribute name="face">Arial</xsl:attribute>
				<xsl:attribute name="size">2</xsl:attribute>	
				<xsl:choose>
				    <xsl:when test="@title">
					    <b><xsl:value-of select="@title"/></b>
					</xsl:when>
				    <xsl:otherwise>
					    <b><xsl:value-of select="title"/></b>
					</xsl:otherwise>
				</xsl:choose>	
			</font>
	    </td>
		<td align="right" bgColor="{$bgColor}">
			<xsl:if test="fullscreen-uri and not(maximize-uri)">
				<a href="{fullscreen-uri}">
	  			    <img src="customize.gif" border="0" alt="Full Screen"/>
	  			</a>
			</xsl:if>
			<xsl:if test="maxpage-uri">
				<a href="{maxpage-uri}">
	  			    <img src="show.gif" border="0" alt="Max Page"/>
	  			</a>
			</xsl:if>
			<xsl:if test="maximize-uri">
				<a href="{maximize-uri}">
	  			    <img src="maximize.gif" border="0" alt="Maximize"/>
	  			</a>
			</xsl:if>
			<xsl:if test="minimize-uri">
				<a href="{minimize-uri}">
	  			    <img src="minimize.gif" border="0" alt="Minimize"/>
	  			</a>
			</xsl:if>
			<xsl:if test="remove-uri">
				<a href="{remove-uri}">
	  			    <img src="delete.gif" border="0" alt="Delete"/>
	  			</a>
			</xsl:if>
		</td>
	</tr>
	<tr>
		<td colSpan="2">
               <xsl:apply-templates select="content"/>
		</td>
	</tr>
</table>
</xsl:template>

<xsl:template match="content">
    <xsl:apply-templates/>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
