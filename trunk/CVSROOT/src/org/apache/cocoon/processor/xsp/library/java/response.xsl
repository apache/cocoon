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
  xmlns:response="http://www.apache.org/1999/XSP/Response"
>
  <!-- *** ServletResponse Templates *** -->

  <!-- Import Global XSP Templates -->
  <!-- <xsl:import href="base-library.xsl"/> -->

  <xsl:template match="response:get-character-encoding">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getCharacterEncoding(response, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.getCharacterEncoding()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>


  <xsl:template match="response:get-locale">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getLocale(response, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.getLocale(response).toString()
        </xsl:when>
        <xsl:when test="$as = 'object'">
          response.getLocale(response)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="response:set-content-type">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="@type">"<xsl:value-of select="@type"/>"</xsl:when>
        <xsl:when test="type">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="type"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      response.setContentType(
        String.valueOf(
          <xsl:copy-of select="$type"/>
        )
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:set-locale">
    <xsp:logic>
      response.setLocale(
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="locale"/>
        </xsl:call-template>
      );
    </xsp:logic>
  </xsl:template>



  <xsl:template match="response:add-cookie">
    <xsp:logic>
      response.addCookie(
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="."/>
        </xsl:call-template>
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:add-date-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="date">
      <xsl:choose>
        <xsl:when test="@date">"<xsl:value-of select="@date"/>"</xsl:when>
        <xsl:when test="date">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="date"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <xsl:when test="format">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="format"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$format">
          XSPResponseLibrary.addDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            String.valueOf(
              <xsl:call-template name="get-nested-content">
                <xsl:with-param name="content" select="$date"/>
              </xsl:call-template>
            ),
            String.valueOf(<xsl:copy-of select="$format"/>)
          );
        </xsl:when>
        <xsl:otherwise>
          XSPResponseLibrary.addDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:call-template name="get-nested-content">
              <xsl:with-param name="content" select="$date"/>
            </xsl:call-template>
          );
        </xsl:otherwise>
      </xsl:choose>
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:add-header">
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

    <xsp:logic>
      response.addHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       String.valueOf(<xsl:copy-of select="$value"/>)
     );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:add-header">
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

    <xsp:logic>
      response.addIntHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       Integer.parseInt(
         String.valueOf(<xsl:copy-of select="$value"/>)
       )
     );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:contains-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPResponseLibrary.containsHeader(
            response,
            String.valueOf(<xsl:copy-of select="$value"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(
            response.containsHeader(
              String.valueOf(<xsl:copy-of select="$value"/>),
            )
          )
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          response.containsHeader(
            String.valueOf(<xsl:copy-of select="$value"/>),
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="response:encode-redirect-url">
    <xsl:variable name="url">
      <xsl:choose>
        <xsl:when test="@url">"<xsl:value-of select="@url"/>"</xsl:when>
        <xsl:when test="url">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="url"/>
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
          XSPResponseLibrary.encodeRedirectURL(
            response,
            String.valueOf(<xsl:copy-of select="$url"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.encodeRedirectUrl(
            String.valueOf(<xsl:copy-of select="$url"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="response:encode-url">
    <xsl:variable name="url">
      <xsl:choose>
        <xsl:when test="@url">"<xsl:value-of select="@url"/>"</xsl:when>
        <xsl:when test="url">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="url"/>
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
          XSPResponseLibrary.encodeURL(
            response,
            String.valueOf(<xsl:copy-of select="$url"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          response.encodeUrl(
            String.valueOf(<xsl:copy-of select="$url"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="response:send-redirect">
    <xsl:variable name="location">
      <xsl:choose>
        <xsl:when test="@location">"<xsl:value-of select="@location"/>"</xsl:when>
        <xsl:when test="location">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="location"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      response.sendRedirect(
       String.valueOf(<xsl:copy-of select="$location"/>)
     );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:set-date-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="date">
      <xsl:choose>
        <xsl:when test="@date">"<xsl:value-of select="@date"/>"</xsl:when>
        <xsl:when test="date">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="date"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <xsl:when test="format">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="format"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$format">
          XSPResponseLibrary.setDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            String.valueOf(
              <xsl:call-template name="get-nested-content">
                <xsl:with-param name="content" select="$date"/>
              </xsl:call-template>
            ),
            String.valueOf(<xsl:copy-of select="$format"/>)
          );
        </xsl:when>
        <xsl:otherwise>
          XSPResponseLibrary.setDateHeader(
            response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:call-template name="get-nested-content">
              <xsl:with-param name="content" select="$date"/>
            </xsl:call-template>
          );
        </xsl:otherwise>
      </xsl:choose>
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:set-header">
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

    <xsp:logic>
      response.setHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       String.valueOf(<xsl:copy-of select="$value"/>)
     );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="response:set-header">
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

    <xsp:logic>
      response.setIntHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       Integer.parseInt(
         String.valueOf(<xsl:copy-of select="$value"/>)
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
