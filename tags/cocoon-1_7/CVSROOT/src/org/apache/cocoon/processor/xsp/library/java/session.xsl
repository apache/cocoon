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
  xmlns:session="http://www.apache.org/1999/XSP/Session"
>
  <!-- *** ServletResponse Templates *** -->

  <!-- Import Global XSP Templates -->
  <!-- <xsl:import href="base-library.xsl"/> -->

  <!-- Deprecated methods -->
  <xsl:template match="session:get-value">
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
          XSPSessionLibrary.getValue(
            session,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          session.getValue(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:get-value-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getValueNames(session, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          session.getValueNames()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:put-value">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="content">
      <xsl:call-template name="get-nested-content">
        <xsl:with-param name="content" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:logic>
      session.putValue(
        String.valueOf(<xsl:copy-of select="$name"/>),
        <xsl:copy-of select="$content"/>
      );
    </xsp:logic>
  </xsl:template>
  <!-- End deprecated methods -->

  <xsl:template match="session:get-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getAttribute(
            session,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'object'">
          session.getAttribute(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:get-attribute-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getAttributeNames(session, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPSessionLibrary.getAttributeNames(session)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:get-creation-time">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getCreationTime(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(new Date(session.getCreationTime()))
        </xsl:when>
        <xsl:when test="$as = 'long'">
          session.getCreationTime()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:get-id">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getId(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          session.getId()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:get-last-accessed-time">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getLastAccessedTime(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(new Date(session.getLastAccessedTime()))
        </xsl:when>
        <xsl:when test="$as = 'long'">
          session.getLastAccessedTime()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:get-max-inactive-interval">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.getMaxInactiveInterval(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(session.getMaxInactiveInterval())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          session.getMaxInactiveInterval()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:invalidate">
    <xsp:logic>
      session.invalidate()
    </xsp:logic>
  </xsl:template>

  <xsl:template match="session:is-new">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPSessionLibrary.isNew(
            session,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(session.isNew())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          session.isNew()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="session:remove-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsp:logic>
      session.removeAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>)
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="session:set-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="content">
      <xsl:call-template name="get-nested-content">
        <xsl:with-param name="content" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:logic>
      session.setAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>),
        <xsl:copy-of select="$content"/>
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="session:set-max-inactive-interval">
    <xsl:variable name="interval">
      <xsl:choose>
        <xsl:when test="@interval">"<xsl:value-of select="@interval"/>"</xsl:when>
        <xsl:when test="interval">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="interval"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      session.setInactiveInterval(
        Integer.parseInt(
          String.valueOf(
            <xsl:copy-of select="$interval"/>
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
