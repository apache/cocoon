<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- $Id: action.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
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
 * Logicsheet for XSP executed in actions using ServerPagesAction.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
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
