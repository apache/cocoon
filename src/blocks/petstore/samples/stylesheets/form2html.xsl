<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xf="http://apache.org/cocoon/jxforms/1.0"
	        exclude-result-prefixes="xalan" >

        <xsl:template match="/">
           <xsl:apply-templates />
        </xsl:template>

	<xsl:template match="xf:form">
		<xf:form method="post">
			<xsl:copy-of select="@*" />
			<table cellpadding="10" cellspacing="0" border="1" align="center" bgcolor="#dddddd">
				<xsl:if test="count(error/xf:violation) > 0">
					<tr>
						<td align="left" colspan="3"
							class="{error/xf:violation[1]/@class}">
							<p>* [<b><xsl:value-of
								select="count(error/xf:violation)"/></b>] 
								error(s). Please fix these errors and submit the
								form again.</p>
							<p>
								<xsl:variable name="localViolations"
									select=".//xf:*[ child::xf:violation ]"/>
								<xsl:for-each select="error/xf:violation">
									<xsl:variable name="eref" select="./@ref"/>
									<xsl:if
										test="count ($localViolations[ @ref=$eref ]) = 0"
										>* <xsl:value-of select="." /> <br/> </xsl:if>
								</xsl:for-each>
							</p>
							<p/>
						</td>
					</tr>
				</xsl:if>
				<xsl:for-each select="*[name() != 'xf:submit']">
					<xsl:choose>
						<xsl:when test="name() = 'error'"/>
						<xsl:when test="name() = 'xf:label'"/>
						<xsl:when test="xf:*">
							<xsl:apply-templates select="."/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of select="."/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
				<tr>
					<td align="center" colspan="3">
						<xsl:for-each select="*[name() = 'xf:submit']">
							<xsl:copy-of select="." />
							<xsl:text>
							</xsl:text>
						</xsl:for-each>
					</td>
				</tr>
			</table>
		</xf:form>
	</xsl:template>
	<xsl:template match="xf:group">
		<tr>
                  <td>
                      <font color="darkgreen"><h3><xsl:value-of select="xf:label" /></h3></font>
				<table cellspacing="1" cellpadding="3" border="0" bgcolor="#008800">
					<xsl:apply-templates select="*"/>
				</table>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="xf:output[@form]">
		<div align="center">
			<hr width="30%"/>
			<br/>
			<font size="-1">
				<code> <xsl:value-of select="xf:label" /> : <xsl:copy-of
					select="." /> </code>
			</font>
			<br/>
		</div>
	</xsl:template>
	<xsl:template match="xf:label"/>
	<xsl:template match="xf:*">
		<tr bgcolor="#FFFF88">
			<td align="left" valign="top">
				<p class="label">
					<xsl:value-of select="xf:label" />
				</p>
			</td>
			<td align="left">
				<table class="plaintable">
					<tr bgcolor="#FFFF88">
						<td align="left">
							<xsl:copy-of select="." />
						</td>
						<xsl:if test="xf:violation">
							<td align="left" class="{xf:violation[1]/@class}" width="100%">
								<xsl:for-each select="xf:violation">* 
									<xsl:value-of select="." /> <br/> </xsl:for-each>
							</td>
						</xsl:if>
					</tr>
				</table>
				<xsl:if test="xf:help">
					<div class="help">
						<xsl:value-of select="xf:help" />
					</div>
					<br />
				</xsl:if>
			</td>
		</tr>
	</xsl:template>
   <!-- copy all the rest of the markup which is not recognized above -->
   <xsl:template match="*">
      <xsl:copy><xsl:copy-of select="@*" /><xsl:apply-templates /></xsl:copy>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:value-of select="." />
   </xsl:template>

</xsl:stylesheet>
