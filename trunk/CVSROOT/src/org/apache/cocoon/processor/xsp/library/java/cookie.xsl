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


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://www.apache.org/1999/XSP/Core"
  xmlns:cookie="http://www.apache.org/1999/XSP/Cookie"
>
  <!-- *** ServletResponse Templates *** -->

  <!-- Import Global XSP Templates -->
  <!-- <xsl:import href="base-library.xsl"/> -->

  <xsl:template match="cookie:create">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <xsl:when test="value">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="value"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:expr>
      new Cookie(
        String.valueOf(<xsl:copy-of select="$name"/>),
        String.valueOf(<xsl:copy-of select="$value"/>)
      )
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:clone">
    <xsp:expr>
      cookie.clone()
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-comment">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getComment(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getComment()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-domain">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getDomain(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getDomain()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-max-age">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getMaxAge(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(cookie.getMaxAge())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          cookie.getMaxAge()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-name">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getName(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getName()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-path">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getPath(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getPath()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-secure">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getSecure(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(cookie.getSecure())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          cookie.getSecure()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-value">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getValue(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          cookie.getValue()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:get-version">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getVersion(
            cookie,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(cookie.getVersion())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          cookie.getVersion()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="cookie:set-comment">
    <xsl:variable name="purpose">
      <xsl:choose>
        <xsl:when test="@purpose">"<xsl:value-of select="@purpose"/>"</xsl:when>
        <xsl:when test="purpose">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="purpose"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setComment(
        String.valueOf(
          <xsl:copy-of select="$purpose"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="cookie:set-domain">
    <xsl:variable name="pattern">
      <xsl:choose>
        <xsl:when test="@pattern">"<xsl:value-of select="@pattern"/>"</xsl:when>
        <xsl:when test="pattern">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="pattern"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setDomain(
        String.valueOf(
          <xsl:copy-of select="$pattern"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="cookie:set-max-age">
    <xsl:variable name="expiry">
      <xsl:choose>
        <xsl:when test="@expiry">"<xsl:value-of select="@expiry"/>"</xsl:when>
        <xsl:when test="expiry">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="expiry"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setMaxAge(
        Integer.parseInt(
          String.valueOf(
            <xsl:copy-of select="$expiry"/>
          )
        )
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="cookie:set-path">
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

    <xsp:logic>
      cookie.setPath(
        String.valueOf(
          <xsl:copy-of select="$path"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="cookie:set-secure">
    <xsl:variable name="flag">
      <xsl:choose>
        <xsl:when test="@flag">"<xsl:value-of select="@flag"/>"</xsl:when>
        <xsl:when test="flag">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="flag"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setSecure(
        new Boolean(
          String.valueOf(
            <xsl:copy-of select="$flag"/>
          )
        ).booleanValue()
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="cookie:set-value">
    <xsl:variable name="new-value">
      <xsl:choose>
        <xsl:when test="@new-value">"<xsl:value-of select="@new-value"/>"</xsl:when>
        <xsl:when test="new-value">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="new-value"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setValue(
        String.valueOf(
          <xsl:copy-of select="$new-value"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="cookie:set-version">
    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <xsl:when test="value">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="value"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      cookie.setVersion(
        Integer.parseInt(
          String.valueOf(
            <xsl:copy-of select="$value"/>
          )
        )
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
