<?xml version="1.0"?>
<!--

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

<!-- written by Ricardo Rocha "ricardo@apache.org" -->


<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://www.apache.org/1999/XSP/Core"
  xmlns:context="http://www.apache.org/1999/XSP/Context"
>
  <!-- *** ServletContext Templates *** -->

  <!-- Import Global XSP Templates -->
  <!-- <xsl:import href="base-library.xsl"/> -->

  <xsl:template match="context:get-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getAttribute(
            servletContext,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          servletContext.getAttribute(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-attribute-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getAttributeNames(servletContext, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPContextLibrary.getAttributeNames(servletContext)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-init-parameter">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getInitParameter(
            servletContext,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          servletContext.getInitParameter(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-init-parameters">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getInitParameterNames(servletContext, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPContextLibrary.getInitParameterNames(servletContext)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-major-version">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getMajorVersion(
            servletContext,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(
            servletContext.getMajorVersion()
          )
        </xsl:when>
        <xsl:when test="$as = 'int'">
          servletContext.getMajorVersion()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-mime-type">
    <xsl:variable name="file">
      <xsl:choose>
        <xsl:when test="@file">"<xsl:value-of select="@file"/>"</xsl:when>
        <xsl:when test="file">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="file"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getMimeType(
            servletContext,
            <xsl:copy-of select="$file"/>,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          servletContext.getMimeType(
            <xsl:copy-of select="$file"/>
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-minor-version">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getMinorVersion(
            servletContext,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(
            servletContext.getMinorVersion()
          )
        </xsl:when>
        <xsl:when test="$as = 'int'">
          servletContext.getMinorVersion()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-real-path">
    <xsl:variable name="path">
      <xsl:choose>
        <xsl:when test="@path">"<xsl:value-of select="@path"/>"</xsl:when>
        <xsl:when test="path">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="path"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getRealPath(
            servletContext,
            <xsl:copy-of select="$path"/>,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          servletContext.getRealPath(
            <xsl:copy-of select="$path"/>
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-resource">
    <xsl:variable name="path">
      <xsl:choose>
        <xsl:when test="@path">"<xsl:value-of select="@path"/>"</xsl:when>
        <xsl:when test="path">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="path"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:expr>
      servletContext.getResource(
        <xsl:copy-of select="$path"/>
      )
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-resource-as-stream">
    <xsl:variable name="path">
      <xsl:choose>
        <xsl:when test="@path">"<xsl:value-of select="@path"/>"</xsl:when>
        <xsl:when test="path">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="path"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:expr>
      servletContext.getResourceAsStream(
        <xsl:copy-of select="$path"/>
      )
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:get-server-info">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPContextLibrary.getServerInfo(
            servletContext,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          servletContext.getServerInfo()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="context:log">
    <xsl:variable name="message">
      <xsl:choose>
        <xsl:when test="@message">"<xsl:value-of select="@message"/>"</xsl:when>
        <xsl:when test="message">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="message"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      servletContext.log(
        String.valueOf(<xsl:copy-of select="$message"/>)
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="context:remove-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsp:logic>
      servletContext.removeAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>)
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="context:set-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="content">
      <xsl:call-template name="get-nested-content">
        <xsl:with-param name="content">
          <content><xsl:apply-templates/></content>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsp:logic>
      servletContext.setAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>),
        <xsl:copy-of select="$content"/>
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template name="value-for-name">
    <xsl:choose>
      <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
      <xsl:when test="name">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="name"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
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
