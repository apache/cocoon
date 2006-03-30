<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--+
    | Generate a samples page by combining the aggregated xsamples files
    | with the gump descriptor and a list of blocks to include in the page.
    |
    | CVS $Id: gump2samples.xsl 36239 2004-08-11 18:28:06Z vgritsenko $
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="currentPage"/>
  
  <xsl:variable name="page" select="root/pages/samples-pages/page[@name=$currentPage]"/>
  <xsl:variable name="xsamples" select="root/xsamples/xsamples"/>
  <xsl:variable name="gump" select="root/gump/module"/>

  <xsl:template match="/">
	<samples name="{normalize-space($page/title)}" add-view-links="false">
	  <xsl:copy-of select="$page/links"/>
	  <!-- select either all samples or those which are selected in the current page -->
	  <xsl:choose>
		  <xsl:when test="$page/@filter-blocks='true'">
		    <xsl:for-each select="$page/blocks/block">
		      <xsl:variable name="currentBlock" select="concat('cocoon-block-',@name)"/>
		      <xsl:apply-templates select="$xsamples/sample[@name=$currentBlock]"/>
		    </xsl:for-each>
		  </xsl:when>
		  
		  <xsl:otherwise>
		    <xsl:apply-templates select="$xsamples/sample">
		      <xsl:sort select="@name"/>
		    </xsl:apply-templates>
		    <xsl:call-template name="inactive-blocks"/>
		  </xsl:otherwise>
	  </xsl:choose>
	</samples>  
  </xsl:template>
  
  <xsl:template match="sample">
    <xsl:apply-templates select="xsamples/*" mode="copy"/>
  </xsl:template>
  
  <xsl:template match="*" mode="copy">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="copy"/>
      <xsl:apply-templates select="." mode="hook"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*" mode="hook"/>
  
  <!-- add block name and status to sample -->
  <xsl:template match="sample" mode="hook">
    <xsl:variable name="gumpBlockName" select="ancestor::sample[1]/@name"/>
    <xsl:variable name="gumpInfo" select="$gump/project[@name=$gumpBlockName]"/>
    <p class="samplesStatusNote">
    		block: <b><xsl:value-of select="ancestor::sample[1]/@block-name"/></b>
    		,
    		status: <b><xsl:value-of select="$gumpInfo/@status"/></b>
    </p>
  </xsl:template>
  
  <!-- if outputting all blocks, indicate which are not active -->
  <xsl:template name="inactive-blocks">
    <additional-info title="Blocks without samples">
    Here is the list of blocks which have not been included in the build or which do not
    have samples (i.e. do not have an .xsamples file):
    <br/>
    <em>
    <xsl:for-each select="$gump/project[starts-with(@name,'cocoon-block')]">
      <xsl:sort select="@name"/>
      <xsl:variable name="blockName" select="@name"/>
      <xsl:if test="not($xsamples/sample[@name=$blockName])">
        <xsl:value-of select="concat(substring-after(@name,'cocoon-block-'),' ')"/>
      </xsl:if>
    </xsl:for-each>
    </em>
    </additional-info>
  </xsl:template>
  
</xsl:stylesheet>
