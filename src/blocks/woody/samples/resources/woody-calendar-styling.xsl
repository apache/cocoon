<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wi="http://apache.org/cocoon/woody/instance/1.0"
                exclude-result-prefixes="wi">
  
  <xsl:param name="resources-uri">resources</xsl:param>

  <!-- must be called in <head> to load calendar script and setup the CSS -->
  <xsl:template name="woody-calendar-head">
    <!-- assume these have been loaded by woody-field-styling
      <script src="{$resources-uri}/mattkruse-lib/PopupWindow.js" language="JavaScript" type="text/javascript"/>
      <script src="{$resources-uri}/mattkruse-lib/AnchorPosition.js" language="JavaScript" type="text/javascript"/>
    -->
    <script src="{$resources-uri}/mattkruse-lib/CalendarPopup.js" language="JavaScript" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/date.js" language="JavaScript" type="text/javascript"/>


    <script language="JavaScript" type="text/javascript">
      // Setup calendar
      var woody_calendar = CalendarPopup('woody_calendarDiv');
      woody_calendar.setWeekStartDay(1);
      woody_calendar.showYearNavigation();
      woody_calendar.showYearNavigationInput();
      woody_calendar.setCssPrefix("woody_");
    </script>
  </xsl:template>
  
  <xsl:template name="woody-calendar-css">
    <link rel="stylesheet" type="text/css" href="{$resources-uri}/woody-calendar.css"/>
  </xsl:template>

  <!--+
      | must be called in <body> 
      +-->
  <xsl:template name="woody-calendar-body">
    <div id="woody_calendarDiv" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"/>
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
    <input id="{@id}" name="{@id}" value="{wi:value}">
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="wi:styling/@*[not(name() = 'type' or name() = 'submit-on-change')]"/>
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
