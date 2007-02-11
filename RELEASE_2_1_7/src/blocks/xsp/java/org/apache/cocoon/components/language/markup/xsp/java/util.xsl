<?xml version="1.0"?>
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
 * XSP Util logicsheet for the Java language
 *
 * @author <a href="mailto:ricardo@apache.org>Ricardo Rocha</a>
 * @author ported by <a href="mailto:bloritsch@apache.org>Berin Loritsch</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/17 11:28:22 $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:util="http://apache.org/xsp/util/2.0"
>
  <xsl:variable name="namespace-uri">http://apache.org/xsp/util/2.0</xsl:variable>
  <xsl:include href="logicsheet-util.xsl"/>

  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>

      <xsp:structure>
        <xsp:include>java.net.URL</xsp:include>
        <xsp:include>java.util.Date</xsp:include>
        <xsp:include>java.io.FileReader</xsp:include>
        <xsp:include>java.text.SimpleDateFormat</xsp:include>
        <xsp:include>org.apache.cocoon.components.language.markup.xsp.XSPUtil</xsp:include>
      </xsp:structure>

      <xsl:if test="util:cacheable">
       <xsp:logic>
        public boolean hasContentChanged( org.apache.cocoon.environment.Request request ) {
          return false;
        }
       </xsp:logic>
      </xsl:if>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="util:cacheable"/>

  <!-- Multiline string (based on code by JQ)  - DOESN'T WORK YET!
  <xsl:template match="util:string">
    <xsl:choose>
      <xsl:when test="contains(., '
') or contains(.,
'
')">
        <xsl:call-template name="get-nested-strings">
          <xsl:with-param name="content" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="."/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-strings">
    <xsl:param name="content"/>
      <xsl:variable name="first" select="substring-before($content, '
')"/>
      <xsl:variable name="rest" select="substring-after($content, '
')"/>
      <xsl:text>"</xsl:text><xsl:value-of select="$first"/><xsl:text>"</xsl:text>
      <xsl:if test="$rest">
        <xsl:text> + "\n" +
</xsl:text>
        <xsl:call-template name="get-nested-strings">
          <xsl:with-param name="content" select="$rest"/>
        </xsl:call-template>
      </xsl:if>
  </xsl:template>
  -->

  <xsl:template match="util:get-source">
    <xsl:variable name="source-uri">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">uri</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsp:expr>
      XSPUtil.getSourceContents(<xsl:copy-of select="$source-uri"/>,this.resolver)
    </xsp:expr>
  </xsl:template>

  <xsl:template match="util:include-source">
    <xsl:variable name="source-uri">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">uri</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="source-base">
      <xsl:call-template name="get-string-parameter">
        <xsl:with-param name="name">base</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsp:logic>
      XSPUtil.includeSource(<xsl:copy-of select="$source-uri"/>, <xsl:copy-of select="$source-base"/>, this.resolver, this.contentHandler);
    </xsp:logic>
  </xsl:template>



  <!-- Include URL contents as SAX -->
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
    <xsp:logic>
      XSPUtil.includeSource(<xsl:copy-of select="$href"/>, null, this.resolver, this.contentHandler);
    </xsp:logic>
  </xsl:template>


   <!-- Include file contents as SAX -->
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
       XSPUtil.includeFile(<xsl:copy-of select="$name"/>,this.manager, this.contentHandler, objectModel);
     </xsp:logic>
   </xsl:template>

  <!-- Include expression as SAX -->
  <xsl:template match="util:include-expr">
    <xsl:variable name="expr">
      <xsl:choose>
        <xsl:when test="@expr"><xsl:value-of select="@expr"/></xsl:when>
        <xsl:when test="util:expr">
          <xsp:expr><xsl:apply-templates select="util:expr/*|util:expr/text()"/></xsp:expr>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      XSPUtil.includeString(String.valueOf(<xsl:copy-of select="$expr"/>),
          this.manager, this.contentHandler);
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

    <xsl:choose>
      <xsl:when test="@encoding">
        <xsp:expr>
          XSPUtil.getFileContents(
            XSPUtil.relativeFilename(
              String.valueOf(<xsl:copy-of select="$name"/>),
              this.objectModel
            ), "<xsl:value-of select="@encoding"/>")
        </xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>
          XSPUtil.getFileContents(
            XSPUtil.relativeFilename(
              String.valueOf(<xsl:copy-of select="$name"/>),
              this.objectModel))
        </xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Counters -->
  <xsl:template match="util:counter">
    <xsl:choose>
      <xsl:when test="@scope = 'session'">
        <xsp:expr>XSPUtil.getSessionCount(this.session)</xsp:expr>
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
      XSPUtil.formatDate(new Date(),
          String.valueOf(<xsl:copy-of select="$format"/>).trim())
    </xsp:expr>
  </xsl:template>

  <!-- 
        Returns value of a sitemap parameter, null if that parameter is undeclared
        "name" attribute should be specified.
  -->
  <xsl:template match="util:get-sitemap-parameter">
    <xsp:expr>(parameters.getParameter("<xsl:value-of select="@name"/>", null))</xsp:expr>
  </xsl:template>

</xsl:stylesheet>
