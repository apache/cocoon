<?xml version="1.0"?>

<!-- $Id: cookie.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
<!--

 ============================================================================
                   The Apache Software License, Version 1.2
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

<!--
 * Logicsheet is intended to be used for setting and extracting
 * cookies
 *
 * @author ?
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
-->

<!-- XSP Cookie logicsheet for the Java language -->
<xsl:stylesheet version="1.0"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xsp-cookie="http://apache.org/xsp/cookie/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

		<xsl:template match="xsp:page">
			<xsp:page>
			  <xsl:apply-templates select="@*"/>
	
			  <xsp:structure>
				<xsp:include>org.apache.cocoon.components.language.markup.xsp.XSPCookieHelper</xsp:include>
			  </xsp:structure>
	
			  <xsl:apply-templates/>
			</xsp:page>
	  </xsl:template>
	  
<!-- 
		Cookie Setting, individual cookies can be set from within <xsp-cookie:create-cookies>
		or each of the individiual cookies can be set by using <xsp-cookie:cookie>
		
		All the values that are to be set for the cookie(s) have to specified with the 
		<xsp-cookie:cookie> tag		
-->
	<xsl:template match="xsp-cookie:create-cookies">		
		<xsl:apply-templates select="xsp-cookie:cookie"/>
	</xsl:template>
	
	<xsl:template match="xsp-cookie:cookie">
		<xsl:variable name="name">
			<xsl:choose>
				<xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>

				<xsl:when test="xsp-cookie:name">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:name"/>
					</xsl:call-template>							
				</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="value">
			<xsl:choose>
				<xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>

				<xsl:when test="xsp-cookie:value">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:value"/>
					</xsl:call-template>							
				</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="setComment">			
			<xsl:choose>
				<xsl:when test="xsp-cookie:setComment">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:setComment"/>
					</xsl:call-template>							
				</xsl:when>
				<xsl:otherwise>""</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="setDomain">
			<xsl:choose>
				<xsl:when test="xsp-cookie:setDomain">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:setDomain"/>
					</xsl:call-template>							
				</xsl:when>
				<xsl:otherwise>""</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="setMaxAge">
			<xsl:choose>
				<xsl:when test="xsp-cookie:setMaxAge">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:setMaxAge"/>
					</xsl:call-template>							
				</xsl:when>
				<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="setPath">
			<xsl:choose>
				<xsl:when test="xsp-cookie:setPath">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:setPath"/>
					</xsl:call-template>							
				</xsl:when>
				<xsl:otherwise>""</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="setSecure">			
			<xsl:choose>
				<xsl:when test="xsp-cookie:setSecure">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:setSecure"/>
					</xsl:call-template>							
				</xsl:when>
				<xsl:otherwise>""</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="setVersion">
			<xsl:choose>
				<xsl:when test="xsp-cookie:setVersion">							
					<xsl:call-template name="get-nested-content">
						<xsl:with-param name="content" select="xsp-cookie:setVersion"/>
					</xsl:call-template>							
				</xsl:when>
				<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsp:logic>
			XSPCookieHelper.addCookie(objectModel, <xsl:value-of select="$name"/>,<xsl:value-of select="$value"/>,<xsl:value-of select="$setComment"/>,<xsl:value-of select="$setDomain"/>, <xsl:value-of select="$setMaxAge"/>, <xsl:value-of select="$setPath"/>, <xsl:value-of select="$setSecure"/> , <xsl:value-of select="$setVersion"/>);
		</xsp:logic>
	</xsl:template>  

<!-- Retrieving the values of the cookie(s) -->

<!-- Extract set of cookies as a cookie object or as xml -->

	<xsl:template match="xsp-cookie:getCookies">
		<xsl:variable name="as">
		      <xsl:call-template name="value-for-as">
		        <xsl:with-param name="default" select="'cookie'"/>
		      </xsl:call-template>
    	</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$as='cookie'">
				<xsp:logic>
					XSPCookieHelper.getCookies(objectModel)
				</xsp:logic>
			</xsl:when>
			<xsl:when test="$as='xml'">
				<xsp:logic>
					XSPCookieHelper.getCookies(objectModel,this.contentHandler);
				</xsp:logic>
			</xsl:when>			
		</xsl:choose>
	</xsl:template>	

