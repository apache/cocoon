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

<!-- CVS $Id: calendar2html.xslt,v 1.3 2004/04/09 14:31:48 ugo Exp $ -->

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
.calendar {
  font-family: Georgia, "Book Antiqua", Palatino, "Times New Roman", serif;
  margin-top: 20px;
  padding-bottom: 1em;
  background-color: white;
  color: #333;
}

.calendar table {
  background-color: #888;
}

.calendar table caption {
  font-size: x-large;
  font-weight: bold;
  font-variant: small-caps;
  padding-top: 0.2em;
  padding-bottom: 0.3em;
  background: #fff;
  color: #333;
}

.calendar table th {
  font-size: small;
  font-variant: small-caps;
  background: #fff;
  color: #333;
  padding-bottom: 2px;
}

.calendar .daytitle {
  position: relative;
  left: 0;
  top: 0;
  width: 25%;
  padding: 3px 0;
  color: #000;
  border-right: 1px solid #888;
  border-bottom: 1px solid #888;
  font-size: small;
  text-align: center;
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

.calendar p {
  text-align: center;
  font-size: small;
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
    <div class="calendar">
      <table cellpadding="0" cellspacing="1" summary="Monthly calendar">
        <caption><xsl:value-of select="@month"/>
          <xsl:text> </xsl:text><xsl:value-of select="@year"/>
        </caption>
        <thead>
          <tr>
            <th>Sunday</th>
            <th>Monday</th>
            <th>Tuesday</th>
            <th>Wednesday</th>
            <th>Thursday</th>
            <th>Friday</th>
            <th>Saturday</th>
          </tr>
        </thead>
        <tbody>
          <xsl:apply-templates select="calendar:week"/>
        </tbody>
      </table>
    </div>
  </xsl:template>
  
  <xsl:template match="calendar:week">
    <tr>
      <xsl:if test="position() = 1">
        <xsl:choose>
          <xsl:when test="count(calendar:day) = 1">
            <td/><td/><td/><td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 2">
            <td/><td/><td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 3">
            <td/><td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 4">
            <td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 5">
            <td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 6">
            <td/>
          </xsl:when>
        </xsl:choose>
      </xsl:if>
      <xsl:for-each select="calendar:day">
        <td>
          <div class="daytitle"><xsl:value-of select="@number"/></div>
          <p><xsl:value-of select="@date"/></p>
        </td>
      </xsl:for-each>
      <xsl:if test="position() = last()">
        <xsl:choose>
          <xsl:when test="count(calendar:day) = 1">
            <td/><td/><td/><td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 2">
            <td/><td/><td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 3">
            <td/><td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 4">
            <td/><td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 5">
            <td/><td/>
          </xsl:when>
          <xsl:when test="count(calendar:day) = 6">
            <td/>
          </xsl:when>
        </xsl:choose>
      </xsl:if>
    </tr>
  </xsl:template>

</xsl:stylesheet>