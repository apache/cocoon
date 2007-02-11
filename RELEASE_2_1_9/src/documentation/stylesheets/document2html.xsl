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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <!-- ====================================================================== -->
  <!-- document section -->
  <!-- ====================================================================== -->
  <xsl:template match="/document">
    <document>
      <title>
        <xsl:choose>
          <xsl:when test="normalize-space(header/title)">
            <xsl:value-of select="header/title"/>
          </xsl:when>
          <xsl:otherwise>Cocoon - No Title</xsl:otherwise>
        </xsl:choose>
      </title>
      <body>
        <xsl:apply-templates/>
      </body>
    </document>
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- header section -->
  <!-- ====================================================================== -->
  <xsl:template match="header">
    <!-- ignore on general document -->
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- body section -->
  <!-- ====================================================================== -->
  <xsl:template match="s1">
    <font color="#0086b2" size="+2" face="verdana, helvetica, sans serif">
      <xsl:value-of select="@title"/>
    </font>
    <hr size="1" style="color: #0086b2"/>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="s2">
    <font color="#0086b2" size="+1" face="verdana, helvetica, sans serif">
      <b>
        <xsl:value-of select="@title"/>
      </b>
    </font>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="s3">
    <font color="#0086b2" size="+1" face="verdana, helvetica, sans serif">
      <xsl:value-of select="@title"/>
    </font>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="s4">
    <font color="#0086b2" face="verdana, helvetica, sans serif">
      <b>
        <xsl:value-of select="@title"/>
      </b>
    </font>
    <xsl:apply-templates/>
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- footer section -->
  <!-- ====================================================================== -->
  <xsl:template match="footer">
    <!-- ignore on general documents -->
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- paragraph section -->
  <!-- ====================================================================== -->
  <xsl:template match="p">
    <p>
      <font face="verdana,helvetica,sanserif" color="black">
        <xsl:apply-templates/>
      </font>
    </p>
  </xsl:template>
  <xsl:template match="note">
    <p>
      <table width="100%" cellspacing="3" cellpadding="0" border="0">
        <tr>
          <td width="28" valign="top">
            <img src="images/note.gif" width="28" height="29" vspace="0" hspace="0" border="0" alt="Note"/>
          </td>
          <td valign="top">
            <font size="-1" face="verdana,helvetica,sanserif" color="black">
              <i>
                <xsl:apply-templates/>
              </i>
            </font>
          </td>
        </tr>
      </table>
    </p>
  </xsl:template>
  <xsl:template match="source">
    <div align="center">
      <table cellspacing="4" cellpadding="0" border="0">
        <tr>
          <td bgcolor="#0086b2" width="1" height="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="#0086b2" height="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="#0086b2" width="1" height="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
        </tr>
        <tr>
          <td bgcolor="#0086b2" width="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="#ffffff">
            <pre>
              <xsl:apply-templates/>
            </pre>
          </td>
          <td bgcolor="#0086b2" width="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
        </tr>
        <tr>
          <td bgcolor="#0086b2" width="1" height="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="#0086b2" height="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="#0086b2" width="1" height="1">
            <img src="images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>
  <xsl:template match="fixme">
    <!-- ignore on documentation -->
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- list section -->
  <!-- ====================================================================== -->
  <xsl:template match="ul|ol">
    <blockquote>
      <xsl:copy>
        <xsl:apply-templates/>
      </xsl:copy>
    </blockquote>
  </xsl:template>
  <xsl:template match="li">
    <xsl:copy>
      <font face="verdana, helvetica, sans serif">
        <xsl:apply-templates/>
      </font>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="sl">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="dl">
    <blockquote>
      <font face="verdana, helvetica, sans serif">
        <dl>
          <xsl:apply-templates/>
        </dl>
      </font>
    </blockquote>
  </xsl:template>

  <xsl:template match="dt">
    <dt>
      <strong>
        <xsl:apply-templates/>
      </strong>
      <xsl:text> - </xsl:text>
    </dt>
  </xsl:template>
  <xsl:template match="dd">
    <dd>
      <xsl:apply-templates/>
    </dd>
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- table section -->
  <!-- ====================================================================== -->
  <xsl:template match="table">
    <table width="100%" border="0" cellspacing="2" cellpadding="2">
      <caption>
        <font face="verdana,helvetica,sanserif">
          <xsl:value-of select="caption"/>
        </font>
      </caption>
      <xsl:apply-templates/>
    </table>
  </xsl:template>
  <xsl:template match="tr">
    <tr>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>
  <xsl:template match="th">
    <td bgcolor="#039acc" colspan="{@colspan}" rowspan="{@rowspan}" valign="center" align="center">
      <font color="white" size="-1" face="verdana,helvetica,sanserif">
        <b>
          <xsl:apply-templates/>
        </b>
      </font>
    </td>
  </xsl:template>
  <xsl:template match="td">
    <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
      <font color="black" size="-1" face="verdana,helvetica,sanserif">
        <xsl:apply-templates/>
      </font>
    </td>
  </xsl:template>
  <xsl:template match="tn">
    <td bgcolor="#ffffff" colspan="{@colspan}" rowspan="{@rowspan}">&#160;</td>
  </xsl:template>
  <xsl:template match="caption">
    <!-- ignore since already used -->
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- markup section -->
  <!-- ====================================================================== -->
  <xsl:template match="strong">
    <b>
      <xsl:apply-templates/>
    </b>
  </xsl:template>
  <xsl:template match="em">
    <i>
      <xsl:apply-templates/>
    </i>
  </xsl:template>
  <xsl:template match="code">
    <code>
      <font face="courier, monospaced">
        <xsl:apply-templates/>
      </font>
    </code>
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- images section -->
  <!-- ====================================================================== -->
  <xsl:template match="figure">
    <p align="center">
      <xsl:choose>
        <xsl:when test="string(@width) and string(@height)">
          <img src="{@src}" alt="{@alt}" width="{@width}" height="{@height}" border="0" vspace="4" hspace="4"/>
        </xsl:when>
        <xsl:otherwise>
          <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4"/>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>
  <xsl:template match="img">
    <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4" align="right"/>
  </xsl:template>
  <xsl:template match="icon">
    <img src="{@src}" alt="{@alt}" border="0" align="absmiddle"/>
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- links section -->
  <!-- ====================================================================== -->
  <xsl:template match="link">
    <xsl:choose>
      <xsl:when test="starts-with(@href, 'mailto:') and contains(@href, '@')">
        <xsl:variable name="mailto-id" select="substring-before(@href,'@')"/>
        <xsl:variable name="mailto-domain" select="substring-after(@href,'@')"/>
        <a href="{$mailto-id}.at.{$mailto-domain}">
          <xsl:apply-templates/>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{@href}">
          <xsl:apply-templates/>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="connect">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="jump">
    <a href="{@href}#{@anchor}">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  <xsl:template match="fork">
    <a href="{@href}" target="_blank">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  <xsl:template match="anchor">
    <a name="{@id}">
      <xsl:comment>anchor</xsl:comment>
    </a>
  </xsl:template>
  <!-- ====================================================================== -->
  <!-- specials section -->
  <!-- ====================================================================== -->
  <xsl:template match="br">
    <br/>
  </xsl:template>
</xsl:stylesheet>
