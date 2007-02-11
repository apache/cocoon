<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="MTrk">
  	<xsl:copy>
  		<xsl:apply-templates select="@*"/>
  		<xsl:variable name="deltas" select="DELTA[descendant::NOTE_ON]"/>
		<xsl:variable name="deltaCount" select="count($deltas)"/>
		<xsl:comment>Count: <xsl:value-of select="$deltaCount"/></xsl:comment>
  		<xsl:for-each select="DELTA">
		    <xsl:choose>
		      <xsl:when test="descendant::NOTE_ON">
  		        <xsl:variable name="position" select="count(preceding-sibling::DELTA[descendant::NOTE_ON]) + 1"/>
		        <xsl:variable name="newPosition" select="($deltaCount - $position) + 1"/>
                <xsl:comment>Position: <xsl:value-of select="$position"/>, New: <xsl:value-of select="$newPosition"/></xsl:comment>
		        <xsl:copy>
  	              <xsl:variable name="followingDelta" select="following-sibling::DELTA[descendant::NOTE_ON]"/>
		          <xsl:attribute name="DTIME">
		            <xsl:choose>
		              <xsl:when test="$position = 1">00000000</xsl:when>
		              <xsl:when test="$followingDelta">
		                <xsl:value-of select="$followingDelta/@DTIME"/>
		              </xsl:when>
		              <xsl:otherwise>00000000</xsl:otherwise>
		            </xsl:choose>
		          </xsl:attribute>
	              <xsl:comment>The DTIME was <xsl:value-of select="@DTIME"/>, and is <xsl:value-of select="$followingDelta/@DTIME"/></xsl:comment>
		          <xsl:copy-of select="$deltas[$newPosition]/*"/>
		        </xsl:copy>
		      </xsl:when>
		      <xsl:otherwise>
		        <xsl:copy>
		          <xsl:apply-templates select="@*"/>
		          <xsl:apply-templates/>
		        </xsl:copy>
		      </xsl:otherwise>
		    </xsl:choose>
  		</xsl:for-each>
  	</xsl:copy>
  </xsl:template>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