<!-- Extract individual cookies as a cookie object or as xml. 
	 cookie being extracted can be refered to by index or by name
-->
	<xsl:template match="xsp-cookie:getCookie">		
		<xsl:variable name="name">
		  <xsl:call-template name="value-for-name">
			<xsl:with-param name="default">null</xsl:with-param>
		  </xsl:call-template>
		</xsl:variable>
    	
    	<xsl:variable name="index">
			<xsl:call-template name="value-for-index">
				<xsl:with-param name="default">-1</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
    		
		<xsl:variable name="as">
			  <xsl:call-template name="value-for-as">
				<xsl:with-param name="default" select="'cookie'"/>
			  </xsl:call-template>
		</xsl:variable>

		<xsl:choose>
			<xsl:when test="$as='cookie'">
				<xsp:logic>
					XSPCookieHelper.getCookie(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
				</xsp:logic>
			</xsl:when>
			<xsl:when test="$as='xml'">
				<xsp:logic>	
					XSPCookieHelper.getCookie(objectModel,<xsl:value-of select="$name"/> , <xsl:value-of select="$index"/> , this.contentHandler);
				</xsp:logic>
			</xsl:when>
		</xsl:choose>
	</xsl:template>	

<!-- Extract name of the cookie -->
	<xsl:template match="xsp-cookie:getName">		
		<xsl:variable name="name">
		  <xsl:call-template name="value-for-name">
			<xsl:with-param name="default">null</xsl:with-param>
		  </xsl:call-template>
		</xsl:variable>

		<xsl:variable name="index">
			<xsl:call-template name="value-for-index">
				<xsl:with-param name="default">-1</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>

		<xsp:logic>
			XSPCookieHelper.getName(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
		</xsp:logic>			
	</xsl:template>	

<!-- Extract comment associated with the cookie -->
	<xsl:template match="xsp-cookie:getComment">		
			<xsl:variable name="name">
			  <xsl:call-template name="value-for-name">
				<xsl:with-param name="default">null</xsl:with-param>
			  </xsl:call-template>
			</xsl:variable>
	
			<xsl:variable name="index">
				<xsl:call-template name="value-for-index">
					<xsl:with-param name="default">-1</xsl:with-param>
				</xsl:call-template>
			</xsl:variable>
	
			<xsp:logic>
				XSPCookieHelper.getComment(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
			</xsp:logic>			
	</xsl:template>	

<!-- Extract domain of the cookie -->
	<xsl:template match="xsp-cookie:getDomain">		
			<xsl:variable name="name">
			  <xsl:call-template name="value-for-name">
				<xsl:with-param name="default">null</xsl:with-param>
			  </xsl:call-template>
			</xsl:variable>
	
			<xsl:variable name="index">
				<xsl:call-template name="value-for-index">
					<xsl:with-param name="default">-1</xsl:with-param>
				</xsl:call-template>
			</xsl:variable>
	
			<xsp:logic>
				XSPCookieHelper.getDomain(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
			</xsp:logic>			
	</xsl:template>	

<!-- Extract maxage of the cookie -->
	<xsl:template match="xsp-cookie:getMaxAge">		
			<xsl:variable name="name">
			  <xsl:call-template name="value-for-name">
				<xsl:with-param name="default">null</xsl:with-param>
			  </xsl:call-template>
			</xsl:variable>
	
			<xsl:variable name="index">
				<xsl:call-template name="value-for-index">
					<xsl:with-param name="default">-1</xsl:with-param>
				</xsl:call-template>
			</xsl:variable>
	
			<xsp:logic>
				XSPCookieHelper.getMaxAge(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
			</xsp:logic>			
	</xsl:template>

<!-- extract path of the cookie -->	
	<xsl:template match="xsp-cookie:getPath">		
			<xsl:variable name="name">
			  <xsl:call-template name="value-for-name">
				<xsl:with-param name="default">null</xsl:with-param>
			  </xsl:call-template>
			</xsl:variable>
	
			<xsl:variable name="index">
				<xsl:call-template name="value-for-index">
					<xsl:with-param name="default">-1</xsl:with-param>
				</xsl:call-template>
			</xsl:variable>
	
			<xsp:logic>
				XSPCookieHelper.getPath(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
			</xsp:logic>			
	</xsl:template>	

<!-- extract value of secure property of the cookie -->	
	<xsl:template match="xsp-cookie:getSecure">		
			<xsl:variable name="name">
			  <xsl:call-template name="value-for-name">
				<xsl:with-param name="default">null</xsl:with-param>
			  </xsl:call-template>
			</xsl:variable>
	
			<xsl:variable name="index">
				<xsl:call-template name="value-for-index">
					<xsl:with-param name="default">-1</xsl:with-param>
				</xsl:call-template>
			</xsl:variable>
	
			<xsp:logic>
				XSPCookieHelper.getSecure(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
			</xsp:logic>			
	</xsl:template>	

<!-- extract value of the cookie -->	
	<xsl:template match="xsp-cookie:getValue">		
		<xsl:variable name="name">
		  <xsl:call-template name="value-for-name">
			<xsl:with-param name="default">null</xsl:with-param>
		  </xsl:call-template>
		</xsl:variable>

		<xsl:variable name="index">
			<xsl:call-template name="value-for-index">
				<xsl:with-param name="default">-1</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>

		<xsp:logic>
			XSPCookieHelper.getValue(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
		</xsp:logic>			
	</xsl:template>	

<!--extract version of the cookie	-->
	<xsl:template match="xsp-cookie:getVersion">		
			<xsl:variable name="name">
			  <xsl:call-template name="value-for-name">
				<xsl:with-param name="default">null</xsl:with-param>
			  </xsl:call-template>
			</xsl:variable>
	
			<xsl:variable name="index">
				<xsl:call-template name="value-for-index">
					<xsl:with-param name="default">-1</xsl:with-param>
				</xsl:call-template>
			</xsl:variable>
	
			<xsp:logic>
				XSPCookieHelper.getVersion(objectModel, <xsl:value-of select="$name"/> , <xsl:value-of select="$index"/>)
			</xsp:logic>			
	</xsl:template>	

<!-- misseleneous templates -->

  	<xsl:template name="value-for-name">
		<xsl:param name="default"/>
		<xsl:choose>
		  <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
		  <xsl:when test="xsp-cookie:name">
			<xsl:call-template name="get-nested-content">
			  <xsl:with-param name="content" select="xsp-cookie:name"/>
			</xsl:call-template>
		  </xsl:when>
		  <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
		</xsl:choose>
  	</xsl:template>
  	
  	<xsl:template name="value-for-index">
		<xsl:param name="default"/>
		<xsl:choose>
		  <xsl:when test="@index"><xsl:value-of select="@index"/></xsl:when>
		  <xsl:when test="xsp-cookie:index">
			<xsl:call-template name="get-nested-content">
			  <xsl:with-param name="content" select="xsp-cookie:index"/>
			</xsl:call-template>
		  </xsl:when>
		  <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
		</xsl:choose>
  	</xsl:template>
  	
	<xsl:template name="value-for-as">
	    <xsl:param name="default"/>
	    <xsl:choose>
	      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
	      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
	    </xsl:choose>
   </xsl:template>
  
	<xsl:template name="get-nested-content">
		<xsl:param name="content"/>
		<xsl:choose>
          <xsl:when test="$content/xsp:text">"<xsl:value-of select="$content"/>"</xsl:when>
		  <xsl:when test="$content/*">
			<xsl:apply-templates select="$content/*"/>
		  </xsl:when>
		  <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	  <xsl:template match="@*|*|text()|processing-instruction()">
	    <xsl:copy>
	      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
	    </xsl:copy>
	  </xsl:template>

</xsl:stylesheet>
