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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
    <html>
      <head>
        <title>
          <xsl:value-of select="title"/>
        </title>
        <META content="0" http-equiv="expires"/>
        <META content="nocache" http-equiv="pragma"/>
      </head>
      <body bgcolor="white" alink="red" link="blue" vlink="blue">
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="title">
    <h2 style="color: navy; text-align: center">
      <xsl:apply-templates/>
    </h2>
  </xsl:template>

  <xsl:template match="para">
    <p align="center">
      <i><xsl:apply-templates/></i>
    </p>
  </xsl:template>

  <xsl:template match="form">
    <form method="POST" action="{@target}">
      <xsl:apply-templates/>
    </form>
  </xsl:template>

  <xsl:template match="input">
    <center>
      <xsl:value-of select="@title"/>
      <input type="{@type}" name="{@name}" value="{.}"/>
    </center><br/>
  </xsl:template>

  <xsl:template match="linkbar">
    <center>
      [
      <a href="login"> login </a>
      |
      <a href="protected"> protected </a>
      |
      <a href="do-logout"> logout </a>
      ]
    </center>
  </xsl:template>

 <xsl:template match="source">
  <div style="background: #b9d3ee; border: thin; border-color: black; border-style: solid; padding-left: 0.8em; 
              padding-right: 0.8em; padding-top: 0px; padding-bottom: 0px; margin: 0.5ex 0px; clear: both;">
  <textarea name="context" cols="80" rows="20" readonly="true">
   <xsl:apply-templates/>
  </textarea>
  </div>
 </xsl:template>

  <xsl:template match="@*|node()" priority="-1" name="copy">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
<!-- vim: set et ts=2 sw=2: -->
