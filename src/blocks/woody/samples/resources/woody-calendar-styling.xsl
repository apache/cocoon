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
    <style type="text/css">
      .woody_cpYearNavigation, .woody_cpMonthNavigation {
        background-color:#C0C0C0;
        text-align:center;
        vertical-align:center;
        text-decoration:none;
        color:#000000;
        font-weight:bold;
      }
      
      .woody_cpDayColumnHeader, .woody_cpYearNavigation, .woody_cpMonthNavigation, .woody_cpCurrentMonthDate, .woody_cpCurrentMonthDateDisabled, .woody_cpOtherMonthDate, .woody_cpOtherMonthDateDisabled, .woody_cpCurrentDate, .woody_cpCurrentDateDisabled, .woody_cpTodayText, .woody_cpTodayTextDisabled, .woody_cpText {
        font-family:arial;
        font-size:8pt;
      }
      
      TD.woody_cpDayColumnHeader {
        text-align:right;
        border:solid thin #C0C0C0;
        border-width:0 0 1 0;
      }
      
      .woody_cpCurrentMonthDate, .woody_cpOtherMonthDate, .woody_cpCurrentDate  {
        text-align:right;
        text-decoration:none;
      }
      
      .woody_cpCurrentMonthDateDisabled, .woody_cpOtherMonthDateDisabled, .woody_cpCurrentDateDisabled {
        color:#D0D0D0;
        text-align:right;
        text-decoration:line-through;
      }
      
      .woody_cpCurrentMonthDate, .woody_cpCurrentDate {
        color:#000000;
      }
      
      .woody_cpOtherMonthDate {
        color:#808080;
      }
      
      TD.woody_cpCurrentDate {
        color:white; background-color: #C0C0C0;
        border-width:1;
        border:solid thin #800000;
      }
      
      TD.woody_cpCurrentDateDisabled {
        border-width:1;
        border:solid thin #FFAAAA;
      }
      
      TD.woody_cpTodayText, TD.woody_cpTodayTextDisabled {
        border:solid thin #C0C0C0;
        border-width:1 0 0 0;
      }
      
      A.woody_cpTodayText, SPAN.woody_cpTodayTextDisabled {
        height:20px;
      }
      
      A.woody_cpTodayText {
        color:black;
      }
      
      .woody_cpTodayTextDisabled {
        color:#D0D0D0;
      }
      
      .woody_cpBorder {
        border:solid thin #808080;
      }
    </style>
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
