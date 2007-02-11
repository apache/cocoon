<?xml version="1.0"?>

<!-- $Id: session-fw.xsl,v 1.4 2003/12/18 13:30:15 antonio Exp $-->
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
 * XSP Session-fw logicsheet for the Java language
 *
 * @author <a href="mailto:antonio@apache.org>Antonio Gallardo</a>
 * @version CVS $Revision: 1.4 $ $Date: 2003/12/18 13:30:15 $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-session-fw="http://apache.org/xsp/session-fw/1.0">

  <!-- *** Session-fw Templates *** -->
  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>

      <xsp:structure>
        <xsp:include>org.apache.cocoon.environment.Session</xsp:include>
        <xsp:include>org.apache.avalon.framework.component.ComponentManager</xsp:include>
        <xsp:include>org.apache.cocoon.components.language.markup.xsp.XSPSessionFwHelper</xsp:include>
        <xsp:include>org.w3c.dom.DocumentFragment</xsp:include>
      </xsp:structure>

      <xsl:variable name="create">
        <xsl:choose>
          <xsl:when test="@create-session='yes' or @create-session='true'">true</xsl:when>
          <xsl:when test="@create-session='no' or @create-session='false'">false</xsl:when>
          <xsl:otherwise>true</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsp:init-page>
        /* Session session = request.getSession(<xsl:value-of select="$create"/>); */
      </xsp:init-page>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="xsp-session-fw:getxml">
    <xsl:variable name="context">
        <xsl:call-template name="value-for-context"/>
    </xsl:variable>
    <xsl:variable name="path">
        <xsl:call-template name="value-for-path"/>
    </xsl:variable>
    <xsl:variable name="as">
        <xsl:call-template name="value-for-as">
            <xsl:with-param name="default" select="'string'"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
        <xsl:when test="$as='string'">
            <xsp:expr>XSPSessionFwHelper.getXMLAsString(this.manager,
                String.valueOf(<xsl:copy-of select="$context"/>),
                String.valueOf(<xsl:copy-of select="$path"/>))</xsp:expr>
        </xsl:when>
        <xsl:when test="$as='xml'">
            <xsp-session-fw:xml>
                <xsp:expr>XSPSessionFwHelper.getXML(this.manager,
                    String.valueOf(<xsl:copy-of select="$context"/>),
                    String.valueOf(<xsl:copy-of select="$path"/>))</xsp:expr>
            </xsp-session-fw:xml>
        </xsl:when>
        <xsl:when test="$as='object'">
            <xsp:expr>XSPSessionFwHelper.getXML(this.manager,
                String.valueOf(<xsl:copy-of select="$context"/>),
                String.valueOf(<xsl:copy-of select="$path"/>))</xsp:expr>
        </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-context">
    <xsl:choose>
        <xsl:when test="@context">"<xsl:value-of select="@context"/>"</xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-path">
    <xsl:choose>
        <xsl:when test="@path">"<xsl:value-of select="@path"/>"</xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-as">
    <xsl:param name="default"/>
    <xsl:choose>
      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
