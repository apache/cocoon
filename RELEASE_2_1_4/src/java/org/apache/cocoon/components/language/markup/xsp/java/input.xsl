<?xml version="1.0"?><!-- -*- xsl -*- -->

<!--

 ============================================================================
                   The Apache Software License, Version 1.1
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
 * InputModule Logicsheet, access methods of InputModule.
 *
 * @author <a href="mailto:haul@apache.org>Christian Haul</a>
 * @version CVS $Id: input.xsl,v 1.2 2004/02/07 16:07:31 ghoward Exp $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:input="http://apache.org/cocoon/xsp/input/1.0">

  <xsl:variable name="namespace-uri">http://apache.org/cocoon/xsp/input/1.0</xsl:variable>
  <xsl:include href="logicsheet-util.xsl"/>


  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>

      <xsp:structure>
        <xsp:include>org.apache.cocoon.components.modules.input.InputModule</xsp:include>
        <xsp:include>org.apache.cocoon.components.language.markup.xsp.XSPModuleHelper</xsp:include>
      </xsp:structure>

      <xsp:init-page>
        // create module cache
        if (this._xsp_module_helper == null) {
           this._xsp_module_helper = new XSPModuleHelper();
        }
        this._xsp_module_helper.setup(manager);
      </xsp:init-page>

      <xsp:exit-page>
        // clear module cache
        if (this._xsp_module_helper != null) this._xsp_module_helper.releaseAll();
      </xsp:exit-page>

      <xsp:logic>
        XSPModuleHelper _xsp_module_helper =  null;
      </xsp:logic>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="input:get-attribute">
    <!-- InputModule short-hand, defaults to "request-param" -->
    <xsl:variable name="module">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">module</xsl:with-param>
        <xsl:with-param name="default">request-param</xsl:with-param>
        <xsl:with-param name="required">false</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- attribute name, no default, required -->
    <xsl:variable name="name">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="required">true</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- default value, defaults to empty string -->
    <xsl:variable name="default">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">default</xsl:with-param>
        <xsl:with-param name="required">false</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- return type: object / string / xml, defaults to object -->
    <xsl:variable name="as">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">as</xsl:with-param>
        <xsl:with-param name="default">object</xsl:with-param>
        <xsl:with-param name="required">false</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- -->
    <xsl:choose>
      <xsl:when test="$as = 'object'">
        <xsp:expr>this._xsp_module_helper.getAttribute(objectModel, <xsl:copy-of select="$module"/>,<xsl:copy-of select="$name"/>,<xsl:copy-of select="$default"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          String.valueOf(this._xsp_module_helper.getAttribute(objectModel, <xsl:copy-of select="$module"/>,<xsl:copy-of select="$name"/>,<xsl:copy-of select="$default"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <input:attribute>
          <xsp:attribute name="name">
            <xsp:expr><xsl:copy-of select="$name"/></xsp:expr>
          </xsp:attribute>
          <xsp:expr>this._xsp_module_helper.getAttribute(objectModel, <xsl:copy-of select="$module"/>,<xsl:copy-of select="$name"/>,<xsl:copy-of select="$default"/>)</xsp:expr>
        </input:attribute>
      </xsl:when>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="input:get-attribute-values">
    <!-- InputModule short-hand, defaults to "request-param" -->
    <xsl:variable name="module">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">module</xsl:with-param>
        <xsl:with-param name="default">request-param</xsl:with-param>
        <xsl:with-param name="required">false</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- attribute name, no default, required -->
    <xsl:variable name="name">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="required">true</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- return type: object / array / string / xml, defaults to object -->
    <xsl:variable name="as">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">as</xsl:with-param>
        <xsl:with-param name="default">object</xsl:with-param>
        <xsl:with-param name="required">false</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- -->
    <xsl:choose>
      <xsl:when test="$as = 'object' or $as = 'array'">
        <xsp:expr>this._xsp_module_helper.getAttributeValues(objectModel, <xsl:copy-of select="$module"/>,<xsl:copy-of select="$name"/>,null)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          String.valueOf(this._xsp_module_helper.getAttributeValues(objectModel, <xsl:copy-of select="$module"/>,<xsl:copy-of select="$name"/>,null))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          this._xsp_module_helper.getAttributeValues(objectModel, this.contentHandler, <xsl:copy-of select="$module"/>,<xsl:copy-of select="$name"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>



  <xsl:template match="input:get-attribute-names">
    <!-- InputModule short-hand, defaults to "request-param" -->
    <xsl:variable name="module">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">module</xsl:with-param>
        <xsl:with-param name="default">request-param</xsl:with-param>
        <xsl:with-param name="required">false</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- return type: object / string / xml, defaults to object -->
    <xsl:variable name="as">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">as</xsl:with-param>
        <xsl:with-param name="default">object</xsl:with-param>
        <xsl:with-param name="required">false</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- -->
    <xsl:choose>
      <xsl:when test="$as = 'object'">
        <xsp:expr>this._xsp_module_helper.getAttributeNames(objectModel, <xsl:copy-of select="$module"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          String.valueOf(this._xsp_module_helper.getAttributeNames(objectModel, <xsl:copy-of select="$module"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          this._xsp_module_helper.getAttributeNames(objectModel, this.contentHandler, <xsl:copy-of select="$module"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>
