<xsl:stylesheet version="1.0" exclude-result-prefixes="msxsl local xql" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="urn:schemas-microsoft-com:xslt" xmlns:local="#local-functions" xmlns:xql="#xql-functions"><!-- [XSL-XSLT] Updated namespace, added the required version attribute, and added namespaces necessary for script extensions. -->
	<!-- [XSL-XSLT] Explicitly apply the default (and only) indent-result behavior -->
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

	<!-- Match The Root Node -->
	<xsl:template match="/">
		<HTML>
		    <BODY>
                <table border="0" width="100%">
                    <tr>
                        <td colspan="3" STYLE="background-color : #B0E0E6; font : x-small Arial, Helvetica, sans-serif;">
                            <CENTER>
                            <b>Current News from iSyndicate.com</b>
                            </CENTER>
                        </td>
                    </tr>
    		        <xsl:apply-templates select="//newsfeed"/>
                </table>
		    </BODY>
		</HTML>
	</xsl:template>

	<xsl:template match="newsfeed">
		<xsl:for-each select="channel">
			<tr>
				<td>
					<table bgColor="#f0f8ff" border="0" cellPadding="2" WIDTH="100%" cellSpacing="0">
						<tr>
							<td colspan="2" bgcolor="midnightblue">
								<font color="white" face="Arial" size="2">
									<b>
										<xsl:value-of select="@title"></xsl:value-of>
									</b>
								</font>
							</td>
						</tr>
						<xsl:for-each select="headline">
							<tr>
								<td valign="top" halign="left">
									<font face="arial" size="2">
										<A>
											<xsl:attribute name="href">
												<xsl:value-of select="@href"></xsl:value-of>
											</xsl:attribute>
                        					<xsl:attribute name="TARGET">_blank</xsl:attribute>
                                            <xsl:value-of select="."></xsl:value-of>
										</A>
									</font>
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
