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

<!-- CVS $Id: calendar2html.xslt,v 1.1 2004/04/09 07:23:45 ugo Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:calendar="http://apache.org/cocoon/calendar/1.0">

  <xsl:template match="/">
    <html>
      <head>
        <title>
          Calendar for <xsl:value-of select="calendar:calendar/@month"/>
          <xsl:text> </xsl:text><xsl:value-of select="calendar:calendar/@year"/>
        </title>
        <style>
          <xsl:comment>
.largecalendar {
  margin-top: 20px;
  padding-bottom: 1em;
  background-color: white;
  color: #333;
}

.largecalendar table {
  background-color: #888;
}

.largecalendar table caption {
  font-family: LuciduxSerif, Georgia, "Book Antiqua", Palatino, "Times New Roman", serif;
  font-size: large;
  font-weight: bold;
  font-variant: small-caps;
  padding-top: 0.2em;
  padding-bottom: 0.3em;
  background: #fff;
  color: #333;
  voice-family: "\"}\"";
  voice-family:inherit;
  font-size: x-large;
}

.largecalendar table th {
  font-family: LuciduxSerif, Georgia, "Book Antiqua", Palatino, "Times New Roman", serif;
  font-size: x-small;
  font-variant: small-caps;
  background: #fff;
  color: #333;
  padding-bottom: 2px;
  voice-family: "\"}\"";
  voice-family:inherit;
  font-size: small;
}

.largecalendar .daytitle {
  position: relative;
  left: 0;
  top: 0;
  width: 25%;
  padding: 3px 0 3px 0;
  background: transparent;
  color: #000;
  border-right: 1px solid #888;
  border-bottom: 1px solid #888;
  font-size: x-small;
  font-family: Verdana, sans-serif;
  text-align: center;
  voice-family: "\"}\"";
  voice-family:inherit;
  font-size: small;
}

td {
  vertical-align: top;
  margin: 0;
  padding: 0 5px 5px 0;
  height: 7em;
  width: 12%;
  background: #fff;
  color: #333;
}

.largecalendar ul {
  list-style: none;
  margin: 0;
  padding: 0 3px 0 3px;
}

.largecalendar li {
  display: block;
  text-align: center;
  font-size: xx-small;
  font-family: Verdana, sans-serif;
  padding-top: 6px;
  voice-family: "\"}\"";
  voice-family:inherit;
  font-size: x-small;
}
html>body .largecalendar li {
  margin-top: 6px;
}

.largecalendar li.first {
  padding-top: 0;
  background: transparent;
}
          </xsl:comment>
        </style>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="calendar:calendar">
    <div class="largecalendar">
      <table cellpadding="0" cellspacing="1" summary="Monthly calendar">
        <caption><xsl:value-of select="@month"/>
          <xsl:text> </xsl:text><xsl:value-of select="@year"/>
        </caption>
        <thead>
          <tr>
            <th class="mon">Monday</th>
            <th class="tue">Tuesday</th>
            <th class="wed">Wednesday</th>
            <th class="thu">Thursday</th>
            <th class="fri">Friday</th>
            <th class="sat">Saturday</th>
            <th class="sun">Sunday</th>
          </tr>
        </thead>
        <tbody>
          <xsl:apply-templates select="calendar:week"/>
        </tbody>
      </table>
    </div>
  </xsl:template>
  
  <xsl:template match="calendar:week">
    <xsl:variable name="start" select="7 - count(../calendar:week[1]/calendar:day)"/>
    <tr>
      <xsl:if test="position() = 1">
        <xsl:choose>
          <xsl:when test="count(calendar:day) = 1">
            <td class="d1"/>
            <td class="d2"/>
            <td class="d3"/>
            <td class="d4"/>
            <td class="d5"/>
            <td class="d6"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 2">
            <td class="d1"/>
            <td class="d2"/>
            <td class="d3"/>
            <td class="d4"/>
            <td class="d5"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 3">
            <td class="d1"/>
            <td class="d2"/>
            <td class="d3"/>
            <td class="d4"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 4">
            <td class="d1"/>
            <td class="d2"/>
            <td class="d3"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 5">
            <td class="d1"/>
            <td class="d2"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 6">
            <td class="d1"/>
          </xsl:when>
        </xsl:choose>
      </xsl:if>
      <xsl:for-each select="calendar:day">
        <td class="d{@number + $start}">
          <div class="daytitle"><xsl:value-of select="@number"/></div>
          <ul>
            <li class="first"><xsl:value-of select="@date"/></li>
          </ul>
        </td>
      </xsl:for-each>
      <xsl:variable name="last" select="count(../calendar:week/calendar:day) + $start"/>
      <xsl:if test="position() = last()">
        <xsl:choose>
          <xsl:when test="count(calendar:day) = 1">
            <td class="d{$last + 1}"/>
            <td class="d{$last + 2}"/>
            <td class="d{$last + 3}"/>
            <td class="d{$last + 4}"/>
            <td class="d{$last + 5}"/>
            <td class="d{$last + 6}"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 2">
            <td class="d{$last + 1}"/>
            <td class="d{$last + 2}"/>
            <td class="d{$last + 3}"/>
            <td class="d{$last + 4}"/>
            <td class="d{$last + 5}"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 3">
            <td class="d{$last + 1}"/>
            <td class="d{$last + 2}"/>
            <td class="d{$last + 3}"/>
            <td class="d{$last + 4}"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 4">
            <td class="d{$last + 1}"/>
            <td class="d{$last + 2}"/>
            <td class="d{$last + 3}"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 5">
            <td class="d{$last + 1}"/>
            <td class="d{$last + 2}"/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 6">
            <td class="d{$last + 1}"/>
          </xsl:when>
        </xsl:choose>
      </xsl:if>
    </tr>
  </xsl:template>

</xsl:stylesheet>