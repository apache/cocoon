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
  xmlns:util="http://www.apache.org/1999/XSP/Util"
>
  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>

      <xsp:structure>
        <xsp:include>java.net.URL</xsp:include>
        <xsp:include>java.util.Date</xsp:include>
        <xsp:include>java.text.SimpleDateFormat</xsp:include>
      </xsp:structure>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <!-- Include URL contents as DOM -->
  <xsl:template match="util:include-uri">
    <xsl:variable name="href">
      <xsl:choose>
        <xsl:when test="@href">"<xsl:value-of select="@href"/>"</xsl:when>
        <xsl:when test="util:href">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="util:href"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <!-- This should be an <xsp:expr> yielding Node... -->
    <xsp:logic> {
      String __name = String.valueOf(<xsl:copy-of select="$href"/>);

      try {
        URL __url = new URL(__name);
        InputSource __is = new InputSource(__url.openStream());
        __is.setSystemId(__url.toString());

        xspCurrentNode.appendChild(
          XSPUtil.cloneNode(
            this.xspParser.parse(__is).getDocumentElement(),
            document
          )
        );
      } catch (Exception e) {
        xspCurrentNode.appendChild(
          document.createTextNode(
            "{" + __name + "}"
          )
        );
      }
    } </xsp:logic>
  </xsl:template>

  <!-- Include file contents as DOM -->
  <xsl:template match="util:include-file">
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
        <xsl:when test="util:name">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="util:name"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      xspCurrentNode.appendChild(
        XSPUtil.cloneNode(
          this.xspParser.parse(
            new InputSource(
              new FileReader(
                XSPUtil.relativeFilename(
                  <xsl:copy-of select="$name"/>,
                  request
                )
              )
            )
          ).getDocumentElement(),
          document
        )
      );
    </xsp:logic>
  </xsl:template>

  <!-- Include file contents as text -->
  <xsl:template match="util:get-file-contents">
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
        <xsl:when test="util:name">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="util:name"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:expr>
      XSPUtil.getFileContents(
	XSPUtil.relativeFilename(
          String.valueOf(<xsl:copy-of select="$name"/>),
	  request,
	  (ServletContext) context
	)
      )
    </xsp:expr>
  </xsl:template>


  <!-- Counters -->
  <xsl:template match="util:counter">
    <xsl:choose>
      <xsl:when test="@scope = 'session'">
        <xsp:expr>XSPUtil.getSessionCount(session)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>XSPUtil.getCount()</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Date -->
  <xsl:template match="util:time">
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <xsl:when test="util:format">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="util:format"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsp:expr>
      XSPUtil.formatDate(
        new Date(),
	String.valueOf(<xsl:copy-of select="$format"/>).trim()
      )
    </xsp:expr>
  </xsl:template>

  <!-- Standard Templates -->
  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
