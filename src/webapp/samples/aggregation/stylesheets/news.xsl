<?xml version="1.0" encoding="ISO-8859-1"?>
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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Match The Root Node -->
	<xsl:template match="page">
		<html>
            <body>
                <xsl:apply-templates/>
            </body>
		</html>
	</xsl:template>

	<xsl:template match="slashdot | moreover | xmlhack">
	    <xsl:variable name="ABC" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
	    <xsl:variable name="abc" select="'abcdefghijklmnopqrstuvwxyz'"/>
        <xsl:apply-templates select="*[translate(local-name(), $ABC, $abc) = 'html']/
                                     *[translate(local-name(), $ABC, $abc) = 'body']/*"/>
    </xsl:template>

	<xsl:template match="@*|*|text()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
