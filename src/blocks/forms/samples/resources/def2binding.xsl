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
  Transforms a form definition file into a binding file. This is an attempt at merging
  these two files in only one.
  To use it, simply load the form bindings from a cocoon: pipeline that applies this stylesheet
  on a form definition. Next step will by to rewrite this directly into Woody's binding system.
  
  The binding is created according to the following rules :
  - fb:* attributes on widget definitions lead to the creation of
    - <fb:context> if a @fb:context attribute is found
    - <fb:simple-repeater> if a @fb:parent-path is found on a <fd:repeater>
    - <fb:value> if a @fb:path is found on any fd:* element    
  - if a <fd:binding> is present, its content is copied as is with the @id of the enclosing widget
  
  @author Sylvain Wallez
  @version CVS $Id: def2binding.xsl,v 1.1 2004/03/09 10:34:10 reinhard Exp $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fd="http://apache.org/cocoon/forms/1.0#definition"
  xmlns:fb="http://apache.org/cocoon/forms/1.0#binding">
  
<xsl:template match="fd:*[@fb:context]">
  <fb:context path="{@fb:context}">
    <xsl:for-each select="@*[(local-name(.) != 'context') and (namespace-uri() = 'http://apache.org/cocoon/forms/1.0#binding')]">
      <xsl:attribute name="{local-name(.)}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:for-each>
    <xsl:apply-templates/>
  </fb:context>
</xsl:template>

<xsl:template match="fd:*[@fb:path]">
  <fb:value id="{@id}">
    <xsl:for-each select="@*[namespace-uri() = 'http://apache.org/cocoon/forms/1.0#binding']">
      <xsl:attribute name="{local-name(.)}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:for-each>
    <xsl:apply-templates/>
  </fb:value>
</xsl:template>

<xsl:template match="fd:repeater[@fb:parent-path]">
  <fb:simple-repeater id="{@id}">
    <xsl:for-each select="@*[namespace-uri() = 'http://apache.org/cocoon/forms/1.0#binding']">
      <xsl:attribute name="{local-name(.)}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:for-each>
    <xsl:apply-templates/>
  </fb:simple-repeater>
</xsl:template>

<xsl:template match="fd:*[fd:binding]">
  <!-- copy the binding element -->
  <xsl:variable name="binding" select="fd:binding/fb:*[1]"/>
  <xsl:element name="{local-name($binding)}" namespace="{namespace-uri($binding)}">
    <xsl:copy-of select="$binding/@*"/>
    <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
    <xsl:copy-of select="$binding/node()"/>
    <!-- and recurse in the widgets while in the binding element -->
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<!-- avoid copying text -->
<xsl:template match="text()|@*"/>

</xsl:stylesheet>
