<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="locale" />
<xsl:param name="page" />

<xsl:template match="book">
        <table class="menu">
            <tr>
                <td>
                        <ul>
                            <xsl:apply-templates select="menu"/>
                        </ul>
                </td>
            </tr>
        </table>
</xsl:template>

<!-- Process only current book -->
<xsl:template match="book[@current]">
    <ul>
        <xsl:apply-templates />
    </ul>
</xsl:template>

<!-- Current (open) menu -->
<xsl:template match="menu">
    <li>
       <xsl:if test="@icon">
           <img src="{@icon}" align="middle"/><xsl:text> </xsl:text>
       </xsl:if>    
        <span class="chapter open"><xsl:value-of select="@label" /></span>
    </li>
    <ul>
        <xsl:apply-templates />
    </ul>
</xsl:template>

<!-- Display a link to a page -->

<xsl:template match="menu-item[substring-after(@href, 'locale=') = $locale or @href=$page or (@href='' and $locale='')]">
    <li class="current" title="{@href}">
       <xsl:if test="@icon">
           <img src="{@icon}" align="middle"/><xsl:text> </xsl:text>
       </xsl:if>        
        <xsl:value-of select="@label" />
    </li>
</xsl:template>

<xsl:template match="menu-item | external">
    <li class="page">
       <xsl:if test="@icon">
           <img src="{@icon}" align="middle"/><xsl:text> </xsl:text>
       </xsl:if>    
        <a href="{@href}" class="page"><xsl:value-of select="@label" /></a>
    </li>    
</xsl:template>

</xsl:stylesheet>
