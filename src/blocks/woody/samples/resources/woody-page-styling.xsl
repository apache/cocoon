<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wi="http://apache.org/cocoon/woody/instance/1.0"
                exclude-result-prefixes="wi">

  <!--+
      | This stylesheet is designed to be included by 'woody-samples-styling.xsl'.
      +-->
  <xsl:template match="head" mode="woody-page">
    <!--+ 'woody-page-styling.xsl' relies on 'woody-field-styling.xsl' for the
        | inclusion of the correct JS and CSS files. To fix it, we have to
        | separate the page specific parts into its own files.
        +-->
  </xsl:template>

  <xsl:template match="body" mode="woody-page"/>

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
      <xsl:variable name="value" select="normalize-space(wi:state/wi:*/wi:value)"/>
      <xsl:choose>
        <xsl:when test="$value">
          <xsl:value-of select="$value"/>
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
          <xsl:variable name="pos" select="position() - 1"/>
          <span id="{$id}_tab_{$pos}" onclick="woody_showTab('{$id}', {$pos}, {last()}, '{$state-widget}')">
            <xsl:attribute name="class">
              <xsl:text>woody-tab</xsl:text>
              <xsl:if test="$active = $pos"> woody-activeTab</xsl:if>
            </xsl:attribute>
            <xsl:copy-of select="wi:label/node()"/>
            <xsl:if test="wi:items/*//wi:validation-message">
              <span class="woody-validation-message">&#160;!&#160;</span>
            </xsl:if>
          </span>
        </xsl:for-each>
      </div>
      <!-- a div for each of the items -->
      <xsl:for-each select="wi:items/wi:*">
        <xsl:variable name="pos" select="position() - 1"/>
        <div class="woody-tabContent" id="{$id}_items_{$pos}">
          <xsl:if test="$active != $pos">
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
      <xsl:variable name="value" select="normalize-space(wi:state/wi:*/wi:value)"/>
      <xsl:choose>
        <xsl:when test="$value">
          <xsl:value-of select="$value"/>
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
            <xsl:variable name="pos" select="position() - 1"/>
            <option>
              <xsl:attribute name="value">
                <xsl:choose>
                  <xsl:when test="wi:value">
                    <xsl:value-of select="wi:value"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$pos"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <xsl:if test="$active = $pos">
                <xsl:attribute name="selected">selected</xsl:attribute>
              </xsl:if>
              <xsl:copy-of select="wi:label/node()"/>
            </option>
          </xsl:for-each>
        </select>
        <xsl:if test="wi:items/*//wi:validation-message">
          <span class="woody-validation-message">&#160;!&#160;</span>
        </xsl:if>
      </legend>
      <!-- a div for each of the items -->
      <xsl:for-each select="wi:items/wi:*">
        <xsl:variable name="pos" select="position() - 1"/>
        <div id="{$id}_items_{$pos}">
          <xsl:if test="$active != $pos">
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
      <xsl:copy-of select="wi:styling/@*[name() != 'type' and name() != 'layout']"/>
      <legend title="{wi:hint}"><xsl:copy-of select="wi:label/node()"/></legend>
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
    Column group items layout
  -->
  <xsl:template match="wi:group[wi:styling/@layout='column']" mode="group-layout">
    <table border="0" summary="{wi:hint}">
      <tbody>
        <xsl:apply-templates select="wi:items/*" mode="group-column-content"/>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default column layout : label above and input below
  -->
  <xsl:template match="wi:*" mode="group-column-content">
    <tr>
      <td valign="top"><label for="{@id}" title="{wi:hint}"><xsl:copy-of select="wi:label/node()"/></label></td>
    </tr>
    <tr>
      <td><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!--
    Columns group items layout
  -->
  <xsl:template match="wi:group[wi:styling/@layout='columns']" mode="group-layout">
    <table border="0" summary="{wi:hint}">
      <tbody>
        <xsl:apply-templates select="wi:items/*" mode="group-columns-content"/>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default columns layout : label left and input right
  -->
  <xsl:template match="wi:*" mode="group-columns-content">
    <tr valign="baseline">
      <td valign="top"><label for="{@id}" title="{wi:hint}"><xsl:copy-of select="wi:label/node()"/></label></td>
      <td><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!--
    Row group items layout
  -->
  <xsl:template match="wi:group[wi:styling/@layout='row']" mode="group-layout">
    <table border="0" summary="{wi:hint}">
      <tbody>
        <tr>
          <xsl:apply-templates select="wi:items/*" mode="group-row-content"/>
        </tr>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default row layout : label left and input right
  -->
  <xsl:template match="wi:*" mode="group-row-content">
    <td valign="top">
      <label for="{@id}" title="{wi:hint}">
        <xsl:copy-of select="wi:label/node()"/>
      </label>
    </td>
    <td><xsl:apply-templates select="."/></td>
  </xsl:template>

  <!--
    Rows group items layout
  -->
  <xsl:template match="wi:group[wi:styling/@layout='rows']" mode="group-layout">
    <table border="0" summary="{wi:hint}">
      <tbody>
        <tr>
          <xsl:apply-templates select="wi:items/*" mode="group-rows-labels"/>
        </tr>
        <tr>
          <xsl:apply-templates select="wi:items/*" mode="group-rows-content"/>
        </tr>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default rows layout : label above and input below
  -->
  <xsl:template match="wi:*" mode="group-rows-labels">
    <td valign="top">
      <label for="{@id}" title="{wi:hint}">
        <xsl:copy-of select="wi:label/node()"/>
      </label>
    </td>
  </xsl:template>

  <xsl:template match="wi:*" mode="group-rows-content">
    <td><xsl:apply-templates select="."/></td>
  </xsl:template>

  <!-- boolean field : checkbox and label on a single line -->
  <xsl:template match="wi:booleanfield" mode="group-columns-content">
    <tr>
      <td colspan="2">
        <xsl:apply-templates select="."/>
        <label for="{@id}">
          <xsl:copy-of select="wi:label/node()"/>
        </label>
      </td>
    </tr>
  </xsl:template>

  <!-- action : on a single line -->
  <xsl:template match="wi:action" mode="group-columns-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!-- any other element : on a single line -->
  <xsl:template match="*" mode="group-columns-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!-- double-list multivaluefield : lists under the label -->
  <xsl:template match="wi:multivaluefield[wi:styling/@list-type='double-listbox']"
                mode="group-columns-content">
    <tr align="center">
      <td colspan="2">
        <label for="{@id}">
          <xsl:copy-of select="wi:label/node()"/>
        </label>
      </td>
    </tr>
    <tr align="center">
      <td colspan="2"><xsl:apply-templates select="."/></td>
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
