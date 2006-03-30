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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fi="http://apache.org/cocoon/forms/1.0#instance"
                exclude-result-prefixes="fi">
  <!--+
      | This stylesheet is designed to be included by 'forms-advanced-styling.xsl'.
      +-->

  <xsl:param name="htmlarea-lang">en</xsl:param>

  <xsl:template match="head" mode="forms-htmlarea">
    <script type="text/javascript">
      _editor_url = "<xsl:value-of select="concat($resources-uri, '/forms/htmlarea/')"/>";
      _editor_lang = "<xsl:value-of select="$htmlarea-lang"/>";
    </script>
    <script type="text/javascript" src="{$resources-uri}/forms/htmlarea/htmlarea.js"></script>
  </xsl:template>

  <xsl:template match="body" mode="forms-htmlarea"/>

  <!--+
      | fi:field with @type 'htmlarea'
      +-->
  <xsl:template match="fi:field[fi:styling[@type='htmlarea']]">
    <textarea id="{@id}" name="{@id}" title="{fi:hint}">
      <xsl:apply-templates select="." mode="styling"/>
      <!-- remove carriage-returns (occurs on certain versions of IE and doubles linebreaks at each submit) -->
      <xsl:apply-templates select="fi:value/node()" mode="htmlarea-copy"/>
    </textarea>
    <xsl:apply-templates select="." mode="common"/>
    <xsl:choose>
      <xsl:when test="fi:styling/conf">
        <!-- use an 'ad hoc'  configuration -->
        <script  type="text/javascript">
          var handler = new Object();    
          handler.fieldId = "<xsl:value-of select="@id"/>";     
          handler.forms_onload = function() {
            var id = "<xsl:value-of select="@id"/>";
            var textarea = document.getElementById(id);
            var editor = new HTMLArea(id);
            textarea.htmlarea = editor;
            var conf = editor.config;
            <xsl:value-of select="fi:styling/conf/text()"/>
            editor.generate();
          }
          forms_onloadHandlers.push(handler);      
        </script>        
      </xsl:when>
      <!-- use a passed configuration function -->
      <xsl:when test="fi:styling/initFunction and not(fi:styling/conf)">
        <script  type="text/javascript">
          var handler = new Object();    
          handler.fieldId = "<xsl:value-of select="@id"/>";
          if(typeof(<xsl:value-of select="fi:styling/initFunction"/>)!="function") {
            alert("<xsl:value-of select="fi:styling/initFunction"/> is not a function " +
            "or not available! Can't render widget '<xsl:value-of select="@id"/>'");
          }
          handler.forms_onload = <xsl:value-of select="fi:styling/initFunction"/>;
          forms_onloadHandlers.push(handler);   
        </script>
      </xsl:when>    
      <!-- default mode with all buttons available -->  
      <xsl:otherwise>
        <script  type="text/javascript">
          var handler = new Object();    
          handler.fieldId = "<xsl:value-of select="@id"/>";     
          handler.forms_onload = function() {
            HTMLArea.replace('<xsl:value-of select="@id"/>');
          }
          forms_onloadHandlers.push(handler);      
        </script>  
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|*" mode="htmlarea-copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="htmlarea-copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()" mode="htmlarea-copy">
    <xsl:copy-of select="translate(., '&#13;', '')"/>
  </xsl:template>

</xsl:stylesheet>
