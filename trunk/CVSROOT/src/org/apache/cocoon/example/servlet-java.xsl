<?xml version="1.0"?>

<!-- $Id: servlet-java.xsl,v 1.1 2000-01-03 01:45:08 stefano Exp $

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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

<!-- written by Stefano Mazzocchi "stefano@apache.org" -->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:servlet="http://apache.org/tags/examples/servlet"
>

  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
      </xsl:copy>

      <xsp:structure>
        <xsp:include>javax.servlet.*</xsp:include>
        <xsp:include>javax.servlet.http.*</xsp:include>
      </xsp:structure>

      <xsp:logic>
        private String normalize(String string) {
          if (string == null || string.equals("")) return "N/A";
          else return string;
        }
      </xsp:logic>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="servlet:method">
    <xsp:expr>normalize(request.getMethod())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:requestURI">
    <xsp:expr>normalize(request.getRequestURI())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:protocol">
    <xsp:expr>normalize(request.getProtocol())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:servletPath">
    <xsp:expr>normalize(request.getServletPath())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:pathInfo">
    <xsp:expr>normalize(request.getPathInfo())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:pathTranslated">
    <xsp:expr>normalize(request.getPathTranslated())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:queryString">
    <xsp:expr>normalize(request.getQueryString())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:contentLength">
    <xsp:expr>request.getContentLength()</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:contentType">
    <xsp:expr>normalize(request.getContentType())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:serverName">
    <xsp:expr>normalize(request.getServerName())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:serverPort">
    <xsp:expr>request.getServerPort()</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:remoteUser">
    <xsp:expr>normalize(request.getRemoteUser())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:remoteAddr">
    <xsp:expr>normalize(request.getRemoteAddr())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:remoteHost">
    <xsp:expr>normalize(request.getRemoteHost())</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:authType">
    <xsp:expr>normalize(request.getAuthType())</xsp:expr>
  </xsl:template>
  
  <xsl:template match="servlet:headers">
    <xsp:logic><![CDATA[
      Enumeration enum1 = request.getHeaderNames(); 
      if ((enum1 != null) && (enum1.hasMoreElements())) { ]]>
        <xsl:apply-templates/>
      }
    </xsp:logic>
  </xsl:template>

  <xsl:template match="servlet:for-each-header">
   <xsp:logic><![CDATA[
    while (enum1.hasMoreElements()) {  
     String headerName = (String) enum1.nextElement(); ]]>
      <xsl:apply-templates/>
    }
   </xsp:logic>
  </xsl:template>

  <xsl:template match="servlet:header-name">
    <xsp:expr>headerName</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:header-value">
    <xsp:expr>request.getHeader(headerName)</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:parameters">
    <xsp:logic><![CDATA[
      Enumeration enum2 = request.getParameterNames(); 
      if ((enum2 != null) && (enum2.hasMoreElements())) { ]]>
        <xsl:apply-templates/>
      }
    </xsp:logic>
  </xsl:template>

  <xsl:template match="servlet:for-each-parameter">
    <xsp:logic><![CDATA[
      while (enum2.hasMoreElements()) { 
        String parameterName = (String) enum2.nextElement();
        String val = request.getParameter(parameterName); 
        String vals[] = request.getParameterValues(parameterName); ]]>
         <xsl:apply-templates/>
      }
    </xsp:logic>
  </xsl:template>

  <xsl:template match="servlet:parameter-name">
    <xsp:expr>parameterName</xsp:expr>
  </xsl:template>

  <xsl:template match="servlet:parameter-value">
    <xsp:logic><![CDATA[
      for(int i = 0; i < vals.length; i++) { ]]>
       <item>
        <xsp:expr>vals[i]</xsp:expr>
       </item>
      }
    </xsp:logic>
  </xsl:template>
  
  <xsl:template match="@*|node()|*[not(starts-with(name(.),'servlet:'))]" priority="-1">
    <xsl:copy><xsl:apply-templates/></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
