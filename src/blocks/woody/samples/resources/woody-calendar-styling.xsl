<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wi="http://apache.org/cocoon/woody/instance/1.0"
                exclude-result-prefixes="wi">
  <!--+
      | This stylesheet is designed to be included by 'woody-advanced-styling.xsl'.
      +-->

  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>

  <xsl:template match="head" mode="woody-calendar">
    <script src="{$resources-uri}/mattkruse-lib/CalendarPopup.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/date.js" type="text/javascript"/>
    <script type="text/javascript">
      // Setup calendar
      var woody_calendar = CalendarPopup('woody_calendarDiv');
      woody_calendar.setWeekStartDay(1);
      woody_calendar.showYearNavigation();
      woody_calendar.showYearNavigationInput();
      woody_calendar.setCssPrefix("woody_");
    </script>
    <link rel="stylesheet" type="text/css" href="{$resources-uri}/woody-calendar.css"/>
  </xsl:template>

  <xsl:template match="body" mode="woody-calendar">
    <div id="woody_calendarDiv"/>
  </xsl:template>

  <!--+
      | wi:field with @type 'date' : use CalendarPopup
      +-->
  <xsl:template match="wi:field[wi:styling/@type='date']">
    <xsl:variable name="id" select="generate-id()"/>
    
    <!-- FIXME: should use the format used by the convertor -->
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="wi:styling/@format"><xsl:value-of select="wi:styling/@format"/></xsl:when>
        <xsl:otherwise>yyyy-MM-dd</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- regular input -->
    <input id="{@id}" name="{@id}" value="{wi:value}" title="{normalize-space(wi:hint)}" type="text">
      <xsl:apply-templates select="." mode="styling"/>
    </input>
    
    <!-- calendar popup -->
    <a href="#" name="{generate-id()}" id="{generate-id()}"
       onClick="woody_calendar.select(woody_getForm(this)['{@id}'],'{generate-id()}','{$format}'); return false;">
      <img src="{$resources-uri}/cal.gif" border="0" alt="Calendar"/>
    </a>

    <!-- common stuff -->
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

</xsl:stylesheet>
