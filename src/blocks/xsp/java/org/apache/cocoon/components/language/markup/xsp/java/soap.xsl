<?xml version="1.0" encoding="utf-8"?>

<!-- $Id: soap.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
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
 * With ideas from an early prototype implemented by Pankaj Kumar
 * "pankaj_kumar@hp.com"
 *
 * Date: July 21, 2001
 *
 * @author <a href="mailto:ovidiu@cup.hp.com>Ovidiu Predescu</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xscript="http://apache.org/xsp/xscript/1.0"
                xmlns:soap="http://apache.org/xsp/soap/3.0">

  <xsl:include href="xscript-lib.xsl"/>


  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>
      <xsp:structure>
        <xsp:include>org.apache.cocoon.components.language.markup.xsp.SOAPHelper</xsp:include>
      </xsp:structure>
      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>


  <xsl:template match="soap:call">
    <xsl:variable name="url">
      <xsl:choose>
        <xsl:when test="soap:url"><xsl:value-of select="soap:url"/></xsl:when>
        <xsl:when test="@url">"<xsl:value-of select="@url"/>"</xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

   <xsl:variable name="authorization">
      <xsl:choose>
        <xsl:when test="soap:authorization"><xsl:value-of select="soap:authorization"/></xsl:when>
        <xsl:when test="@authorization">"<xsl:value-of select="@authorization"/>"</xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="method">
      <xsl:choose>
        <xsl:when test="soap:method"><xsl:value-of select="soap:method"/></xsl:when>
        <xsl:when test="@method">"<xsl:value-of select="@method"/>"</xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="scope">
      <xsl:call-template name="xscript-get-scope-for-creation">
        <xsl:with-param name="scope" select="'request'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="tempvar">
      <xsl:text>__soap_call_</xsl:text>
      <xsl:value-of select="generate-id(.)"/>
    </xsl:variable>

    <xscript:variable scope="request" name="{$tempvar}">
      <xsl:choose>
        <xsl:when test="soap:env">
          <xsl:apply-templates select="soap:env"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="soap-env"/>
        </xsl:otherwise>
      </xsl:choose>
    </xscript:variable>

    <xsp:logic>
      if (getLogger().isDebugEnabled()) {
          getLogger().debug("XScriptObject for soap:call is\n" +
              <xscript:get scope="request" name="{$tempvar}" as="object"/> +
              ", sending request to: " + <xsl:value-of select="$url"/>);
      }
      try {
          xscriptManager.put(pageScope, objectModel, "<xsl:value-of select="$tempvar"/>",
              new SOAPHelper(manager,
                             XSPRequestHelper.getRequestedURL(objectModel),
                             String.valueOf(<xsl:value-of select="$url"/>),
                             <xsl:value-of select="$method"/>,
                             <xsl:value-of select="$authorization"/>,
                             <xscript:get scope="request" name="{$tempvar}" as="object"/>).invoke(),
              <xsl:value-of select="$scope"/>);
          if (getLogger().isDebugEnabled()) {
              getLogger().debug("SOAP result is\n" +
                                <xscript:get scope="request" name="{$tempvar}" as="object"/>);
          }
          <xscript:get scope="request" name="{$tempvar}"/>
      } catch (Exception ex) {
          <soap-err:error xmlns:soap-err="http://apache.org/xsp/soap/3.0"><xsp:expr>ex</xsp:expr></soap-err:error>
          getLogger().error("SOAP call failed", ex);
      }
    </xsp:logic>
  </xsl:template>


  <xsl:template match="soap:env" name="soap-env">
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <xsl:if test="soap:header">
          <xsl:apply-templates select="soap:header"/>
      </xsl:if>
    <xsl:choose>
      <xsl:when test="soap:body">
          <xsl:apply-templates select="soap:body"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="soap-body"/>
      </xsl:otherwise>
    </xsl:choose>
    </SOAP-ENV:Envelope>
  </xsl:template>

  <xsl:template match="soap:header" name="soap-header">
      <SOAP-ENV:Header xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
        <xsl:apply-templates select="*[name() != 'soap:url'
                                       and name() != 'soap:method'
                                       and name() != 'soap:authorization'                                       
                                       and name() != 'soap:namespace']"/>
      </SOAP-ENV:Header>
  </xsl:template>
  
  <xsl:template match="soap:body" name="soap-body">
    <xsp:logic>
      <xsl:for-each select="soap:namespace">
        // Generate the namespace defined with soap:namespace
        this.contentHandler.startPrefixMapping(
          "<xsl:value-of select="@prefix"/>",
          "<xsl:value-of select="@uri"/>");
      </xsl:for-each>
    </xsp:logic>
    <SOAP-ENV:Body xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <xsl:apply-templates select="*[name() != 'soap:url'
                                     and name() != 'soap:method'
                                     and name() != 'soap:authorization'
                                     and name() != 'soap:namespace']"/>
    </SOAP-ENV:Body>
    <xsp:logic>
      <xsl:for-each select="soap:namespace">
        // End the namespace defined with soap:namespace
        this.contentHandler.endPrefixMapping(
          "<xsl:value-of select="@prefix"/>");
      </xsl:for-each>
    </xsp:logic>
  </xsl:template>
  
  <xsl:template match="soap:enc">
      <xsp:attribute name="SOAP-ENV:encodingStyle">http://schemas.xmlsoap.org/soap/encoding/</xsp:attribute>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
