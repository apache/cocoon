<?xml version="1.0" encoding="UTF-8"?>
<!--
	Cocoon Feedback Wizard XMLForm processing and displaying stylesheet.	
  
  This stylesheet merges an XMLForm document into 
  a final document. It includes other presentational
  parts of a page orthogonal to the xmlform.

  author: Ivelin Ivanov, ivelin@apache.org, May 2002
  author: Konstantin Piroumian <kpiroumian@protek.com>, September 2002
  author: Simon Price <price@bristol.ac.uk>, September 2002

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xf="http://xml.apache.org/cocoon/xmlform/2002"
	exclude-result-prefixes="xalan" >
	<xsl:template match="document">
		<html>
			<head>
				<title>XMLForm - Cocoon Feedback Wizard</title>
				<style type="text/css"> <![CDATA[
              H1{font-family : sans-serif,Arial,Tahoma;color : white;background-color : #0086b2;} 
              BODY{font-family : sans-serif,Arial,Tahoma;color : black;background-color : white;} 
              B{color : white;background-color : blue;} 
              HR{color : #0086b2;}
              input { background-color: #FFFFFF; color: #000099; border: 1px solid #0000FF; }		
              table { background-color: #EEEEEE; color: #000099; font-size: x-small; border: 2px solid brown;}
              select { background-color: #FFFFFF; color: #000099 }
             .caption { line-height: 195% }
             .error { color: #FF0000; }	      
             .help { color: #0000FF; font-style: italic; }
             .invalid { color: #FF0000; border: 2px solid #FF0000; }
             .info { color: #0000FF; border: 1px solid #0000FF; }
             .repeat { border: 0px inset #999999;border: 1px inset #999999; width: 100%; }
             .group { border: 0px inset #999999;border: 0px inset #999999;  width: 100%; }
             .sub-table { border: none; }
             .button { background-color: #FFFFFF; color: #000099; border: 1px solid #666666; width: 70px; }
             .plaintable { border: 0px inset black;border: 0px inset black; width: 100%; }
              ]]> </style>
			</head>
			<body>
				<xsl:apply-templates />
			</body>
		</html>
	</xsl:template>
	<xsl:template match="xf:form">
		<xf:form method="post">
			<xsl:copy-of select="@*" />
			<br/>
			<br/>
			<br/>
			<br/>
			<table align="center" border="0">
				<tr>
					<td align="center" colspan="3">
						<h1>
							<xsl:value-of select="xf:caption"/>
							<hr/>
						</h1>
					</td>
				</tr>
				<xsl:if test="count(error/xf:violation) > 0">
					<tr>
						<td align="left" colspan="3"
							class="{error/xf:violation[1]/@class}">
							<p>* There are [<b><xsl:value-of
								select="count(error/xf:violation)"/></b>] 
								errors. Please fix these errors and submit the
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
						<xsl:when test="name() = 'xf:caption'"/>
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
	<xsl:template match="xf:repeat">
		<tr width="100%">
			<td colspan="3" width="100%">
				<table class="repeat">
					<xsl:apply-templates select="*"/>
				</table>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="xf:group">
		<tr width="100%">
			<td width="100%" colspan="2">
				<table class="group" border="0">
					<tr>
						<td align="left">
							<xsl:value-of select="xf:caption" />
						</td>
					</tr>
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
				<code> <xsl:value-of select="xf:caption" /> : <xsl:copy-of
					select="." /> </code>
			</font>
			<br/>
		</div>
	</xsl:template>
	<xsl:template match="xf:caption"/>
	<xsl:template match="xf:*">
		<tr>
			<td align="left" valign="top">
				<p class="caption">
					<xsl:value-of select="xf:caption" />
				</p>
			</td>
			<td align="left">
				<table class="plaintable">
					<tr>
						<td align="left">
							<xsl:copy-of select="." />
						</td>
						<xsl:if test="xf:violation">
							<td align="left" class="{xf:violation[1]/@class}"
								width="100%">
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
	<xsl:template match="*">
		<xsl:copy-of select="." />
	</xsl:template>
</xsl:stylesheet>
