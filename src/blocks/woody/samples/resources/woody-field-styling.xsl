<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wi="http://apache.org/cocoon/woody/instance/1.0"
                exclude-result-prefixes="wi">

  <!-- must be called in <head> to load calendar script and setup the CSS -->
  <xsl:template name="woody-field-head">
    <xsl:param name="uri" select="'resources'"/>
    <xsl:param name="div">woody_calendarDiv</xsl:param>

    <!-- use this for debugging
    <script src="{$uri}/mattkruse-lib/AnchorPosition.js" language="JavaScript" type="text/javascript"/>
    <script src="{$uri}/mattkruse-lib/CalendarPopup.js" language="JavaScript" type="text/javascript"/>
    <script src="{$uri}/mattkruse-lib/date.js" language="JavaScript" type="text/javascript"/>
    <script src="{$uri}/mattkruse-lib/OptionTransfer.js" language="JavaScript" type="text/javascript"/>
    <script src="{$uri}/mattkruse-lib/PopupWindow.js" language="JavaScript" type="text/javascript"/>
    <script src="{$uri}/mattkruse-lib/selectbox.js" language="JavaScript" type="text/javascript"/>
    -->
    <script src="{$uri}/mattkruse-lib.js" language="JavaScript" type="text/javascript"/>

    <script src="{$uri}/woody-lib.js" language="JavaScript" type="text/javascript"/>
    <script language="JavaScript" type="text/javascript">
      // Setup calendar
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
      //FIXME: avoid this and simply write them in the CSS?
      document.write(woody_calendar.getStyles());
    </script>
  </xsl:template>

  <!--+
      | must be called in <head> to load help popups, calendar script,
      | and setup the CSS
      +-->
  <xsl:template name="woody-field-body">
    <xsl:attribute name="onload">woody_onload(); <xsl:value-of select="@onload"/></xsl:attribute>
    <!--script language="JavaScript">
      // Register woody startup function
      document.body.onload = woody_init;
    </script-->
    <div id="woody_calendarDiv" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"/>
  </xsl:template>

  <!--+
      | Generic wi:field : produce an <input>
      +-->
  <xsl:template match="wi:field">
    <input name="{@id}" id="{@id}" value="{wi:value}" title="{wi:hint}">
      <xsl:if test="wi:styling">
        <xsl:copy-of select="wi:styling/@*"/>
      </xsl:if>
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
    </input>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <xsl:template match="wi:*" mode="common">
    <!-- validation message -->
    <xsl:apply-templates select="wi:validation-message"/>
    <!-- required mark -->
    <xsl:if test="@required='true'">
      <span class="woody-field-required"> * </span>
    </xsl:if>
    <xsl:apply-templates select="wi:help"/>
  </xsl:template>

  <xsl:template match="wi:help">
    <div class="woody-help" id="help{generate-id()}" style="visibility:hidden; position:absolute;">
      <xsl:apply-templates select="node()"/>
    </div>
    <script language="JavaScript">
      var helpWin<xsl:value-of select="generate-id()"/> = woody_createPopupWindow('help<xsl:value-of select="generate-id()"/>');
    </script>
    <a id="{generate-id()}" href="#" onclick="helpWin{generate-id()}.showPopup('{generate-id()}');return false;"><img border="0" src="resources/help.gif"/></a>
  </xsl:template>

  <xsl:template match="wi:validation-message">
    <a href="#" class="woody-validation-message-indicator" onclick="alert('{normalize-space(.)}');return false;">&#160;!&#160;</a>
  </xsl:template>

  <!--+
      | Hidden wi:field : produce input with type='hidden'
      +-->
  <xsl:template match="wi:field[wi:styling[@type='hidden']]" priority="2">
    <input type="hidden" name="{@id}" id="{@id}" value="{wi:value}">
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
    </input>
  </xsl:template>

  <!--+
      | wi:field with a selection list and @list-type 'radio' : produce
      | radio-buttons oriented according to @list-orientation
      | ("horizontal" or "vertical" - default)
      +-->
  <xsl:template match="wi:field[wi:selection-list and wi:styling/@list-type='radio']" priority="2">
    <xsl:variable name="value" select="wi:value"/>
    <xsl:variable name="vertical" select="string(wi:styling/@list-orientation) != 'horizontal'"/>
    <xsl:choose>
      <xsl:when test="$vertical">
        <table cellpadding="0" cellspacing="0" border="0" title="{wi:hint}">
          <xsl:variable name="id" select="@id"/>
          <xsl:for-each select="wi:selection-list/wi:item">
            <tr>
              <td>
                <input type="radio" id="{generate-id()}" name="{$id}" value="{@value}">
                  <xsl:if test="@value = $value">
                    <xsl:attribute name="checked">true</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="wi:styling/@submit-on-change='true'">
                    <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
                  </xsl:if>
                </input>
              </td>
              <td>
                <label for="{generate-id()}"><xsl:copy-of select="wi:label/node()"/></label>
              </td>
              <xsl:if test="position() = 1">
                <td rowspan="{count(../wi:item)}">
                  <xsl:apply-templates select="../.." mode="common"/>
                </td>
              </xsl:if>
            </tr>
          </xsl:for-each>
        </table>
      </xsl:when>
      <xsl:otherwise>
        <span title="{wi:hint}">
          <xsl:variable name="id" select="@id"/>
          <xsl:for-each select="wi:selection-list/wi:item">
            <input type="radio" id="{generate-id()}" name="{$id}" value="{@value}">
              <xsl:if test="@value = $value">
                <xsl:attribute name="checked">true</xsl:attribute>
              </xsl:if>
              <xsl:if test="wi:styling/@submit-on-change='true'">
                <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
              </xsl:if>
            </input>
            <label for="{generate-id()}"><xsl:copy-of select="wi:label/node()"/></label>
          </xsl:for-each>
        </span>
        <xsl:apply-templates select="." mode="common"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--+
      | wi:field with a selection list (not 'radio' style)
      | Rendering depends on the attributes of wi:styling :
      | - if @list-type is "listbox" : produce a list box with @list-size visible
      |   items (default 5)
      | - otherwise, produce a dropdown menu
      +-->
  <xsl:template match="wi:field[wi:selection-list]" priority="1">
    <xsl:variable name="value" select="wi:value"/>
    <xsl:variable name="liststyle" select="wi:styling/@list-type"/>

    <!-- dropdown or listbox -->
    <select title="{wi:hint}" id="{@id}" name="{@id}">
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
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
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <!--+
      | wi:field with a selection list and @type 'output'
      +-->
  <xsl:template match="wi:field[wi:selection-list and wi:styling[@type='output']]" priority="3">
    <xsl:variable name="value" select="wi:value"/>
    <xsl:variable name="selected" select="wi:selection-list/wi:item[@value = $value]"/>
    <xsl:choose>
      <xsl:when test="$selected/wi:label">
        <xsl:apply-templates select="$selected/wi:label"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--+
      | wi:field with @type 'textarea'
      +-->
  <xsl:template match="wi:field[wi:styling[@type='textarea']]">
    <textarea id="{@id}" name="{@id}" title="{wi:hint}">
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="wi:styling/@*[not(name() = 'type')]"/>
      <xsl:copy-of select="wi:value/node()"/>
    </textarea>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <!--+
      | wi:field with @type 'output' : rendered as text
      +-->
  <xsl:template match="wi:field[wi:styling[@type='output']]" priority="2">
    <xsl:copy-of select="wi:value"/>
  </xsl:template>

  <!--+
      | wi:field with @type 'date' : use CalendarPopup
      +-->
  <xsl:template match="wi:field[wi:styling[@type='date']]">
    <xsl:variable name="id" select="generate-id()"/>
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="wi:styling/@format"><xsl:value-of select="wi:styling/@format"/></xsl:when>
        <xsl:otherwise>yyyy-MM-dd</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <input id="{@id}" name="{@id}" value="{wi:value}">
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="wi:styling/@*[not(name() = 'type')]"/>
    </input>
    <a href="#" onClick="woody_calendar.select(woody_getForm(this)['{@id}'],'{generate-id()}','{$format}'); return false;" NAME="{generate-id()}" ID="{generate-id()}"><img src="resources/cal.gif" border="0" alt="Calendar"/></a>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <!--+
      | wi:output
      +-->
  <xsl:template match="wi:output">
    <xsl:copy-of select="wi:value/node()"/>
  </xsl:template>

  <!--+
      | wi:booleanfield : produce a checkbox
      +-->
  <xsl:template match="wi:booleanfield">
    <input id="{@id}" type="checkbox" value="true" name="{@id}" title="{wi:hint}">
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
      <xsl:if test="wi:value/text() = 'true'">
        <xsl:attribute name="checked">true</xsl:attribute>
      </xsl:if>
    </input>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <!--+
      | wi:booleanfield with @type 'output' : rendered as text
      +-->
  <xsl:template match="wi:booleanfield[wi:styling[@type='output']]">
    <xsl:choose>
      <xsl:when test="wi:value = 'true'">
        yes
      </xsl:when>
      <xsl:otherwise>
        no
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--+
      | wi:action
      +-->
  <xsl:template match="wi:action">
    <input id="{@id}" type="submit" name="{@id}" title="{wi:hint}">
      <xsl:attribute name="value"><xsl:value-of select="wi:label/node()"/></xsl:attribute>
      <xsl:copy-of select="wi:styling/@*"/>
    </input>
  </xsl:template>

  <!--+
      | wi:continuation-id : produce a hidden "continuation-id" input
      +-->
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

  <!--+
      | wi:multivaluefield : produce a list of checkboxes
      +-->
  <xsl:template match="wi:multivaluefield[wi:styling[@list-type='checkbox']]">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="wi:values/wi:value/text()"/>

    <span title="{wi:hint}">
      <xsl:for-each select="wi:selection-list/wi:item">
        <xsl:variable name="value" select="@value"/>
        <input id="{generate-id()}" type="checkbox" value="{@value}" name="{$id}">
          <xsl:if test="$values[. = $value]">
            <xsl:attribute name="checked">true</xsl:attribute>
          </xsl:if>
        </input>
        <label for="{generate-id()}"><xsl:copy-of select="wi:label/node()"/></label>
        <br/>
      </xsl:for-each>
    </span>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <!--+
      | wi:multivaluefield : produce a multiple-selection list
      +-->
  <xsl:template match="wi:multivaluefield">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="wi:values/wi:value/text()"/>

    <span title="{wi:hint}">
      <select id="{@id}" name="{$id}" multiple="multiple">
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
        <xsl:for-each select="wi:selection-list/wi:item">
          <xsl:variable name="value" select="@value"/>
          <option value="{$value}">
            <xsl:if test="$values[. = $value]">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="wi:label/node()"/>
          </option>
        </xsl:for-each>
      </select>
    </span>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <!--+
      | wi:multivaluefield : produce a double selection list
      +-->
  <xsl:template match="wi:multivaluefield[wi:styling[@list-type='double-listbox']]">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="wi:values/wi:value/text()"/>
    <xsl:variable name="size">
      <xsl:choose>
        <xsl:when test="wi:styling/@listbox-size">
          <xsl:value-of select="wi:styling/@listbox-size"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>5</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <span  class="woody-doubleList" title="{wi:hint}">
      <table>
        <tr>
          <td>
            <!-- select for the unselected values -->
            <select ondblclick="opt{generate-id()}.transferRight()" id="{@id}.unselected" name="{@id}.unselected" size="{$size}" multiple="multiple">
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
            <select ondblclick="opt{generate-id()}.transferLeft()" id="{@id}" name="{@id}" size="{$size}" multiple="multiple">
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
      <script language="JavaScript">
        var opt<xsl:value-of select="generate-id()"/> = woody_createOptionTransfer('<xsl:value-of select="@id"/>');
      </script>
    </span>
  </xsl:template>

  <!--+
      | wi:upload
      +-->
  <xsl:template match="wi:upload">
    <xsl:choose>
      <xsl:when test="wi:value">
        <!-- Has a value (filename): display it with a change button -->
        <span title="{wi:hint}">
          [<xsl:value-of select="wi:value"/>] <input type="submit" id="{@id}" name="{@id}" value="..."/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <input type="file" id="{@id}" name="{@id}" title="{wi:hint}" accept="{@mime-types}"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <!--+
      | wi:repeater
      +-->
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

  <!--+
      | wi:repeater-row
      +-->
  <xsl:template match="wi:repeater-row">
    <tr>
      <xsl:for-each select="*">
        <td>
          <xsl:apply-templates select="."/>
        </td>
      </xsl:for-each>
    </tr>
  </xsl:template>

  <!--+
      | wi:repeater-size
      +-->
  <xsl:template match="wi:repeater-size">
    <input type="hidden" name="{@id}.size" value="{@size}"/>
  </xsl:template>

  <!--+
      | wi:form-template
      +-->
  <xsl:template match="wi:form-template">
    <form>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="onsubmit">woody_onsubmit(); <xsl:value-of select="@onsubmit"/></xsl:attribute>
      <!-- hidden field to store the submit id -->
      <input type="hidden" name="woody_submit_id"/>
      <xsl:apply-templates/>
    </form>
  </xsl:template>

  <!--+
      | wi:form
      +-->
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
    <input id="{@id}" name="{@id}" value="{wi:value}" title="{wi:hint}">
      <xsl:if test="wi:styling/@submit-on-change='true'">
        <xsl:attribute name="onchange">woody_submitForm(this)</xsl:attribute>
      </xsl:if>
    </input>
    <xsl:apply-templates select="." mode="common"/>
  </xsl:template>

  <xsl:template match="wi:messages">
    <xsl:if test="wi:message">
      <xsl:copy-of select="wi:label/node()"/>:
      <ul>
        <xsl:for-each select="wi:message">
          <li><xsl:apply-templates/></li>
        </xsl:for-each>
      </ul>
    </xsl:if>
  </xsl:template>

  <xsl:template match="wi:validation-errors">
    <xsl:variable name="header">
      <xsl:choose>
        <xsl:when test="header">
          <xsl:copy-of select="header"/>
        </xsl:when>
        <xsl:otherwise>
          <p class="validation-errors">The following errors have been detected (marked with !):</p>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="footer">
      <xsl:choose>
        <xsl:when test="footer">
          <xsl:copy-of select="footer"/>
        </xsl:when>
        <xsl:otherwise>
          <p class="validation-errors">Please, correct them and re-submit the form.</p>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="frm" select="ancestor::wi:form-template"/>
    <xsl:if test="$frm and $frm//wi:validation-message">
      <xsl:copy-of select="$header"/>
      <ul>
        <xsl:for-each select="$frm//wi:validation-message">
          <li class="validation-error">
            <xsl:if test="../wi:label">
              <xsl:value-of select="../wi:label"/><xsl:text>: </xsl:text>
            </xsl:if>
            <xsl:value-of select="."/>
          </li>
        </xsl:for-each>
      </ul>
      <xsl:copy-of select="$footer"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
