<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:wi="http://apache.org/cocoon/woody/instance/1.0">
 
  <xsl:include href="woody-field-layout.xsl"/>
  
  <!-- head and body stuff required to use the calendar popup -->
  <xsl:template match="head">
    <xsl:copy>
      <xsl:apply-templates/>
      <xsl:call-template name="calendar-head">
        <xsl:with-param name="div">WoodyCalendarDiv</xsl:with-param>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="body">
    <xsl:copy>
      <xsl:apply-templates/>
       <div id="WoodyCalendarDiv" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"/>
    </xsl:copy>
  </xsl:template>
  
  <!--
    wi:group : columnized layout of fields, with a surrounding frame if the group has a wi:label
  -->
  <xsl:template match="wi:group">
    <xsl:choose>
      <xsl:when test="wi:label">
        <fieldset>
          <legend><xsl:copy-of select="wi:label/node()"/></legend>
          <xsl:call-template name="group-content"/>
        </fieldset>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="group-content"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="group-content">
    <table border="0">
      <tbody>
        <xsl:apply-templates select="wi:items/*" mode="group-content"/>
      </tbody>
    </table>
  </xsl:template>
  
  <!--
    Default layout : label left and input right
  -->
  <xsl:template match="wi:*" mode="group-content">
    <tr>
      <td valign="top"><xsl:copy-of select="wi:label/node()"/></td>
      <td><xsl:apply-templates select="."/></td>
   </tr>
  </xsl:template>

  <!-- boolean field : checkbox and label on a single line -->
  <xsl:template match="wi:booleanfield" mode="group-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/> <xsl:copy-of select="wi:label/node()"/></td>
    </tr>
  </xsl:template>

  <!-- nested group -->
  <xsl:template match="wi:group" mode="group-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
