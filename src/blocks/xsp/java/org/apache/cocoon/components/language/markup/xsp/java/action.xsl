<?xml version="1.0" encoding="ISO-8859-1"?>
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
 * Logicsheet for XSP executed in actions using ServerPagesAction.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/17 11:28:21 $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:action="http://apache.org/cocoon/action/1.0"
  exclude-result-prefixes="action"
>
<!-- Namespace URI for this logicsheet -->
<xsl:param name="namespace-uri">http://apache.org/cocoon/action/1.0</xsl:param>

<!-- Include logicsheet common stuff -->
<xsl:include href="logicsheet-util.xsl"/>

<!--
   Class-level declarations.
-->
<xsl:template match="xsp:page">
  <xsp:page>
    <xsl:apply-templates select="@*"/>
    <xsp:structure>
      <xsp:include>org.apache.cocoon.environment.Redirector</xsp:include>
      <xsp:include>org.apache.cocoon.acting.ServerPagesAction</xsp:include>
      <xsp:include>java.util.Map</xsp:include>
    </xsp:structure>
    <xsp:logic>
      private Redirector actionRedirector;
      private Map actionResultMap;
    </xsp:logic>
    <xsl:apply-templates/>
  </xsp:page>
</xsl:template>

<!--
   Before generation begins, get redirector and result map from the object model.
   If it's not there, we're not in an action, so throw a ProcessingException.
-->
<xsl:template match="xsp:page/*[not(self::xsp:*)]">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsp:logic>
      // action prefix is "<xsl:value-of select="$namespace-prefix"/>"
      this.actionRedirector = (Redirector)this.objectModel.get(ServerPagesAction.REDIRECTOR_OBJECT);
      this.actionResultMap = (Map)this.objectModel.get(ServerPagesAction.ACTION_RESULT_OBJECT);
      if (this.actionRedirector == null) {
        throw new ProcessingException("action logicsheet cannot be used in generators");
      }
    </xsp:logic>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<!--
   Redirects to a given URL.
   @param uri the URI to redirect to (String)
   @param session-mode keep session across redirect (boolean, default = true).
-->
<xsl:template match="action:redirect-to">
  <xsl:variable name="uri">
    <xsl:call-template name="get-string-parameter">
      <xsl:with-param name="name">uri</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="session-mode">
    <xsl:call-template name="get-parameter">
      <xsl:with-param name="name">session-mode</xsl:with-param>
      <xsl:with-param name="default">true</xsl:with-param>
      <xsl:with-param name="required">false</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsp:logic>
    this.actionRedirector.redirect(<xsl:value-of select="$session-mode"/>, <xsl:value-of select="$uri"/>);
  </xsp:logic>
</xsl:template>

<!--
   Adds an entry in the action result map, and implicitly set the action status to successful.

   @param name the entry name (String)
   @param value the entry value (String)
-->
<xsl:template match="action:set-result">
  <xsl:variable name="name">
    <xsl:call-template name="get-string-parameter">
      <xsl:with-param name="name">name</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="value">
    <xsl:call-template name="get-string-parameter">
      <xsl:with-param name="name">value</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsp:logic>
    this.actionResultMap.put(<xsl:value-of select="$name"/>, <xsl:value-of select="$value"/>);
  </xsp:logic>
</xsl:template>

<!--
   Gets the value of an entry of the Action result map (previously set
   with &lt;action:set-result&gt;)

   @param name the entry name (String)
-->
<xsl:template match="action:get-result">
  <xsl:variable name="name">
    <xsl:call-template name="get-string-parameter">
      <xsl:with-param name="name">name</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsp:expr>this.actionResultMap.get(<xsl:value-of select="$name"/>)</xsp:expr>
</xsl:template>

<!--
   Sets the action status to successful.
-->

<xsl:template match="action:set-success">
  <xsp:logic>
    this.objectModel.put(ServerPagesAction.ACTION_SUCCESS_OBJECT, Boolean.TRUE);
  </xsp:logic>
</xsl:template>

<!--
   Sets the action status to failure (child statements in the sitemap won't be
   executed).
-->

<xsl:template match="action:set-failure">
  <xsp:logic>
    this.objectModel.put(ServerPagesAction.ACTION_SUCCESS_OBJECT, Boolean.FALSE);
  </xsp:logic>
</xsl:template>

</xsl:stylesheet>
