<?xml version="1.0"?>
<!--

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

-->

<!-- Written by Jeremy Quinn "sharkbait@mac.com" -->

<xsl:stylesheet version="1.0"
	xmlns:fp="http://apache.org/cocoon/XSP/FP/1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsp="http://www.apache.org/1999/XSP/Core"
>

	
<xsl:template match="xsp:page">
	<xsp:page>
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
		</xsl:copy>
		<xsp:structure>
			<xsp:include>org.apache.cocoon.processor.xsp.library.fp.*</xsp:include>
		</xsp:structure>
		<xsl:apply-templates/>
	</xsp:page>
</xsl:template>
	
<xsl:template match="xsp:page/*[not(starts-with(name(.), 'xsp:'))]">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsp:logic>
			Hashtable fpResources = new Hashtable();
			Hashtable fpErrors = new Hashtable();
			String fpRedirect = new String();
			Document fpDocument = null;
		</xsp:logic>
		<xsl:apply-templates/>
		<xsp:attribute name="fp-errors">
			<xsp:expr>fpErrors.size()</xsp:expr>
		</xsp:attribute>
		<xsp:logic>
			/* if no errors and post, use redirect */
			if ( fpErrors.size() == 0 ) {
				/* serialise any changed resources */
				fpLibrary.saveResources(xspCurrentNode, fpResources, fpErrors);
				if ("POST".equalsIgnoreCase(request.getMethod())) {
					/* send redirect */
					if (!fpRedirect.equals("")) {
						response.sendRedirect(fpRedirect);
					}
				}
			} else {
				// output errors
				fpLibrary.getErrors(xspCurrentNode, fpErrors);
			}
		</xsp:logic>
	</xsl:copy>
</xsl:template>

<xsl:template match="fp:write">
	<xsl:variable name="select">
		<xsl:if test="select"><xsl:value-of select="normalize-space(select)"/></xsl:if>
		<xsl:if test="@select"><xsl:value-of select="@select"/></xsl:if>
	</xsl:variable>
	<xsl:variable name="to">
		<xsl:if test="to"><xsl:value-of select="normalize-space(to)"/></xsl:if>
		<xsl:if test="@to"><xsl:value-of select="@to"/></xsl:if>
	</xsl:variable>
	<xsl:variable name="mode">
		<xsl:if test="mode"><xsl:value-of select="normalize-space(mode)"/></xsl:if>
		<xsl:if test="@mode"><xsl:value-of select="@mode"/></xsl:if>
	</xsl:variable>
	<xsl:variable name="value">
		<xsl:call-template name="get-nested-content">
			<xsl:with-param name="content" select="."/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="as">
		<xsl:call-template name="value-for-as">
        	<xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
	<xsp:logic>
		fpLibrary.handleWrite(
			xspCurrentNode,
			fpResources, 
	    	fpErrors, 
	    	"<xsl:copy-of select="$select"/>", 
	    	"<xsl:copy-of select="$to"/>", 
	    	<xsl:copy-of select="$value"/>,
	    	"<xsl:value-of select="$as"/>", 
	    	"<xsl:copy-of select="$mode"/>"
	    );
	</xsp:logic>
</xsl:template>

<xsl:template match="fp:read">
	<xsl:variable name="select">
		<xsl:if test="select"><xsl:value-of select="normalize-space(select)"/></xsl:if>
		<xsl:if test="@select"><xsl:value-of select="@select"/></xsl:if>
	</xsl:variable>
	<xsl:variable name="from">
		<xsl:if test="from"><xsl:value-of select="normalize-space(from)"/></xsl:if>
		<xsl:if test="@from"><xsl:value-of select="@from"/></xsl:if>
	</xsl:variable>
	<xsl:variable name="as">
		<xsl:call-template name="value-for-as">
        	<xsl:with-param name="default" select="'string'"/>
    	</xsl:call-template>
    </xsl:variable>
    <xsl:choose>
    	<xsl:when test="name(..) = 'fp:write'">
			<xsp:logic>
				fpLibrary.handleRead(
					xspCurrentNode,
					fpResources, 
					fpErrors, 
					"<xsl:value-of select="$select"/>", 
					"<xsl:value-of select="$from"/>",
					"<xsl:value-of select="$as"/>")
			</xsp:logic>
    	</xsl:when>
    	<xsl:otherwise>
			<xsp:expr>
				fpLibrary.handleRead(
					xspCurrentNode,
					fpResources, 
					fpErrors, 
					"<xsl:value-of select="$select"/>", 
					"<xsl:value-of select="$from"/>",
					"<xsl:value-of select="$as"/>")
			</xsp:expr>
    	</xsl:otherwise>
    </xsl:choose>
</xsl:template>
	
<xsl:template match="fp:resource">
	<xsl:variable name="file">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="fp:resource-file"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="node">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="fp:resource-node"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="mode">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="fp:default-mode"/>
		</xsl:call-template>
	</xsl:variable>
	<xsp:logic>
		fpLibrary.handleFileResource(
			xspCurrentNode,
			request, 
			(javax.servlet.ServletContext)context, 
			fpResources, 
			fpErrors,
			String.valueOf("<xsl:value-of select="@id"/>"), 
			String.valueOf(<xsl:copy-of select="$file"/>), 
			String.valueOf(<xsl:copy-of select="$node"/>), 
			String.valueOf(<xsl:copy-of select="$mode"/>)
		);
	</xsp:logic>
</xsl:template>

<xsl:template match="fp:resource-file"></xsl:template>
<xsl:template match="fp:resource-node"></xsl:template>
<xsl:template match="fp:write-mode"></xsl:template>
<xsl:template match="select"></xsl:template>
<xsl:template match="from"></xsl:template>
<xsl:template match="to"></xsl:template>
<xsl:template match="mode"></xsl:template>

<xsl:template match="fp:redirect">
	<xsl:variable name="value">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="."/>
		</xsl:call-template>
	</xsl:variable>
	<xsp:logic>
		fpRedirect = <xsl:copy-of select="$value"/>;
	</xsp:logic>
</xsl:template>

<xsl:template match="fp:if-post">
	<xsp:logic>
		if ("POST".equalsIgnoreCase(request.getMethod())) {</xsp:logic>
			<xsl:apply-templates/>
		<xsp:logic>}; // end if-post
	</xsp:logic>
</xsl:template>

<xsl:template match="fp:if-get">
	<xsp:logic>
		if ("GET".equalsIgnoreCase(request.getMethod())) {</xsp:logic>
			<xsl:apply-templates/>
		<xsp:logic>}; // end if-get
	</xsp:logic>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
	<xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

<xsl:template name="value-for-as">
	<xsl:param name="default"/>
	<xsl:choose>
		<xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
	</xsl:choose>
</xsl:template>

 <xsl:template name="value-for-name">
	<xsl:choose>
		<xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
		<xsl:when test="name">
			<xsl:call-template name="get-nested-content">
				<xsl:with-param name="content" select="name"/>
			</xsl:call-template>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template name="get-nested-content">
	<xsl:param name="content"/>
	<xsl:choose>
		<xsl:when test="$content/*"><xsl:apply-templates select="$content/*"/></xsl:when>
		<xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="get-nested-string">
	<xsl:param name="content"/>
	<xsl:choose>
		<xsl:when test="$content/*">
			""
			<xsl:for-each select="$content/node()">
				<xsl:choose>
					<xsl:when test="name(.)">
						+ <xsl:apply-templates select="."/>
					</xsl:when>
					<xsl:otherwise>
						+ "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','		')"/>"
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:when>
		<xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
