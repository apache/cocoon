<?xml version="1.0"?><!-- -*- xsl -*- -->
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--
 * InputModule Logicsheet, access methods of InputModule.
 *
 * @author <a href="mailto:haul@apache.org>Christian Haul</a>
 * @version CVS $Id: input.xsl,v 1.2 2004/03/17 11:28:22 crossley Exp $
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
