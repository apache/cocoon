<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wi="http://apache.org/cocoon/woody/instance/1.0"
                exclude-result-prefixes="wi">

  <!--+
      | This stylesheet is designed to be included by 'woody-samples-styling.xsl'.
      | It extends the 'woody-field-styling.xsl' with additional stylings.
      | The calendar can also be seen as advanced field styling, it's only
      | separated out of this file because of its much specific stuff.
      +-->

  <xsl:import href="woody-field-styling.xsl"/>
  <xsl:include href="woody-calendar-styling.xsl"/>
  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>

  <xsl:template match="head" mode="woody-field">
    <xsl:apply-imports/>
    <script src="{$resources-uri}/mattkruse-lib/OptionTransfer.js" language="JavaScript" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/selectbox.js" language="JavaScript" type="text/javascript"/>
    <xsl:apply-templates select="." mode="woody-calendar"/>
  </xsl:template>

  <xsl:template match="body" mode="woody-field">
    <xsl:apply-imports/>
    <xsl:apply-templates select="." mode="woody-calendar"/>
  </xsl:template>

  <!--+ This template should not be necessary as this stylesheet "inherits"
      | all templates from 'woody-field-styling.xsl', but without it, it does
      | not work for me (using Xalan 2.5.1). It's like adding all methods of
      | a superclass in a subclass and calling everywhere only the super
      | implementation.
      +-->
  <xsl:template match="*">
    <xsl:apply-imports/>
  </xsl:template>

  <!--+
      | wi:multivaluefield with list-type='double-listbox' styling
      +-->
  <xsl:template match="wi:multivaluefield[wi:styling/@list-type='double-listbox']">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="wi:values/wi:value/text()"/>

    <span class="woody-doubleList" title="{wi:hint}">
      <table>
        <xsl:if test="wi:styling/wi:available-label|wi:styling/wi:selected-label">
          <tr>
            <th>
              <xsl:copy-of select="wi:styling/wi:available-label/node()"/>
            </th>
            <th> </th>
            <th>
              <xsl:copy-of select="wi:styling/wi:selected-label/node()"/>
            </th>
          </tr>
        </xsl:if>
        <tr>
          <td>
            <!-- select for the unselected values -->
            <select id="{@id}.unselected" name="{@id}.unselected" multiple="multiple"
                    ondblclick="opt{generate-id()}.transferRight()">
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:for-each select="wi:selection-list/wi:item">
                <xsl:variable name="value" select="@value"/>
                <xsl:if test="not($values[. = $value])">
                  <option value="{$value}">
                    <xsl:copy-of select="wi:label/node()"/>
                  </option>
                </xsl:if>
              </xsl:for-each>
            </select>
          </td>
          <td align="center" valign="middle">
            <!-- command buttons -->
            <!-- strangely, IE adds an extra blank line if there only a button on a line. So we surround it with nbsp -->
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&gt;" onclick="opt{generate-id()}.transferRight()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&gt;&gt;" onclick="opt{generate-id()}.transferAllRight()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;" onclick="opt{generate-id()}.transferLeft()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;&lt;" onclick="opt{generate-id()}.transferAllLeft()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:apply-templates select="." mode="common"/>
          </td>
          <td>
            <!-- select for the selected values -->
            <select id="{@id}" name="{@id}" multiple="multiple"
                    ondblclick="opt{generate-id()}.transferLeft()" >
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:for-each select="wi:selection-list/wi:item">
                <xsl:variable name="value" select="@value"/>
                <xsl:if test="$values[. = $value]">
                  <option value="{$value}">
                    <xsl:copy-of select="wi:label/node()"/>
                  </option>
                </xsl:if>
              </xsl:for-each>
            </select>
          </td>
        </tr>
      </table>
      <script language="JavaScript" type="text/javascript">
        var opt<xsl:value-of select="generate-id()"/> = woody_createOptionTransfer('<xsl:value-of select="@id"/>');
      </script>
    </span>
  </xsl:template>

</xsl:stylesheet>
