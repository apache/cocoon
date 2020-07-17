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
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- Process a tab  -->
<xsl:template match="tool-functions">
				<table style="height: 1.8em" border="0" cellpadding="0" cellspacing="0" width="100%" xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
					<tr>
						<td valign="middle" bgcolor="#B2C4E0" >
							<div class="tab">
								<xsl:for-each select="function">
								 | <a href="{@parameter}" style=" font-size : 85%; border: 0; color: #000066;">
									<xsl:value-of select="@name"/>
								</a>
								</xsl:for-each>
							</div>
						</td>
					</tr>
				</table>

</xsl:template>

<!-- Copy all and apply templates -->

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
