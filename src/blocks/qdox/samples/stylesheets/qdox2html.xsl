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

<!-- $Id: qdox2html.xsl,v 1.2 2004/03/06 02:26:14 antonio Exp $ -->

<!DOCTYPE xsl:stylesheet [
  <!ENTITY nbsp "&#160;">
  <!ENTITY lt   "&#60;">
  <!ENTITY gt   "&#62;">
  ]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:jd="http://apache.org/cocoon/javadoc/1.0">
  
  
  <xsl:output method="html" />
  
  
  <xsl:template match="/">
    <html>
      <head>
        <title>API specs of <xsl:value-of select="@qname" /></title>
      </head>
      <body bcolor="white">
        <font size="-1" color="grey">This Javadoc has been produced by Cocoon using the QDoxSource component</font>
        <xsl:comment> ======== START OF CLASS DATA ======== </xsl:comment>
        <h2>
          <font size="-1"><xsl:value-of select="*/@package" /></font>
          <br />
          <xsl:choose>
            <xsl:when test="jd:class">
              <xsl:text>Class </xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>Interface </xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="*/jd:nested-in">
            <xsl:value-of select="concat(*/jd:nested-in/*/@name, '.')" />
          </xsl:if>
          <xsl:value-of select="*/@name" />
        </h2>
        <xsl:apply-templates select="*" />
      </body>
    </html>
  </xsl:template>
  
  
  <xsl:template match="/jd:class">
    <pre>
      <xsl:apply-templates select="jd:inherit" mode="tree" />
    </pre>
    <xsl:if test="jd:implements/descendant::jd:interface">
      <dl>
        <dt>
          <b>All Implemented Interfaces:</b>
        </dt>
        <dd>
          <xsl:apply-templates select="jd:implements" mode="implemented_interfaces" />
        </dd>
      </dl>
    </xsl:if>
    <xsl:apply-templates select="jd:nested-in" />
    <hr />
    
    <dl>
      <dt>
        <xsl:for-each select="jd:modifiers/*">
          <xsl:value-of select="concat(local-name(), ' ')" />
        </xsl:for-each>
        <xsl:text>class </xsl:text>
        <b><xsl:value-of select="@name" /></b>
      </dt>
      <dt>
        <xsl:text>extends </xsl:text>
        <a href="{jd:inherit/@qname}">
          <xsl:value-of select="jd:inherit/@name" />
        </a>
      </dt>
      <xsl:if test="jd:implements/*">
        <dt>
          <xsl:text>implements </xsl:text>
          <xsl:for-each select="jd:implements/jd:interface">
            <a href="{@qname}">
              <xsl:value-of select="@name" />
            </a>
            <xsl:if test="position() != last()">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>
        </dt>
      </xsl:if>
    </dl>
    <p>
      <xsl:apply-templates select="jd:comment" mode="detail" />
    </p>
    <p>
      <xsl:apply-templates select="jd:tags" />
    </p>
    <hr />
    
    <xsl:apply-templates select="jd:innerclasses" mode="summary" />
    <xsl:apply-templates select="jd:fields"       mode="summary" />
    <xsl:apply-templates select="jd:constructors" mode="summary" />
    <xsl:apply-templates select="jd:methods"      mode="summary" />
    
    <p />
    
    <xsl:apply-templates select="jd:fields[jd:field]"             mode="detail" />
    <xsl:apply-templates select="jd:constructors[jd:constructor]" mode="detail" />
    <xsl:apply-templates select="jd:methods[jd:method]"           mode="detail" />
  </xsl:template>
  
  
  <xsl:template match="/jd:interface">
    <pre>
      <xsl:apply-templates select="jd:inherit" mode="tree" />
    </pre>
    <xsl:if test="jd:implements">
      <dl>
        <dt>
          <b>All Superinterfaces:</b>
        </dt>
        <dd>
          <xsl:for-each select="jd:implements//jd:interface">
            <a href="{@qname}">
              <xsl:value-of select="@name" />
            </a>
            <xsl:if test="position() != last()">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>
        </dd>
      </dl>
    </xsl:if>
    <xsl:apply-templates select="jd:nested-in" />
    <hr />
    
    <dl>
      <dt>
        <xsl:for-each select="jd:modifiers/*">
          <xsl:value-of select="concat(local-name(), ' ')" />
        </xsl:for-each>
        <xsl:text>interface </xsl:text>
        <b>
          <xsl:if test="jd:nested-in">
            <xsl:value-of select="concat(jd:nested-in/*/@name, '.')" />
          </xsl:if>
          <xsl:value-of select="@name" />
        </b>
      </dt>
      <xsl:if test="jd:implements/*">
        <dt>
          <xsl:text>extends </xsl:text>
          <xsl:for-each select="jd:implements/jd:interface">
            <a href="{@qname}">
              <xsl:value-of select="@name" />
            </a>
            <xsl:if test="position() != last()">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>
        </dt>
      </xsl:if>
    </dl>
    <p>
      <xsl:apply-templates select="jd:comment" mode="detail" />
    </p>
    <p>
      <xsl:apply-templates select="jd:tags" />
    </p>
    <hr />
    
    <xsl:apply-templates select="jd:innerclasses" mode="summary" />
    <xsl:apply-templates select="jd:fields"       mode="summary" />
    <xsl:apply-templates select="jd:methods"      mode="summary" />
    
    <p />
    
    <xsl:apply-templates select="jd:fields[jd:field]"   mode="detail" />
    <xsl:apply-templates select="jd:methods[jd:method]" mode="detail" />
  </xsl:template>
  
  
  <xsl:template match="jd:inherit" mode="tree">
    <xsl:for-each select="descendant-or-self::jd:inherit">
      <xsl:sort select="position()" data-type="number" order="descending" />
      
      <xsl:variable name="spaces">
        <xsl:call-template name="repeat">
          <xsl:with-param name="cnt" select="count(descendant::jd:inherit) * 6 - 2" />
          <xsl:with-param name="text" select="' '" />
        </xsl:call-template>
      </xsl:variable>
      
      <xsl:if test="position() != 1">
        <xsl:value-of select="concat($spaces, '|&#xA;', $spaces, '+-')" />
      </xsl:if>
      <a href="{@qname}"><xsl:value-of select="@qname" /></a>
      <xsl:text>&#xA;</xsl:text>
    </xsl:for-each>
    
    <xsl:variable name="spaces">
      <xsl:call-template name="repeat">
        <xsl:with-param name="cnt" select="count(descendant::jd:inherit) * 6 + 4" />
        <xsl:with-param name="text" select="' '" />
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:value-of select="concat($spaces, '|&#xA;', $spaces, '+-')" />
    <b><xsl:value-of select="/jd:class/@qname" /></b>
  </xsl:template>
  
  
  <xsl:template match="jd:implements" mode="implemented_interfaces">
    <xsl:for-each select="//jd:interface">
      <xsl:sort select="@name" data-type="text" />
      <xsl:if test="1=1"> <!-- TODO: refine condition ;-) -->
        <xsl:if test="position() != 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <a href="{@qname}">
          <xsl:value-of select="@name" />
        </a>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  
  <xsl:template match="jd:tags">
    <dl>
      <xsl:if test="jd:since">
        <dt>
          <b>Since:</b>
        </dt>
        <dd><xsl:value-of select="jd:since" /></dd>
      </xsl:if>
      <xsl:if test="jd:version">
        <dt>
          <b>Version:</b>
        </dt>
        <dd><xsl:value-of select="jd:version" disable-output-escaping="yes" /></dd>
      </xsl:if>
      <xsl:if test="jd:author">
        <dt>
          <b>Author:</b>
        </dt>
        <dd>
          <xsl:for-each select="jd:author">
            <xsl:value-of select="text()" disable-output-escaping="yes" />
            <xsl:if test="position() != last()">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>
        </dd>
      </xsl:if>
      <xsl:if test="jd:see">
        <dt>
          <b>See Also:</b>
        </dt>
        <dd>
          <xsl:for-each select="jd:see">
            <xsl:apply-templates select="jd:link" mode="comment" />
            <xsl:if test="position() != last()">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>
        </dd>
      </xsl:if>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="jd:innerclasses" mode="summary">
    <xsl:comment> =========== NESTED CLASS SUMMARY =========== </xsl:comment>
    <a name="nested_class_summary" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#CCCCFF" class="TableHeadingColor">
        <td colspan="2">
          <font size="+2">
            <b>Nested Class Summary</b>
          </font>
        </td>
      </tr>
      <xsl:for-each select="jd:class | jd:interface">
        <xsl:sort select="@name" />
        <tr bgcolor="white" class="TableRowColor">
          <td align="right" valign="top" width="1%">
            <font size="-1">
              <code>
                <xsl:text>&nbsp;</xsl:text>
                <xsl:for-each select="jd:modifiers/*">
                  <xsl:value-of select="local-name()" />
                  <xsl:text> </xsl:text>
                </xsl:for-each>
                <xsl:value-of select="local-name()" />
              </code>
            </font>
          </td>
          <td>
            <code>
              <b>
                <a href="{concat(/*/@qname, '.', @name)}">
                  <xsl:value-of select="concat(/*/@name, '.', @name)" />
                </a>
              </b>
            </code>
            <br />
            <xsl:apply-templates select="jd:comment" mode="summary" />
          </td>
        </tr>
      </xsl:for-each>
    </table>
    <xsl:text>&nbsp;</xsl:text>
    <xsl:apply-templates select="descendant::jd:inherit" mode="innerclass-summary" />
  </xsl:template>
  
  
  <xsl:template match="jd:inherit" mode="innerclass-summary">
    <a name="{concat('nested_classes_inherited_from_class_', @qname)}" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#EEEEFF" class="TableSubHeadingColor">
        <td>
          <b>
            <xsl:value-of select="concat('Nested classes inherited from ', local-name(/*), ' ', @package, '.')" />
            <a href="{@qname}"><xsl:value-of select="@name" /></a>
          </b>
        </td>
      </tr>
      <tr bgcolor="white" class="TableRowColor">
        <td>
          <code>
            <xsl:for-each select="*">
              <xsl:sort select="@name" data-type="text" />
              <a href="{concat(parent::*/@qname, '.', @name)}">
                <xsl:value-of select="concat(parent::*/@name, '.', @name)" />
              </a>
              <xsl:if test="position() != last()">
                <xsl:text>, </xsl:text>
              </xsl:if>
            </xsl:for-each>
          </code>
        </td>
      </tr>
    </table>
    <xsl:text>&nbsp;</xsl:text>
  </xsl:template>
  
  
  <xsl:template match="jd:fields" mode="summary">
    <xsl:comment> =========== FIELD SUMMARY =========== </xsl:comment>
    <a name="field_summary" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#CCCCFF" class="TableHeadingColor">
        <td colspan="2">
          <font size="+2">
            <b>Field Summary</b>
          </font>
        </td>
      </tr>
      <xsl:for-each select="jd:field">
        <xsl:sort select="@name" data-type="text" />
        <tr bgcolor="white" class="TableRowColor">
          <td align="right" valign="top" width="1%">
            <font size="-1">
              <code>
                <xsl:text>&nbsp;</xsl:text>
                <xsl:for-each select="jd:modifiers/*">
                  <xsl:value-of select="local-name()" />
                  <xsl:text> </xsl:text>
                </xsl:for-each>
                <xsl:call-template name="output-type">
                  <xsl:with-param name="type" select="current()" />
                </xsl:call-template>
              </code>
            </font>
          </td>
          <td>
            <code>
              <b>
                <a href="{concat('#', @name)}"><xsl:value-of select="@name" /></a>
              </b>
            </code>
            <br />
            <xsl:apply-templates select="jd:comment" mode="summary" />
          </td>
        </tr>
      </xsl:for-each>
    </table>
    <xsl:text>&nbsp;</xsl:text>
    <xsl:apply-templates select="descendant::jd:inherit" mode="field-summary" />
  </xsl:template>

  
  <xsl:template match="jd:inherit" mode="field-summary">
    <a name="{concat('fields_inherited_from_class_', @qname)}" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#EEEEFF" class="TableSubHeadingColor">
        <td>
          <b>
            <xsl:value-of select="concat('Fields inherited from ', local-name(/*), ' ', @package, '.')" />
            <a href="{@qname}"><xsl:value-of select="@name" /></a>
          </b>
        </td>
      </tr>
      <tr bgcolor="white" class="TableRowColor">
        <td>
          <code>
            <xsl:for-each select="jd:field">
              <xsl:sort select="@name" data-type="text" />
              <a href="{concat(parent::*/@qname, '#', @name)}"><xsl:value-of select="@name" /></a>
              <xsl:if test="position() != last()">
                <xsl:text>, </xsl:text>
              </xsl:if>
            </xsl:for-each>
          </code>
        </td>
      </tr>
    </table>
    <xsl:text>&nbsp;</xsl:text>
  </xsl:template>
  
  
  <xsl:template match="jd:fields" mode="detail">
    <xsl:comment> =========== FIELD DETAIL =========== </xsl:comment>
    <a name="field_detail" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#CCCCFF" class="TableHeadingColor">
        <td>
          <font size="+2">
            <b>Field Detail</b>
          </font>
        </td>
      </tr>
    </table>
    <xsl:for-each select="jd:field">
      <xsl:sort select="@name" data-type="text" />
      <a name="{@name}" />
      <h3><xsl:value-of select="@name" /></h3>
      <pre>
        <xsl:for-each select="jd:modifiers/*">
          <xsl:value-of select="local-name()" />
          <xsl:text> </xsl:text>
        </xsl:for-each>
        <xsl:call-template name="output-type">
          <xsl:with-param name="type" select="current()" />
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <b><xsl:value-of select="@name" /></b>
      </pre>
      <dl>
        <xsl:if test="jd:comment">
          <dd>
            <xsl:apply-templates select="jd:comment" mode="detail" />
          </dd>
        </xsl:if>
      </dl>
      <xsl:if test="position() != last()">
        <hr />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  
  <xsl:template match="jd:constructors" mode="summary">
    <xsl:comment> =========== CONSTRUCTOR SUMMARY =========== </xsl:comment>
    <a name="constructor_summary" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#CCCCFF" class="TableHeadingColor">
        <td colspan="2">
          <font size="+2">
            <b>Constructor Summary</b>
          </font>
        </td>
      </tr>
      <xsl:for-each select="jd:constructor">
        <xsl:sort select="jd:parameters/jd:parameter[1]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[2]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[3]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[4]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[5]/@name" data-type="text" />
        <tr bgcolor="white" class="TableRowColor">
          <td align="right" valign="top" width="1%">
            <font size="-1">
              <code>
                <xsl:text>&nbsp;</xsl:text>
                <xsl:for-each select="jd:modifiers/*">
                  <xsl:value-of select="local-name()" />
                  <xsl:if test="position() != last()">
                    <xsl:text> </xsl:text>
                  </xsl:if>
                </xsl:for-each>
              </code>
            </font>
          </td>
          <td>
            <code>
              <b>
                <a href="{concat('#', @signature)}"><xsl:value-of select="@name" /></a>
              </b>
              <!-- Parameters: -->
              <xsl:text>(</xsl:text>
              <xsl:for-each select="jd:parameters/jd:parameter">
                <xsl:call-template name="output-type">
                  <xsl:with-param name="type" select="current()" />
                </xsl:call-template>
                <xsl:value-of select="concat('&nbsp;', @name)" />
                <xsl:if test="position() != last()">
                  <xsl:text>, </xsl:text>
                </xsl:if>
              </xsl:for-each>
              <xsl:text>)</xsl:text>
            </code>
            <br />
            <xsl:apply-templates select="jd:comment" mode="summary" />
          </td>
        </tr>
      </xsl:for-each>
    </table>
    <xsl:text>&nbsp;</xsl:text>
  </xsl:template>
  
  
  <xsl:template match="jd:constructors" mode="detail">
    <xsl:comment> =========== CONSTRUCTOR DETAIL =========== </xsl:comment>
    <a name="constructor_detail" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#CCCCFF" class="TableHeadingColor">
        <td>
          <font size="+2">
            <b>Constructor Detail</b>
          </font>
        </td>
      </tr>
    </table>
    <xsl:for-each select="jd:constructor">
      <xsl:sort select="jd:parameters/jd:parameter[1]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[2]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[3]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[4]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[5]/@name" data-type="text" />
      
      <a name="{@signature}" />
      <h3><xsl:value-of select="@name" /></h3>
      <xsl:variable name="mod">
        <xsl:for-each select="jd:modifiers/*">
          <xsl:value-of select="local-name()" />
          <xsl:text> </xsl:text>
        </xsl:for-each>
      </xsl:variable>
      <xsl:variable name="spaces">
        <xsl:call-template name="repeat">
          <xsl:with-param name="text" select="' '" />
          <xsl:with-param name="cnt" select="string-length($mod) + string-length(@name) + 1" />
        </xsl:call-template>
      </xsl:variable>
      <pre>
        <xsl:value-of select="$mod" />
        <b><xsl:value-of select="@name" /></b>
        <xsl:text>(</xsl:text>
        <xsl:for-each select="jd:parameters/jd:parameter">
          <xsl:call-template name="output-type">
            <xsl:with-param name="type" select="current()" />
          </xsl:call-template>
          <xsl:value-of select="concat('&nbsp;', @name)" />
          <xsl:if test="position() != last()">
            <xsl:value-of select="concat(',&#xA;', $spaces)" />
          </xsl:if>
        </xsl:for-each>
        <xsl:text>)</xsl:text>
        <xsl:if test="jd:throws/jd:exception">
          <xsl:text>&#xA;</xsl:text>
          <xsl:call-template name="repeat">
            <xsl:with-param name="text" select="' '" />
            <xsl:with-param name="cnt" select="string-length($mod) + string-length(@name) - 6" />
          </xsl:call-template>
          <xsl:text>throws </xsl:text>
          <xsl:for-each select="jd:throws/jd:exception">
            <a href="{@name}">
              <xsl:call-template name="class-part">
                <xsl:with-param name="classname" select="@name" />
              </xsl:call-template>
            </a>
            <xsl:if test="position() != last()">
              <xsl:value-of select="concat(',&#xA;', $spaces)" />
            </xsl:if>
          </xsl:for-each>
        </xsl:if>
      </pre>
      <dl>
        <xsl:if test="jd:comment">
          <dd>
            <xsl:apply-templates select="jd:comment" mode="detail" />
          </dd>
        </xsl:if>
        <p />
        <xsl:apply-templates select="jd:parameters" mode="list_comment" />
        <xsl:if test="jd:throws/jd:exception">
          <dt>
            <b>Throws:</b>
          </dt>
          <xsl:apply-templates select="jd:throws/jd:exception" mode="list_comment" />
        </xsl:if>
      </dl>
      <xsl:if test="position() != last()">
        <hr />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  
  <xsl:template match="jd:methods" mode="summary">
    <xsl:comment> =========== METHOD SUMMARY =========== </xsl:comment>
    <a name="method_summary" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#CCCCFF" class="TableHeadingColor">
        <td colspan="2">
          <font size="+2">
            <b>Method Summary</b>
          </font>
        </td>
      </tr>
      <xsl:apply-templates select="jd:method" mode="summary">
        <xsl:sort select="@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[1]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[2]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[3]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[4]/@name" data-type="text" />
        <xsl:sort select="jd:parameters/jd:parameter[5]/@name" data-type="text" />
      </xsl:apply-templates>
    </table>
    <xsl:text>&nbsp;</xsl:text>
    <xsl:apply-templates select="descendant::jd:inherit[@type=local-name(/*)]" mode="method-summary" />
  </xsl:template>
  
  
  <xsl:template match="jd:method" mode="summary">
    <tr bgcolor="white" class="TableRowColor">
      <td align="right" valign="top" width="1%">
        <font size="-1">
          <code>
            <xsl:text>&nbsp;</xsl:text>
            <xsl:for-each select="jd:modifiers/*">
              <xsl:value-of select="local-name()" />
              <xsl:text> </xsl:text>
            </xsl:for-each>
            <xsl:call-template name="output-type">
              <xsl:with-param name="type" select="current()" />
            </xsl:call-template>
          </code>
        </font>
      </td>
      <td>
        <code>
          <b>
            <a href="{concat('#', @signature)}"><xsl:value-of select="@name" /></a>
          </b>
          <!-- Parameters: -->
          <xsl:text>(</xsl:text>
          <xsl:for-each select="jd:parameters/jd:parameter">
            <xsl:call-template name="output-type">
              <xsl:with-param name="type" select="current()" />
            </xsl:call-template>
            <xsl:value-of select="concat('&nbsp;', @name)" />
            <xsl:if test="position() != last()">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>
          <xsl:text>)</xsl:text>
        </code>
        <br />
        <xsl:apply-templates select="jd:comment" mode="summary" />
      </td>
    </tr>
  </xsl:template>
  
  
  <xsl:template match="jd:inherit" mode="method-summary">
    <xsl:variable name="signatures" select="ancestor::*[local-name()='inherit' or local-name()='methods']/jd:method/@signature" />
    <xsl:if test="jd:method[not(@signature = $signatures)]">
      <a name="{concat('methods_inherited_from_class_', @qname)}" />
      <table border="1" cellpadding="3" cellspacing="0" width="100%">
        <tr bgcolor="#EEEEFF" class="TableSubHeadingColor">
          <td>
            <b>
              <xsl:value-of select="concat('Methods inherited from ', local-name(/*), ' ', @package, '.')" />
              <a href="{@qname}"><xsl:value-of select="@name" /></a>
            </b>
          </td>
        </tr>
        <tr bgcolor="white" class="TableRowColor">
          <td>
            <code>
              <xsl:for-each select="jd:method[not(@signature = $signatures)]">
                <xsl:sort select="@name" data-type="text" />
                <a href="{concat(parent::*/@qname, '#', @signature)}"><xsl:value-of select="@name" /></a>
                <xsl:if test="position() != last()">
                  <xsl:text>, </xsl:text>
                </xsl:if>
              </xsl:for-each>
            </code>
          </td>
        </tr>
      </table>
      <xsl:text>&nbsp;</xsl:text>
    </xsl:if>
  </xsl:template>
  
  
  <xsl:template match="jd:methods" mode="detail">
    <xsl:comment> =========== METHOD DETAIL =========== </xsl:comment>
    <a name="method_detail" />
    <table border="1" cellpadding="3" cellspacing="0" width="100%">
      <tr bgcolor="#CCCCFF" class="TableHeadingColor">
        <td>
          <font size="+2">
            <b>Method Detail</b>
          </font>
        </td>
      </tr>
    </table>
    <xsl:for-each select="jd:method">
      <xsl:sort select="@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[1]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[2]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[3]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[4]/@name" data-type="text" />
      <xsl:sort select="jd:parameters/jd:parameter[5]/@name" data-type="text" />
      
      <a name="{@signature}" />
      <h3><xsl:value-of select="@name" /></h3>
      <xsl:variable name="mod-type">
        <xsl:for-each select="jd:modifiers/*">
          <xsl:value-of select="local-name()" />
          <xsl:text> </xsl:text>
        </xsl:for-each>
        <xsl:call-template name="output-type">
          <xsl:with-param name="type" select="current()" />
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="spaces">
        <xsl:call-template name="repeat">
          <xsl:with-param name="text" select="' '" />
          <xsl:with-param name="cnt" select="string-length($mod-type) + string-length(@name) + 2" />
        </xsl:call-template>
      </xsl:variable>
      <pre>
        <xsl:value-of select="$mod-type" />
        <xsl:text> </xsl:text>
        <b><xsl:value-of select="@name" /></b>
        <xsl:text>(</xsl:text>
        <xsl:for-each select="jd:parameters/jd:parameter">
          <xsl:call-template name="output-type">
            <xsl:with-param name="type" select="current()" />
          </xsl:call-template>
          <xsl:value-of select="concat('&nbsp;', @name)" />
          <xsl:if test="position() != last()">
            <xsl:value-of select="concat(',&#xA;', $spaces)" />
          </xsl:if>
        </xsl:for-each>
        <xsl:text>)</xsl:text>
        <xsl:if test="jd:throws/jd:exception">
          <xsl:text>&#xA;</xsl:text>
          <xsl:call-template name="repeat">
            <xsl:with-param name="text" select="' '" />
            <xsl:with-param name="cnt" select="string-length($mod-type) + string-length(@name) - 5" />
          </xsl:call-template>
          <xsl:text>throws </xsl:text>
          <xsl:for-each select="jd:throws/jd:exception">
            <a href="{@name}">
              <xsl:call-template name="class-part">
                <xsl:with-param name="classname" select="@name" />
              </xsl:call-template>
            </a>
            <xsl:if test="position() != last()">
              <xsl:value-of select="concat(',&#xA;', $spaces)" />
            </xsl:if>
          </xsl:for-each>
        </xsl:if>
      </pre>
      <dl>
        <xsl:if test="jd:comment">
          <dd>
            <xsl:apply-templates select="jd:comment" mode="detail" />
          </dd>
        </xsl:if>
        <p />
        <!-- Circumvention for Xalan bug - Xalan does not understand braces in xpath expressions! -->
        <xsl:variable name="m-specifies" select="ancestor::jd:methods/descendant::jd:inherit[@type='interface']/jd:method[@signature = current()/@signature]" />
        <xsl:variable name="specifies" select="$m-specifies[1]" />
        <xsl:if test="$specifies">
          <dt>
            <b>Specified by:</b>
          </dt>
          <dd>
            <a href="{concat($specifies/../@qname, '#', $specifies/@signature)}">
              <xsl:value-of select="$specifies/@name" />
            </a>
            <xsl:text> in interface </xsl:text>
            <a href="{$specifies/../@qname}">
              <xsl:value-of select="$specifies/../@name" />
            </a>
          </dd>
        </xsl:if>
        <!-- Circumvention for Xalan bug - Xalan does not understand braces in xpath expressions! -->
        <xsl:variable name="m-overrides" select="ancestor::jd:methods/descendant::jd:inherit[@type='class']/jd:method[@signature = current()/@signature]" />
        <xsl:variable name="overrides" select="$m-overrides[last()]" />
        <xsl:if test="$overrides">
          <dt>
            <b>Overrides:</b>
          </dt>
          <dd>
            <a href="{concat($overrides/../@qname, '#', $overrides/@signature)}">
              <xsl:value-of select="$overrides/@name" />
            </a>
            <xsl:value-of select="concat(' in ', local-name(/*), ' ')" />
            <a href="{$overrides/../@qname}">
              <xsl:value-of select="$overrides/../@name" />
            </a>
          </dd>
        </xsl:if>
        <xsl:apply-templates select="jd:parameters" mode="list_comment" />
        <xsl:if test="jd:tags/jd:return">
          <dt>
            <b>Returns:</b>
          </dt>
          <dd>
            <xsl:value-of select="jd:tags/jd:return" disable-output-escaping="yes" />
          </dd>
        </xsl:if>
        <xsl:if test="jd:throws/jd:exception">
          <dt>
            <b>Throws:</b>
          </dt>
          <xsl:apply-templates select="jd:throws/jd:exception" mode="list_comment" />
        </xsl:if>
      </dl>
      <xsl:if test="position() != last()">
        <hr />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  
  <xsl:template match="jd:nested-in">
    <dl>
      <dt>
        <b><xsl:value-of select="concat('Enclosing ', local-name(*), ':')" /></b>
      </dt>
      <dd>
        <a href="{concat(*/@package, '.', */@name)}">
          <xsl:value-of select="*/@name" />
        </a>
      </dd>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="jd:comment" mode="summary">
    <xsl:text>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</xsl:text>
    <xsl:variable name="before-dotspace">
      <xsl:choose>
        <xsl:when test="contains(text(), '. ')">
          <xsl:value-of select="concat(substring-before(text(), '. '), '.')" disable-output-escaping="yes" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="text()" disable-output-escaping="yes" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="contains($before-dotspace, '.&lt;')">
        <xsl:value-of select="concat(substring-before($before-dotspace, '.&lt;'), '.')" disable-output-escaping="yes" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$before-dotspace" disable-output-escaping="yes" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <xsl:template match="jd:comment" mode="detail">
    <xsl:apply-templates select="text()|jd:link" mode="comment" />
  </xsl:template>
  
  
  <xsl:template match="text()" mode="comment">
    <xsl:value-of select="." disable-output-escaping="yes" />
  </xsl:template>
  
  
  <xsl:template match="jd:link" mode="comment">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="@class" />
        <xsl:if test="@member">
          <xsl:value-of select="concat('#', @member)" />
        </xsl:if>
      </xsl:attribute>
      <code>
        <xsl:choose>
          <xsl:when test="text()">
            <xsl:value-of select="text()" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@class" />
            <xsl:if test="@member">
              <xsl:value-of select="concat('.', @member)" />
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </code>
    </a>
  </xsl:template>
  
  
  <xsl:template match="jd:parameters" mode="list_comment">
    <dt>
      <b>Parameters:</b>
    </dt>
    <xsl:for-each select="jd:parameter">
      <dd>
        <code><xsl:value-of select="@name" /></code>
        <xsl:if test="jd:comment">
          <xsl:text> - </xsl:text>
          <xsl:apply-templates select="jd:comment" mode="detail" />
        </xsl:if>
      </dd>
    </xsl:for-each>
  </xsl:template>


  <xsl:template match="jd:exception" mode="list_comment">
    <dd>
      <a href="{@name}">
        <code>
          <xsl:call-template name="class-part">
            <xsl:with-param name="classname" select="@name" />
          </xsl:call-template>
        </code>
      </a>
      <xsl:text> - </xsl:text>
      <xsl:apply-templates select="jd:comment" mode="detail" />
    </dd>
  </xsl:template>

  
  <xsl:template name="output-type">
    <xsl:param name="type" />
    
    <xsl:choose>
      <xsl:when test="contains($type/@type, '.')">
        <!-- Class type: -->
        <a href="{$type/@type}">
          <xsl:call-template name="class-part">
            <xsl:with-param name="classname" select="$type/@type" />
          </xsl:call-template>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <!-- Primitive type: -->
        <xsl:value-of select="$type/@type" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="repeat">
      <xsl:with-param name="text" select="'[]'" />
      <xsl:with-param name="cnt" select="$type/@dimensions" />
    </xsl:call-template>
  </xsl:template>
  
  
  <xsl:template name="repeat">
    <xsl:param name="cnt" />
    <xsl:param name="text" />
    
    <xsl:if test="$cnt &gt; 0">
      <xsl:value-of select="$text" />
      <xsl:call-template name="repeat">
        <xsl:with-param name="cnt" select="$cnt - 1" />
        <xsl:with-param name="text" select="$text" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  
  <xsl:template name="class-part">
    <xsl:param name="classname" />
    
    <xsl:choose>
      <xsl:when test="contains($classname, '.')">
        <xsl:call-template name="class-part">
          <xsl:with-param name="classname" select="substring-after($classname, '.')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$classname" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>