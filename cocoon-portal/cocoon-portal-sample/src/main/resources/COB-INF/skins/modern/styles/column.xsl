<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- SVN $Id$ -->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Process a Column  -->
  <xsl:template match="column-layout">
    <div class="cocoon-portal-columns">
      <xsl:variable name="itemCount"><xsl:value-of select="count(item)"/></xsl:variable>
      <xsl:variable name="maxWidth"><xsl:value-of select="99 - number($itemCount)"/></xsl:variable>
	  <xsl:variable name="defaultWidth"><xsl:value-of select="number($maxWidth) div number($itemCount)"/></xsl:variable>
      <xsl:for-each select="item">
	    <div style="width:{$defaultWidth}%" class="cocoon-portal-column">
		  <!-- xsl:choose>
		    <xsl:when test="@width">
			  <xsl:attribute name="style">width:<xsl:value-of select="@width"/>;</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
			  <xsl:attribute name="style">width:<xsl:value-of select="$defaultWidth"/>;</xsl:attribute>
			</xsl:otherwise>
          </xsl:choose -->
          <xsl:copy-of select="*"/>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
