<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:wi="http://apache.org/cocoon/woody/instance/1.0">
  
  <xsl:template name="woody-page-head">
      
    <script language="JavaScript">
      function woody_showTab(tabgroup, idx, length, state) {
        //alert(tabgroup + " - " + idx);
        for (var i = 0; i &lt; length; i++) {
          // Change tab status (selected/unselected)
          var tab = document.getElementById(tabgroup + "_tab_" + i);
          if (tab != null) {
            tab.className = (i == idx) ? 'woody-tab woody-activeTab': 'woody-tab';
          }
          // Change tab content visibilty
          var tabitems = document.getElementById(tabgroup + "_items_" + i);
          if (tabitems != null) {
            tabitems.style.display = (i == idx) ? '' : 'none';
          }
        }
        // Change state value
        if (state.length > 0) {
          document.forms[0][state].value = idx;
        }
      }
      </script>
  </xsl:template>
  
  <xsl:template name="woody-page-body">
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
        <xsl:when test="wi:state">
          <xsl:variable name="value" select="wi:state/wi:*/wi:value"/>
          <xsl:choose>
            <xsl:when test="string-length($value) > 0"><xsl:value-of select="$value"/></xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- copy the "state-widget" attribute for use in for-each -->
    <xsl:variable name="state-widget" select="wi:state/wi:*/@id"/>
    
    <xsl:variable name="id" select="generate-id()"/>
    <div id="{$id}">
    
      <!-- add an hidden input for the state -->
      <xsl:if test="$state-widget">
        <input type="hidden" name="{$state-widget}" value="{$active}"/>
      </xsl:if>
    
      <!-- div containing the tabs -->
      <div class="woody-tabArea">
        <xsl:for-each select="wi:items/wi:*">
          <span id="{$id}_tab_{position() - 1}" onclick="woody_showTab('{$id}', {position() - 1}, {last()}, '{$state-widget}')">
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
    wi:group of type choice : a popup is used instead of tabs
  -->
  <xsl:template match="wi:group[wi:styling/@type='choice']">
    <!-- find the currently selected tab.
         Thoughts still needed here, such as autogenerating a field in the woodytransformer
         to hold this state.
    -->
    <xsl:variable name="active">
      <xsl:choose>
        <xsl:when test="wi:state">
          <xsl:variable name="value" select="wi:state/wi:*/wi:value"/>
          <xsl:choose>
            <xsl:when test="string-length($value) > 0"><xsl:value-of select="$value"/></xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- copy the "state-widget" attribute for use in for-each -->
    <xsl:variable name="state-widget" select="wi:state/wi:*/@id"/>
    
    <xsl:variable name="id" select="generate-id()"/>
    <fieldset id="{$id}">
      <legend>
        <xsl:apply-templates select="wi:label/node()"/>
        <select name="{$state-widget}" onchange="woody_showTab('{$id}', this.selectedIndex, {count(wi:items/*)}, '{$state-widget}')">
          <xsl:for-each select="wi:items/wi:*">
            <option>
              <xsl:attribute name="value">
                <xsl:choose>
                  <xsl:when test="wi:value">
                    <xsl:value-of select="wi:value"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="position() - 1"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <xsl:if test="$active = (position() - 1)">
                <xsl:attribute name="selected">selected</xsl:attribute>
              </xsl:if>
              <xsl:copy-of select="wi:label"/>
            </option>
          </xsl:for-each>
        </select>
        <xsl:if test="wi:items/*//wi:validation-message">
          <span style="color:red; font-weight: bold">&#160;!&#160;</span>
        </xsl:if>
      </legend>
      
      <!-- a div for each of the items -->
      <xsl:for-each select="wi:items/wi:*">
        <div id="{$id}_items_{position() - 1}">
          <xsl:if test="$active != position() - 1">
            <xsl:attribute name="style">display:none</xsl:attribute>
          </xsl:if>
          <xsl:apply-templates select="."/>
        </div>
      </xsl:for-each>
    </fieldset>
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
