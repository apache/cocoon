<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:wi="http://apache.org/cocoon/woody/instance/1.0">
  
  <xsl:include href="woody-field-layout.xsl"/>
  
  <xsl:template name="woody-layout-head">
    <xsl:call-template name="woody-calendar-head">
      <xsl:with-param name="div">WoodyCalendarDiv</xsl:with-param>
    </xsl:call-template>
      
    <script language="JavaScript">
      function showWoodyTab(tabgroup, idx, length, state) {
        for (var i = 0; i &lt; length; i++) {
          var tab = document.getElementById(tabgroup + "_tab_" + i);
          tab.className = (i == idx) ? 'woody-tab woody-activeTab': 'woody-tab';
           
          var tabitems = document.getElementById(tabgroup + "_items_" + i);
          tabitems.style.display = (i == idx) ? '' : 'none';
        }
        if (state.length > 0) {
          document.forms[0][state].value = idx;
        }
      }
      </script>
  </xsl:template>
  
  <xsl:template name="woody-layout-body">
     <div id="WoodyCalendarDiv" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"/>
  </xsl:template>
  
  <!--
    wi:group : default is to enclose items in a div
  -->
  <xsl:template match="wi:group">
    <div title="{wi:help}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="group-layout" select="."/>
    </div>
  </xsl:template>
  
  <!--
    wi:group of type tabs
  -->
  <xsl:template match="wi:group[wi:styling/@type='tabs']">
    <!-- find the currently selected tab.
         Thoughts still needed here, such as autogenerating a field in the woodytransformer
         to hold this state.
    -->
    <xsl:variable name="active">
      <xsl:choose>
        <xsl:when test="@state-widget">
          <xsl:variable name="value" select="string(//wi:field[@id = current()/@state-widget]/wi:value)"/>
          <xsl:choose>
            <xsl:when test="string-length($value) > 0"><xsl:value-of select="$value"/></xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- copy the "state-widget" attribute for use in for-each -->
    <xsl:variable name="state-widget" select="@state-widget"/>
    
    <xsl:variable name="id" select="generate-id()"/>
    <div id="{$id}">
    
      <!-- div containing the tabs -->
      <div class="woody-tabArea">
        <xsl:for-each select="wi:items/wi:*">
          <span id="{$id}_tab_{position() - 1}" onclick="showWoodyTab('{$id}', {position() - 1}, {last()}, '{$state-widget}')">
            <xsl:choose>
              <xsl:when test="$active = (position() - 1)">
                <xsl:attribute name="class">woody-tab woody-activeTab</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="class">woody-tab</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:copy-of select="wi:label/node()"/>
            <xsl:if test=".//wi:validation-message">
              <span style="color:red; font-weight: bold">&#160;!&#160;</span>
            </xsl:if>
          </span>
        </xsl:for-each>
      </div>
      
      <!-- a div for each of the items -->
      <xsl:for-each select="wi:items/wi:*">
        <div class="woody-tabContent" id="{$id}_items_{position() - 1}">
          <xsl:if test="$active != position() - 1">
            <xsl:attribute name="style">display:none</xsl:attribute>
          </xsl:if>
          <xsl:apply-templates select="."/>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>
  
  <!--
    wi:group of type fieldset : enclose items in a fieldset frame
  -->
  <xsl:template match="wi:group[wi:styling/@type='fieldset']">
    <fieldset>
      <xsl:copy-of select="wi:styling/@*[name() != 'type']"/>
      <legend title="{wi:help}"><xsl:copy-of select="wi:label/node()"/></legend>
      <xsl:apply-templates mode="group-layout" select="."/>
    </fieldset>
  </xsl:template>
  
  <!--
    Group items layout : default is no layout
  -->
  <xsl:template match="wi:group" mode="group-layout">
    <xsl:apply-templates select="wi:items/node()"/>
  </xsl:template>
  
  <!--
    Columnized group items layout
  -->
  <xsl:template match="wi:group[wi:styling/@layout='columns']" mode="group-layout">
    <table border="0">
      <tbody>
        <xsl:apply-templates select="wi:items/*" mode="group-columns-content"/>
      </tbody>
    </table>
  </xsl:template>
  
  <!--
    Default column layout : label left and input right
  -->
  <xsl:template match="wi:*" mode="group-columns-content">
    <tr>
      <td valign="top"><xsl:copy-of select="wi:label/node()"/></td>
      <td><xsl:apply-templates select="."/></td>
   </tr>
  </xsl:template>

  <!-- boolean field : checkbox and label on a single line -->
  <xsl:template match="wi:booleanfield" mode="group-columns-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/> <xsl:copy-of select="wi:label/node()"/></td>
    </tr>
  </xsl:template>

  <!-- nested group -->
  <xsl:template match="wi:group" mode="group-columns-content">
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
