<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
<xsl:template match="body">
    <body>
        <xsl:apply-templates select="children::node()[1]">
            <xsl:with-param name="level" select="0"/>
        </xsl:apply-templates>
    </body>
</xsl:template>

<xsl:template match="h1|h2|h3">
    <xsl:param name="stop-id"/>
    <xsl:if test="generate-id() != $stop-id">
        <xsl:variable name="name" select="name(.)"/>
        <xsl:variable name="nextHeader" select="following-sibling::*[name() = $name]"/>
        
        <xsl:variable name="next-id" select="generate-id($nextHeader)"/>
        
        <xsl:element name="s{substring(name(.), 2)}">
            <xsl:attribute name="title"><xsl:value-of select="."/></xsl:attribute>
            <xsl:apply-templates select="following-sibling::node()[1]">
                <xsl:with-param name="stop-id" select="$next-id"/>
            </xsl:apply-templates>
        </xsl:element>
        
        <xsl:apply-templates select="$nextHeader"/>
    </xsl:if>
</xsl:template>

<xsl:template match="*">
    <xsl:param name="stop-id"/>
    <xsl:if test="generate-id() != $stop-id">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates>
                <xsl:with-param name="stop-id" select="$stop-id"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:if>
</xsl:template>

<!-- unknown elements: only copy the text -->
<xsl:template match="*">
    <xsl:value-of select="."/>
</xsl:template>


</xsl:stylesheet>
