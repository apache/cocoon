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

<!-- $Id: logicsheet-util.xsl,v 1.3 2004/04/05 12:25:31 antonio Exp $-->
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
 * @version CVS $Revision: 1.3 $ $Date: 2004/04/05 12:25:31 $
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
