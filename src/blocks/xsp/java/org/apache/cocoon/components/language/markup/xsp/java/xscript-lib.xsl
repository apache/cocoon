<?xml version="1.0" encoding="utf-8"?>

<!-- $Id: xscript-lib.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
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
 *
 * Date: September 19, 2001
 *
 * @author <a href="mailto:ovidiu@cup.hp.com>Ovidiu Predescu</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-request="http://apache.org/xsp/request/2.0"
  xmlns:xscript="http://apache.org/xsp/xscript/1.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:saxon="http://icl.com/saxon">

  <xsl:template name="xscript-variable">
    <!-- PUBLIC: create a new XScript variable -->
    <xsl:param name="name"/>
    <xsl:param name="href"/>
    <xsl:param name="scope"/>

    <xsl:choose>
      <xsl:when test="$href">
        <xsl:call-template name="xscript-variable-from-url">
          <xsl:with-param name="name" select="$name"/>
          <xsl:with-param name="scope" select="$scope"/>
          <xsl:with-param name="href" select="$href"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="xscript-variable-inline">
          <xsl:with-param name="name" select="$name"/>
          <xsl:with-param name="scope" select="$scope"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="xscript-get">
    <!-- PUBLIC: obtain the value of an XScript variable -->
    <xsl:param name="name"/>
    <xsl:param name="as"/>
    <xsl:param name="scope"/>

    <xsl:variable name="object">
      xscriptManager.get(pageScope, objectModel, "<xsl:value-of select="$name"/>",
                         <xsl:value-of select="$scope"/>
      <xsl:text>)</xsl:text>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'object'">
        <xsp:expr><xsl:value-of select="$object"/></xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <!-- insert the content of the XScript variable in the SAX
             event stream -->
        <xsp:logic>
          try {
            <xsl:value-of select="$object"/>.toEmbeddedSAX(this.contentHandler);
          } catch (IllegalArgumentException ex) {
            <xscript:error><xsp:expr>ex.getMessage()</xsp:expr></xscript:error>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="xscript-remove">
    <!-- PUBLIC: undeclare an XScript variable -->
    <xsl:param name="name"/>
    <xsl:param name="scope"/>
    <xsp:logic>
      xscriptManager.remove(pageScope, objectModel, "<xsl:value-of select="$name"/>", <xsl:value-of select="$scope"/>);
    </xsp:logic>
  </xsl:template>


  <xsl:template name="xscript-transform">
    <!-- PUBLIC: transform an XScriptObject pointed to by a variable
         using another XScriptObject, which is assumed to contain an
         XSLT stylesheet. -->
    <xsl:param name="name"/>
    <xsl:param name="scope"/>
    <xsl:param name="stylesheet"/>
    <xsl:param name="stylesheet-scope"/>
    <xsl:param name="parameters"/>

    <xsl:variable name="object">
      <xsl:call-template name="xscript-get">
        <xsl:with-param name="name" select="$name"/>
        <xsl:with-param name="scope" select="$scope"/>
        <xsl:with-param name="as" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="stylesheet-object">
      <xsl:call-template name="xscript-get">
        <xsl:with-param name="name" select="$stylesheet"/>
        <xsl:with-param name="scope" select="$stylesheet-scope"/>
        <xsl:with-param name="as" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="params">
      <xsl:text>params</xsl:text><xsl:value-of select="count(ancestor-or-self::*)"/>
    </xsl:variable>

    <xsp:logic>
      {
        Parameters <xsl:value-of select="$params"/> = new Parameters();
        <xsl:call-template name="xscript-parameter">
          <xsl:with-param name="params" select="$params"/>
          <xsl:with-param name="parameters" select="$parameters"/>
        </xsl:call-template>
        XScriptObject result = <xsl:value-of select="$object"/>.transform(
            <xsl:value-of select="$stylesheet-object"/>, <xsl:value-of select="$params"/>);
<!--
        System.out.println("input source =\n" + <xsl:value-of select="$object"/>);
        System.out.println("stylesheet source =\n" + <xsl:value-of select="$stylesheet-object"/>);
        System.out.println("transformation result =\n" + result);
-->
        result.toEmbeddedSAX(this.contentHandler);
      }
    </xsp:logic>
  </xsl:template>


  <!-- Helper templates used by this stylesheet, and possibly others
       as well -->

  <xsl:template name="xscript-variable-from-url">
    <!-- PUBLIC: create an XScript variable from an URL -->
    <xsl:param name="name"/>
    <xsl:param name="scope"/>
    <xsl:param name="href"/>
    <xsp:logic>
      xscriptManager.put(pageScope, objectModel, "<xsl:value-of select="$name"/>",
                         new XScriptObjectFromURL(xscriptManager, "<xsl:value-of select="$href"/>"),
                         <xsl:value-of select="$scope"/>);
    </xsp:logic>
  </xsl:template>


  <xsl:template name="xscript-variable-inline">
    <!-- PUBLIC: create an XScript variable from inline XML -->
    <xsl:param name="name"/>
    <xsl:param name="scope"/>

    <xsl:variable name="xml-inline">
      <xsl:text>xmlInline</xsl:text><xsl:value-of select="count(ancestor-or-self::*)"/>
    </xsl:variable>

    <xsl:variable name="oldHandler">
      oldHandler<xsl:value-of select="count(ancestor-or-self::*)"/>
    </xsl:variable>

    <xsp:logic>
      {
        XScriptObjectInlineXML <xsl:value-of select="$xml-inline"/>
          = new XScriptObjectInlineXML(xscriptManager);
        ContentHandler <xsl:value-of select="$oldHandler"/> = this.contentHandler;
<!--        <xsl:value-of select="$xml-inline"/>.setNextContentHandler(<xsl:value-of select="$oldHandler"/>);-->
        this.contentHandler = <xsl:value-of select="$xml-inline"/>.getContentHandler();
        <xsl:apply-templates/>
        this.contentHandler = <xsl:value-of select="$oldHandler"/>;
        xscriptManager.put(pageScope, objectModel, "<xsl:value-of select="$name"/>",
                           <xsl:value-of select="$xml-inline"/>,
                           <xsl:value-of select="$scope"/>);
      }
    </xsp:logic>
  </xsl:template>


  <xsl:template name="xscript-get-scope">
    <!-- PUBLIC: obtain the Java expression for a given XScript
         variable scope. If no scope parameter is specified,
         ALL_SCOPES is assumed. -->
    <xsl:param name="scope" select="'all-scopes'"/>
    <xsl:choose>
      <xsl:when test="$scope = 'global' or $scope = 'application'">org.apache.cocoon.components.xscript.XScriptManager.GLOBAL_SCOPE</xsl:when>
      <xsl:when test="$scope = 'request'">org.apache.cocoon.components.xscript.XScriptManager.REQUEST_SCOPE</xsl:when>
      <xsl:when test="$scope = 'page'">org.apache.cocoon.components.xscript.XScriptManager.PAGE_SCOPE</xsl:when>
      <xsl:when test="$scope = 'session'">org.apache.cocoon.components.xscript.XScriptManager.SESSION_SCOPE</xsl:when>
      <xsl:otherwise>org.apache.cocoon.components.xscript.XScriptManager.ALL_SCOPES</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="xscript-get-scope-for-creation">
    <!-- PUBLIC: obtain the Java expression for a given XScript
         variable scope. Similar with xscript-get-scope, but when
         defining an XScript variable, ALL_SCOPES doesn't make
         sense: if no scope parameter is specified, SESSION_SCOPE is
         assumed. -->
    <xsl:param name="scope" select="'session'"/>
    <xsl:choose>
      <xsl:when test="$scope = 'global'">org.apache.cocoon.components.xscript.XScriptManager.GLOBAL_SCOPE</xsl:when>
      <xsl:when test="$scope = 'request'">org.apache.cocoon.components.xscript.XScriptManager.REQUEST_SCOPE</xsl:when>
      <xsl:when test="$scope = 'page'">org.apache.cocoon.components.xscript.XScriptManager.PAGE_SCOPE</xsl:when>
      <xsl:otherwise>org.apache.cocoon.components.xscript.XScriptManager.SESSION_SCOPE</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="xscript-parameter">
    <xsl:param name="parameters"/>
    <xsl:param name="params"/>

    <xsl:choose>
      <xsl:when test="contains(system-property('xsl:vendor-url'), 'xalan')">
        <xsl:for-each select="xalan:nodeset($parameters)/xscript:parameter">
          <xsp:logic>
            <xsl:value-of select="$params"/>.setParameter(
              "<xsl:value-of select="@name"/>",
              "<xsl:value-of select="."/>");
          </xsp:logic>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="contains(system-property('xsl:vendor-url'), 'saxon')">
        <xsl:for-each select="saxon:node-set($parameters)/xscript:parameter">
          <xsp:logic>
            <xsl:value-of select="$params"/>.setParameter(
            "<xsl:value-of select="./@name"/>",
              "<xsl:value-of select="."/>");
          </xsp:logic>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

