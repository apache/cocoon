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
                xmlns:profile="http://apache.org/cocoon/profiler/1.0">

 <xsl:import href="xml2html.xsl"/>

 <xsl:output indent="yes"/>

 <xsl:param name="key"/>
 <xsl:param name="result"/>
 <xsl:param name="component"/>

 <xsl:template match="/">
  <document>
   <header>
    <tab title="Back" href="profile.html"/>
    <tab title="Overview" href="welcome"/>
    <style href="xml2html.css"/>
    <script href="xml2html.js"/>
   </header>
   <body>
    <xsl:choose>
     <xsl:when test="$component!=''">
      <xsl:apply-templates
          select="profile:profilerinfo/profile:pipeline/profile:result/profile:component[@index=$component]"
          mode="fragment"/>
     </xsl:when>
     <xsl:when test="$result!=''">
      <xsl:apply-templates select="profile:profilerinfo/profile:pipeline/profile:result" mode="result"/>
     </xsl:when>
     <xsl:when test="$key!=''">
      <xsl:apply-templates select="profile:profilerinfo/profile:pipeline" mode="results"/>
     </xsl:when>
     <xsl:otherwise>
      <xsl:apply-templates select="profile:profilerinfo" mode="pipelines"/>
     </xsl:otherwise>
    </xsl:choose>

   </body>
  </document>
 </xsl:template>

 <xsl:template match="profile:profilerinfo" mode="pipelines">

  <row>
   <column title="Overview">

    <table width="100%" cellspacing="0" cellpadding="5" align="left">
     <font size="+0" face="arial,helvetica,sanserif" color="#000000">

      <tr>
       <td align="left"><b>URI</b></td>
       <td align="left"><b>Average(Total time)</b></td>
       <td align="left" colspan="10"><b>Last Results(Total time)</b></td>
      </tr>

      <xsl:for-each select="profile:pipeline">

       <tr bgcolor="#FFFFFF">
        <td>
         <a href="?key={@key}">
          <font face="verdana"><strong><xsl:value-of select="@uri"/></strong></font>
         </a>
        </td>
        <td>
         <a href="?key={@key}">
          <xsl:value-of select="profile:average/@time"/> ms
         </a>
        </td>
        <xsl:for-each select="profile:result">
         <td>
          <a href="?key={../@key}&amp;result={@index}">
           <xsl:value-of select="@time"/> ms
          </a>
         </td>
        </xsl:for-each>
       </tr>

      </xsl:for-each>

     </font>
    </table>

   </column>
  </row>

 </xsl:template>

 <xsl:template match="profile:pipeline" mode="results">

  <row>
   <column title="{@uri}">

    <table width="100%" cellspacing="0" cellpadding="5" align="left">
     <font size="+0" face="arial,helvetica,sanserif" color="#000000">
 
      <tr>
       <td align="left"><b>NN</b></td>
       <td align="left"><b>Components(Role)</b></td>
       <td align="left"><b>Average time</b></td>
       <td align="left"><b>Last time</b></td>
      </tr>

      <xsl:for-each select="profile:average/profile:component">
       <tr>

        <xsl:variable name="pos" select="position()"/>
        <td width="1%">
         <xsl:value-of select="$pos"/>
        </td>
        <td width="10%">
         <xsl:value-of select="@role"/>
         <xsl:if test="@source">
          (<xsl:value-of select="@source"/>)
         </xsl:if>
        </td>

        <xsl:for-each select="../../profile:average/profile:component[position()=$pos]">
         <td>
          <xsl:value-of select="@time"/> ms
         </td>
        </xsl:for-each>
 
        <xsl:for-each select="../../profile:result/profile:component[position()=$pos]">
         <td>
          <a href="?key={../../@key}&amp;result={../@index}&amp;component={@index}">
           <xsl:value-of select="@time"/> ms
          </a>
         </td>
        </xsl:for-each>

       </tr>
      </xsl:for-each>

      <tr>
       <td></td>
       <td><b>Total time</b></td>
       <td><b><xsl:value-of select="profile:result/@time"/> ms</b></td>
       <xsl:for-each select="profile:result">
        <td>
         <b>
          <xsl:value-of select="@time"/> ms
         </b>
        </td>
       </xsl:for-each>
      </tr>

     </font>
    </table>

   </column>
  </row>

 </xsl:template>

 <xsl:template match="profile:result" mode="result">

  <row>
   <column title="{../@uri}">

    <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">

     <tr>
      <td align="left"><b>Components</b></td>
      <td align="left"><b>Total time</b></td>
      <td align="left"><b>Setup time</b></td>
      <td align="left"><b>Processing time</b></td>
     </tr>


     <xsl:for-each select="profile:component">
      <tr>
       <td>
        <a href="?key={../../@key}&amp;result={../@index}&amp;component={@index}">
         <xsl:value-of select="@role"/>
         <xsl:if test="@source">
          (<xsl:value-of select="@source"/>)
         </xsl:if>
        </a>
       </td>
       <td>
        <xsl:value-of select="@time"/> ms
       </td>
       <td>
        <xsl:value-of select="@setup"/> ms
       </td>
       <td>
        <xsl:value-of select="@processing"/> ms
       </td>
      </tr>
     </xsl:for-each>

    </table>

   </column>

  </row>

  <row>

   <column title="Request parameters">

    <table width="100%" cellspacing="0" cellpadding="5" align="center">
     <font size="+0" face="arial,helvetica,sanserif" color="#000000">

      <tr>
       <td align="left"><b>Name</b></td>
       <td align="left"><b>Value</b></td>
      </tr>

      <xsl:for-each select="profile:environmentinfo/profile:request-parameters/profile:parameter">
       <tr>
        <td><xsl:value-of select="@name"/></td>
        <td><xsl:value-of select="@value"/></td>
       </tr>
      </xsl:for-each>

      <tr>
       <td>&#160;</td>
       <td>&#160;</td>
      </tr>

     </font>
    </table>
    
   </column>

  </row>

  <row>

   <column title="Session attributes">

    <table width="100%" cellspacing="0" cellpadding="5" align="center">
     <font size="+0" face="arial,helvetica,sanserif" color="#000000">

      <tr>
       <td align="left"><b>Name</b></td>
       <td align="left"><b>Value</b></td>
      </tr>
  
      <xsl:for-each select="profile:environmentinfo/profile:session-attributes/profile:attribute">
       <tr>
        <td><xsl:value-of select="@name"/></td>
        <td><xsl:value-of select="@value"/></td>
       </tr>
      </xsl:for-each>

      <tr>
       <td>&#160;</td>
       <td>&#160;</td>
      </tr>

     </font>
    </table>

   </column>
  </row>

 </xsl:template>

 <xsl:template match="profile:component" mode="fragment">

  <row>
   <column>
    <xsl:attribute name="title">
     <xsl:value-of select="@role"/>
     <xsl:if test="@source">
      (<xsl:value-of select="@source"/>)
     </xsl:if>
    </xsl:attribute>

    <xsl:choose>
     <xsl:when test="profile:fragment">
      <xsl:for-each select="profile:fragment">
       <xsl:apply-templates mode="xml2html"/>
      </xsl:for-each>
     </xsl:when>
     <xsl:otherwise>
      <b>Fragment not available!</b>
     </xsl:otherwise>
    </xsl:choose>

   </column>
  </row>

 </xsl:template>

</xsl:stylesheet>
