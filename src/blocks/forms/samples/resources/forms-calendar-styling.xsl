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

  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>

  <xsl:template match="head" mode="forms-calendar">
    <script src="{$resources-uri}/mattkruse-lib/CalendarPopup.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/date.js" type="text/javascript"/>
    <script type="text/javascript">
      // Setup calendar
      var forms_calendar = CalendarPopup('forms_calendarDiv');
      forms_calendar.setWeekStartDay(1);
      forms_calendar.showYearNavigation();
      forms_calendar.showYearNavigationInput();
      forms_calendar.setCssPrefix("forms_");
    </script>
    <link rel="stylesheet" type="text/css" href="{$resources-uri}/forms-calendar.css"/>
  </xsl:template>

  <xsl:template match="body" mode="forms-calendar">
    <div id="forms_calendarDiv"/>
  </xsl:template>

  <!--+
      | fi:field with @type 'date' : use CalendarPopup
      +-->
  <xsl:template match="fi:field[fi:datatype/@type='date']">
    <xsl:variable name="id" select="generate-id()"/>
    
    <!-- FIXME: should use the format used by the convertor -->
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="fi:datatype/fi:convertor/@pattern"><xsl:value-of select="fi:datatype/fi:convertor/@pattern"/></xsl:when>
        <xsl:otherwise>yyyy-MM-dd</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- regular input -->
    <input id="{@id}" name="{@id}" value="{fi:value}" title="{normalize-space(fi:hint)}" type="text">
      <xsl:apply-templates select="." mode="styling"/>
    </input>
    
    <!-- calendar popup -->
    <a href="#" name="{$id}" id="{$id}"
       onClick="forms_calendar.select(forms_getForm(this)['{@id}'],'{$id}','{$format}'); return false;">
      <img src="{$resources-uri}/cal.gif" border="0" alt="Calendar"/>
    </a>

    <!-- common stuff -->
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

</xsl:stylesheet>
