<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- $Id: logicsheet-util.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
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
 * A collection of utility templates to help logicsheet writers, and ensure
 * a consistent behaviour of logicsheets using them.
 *
 * To use them, just add &lt;xsl:include href="logicsheet-util.xsl"/&gt; at the
 * top of your logicsheet.
 * <br/>
 * Note : some templates rely on the <code>$namespace-uri</code> variable which must
 * be set in the including logicsheet to its namespace URI.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
-->

<xsl:stylesheet version="1.0"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--
  Namespace prefix for this logicsheet.
-->
<xsl:param name="namespace-prefix">
  <xsl:call-template name="get-namespace-prefix"/>
</xsl:param>

<!--
  Find the namespace prefix used in the source document for a given namespace URI.

  @param uri the namespace URI (default value = $namespace-uri)
  @return the namespace prefix
-->
<xsl:template name="get-namespace-prefix">
  <xsl:param name="uri"><xsl:value-of select="$namespace-uri"/></xsl:param>
  <!-- Search it on the root element -->
  <xsl:variable name="ns-attr" select="/*/namespace::*[string(.) = $uri]"/>
  <xsl:choose>
    <xsl:when test="$ns-attr"><xsl:value-of select="local-name($ns-attr)"/></xsl:when>
    <xsl:otherwise>
      <!-- Examine all nodes (requires the XSL processor to traverse the DOM) -->
      <xsl:variable name="ns-attr2" select="//*/namespace::*[string(.) = $uri]"/>
      <xsl:choose>
        <xsl:when test="$ns-attr2"><xsl:value-of select="local-name($ns-attr2)"/></xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="error">
            <xsl:with-param name="message">Cannot find namespace prefix for <xsl:value-of select="$uri"/></xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--
  Break on error.
  @param message explanation of the error
-->
<xsl:template name="error">
  <xsl:param name="message"/>
  <xsl:message terminate="yes"><xsl:value-of select="$message"/></xsl:message>
</xsl:template>

<!--
  Ignore $namespace-prefix:param elements used in get-parameter and get-string-parameter
-->
<xsl:template match="*[namespace-uri(.) = $namespace-uri and local-name(.) = 'param']" priority="-1"/>

<!--
  Break on error on all elements belonging to this logicheet's namespace not
  handled by a template.
-->
<xsl:template match="*[namespace-uri(.) = $namespace-uri and local-name(.) != 'param']" priority="-1">
  <xsl:call-template name="error">
    <xsl:with-param name="message">Unknown logicsheet tag : <xsl:value-of select="name(.)"/></xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!--
  Copy all nodes that were not handled by a dedicated template.
-->
<xsl:template match="@*|node()" priority="-2">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<!--
  Get the value of a logicsheet tag parameter as a String (borrowed from ESQL).

  @name the name of the parameter
  @default the default value (String expression)
  @required true if the parameter is required
-->
  <xsl:template name="get-string-parameter">
    <xsl:param name="name"/>
    <xsl:param name="default"/>
    <xsl:param name="required">false</xsl:param>

    <!-- for some unknown reason this needs to be called every time,
         otherwise only the first invocation uses a correct namespace
         prefix. -->
    <xsl:variable name="namespace-prefix"><xsl:call-template name="get-namespace-prefix"/></xsl:variable>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($namespace-prefix, ':param')"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="@*[name(.) = $name]">"<xsl:value-of select="@*[name(.) = $name]"/>"</xsl:when>
      <xsl:when test="(*[name(.) = $qname])[@name = $name]">
        <xsl:call-template name="get-nested-string">
          <xsl:with-param name="content" select="(*[name(.) = $qname])[@name = $name]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="string-length($default) = 0">
            <xsl:choose>
              <xsl:when test="$required = 'true'">
                <xsl:call-template name="error">
                  <xsl:with-param name="message">[Logicsheet processor]
Parameter '<xsl:value-of select="$name"/>' missing in dynamic tag &lt;<xsl:value-of select="name(.)"/>&gt;
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>""</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise><xsl:copy-of select="$default"/></xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-string">
    <xsl:param name="content"/>
      <xsl:choose>
        <!-- if $content has sub-elements, concatenate them -->
        <xsl:when test="$content/*">
          ""
          <xsl:for-each select="$content/node()">
            <xsl:choose>
              <xsl:when test="name(.)">
                <!-- element -->
                <xsl:choose>
                  <xsl:when test="namespace-uri(.)='http://apache.org/xsp' and local-name(.)='text'">
                    <!-- xsp:text element -->
                    + "<xsl:value-of select="."/>"
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- other elements -->
                    + <xsl:apply-templates select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <!-- text node -->
                + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:when>
        <!-- else return the text value of $content -->
        <xsl:otherwise>"<xsl:value-of select="normalize-space($content)"/>"</xsl:otherwise>
      </xsl:choose>
  </xsl:template>

<!--
  Get the value of a logicsheet tag parameter either as a primitive value or an object.
  @name the name of the parameter
  @default the default value
  @required true if the parameter is required
-->
  <xsl:template name="get-parameter">
    <xsl:param name="name"/>
    <xsl:param name="default"/>
    <xsl:param name="required">false</xsl:param>

    <!-- for some unknown reason this needs to be called every time,
         otherwise only the first invocation uses a correct namespace
         prefix. -->
    <xsl:variable name="namespace-prefix"><xsl:call-template name="get-namespace-prefix"/></xsl:variable>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($namespace-prefix, ':param')"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="@*[name(.) = $name]"><xsl:value-of select="@*[name(.) = $name]"/></xsl:when>
      <xsl:when test="(*[name(.) = $qname])[@name = $name]">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="(*[name(.) = $qname])[@name = $name]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="string-length($default) = 0">
            <xsl:choose>
              <xsl:when test="$required = 'true'">
                <xsl:call-template name="error">
                  <xsl:with-param name="message">[Logicsheet processor]
Parameter '<xsl:value-of select="$name"/>' missing in dynamic tag &lt;<xsl:value-of select="name(.)"/>&gt;
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>null</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise><xsl:copy-of select="$default"/></xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/xsp:text">"<xsl:value-of select="$content"/>"</xsl:when>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$content"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!--
  Get a primitive object when :param is used, for @ a String is returned. Default is also String.
  Without default, primitive value 'null' is returned
  @name the name of the parameter
  @default the default value
  @required true if the parameter is required
-->
  <xsl:template name="get-ls-parameter">
    <xsl:param name="name"/>
    <xsl:param name="default"/>
    <xsl:param name="required">false</xsl:param>

    <!-- for some unknown reason this needs to be called every time,
         otherwise only the first invocation uses a correct namespace
         prefix. -->
    <xsl:variable name="namespace-prefix"><xsl:call-template name="get-namespace-prefix"/></xsl:variable>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($namespace-prefix, ':param')"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="@*[name(.) = $name]">"<xsl:value-of select="@*[name(.) = $name]"/>"</xsl:when>
      <xsl:when test="(*[name(.) = $qname])[@name = $name]">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="(*[name(.) = $qname])[@name = $name]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="string-length($default) = 0">
            <xsl:choose>
              <xsl:when test="$required = 'true'">
                <xsl:call-template name="error">
                  <xsl:with-param name="message">[Logicsheet processor]
Parameter '<xsl:value-of select="$name"/>' missing in dynamic tag &lt;<xsl:value-of select="name(.)"/>&gt;
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>null</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>"<xsl:copy-of select="$default"/>"</xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
