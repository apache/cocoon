<?xml version="1.0" encoding="utf-8"?>
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
  Author: Ovidiu Predescu "ovidiu@cup.hp.com"

  With fixes for Xalan 2.4.1 and XSLTC from Joerg Heinicke
  "joerg.heinicke@gmx.de"

  Date: February 15, 2002
  
  XSP logicsheet for the control flow layer.
 -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:jpath="http://apache.org/xsp/jpath/1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:key name="JPathExprs"
           match="jpath:if | jpath:when | jpath:for-each | jpath:value-of"
           use="concat(@test, @select)"/>

  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>
      <xsp:structure>
        <xsp:include>java.util.Iterator</xsp:include>
        <xsp:include>org.apache.cocoon.environment.Environment</xsp:include>
        <xsp:include>org.apache.cocoon.components.flow.WebContinuation</xsp:include>
        <xsp:include>org.apache.cocoon.components.flow.FlowHelper</xsp:include>
        <xsp:include>org.apache.commons.jxpath.JXPathContext</xsp:include>
        <xsp:include>org.apache.commons.jxpath.CompiledExpression</xsp:include>
      </xsp:structure>

      <xsp:logic>
      </xsp:logic>

      <xsp:init-page>
        Object bean = FlowHelper.getContextObject(this.objectModel);
        WebContinuation kont
          = FlowHelper.getWebContinuation(this.objectModel);
        JXPathContext jxpathContext = JXPathContext.newContext(bean);
        Object __jxpathResult;
        // Generate the compiled representation of the JXPath
        // expressions used by this page.
        <xsl:apply-templates select="
              //jpath:if      [generate-id(.) = generate-id(key('JPathExprs', @test))]
            | //jpath:when    [generate-id(.) = generate-id(key('JPathExprs', @test))]
            | //jpath:value-of[generate-id(.) = generate-id(key('JPathExprs', @select))]
            | //jpath:for-each[generate-id(.) = generate-id(key('JPathExprs', @select))]"
          mode="compile"/>
      </xsp:init-page>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="jpath:if | jpath:when" mode="compile">
    <xsl:variable name="var-name">
      <xsl:call-template name="get-var-name">
        <xsl:with-param name="expr" select="@test"/>
      </xsl:call-template>
    </xsl:variable>
    CompiledExpression <xsl:value-of select="$var-name"/>
      = jxpathContext.compile("<xsl:value-of select="@test"/>");
  </xsl:template>

  <xsl:template match="jpath:for-each | jpath:value-of" mode="compile">
    <xsl:variable name="var-name">
      <xsl:call-template name="get-var-name">
        <xsl:with-param name="expr" select="@select"/>
      </xsl:call-template>
    </xsl:variable>
    CompiledExpression <xsl:value-of select="$var-name"/>
      = jxpathContext.compile("<xsl:value-of select="@select"/>");
  </xsl:template>

  <xsl:template name="get-var-name">
    <xsl:param name="expr"/>
    jxpath_<xsl:value-of select="generate-id(key('JPathExprs', $expr))"/>
  </xsl:template>

  <xsl:template match="jpath:if">
    <xsl:choose>
      <xsl:when test="@test">
        <xsp:logic>

          __jxpathResult = <xsl:call-template name="get-var-name">
                             <xsl:with-param name="expr" select="@test"/>
                           </xsl:call-template>
                            .getValue(jxpathContext);
          if ((__jxpathResult instanceof Boolean
                &amp;&amp; ((Boolean)__jxpathResult).booleanValue() == true)
              || (!(__jxpathResult instanceof Boolean)
                   &amp;&amp; __jxpathResult != null)) {
            <xsl:apply-templates/>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          <xsl:text>Required 'test' attribute in &lt;jpath:if&gt; is missing!</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="jpath:choose">
    <xsp:logic>
      if (false) {
      }
      <xsl:apply-templates select="jpath:when|jpath:otherwise"/>
    </xsp:logic>
  </xsl:template>


  <xsl:template match="jpath:when">
    <xsp:logic>
      else if ((((__jxpathResult = <xsl:call-template name="get-var-name">
                                    <xsl:with-param name="expr" select="@test"/>
                                  </xsl:call-template>
                                  .getValue(jxpathContext))
                       instanceof Boolean)
                &amp;&amp; ((Boolean)__jxpathResult).booleanValue() == true)
              || (!(__jxpathResult instanceof Boolean)
                   &amp;&amp; __jxpathResult != null)) {
        <xsl:apply-templates/>
      }
    </xsp:logic>
  </xsl:template>


  <xsl:template match="jpath:otherwise">
    <xsp:logic>
      else {
        <xsl:apply-templates/>
      }
    </xsp:logic>
  </xsl:template>

  <xsl:template match="jpath:for-each">
    <xsl:variable name="old-context">
      oldJPathContext<xsl:value-of select="count(ancestor-or-self::*)"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="@select">
        <xsp:logic>
          {
            Iterator iter_<xsl:value-of select="generate-id(.)"/>
              = <xsl:call-template name="get-var-name">
                  <xsl:with-param name="expr" select="@select"/>
                </xsl:call-template>
                .iterate(jxpathContext);
            JXPathContext <xsl:value-of select="$old-context"/> = jxpathContext;
            while (iter_<xsl:value-of select="generate-id(.)"/>.hasNext()) {
              Object current_<xsl:value-of select="generate-id(.)"/> = iter_<xsl:value-of select="generate-id(.)"/>.next();
              jxpathContext = JXPathContext.newContext(current_<xsl:value-of select="generate-id(.)"/>);
        </xsp:logic>

        <xsl:apply-templates/>

        <xsp:logic>
            }
            jxpathContext = <xsl:value-of select="$old-context"/>;
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          <xsl:text>Required 'select' attribute in &lt;jpath:for-each&gt; is missing!</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="jpath:value-of">
    <xsp:expr>
      <xsl:choose>
        <xsl:when test="@select">
          <xsl:call-template name="get-var-name">
            <xsl:with-param name="expr" select="@select"/>
          </xsl:call-template>
          .getValue(jxpathContext)
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            <xsl:text>Required 'select' attribute in &lt;jpath:value-of&gt; is missing!</xsl:text>
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="jpath:continuation">
    <xsl:variable name="level">
      <xsl:choose>
        <xsl:when test="@select">
          <xsl:value-of select="@select"/>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:expr>
      (kont == null? "": kont.getContinuation(<xsl:value-of select="$level"/>).getId())
    </xsp:expr>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <!-- Catch all template. Just pass along unmodified everything we
         don't handle. -->
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>
