<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0">
    <xsl:output method="xml"/>
    
    <xsl:template match="/site">
    <!--
    <osm:page>  
        <xsl:apply-templates select="osm:page" />
    </osm:page>     
    -->
    <osm:site>  
        <xsl:apply-templates select="layout"/>
    </osm:site>

    </xsl:template>
    
    <xsl:template match="layout">   
            <xsl:apply-templates/>
    </xsl:template>     
    
    <xsl:template match="osm:page">
            <xsl:apply-templates/>
    </xsl:template> 
    
    
    
    <xsl:template match="osm:content-copy">
            <xsl:call-template name="getContent">
                <xsl:with-param name="select" select="@select"/>
            </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="osm:menu-copy">
                <xsl:call-template name="getMenu">
                <xsl:with-param name="select" select="@select"/>
            </xsl:call-template>
    </xsl:template> 
    
    <xsl:template match="osm:block-copy">
            <xsl:call-template name="getBlock">
                <xsl:with-param name="select" select="@select"/>
            </xsl:call-template>
    </xsl:template>
    <!--
    <xsl:apply-templates select="osm:dummy[@contentID='vMenu'] "/>  
    -->
    
    <!--
    <xsl:template name="getContent">
        <xsl:param name="select"/>
        <xsl:apply-templates select="//osm:container[@contentID=$select]"/>
    </xsl:template>
    -->

    <xsl:template name="getContent">
        <xsl:param name="select"/>
        <xsl:apply-templates select="//*[@contentID=$select]"/>
    </xsl:template>
    
    <xsl:template name="getMenu">
        <xsl:param name="select"/>
        <xsl:apply-templates select="//osm:menus/osm:menu[@menuID=$select]"/>
    </xsl:template> 
    
    <xsl:template name="getBlock">
        <xsl:param name="select"/>
        <xsl:apply-templates select="//osm:blocks/osm:block[@blockID=$select]"/>
    </xsl:template>     
    
    <xsl:template match="node()|@*" priority="-1">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
