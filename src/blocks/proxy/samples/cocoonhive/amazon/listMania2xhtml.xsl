<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:output  method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"  doctype-system="DTD/xhtml1-strict.dtd"/>

  <xsl:template match="/ProductInfo"> 
       <table class="content table">
         <xsl:if test="Details">
           <xsl:call-template name="bookRow">
             <xsl:with-param name="bookDetailsSet" select="Details"/>
           </xsl:call-template>                               
         </xsl:if>
         <xsl:apply-templates select="ErrorMsg"/>
       </table>
  </xsl:template>


  <!-- render multiple rows, each listing 3 books -->  
  <xsl:template name="bookRow">
    <xsl:param name="bookDetailsSet"/>
    <xsl:if test="$bookDetailsSet">
       <tr>
         <xsl:for-each select="$bookDetailsSet[position() &lt; 4]">
             <xsl:call-template name="bookInfo"/>
         </xsl:for-each>
       </tr>
       <xsl:call-template name="bookRow">
         <xsl:with-param name="bookDetailsSet" select="$bookDetailsSet[position() &gt; 3]"/>
       </xsl:call-template>
     </xsl:if>
  </xsl:template>



  <xsl:template name="bookInfo">
     <td>
       <xsl:element name="a">
         <xsl:attribute name="href">page-amazon-bookInfo?AsinSearch=<xsl:value-of select="Asin"/></xsl:attribute>
         <xsl:element name="img">
           <xsl:attribute name="src"><xsl:value-of   select="ImageUrlSmall"/></xsl:attribute>
           <xsl:attribute name="alt">cover art</xsl:attribute>
         </xsl:element>
       </xsl:element>
      </td>
      <TD>
       <xsl:element name="a">
         <xsl:attribute name="href">page-amazon-bookInfo?AsinSearch=<xsl:value-of select="Asin"/></xsl:attribute>
          <FONT face="Arial" size="-2" color="3366FF">
            <xsl:value-of select="ProductName"/>
          </FONT>
          <br/>
       </xsl:element>
        <FONT face="Arial" size="-2" color="000000">
          <xsl:value-of select="Manufacturer"/>
        </FONT>
        <br/>
        <font face="Arial" size="-2">
          <font color="990000">New <xsl:value-of select="OurPrice"/></font>
        </font>
      </TD>
  
  </xsl:template>

  <xsl:template match="ErrorMsg">
    <div title="Error Message">
    <xsl:value-of select="."/>
    </div>
  </xsl:template>

</xsl:stylesheet>

