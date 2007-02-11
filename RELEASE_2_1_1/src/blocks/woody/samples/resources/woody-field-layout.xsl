<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:wi="http://apache.org/cocoon/woody/instance/1.0">
  
  <!--
    Generic wi:field : produce an <input>
  -->
  <xsl:template match="wi:field">
    <input name="{@id}" value="{wi:value}" title="{wi:help}">
      <xsl:if test="wi:styling">
        <xsl:copy-of select="wi:styling/@*"/>
      </xsl:if>
    </input>
    <xsl:call-template name="woody-field-common"/>
  </xsl:template>
  
  <xsl:template name="woody-field-common">
    <xsl:apply-templates select="wi:validation-message"/>
    <xsl:if test="@required='true'">
      <b>*</b>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="wi:validation-message">
    <a href="#" style="color:red; font-weight: bold" onclick="alert('{normalize-space(.)}'); return false;">&#160;!&#160;</a>
  </xsl:template>

  <!--
    wi:field with a selection list
    Rendering depends on the attributes of wi:styling :
    - if @list-type is "radio" : produce radio-buttons oriented according to @list-orientation
      ("horizontal" or "vertical" - default)
    - if @list-type is "listbox" : produce a list box with @list-size visible items (default 5)
    - otherwise, produce a dropdown menu
  -->
  <xsl:template match="wi:field[wi:selection-list]" priority="1">
    <xsl:variable name="value" select="wi:value"/>
    <xsl:variable name="liststyle" select="wi:styling/@list-type"/>
    <xsl:choose>
      <!-- radio buttons -->
      <xsl:when test="$liststyle='radio'">
        <span title="{wi:help}">
        <xsl:variable name="vertical" select="string(wi:styling/@list-orientation) != 'horizontal'"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:for-each select="wi:selection-list/wi:item">
          <xsl:if test="$vertical and position() != 1"><br/></xsl:if>
          <input type="radio" name="{$id}" value="{@value}">
            <xsl:if test="@value = $value">
              <xsl:attribute name="checked">true</xsl:attribute>
            </xsl:if>
          </input>
          <xsl:copy-of select="wi:label/node()"/>
        </xsl:for-each>
        </span>
      </xsl:when>
      <!-- dropdown or listbox -->
      <xsl:otherwise>
        <select title="{wi:help}" name="{@id}">
          <xsl:if test="$liststyle='listbox'">
            <xsl:attribute name="size">
              <xsl:choose>
                <xsl:when test="wi:styling/@listbox-size">
                  <xsl:value-of select="wi:styling/@listbox-size"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>5</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
          </xsl:if>
          <xsl:for-each select="wi:selection-list/wi:item">
            <option value="{@value}">
              <xsl:if test="@value = $value">
                <xsl:attribute name="selected">selected</xsl:attribute>
              </xsl:if>
              <xsl:copy-of select="wi:label/node()"/>
            </option>
          </xsl:for-each>
        </select>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="woody-field-common"/>
  </xsl:template>
  
  <!--
    wi:field with @type 'textarea'
  -->
  <xsl:template match="wi:field[@type='textarea']">
    <textarea name="{@id}" title="{wi:help}">
      <xsl:copy-of select="wi:styling/@*[not(name() = 'type')]"/>
      <xsl:copy-of select="wi:value/node()"/>
    </textarea>
    <xsl:call-template name="woody-field-common"/>
  </xsl:template>

  <!--
    wi:field with @type 'output' : rendered as text
  -->
  <xsl:template match="wi:field[wi:styling[@type='output']]">
    <xsl:copy-of select="wi:value"/>
  </xsl:template>

  <!--
    wi:field with @type 'date' : use CalendarPopup
  -->
  <xsl:template match="wi:field[wi:styling[@type='date']]">
    <xsl:variable name="id" select="generate-id()"/>
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="wi:styling/@format"><xsl:value-of select="wi:styling/@format"/></xsl:when>
        <xsl:otherwise>yyyy-MM-dd</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <input name="{@id}" value="{wi:value}">
      <xsl:copy-of select="wi:styling/@*[not(name() = 'type')]"/>
    </input>
    <!-- note: we use forms[0]['{@id}'] and not forms[0].{@id} since @id may not be a valid JS property name -->
    <a href="#" onClick="woody_calendar.select(document.forms[0]['{@id}'],'{generate-id()}','{$format}'); return false;" NAME="{generate-id()}" ID="{generate-id()}"><img src="resources/cal.gif" border="0" alt="Calendar"/></a>
    <xsl:call-template name="woody-field-common"/>
  </xsl:template>

  <!-- must be called in <head> to load calendar script and setup the CSS -->
  <xsl:template name="woody-calendar-head">
    <xsl:param name="uri" select="'resources'"/>
    <xsl:param name="div"/>
    <script src="{$uri}/CalendarPopup.js" language="JavaScript"></script>
    <script language="JavaScript">
      <xsl:choose>
        <xsl:when test="$div">
      var woody_calendar = CalendarPopup('<xsl:value-of select="$div"/>');
        </xsl:when>
        <xsl:otherwise>
      var woody_calendar = CalendarPopup();
        </xsl:otherwise>
      </xsl:choose>
      woody_calendar.setWeekStartDay(1);
      woody_calendar.showYearNavigation();
      woody_calendar.showYearNavigationInput();
      document.write(woody_calendar.getStyles());
    </script>
  </xsl:template>
  
  <!--
    wi:output
  -->
  <xsl:template match="wi:output">
    <xsl:copy-of select="wi:value/node()"/>
  </xsl:template>

  <!--
    wi:booleanfield : produce a checkbox
  -->
  <xsl:template match="wi:booleanfield">
    <input type="checkbox" value="true" name="{@id}" title="{wi:help}">
      <xsl:if test="wi:value/text() = 'true'">
        <xsl:attribute name="checked">true</xsl:attribute>
      </xsl:if>
    </input>
    <xsl:call-template name="woody-field-common"/>
  </xsl:template>

  <!--
    wi:action
  -->
  <xsl:template match="wi:action">
    <input type="submit" name="{@id}" title="{wi:help}">
      <xsl:attribute name="value"><xsl:value-of select="wi:label/node()"/></xsl:attribute>
    </input>
  </xsl:template>

  <!--
    wi:continuation-id : produce a hidden "continuation-id" input
  -->
  <xsl:template match="wi:continuation-id">
    <xsl:choose>
      <xsl:when test="@name">
        <input name="{@name}" type="hidden" value="{.}"/>
      </xsl:when>
      <xsl:otherwise>
        <input name="continuation-id" type="hidden" value="{.}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    wi:mutivaluefield : produce a list of checkboxes
    TODO : add other representations existing for selection-lists.
  -->
  <xsl:template match="wi:multivaluefield">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="wi:values/wi:value/text()"/>
    <span title="{wi:help}">
    <xsl:for-each select="wi:selection-list/wi:item">
      <xsl:variable name="value" select="@value"/>
      <input type="checkbox" value="{@value}" name="{$id}">
        <xsl:if test="$values[.=$value]">
          <xsl:attribute name="checked">true</xsl:attribute>
        </xsl:if>
      </input>
      <xsl:copy-of select="wi:label/node()"/>
      <br/>
    </xsl:for-each>
    </span>
    <xsl:call-template name="woody-field-common"/>
  </xsl:template>

  <!--
    wi:repeater
  -->
  <xsl:template match="wi:repeater">
    <input type="hidden" name="{@id}.size" value="{@size}"/>
    <table border="1">
      <tr>
        <xsl:for-each select="wi:headings/wi:heading">
          <th><xsl:value-of select="."/></th>
        </xsl:for-each>
      </tr>
      <xsl:apply-templates select="wi:repeater-row"/>
    </table>
  </xsl:template>

  <!--
    wi:repeater-row
  -->
  <xsl:template match="wi:repeater-row">
    <tr>
      <xsl:for-each select="*">
        <td>
          <xsl:apply-templates select="."/>
        </td>
      </xsl:for-each>
    </tr>
  </xsl:template>

  <!--
    wi:repeater-size
  -->
  <xsl:template match="wi:repeater-size">
    <input type="hidden" name="{@id}.size" value="{@size}"/>
  </xsl:template>

  <!--
    wi:form-template
  -->
  <xsl:template match="wi:form-template">
    <form>
      <xsl:apply-templates select="@*|node()"/>
    </form>
  </xsl:template>
  
  <!--
    wi:form
  -->
  <xsl:template match="wi:form">
    <table border="1">
      <xsl:for-each select="wi:children/*">
        <tr>
          <xsl:choose>
            <xsl:when test="local-name(.) = 'repeater'">
              <td valign="top" colspan="2">
                <xsl:apply-templates select="."/>
              </td>
            </xsl:when>
            <xsl:when test="local-name(.) = 'booleanfield'">
              <td>&#160;</td>
              <td valign="top">
                <xsl:apply-templates select="."/>
                <xsl:text> </xsl:text>
                <xsl:copy-of select="wi:label"/>
              </td>
            </xsl:when>
            <xsl:otherwise>
              <td valign="top">
                <xsl:copy-of select="wi:label"/>
              </td>
              <td valign="top">
                <xsl:apply-templates select="."/>
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="wi:aggregatefield">
    <input name="{@id}" value="{wi:value}" title="{wi:help}"/>
    <xsl:call-template name="woody-field-common"/>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
