<?xml version="1.0"?>

<!-- $Id: log.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
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
 * XSP Logger logicsheet for the Java language
 *
 * @author <a href="mailto:bloritsch@apache.org>Berin Loritsch</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:log="http://apache.org/xsp/log/2.0"

  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:template match="log:logger">
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
        <xsl:when test="name">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="log:name"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

<!-- Files are now under control of LogKitManager
    <xsl:variable name="filename">
      <xsl:choose>
        <xsl:when test="@filename">"<xsl:value-of select="@filename"/>"</xsl:when>
        <xsl:when test="filename">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="log:filename"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
-->

    <xsl:variable name="level">
      <xsl:choose>
        <xsl:when test="@level">"<xsl:value-of select="@level"/>"</xsl:when>
        <xsl:when test="level">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="log:level"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>"DEBUG"</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      if (getLogger() == null) {
          try {
            String category = <xsl:value-of select="$name"/>;
            org.apache.log.Logger logger = org.apache.log.Hierarchy.getDefaultHierarchy().getLoggerFor( category );
<!--
            if (!"".equals(<xsl:value-of select="$filename"/>)) {
                String file = this.avalonContext.get(org.apache.cocoon.Constants.CONTEXT_LOG_DIR) + <xsl:value-of select="$filename"/>;
                org.apache.log.LogTarget[] targets = new org.apache.log.LogTarget[] {
                    new org.apache.log.output.FileOutputLogTarget(file)
                };
                logger.setLogTargets(targets);
            }
-->
            logger.setPriority(org.apache.log.Priority.getPriorityForName(<xsl:value-of select="$level"/>));
            this.enableLogging(new org.apache.avalon.framework.logger.LogKitLogger(logger));
          } catch (Exception e) {
            getLogger().error("Could not create logger for \"" +
                               <xsl:value-of select="$name"/> + "\".", e);
          }
      }
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:debug">
    <xsp:logic>
      if(getLogger() != null)
        getLogger().debug(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:info">
    <xsp:logic>
      if(getLogger() != null)
        getLogger().info(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:warn">
    <xsp:logic>
      if(getLogger() != null)
        getLogger().warn(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:error">
    <xsp:logic>
      if(getLogger() != null)
        getLogger().error(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:fatal-error">
    <xsp:logic>
      if(getLogger() != null)
        getLogger().fatalError(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
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

  <xsl:template name="get-log-message">
    <xsl:for-each select="./child::node()">
      <xsl:choose>
        <xsl:when test="xsp:expr"><xsl:apply-templates select="node()"/></xsl:when>
        <xsl:otherwise>"<xsl:value-of select="."/>"</xsl:otherwise>
      </xsl:choose>
      <xsl:if test="not(position() = last())"> + </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
