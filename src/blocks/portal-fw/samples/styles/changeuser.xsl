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

<!-- $Id: changeuser.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="password">
	<xsl:choose>
		<xsl:when test="normalize-space(new) = normalize-space(old)">
			<password><xsl:value-of select="normalize-space(old)"/></password>
		</xsl:when>
		<xsl:otherwise>
			<password><xsl:value-of select="normalize-space(hashed)"/></password>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>
