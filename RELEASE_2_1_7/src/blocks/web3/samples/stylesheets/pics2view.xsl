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

<!-- 
     Author: Michael Gerzabek, michael.gerzabek@at.efp.cc, EFP Consulting �sterreich
     @version CVS $Id: pics2view.xsl,v 1.5 2004/04/05 12:25:32 antonio Exp $
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rfc="http://efp.cc/Web3-Rfc/1.0">
	<xsl:template match="@src">
		<xsl:attribute name="src">../../docs/<xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
