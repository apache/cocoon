<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:msxsl="urn:schemas-microsoft-com:xslt"
                xmlns:ow="http://openwiki.com/2001/OW/Wiki"
                xmlns="http://www.w3.org/1999/xhtml"
                version="1.0">
                
<xsl:output  method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"  doctype-system="DTD/xhtml1-strict.dtd"/>

<!--
<xsl:include href="owinc.xsl"/>
-->
<xsl:include href="owattach.xsl"/>
<xsl:include href="mystyle.xsl"/>

<xsl:variable name="name" select="string(/ow:wiki/ow:page/@name)" />

<xsl:template match="*">
  <xsl:element name="{name()}">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="processing-instruction()|comment()|text()">
  <xsl:copy>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<!-- ridiculous! IE processes <br></br> differently compared to <br /> ! -->
<xsl:template match="br">
  <br />
</xsl:template>

<xsl:template match="big">
  <b><big><xsl:apply-templates/></big></b>
</xsl:template>

<xsl:template match="table">
  <table cellspacing="0" cellpadding="2" border="1" width="100%">
    <xsl:apply-templates/>
  </table>
</xsl:template>

<!-- ==================== used to do client-side transformation ==================== -->
<xsl:template match="/ow:wiki">
  <xsl:choose>
    <xsl:when test="@mode='view'">
      <xsl:apply-templates select="." mode="view"/>
    </xsl:when>
    <xsl:when test="@mode='edit'">
      <xsl:apply-templates select="." mode="edit"/>
    </xsl:when>
    <xsl:when test="@mode='print'">
      <xsl:apply-templates select="." mode="print"/>
    </xsl:when>
    <xsl:when test="@mode='naked'">
      <xsl:apply-templates select="." mode="naked"/>
    </xsl:when>
    <xsl:when test="@mode='diff'">
      <xsl:apply-templates select="." mode="diff"/>
    </xsl:when>
    <xsl:when test="@mode='changes'">
      <xsl:apply-templates select="." mode="changes"/>
    </xsl:when>
    <xsl:when test="@mode='titlesearch'">
      <xsl:apply-templates select="." mode="titlesearch"/>
    </xsl:when>
    <xsl:when test="@mode='fullsearch'">
      <xsl:apply-templates select="." mode="fullsearch"/>
    </xsl:when>
    <xsl:when test="@mode='login'">
      <xsl:apply-templates select="." mode="login"/>
    </xsl:when>
    <xsl:when test="@mode='attach'">
      <xsl:apply-templates select="." mode="attach"/>
    </xsl:when>
    <xsl:when test="@mode='attachchanges'">
      <xsl:apply-templates select="." mode="attachchanges"/>
    </xsl:when>
    <xsl:when test="@mode='embedded'">
      <xsl:apply-templates select="." mode="embedded"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="." mode="view"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="pi">
</xsl:template>

<xsl:template match="/ow:wiki" mode="view">
    <div>
        <xsl:call-template name="brandingImage"/>
        <xsl:apply-templates select="ow:page"/>
    </div>
</xsl:template>



<xsl:template match="ow:page">
    <xsl:if test="/ow:wiki/ow:userpreferences/ow:editlinkontop">
        <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=edit<xsl:if test="@revision">&amp;revision=<xsl:value-of select="@revision"/></xsl:if></xsl:attribute>Edit</a> this page
        <xsl:if test="not(@changes='0')">
            <font size="-2">(last edited <xsl:value-of select="string(ow:change/ow:date)"/>)</font>
        </xsl:if>
        <br />
    </xsl:if>
    <xsl:if test="/ow:wiki/ow:userpreferences/ow:bookmarksontop">
      <xsl:if test="not(/ow:wiki/ow:userpreferences/ow:bookmarks='None')">
        <xsl:apply-templates select="/ow:wiki/ow:userpreferences/ow:bookmarks"/>
      </xsl:if>
    </xsl:if>
    <hr noshade="noshade" size="1" />
    <xsl:apply-templates select="../ow:trail"/>
    <xsl:if test="../ow:redirectedfrom">
        <b>Redirected from <a title="Edit this page"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?a=edit&amp;p=<xsl:value-of select="string(../ow:redirectedfrom/@name)"/></xsl:attribute><xsl:value-of select="../ow:redirectedfrom/text()"/></a></b>
        <p />
    </xsl:if>
    <xsl:if test="@revision">
        <b>Showing revision <xsl:value-of select="@revision"/></b>
    </xsl:if>

    <xsl:apply-templates select="ow:body"/>

    <form name="f" method="get">
    <xsl:attribute name="action"><xsl:value-of select="/ow:wiki/ow:scriptname"/></xsl:attribute>
    <hr noshade="noshade" size="1" />
    <table cellspacing="0" cellpadding="0" border="0" width="100%">

      <xsl:if test="not(/ow:wiki/ow:userpreferences/ow:bookmarks='None')">
        <tr>
          <td align="left" class="n">
            <xsl:apply-templates select="/ow:wiki/ow:userpreferences/ow:bookmarks"/>
          </td>
          <td align="right" rowspan="2">
            <xsl:call-template name="poweredBy"/>
          </td>
        </tr>
      </xsl:if>

      <tr>
        <td align="left" class="n">
            <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=edit<xsl:if test='@revision'>&amp;revision=<xsl:value-of select="@revision"/></xsl:if></xsl:attribute>Edit <xsl:if test='@revision'>revision <xsl:value-of select="@revision"/> of</xsl:if> this page</a>
            <xsl:if test="@revision or (ow:change and not(ow:change/@revision = 1))">
                |
                <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=changes</xsl:attribute>View other revisions</a>
            </xsl:if>
            <xsl:if test='@revision'>
                |
                <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/></xsl:attribute>View current revision</a>
            </xsl:if>
            <xsl:if test="/ow:wiki/ow:allowattachments">
                |
                <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=attach</xsl:attribute>Attachments</a> (<xsl:value-of select="count(ow:attachments/ow:attachment[@deprecated='false'])"/>)
            </xsl:if>
        </td>
      </tr>
      <tr>
        <td align="left" class="n">
            <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=print&amp;revision=<xsl:value-of select="ow:change/@revision"/></xsl:attribute>Print this page</a>
            |
            <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=xml&amp;revision=<xsl:value-of select="ow:change/@revision"/></xsl:attribute>View XML</a>
            <br />
            <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=FindPage&amp;txt=<xsl:value-of select="$name"/></xsl:attribute>Find page</a> by browsing, searching or an index
            <br />
            <xsl:if test="not(@changes='0')">
                Edited <xsl:value-of select="string(ow:change/ow:date)"/>
                <xsl:text> </xsl:text>
                <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/><xsl:if test="@revision">&amp;difffrom=<xsl:value-of select="@revision"/></xsl:if>&amp;a=diff</xsl:attribute>(diff)</a>
                <br />
            </xsl:if>
            <input type="hidden" name="a" value="fullsearch" />
            <input type="text" name="txt" size="30" ondblclick='event.cancelBubble=true;' /> <input type="submit" value="Search"/>
        </td>
        <td align="right">
            <xsl:call-template name="validatorButtons"/>
        </td>
      </tr>
    </table>
    </form>
</xsl:template>

<!-- ==================== wiki link to an existing page ==================== -->

<xsl:template name="href">
  <xsl:param name="href"/>
  <xsl:choose>
    <xsl:when test="starts-with($href, 'ow.asp?')">
      <xsl:value-of select="concat('ow.asp?p=', substring-after($href,'ow.asp?'))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$href"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="ow:link">
    <xsl:variable name="href">
      <xsl:call-template name="href">
          <xsl:with-param name="href" select="@href"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
        <xsl:when test="@date">
            <a href="{$href}{@anchor}" title="Last changed: {string(@date)}"><xsl:value-of select="text()"/></a>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="text()"/><a class="nonexistent" href="{$href}" title="Describe this page">?  </a>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- ==================== bookmarks from the user preferences ==================== -->
<xsl:template match="ow:bookmarks">
    <xsl:for-each select="ow:link">
        <xsl:variable name="href">
          <xsl:call-template name="href">
              <xsl:with-param name="href" select="@href"/>
          </xsl:call-template>
        </xsl:variable>
        <a href="{$href}"><xsl:value-of select="text()"/></a>
        <xsl:if test="not(position()=last())"> | </xsl:if>
    </xsl:for-each>
</xsl:template>

<!-- ==================== the trail, the last visited wiki pages ==================== -->
<xsl:template match="ow:trail">
    <xsl:if test="count(ow:link) &gt; 1 and ../ow:userpreferences/ow:trailontop">
        <small>
            <xsl:for-each select="ow:link">
                <xsl:choose>
                    <xsl:when test="../../ow:page/ow:link/@href=@href">
                        &#187; <xsl:value-of select="text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="href">
                          <xsl:call-template name="href">
                              <xsl:with-param name="href" select="@href"/>
                          </xsl:call-template>
                        </xsl:variable>
                        &#187; <a href="{$href}"><xsl:value-of select="text()"/></a>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </small>
        <hr noshade="noshade" size="1" />
    </xsl:if>
</xsl:template>


<!-- ==================== actual body of a page ==================== -->
<xsl:template match="ow:body">
    <xsl:if test=".='' and not(/ow:wiki/@mode='embedded')">
        <br />
        <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=edit</xsl:attribute>Describe <xsl:value-of select="../ow:link/text()"/> here</a>
        <xsl:apply-templates select="../../ow:templates"/>
    </xsl:if>
    <xsl:if test="starts-with(text(), '#DEPRECATED')">
        <font color="#ff0000"><b>This page will be permanently destroyed.</b></font>
        <p />
    </xsl:if>
    <xsl:apply-templates select="text() | *"/>
    <xsl:apply-templates select="../ow:attachments">
        <xsl:with-param name="showhidden">false</xsl:with-param>
        <xsl:with-param name="showactions">false</xsl:with-param>
    </xsl:apply-templates>
</xsl:template>


<!-- ==================== templates one can use to create a new page ==================== -->
<xsl:template match="ow:templates">
    <p/>
    <br />
    <br />
    Alternatively, create this page using one of these templates:
    <ul>
    <xsl:apply-templates select="ow:page"/>
    </ul>
    To create your own template add a page with a name ending in Template.
</xsl:template>

<!-- ==================== template one can use to create a new page ==================== -->
<xsl:template match="ow:templates/ow:page">
    <li>
      <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=edit&amp;template=<xsl:value-of select="string(@name)"/></xsl:attribute><xsl:value-of select="ow:link/text()"/></a>
      &#160;
      (<a target="_blank"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="string(@name)"/></xsl:attribute>view template</a>
       <a target="_blank"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="string(@name)"/></xsl:attribute><img src="ow/images/popup.gif" width="15" height="9" border="0" alt="" /></a>)
    </li>
</xsl:template>


<!-- ==================== handles the openwiki-html element ==================== -->
<xsl:template match="ow:html">
  <xsl:value-of select="." disable-output-escaping="yes" />
</xsl:template>


<!-- ==================== handles the openwiki-math element ==================== -->
<xsl:template match="ow:math">
  <math xmlns="http://www.w3.org/1998/Math/MathML">
    <xsl:value-of select="." disable-output-escaping="yes" />
  </math>
</xsl:template>


<!-- ==================== inclusion of another wikipage in this wikipage ==================== -->
<xsl:template match="ow:body/ow:page">
    <xsl:apply-templates select="ow:body"/>
    <div align="right"><small>[goto <xsl:apply-templates select="ow:link"/>]</small></div>
    <p/>
</xsl:template>


<!-- ==================== shows an error message ==================== -->
<xsl:template match="ow:error">
    <li><font color="red"><xsl:value-of select="."/></font></li>
</xsl:template>

<!-- ==================== shows footnotes ==================== -->
<xsl:template match="ow:footnotes">
    <p></p>
    ____
    <xsl:apply-templates select="ow:footnote" />
</xsl:template>

<xsl:template match="ow:footnote">
    <br /><a name="#footnote{@index}"></a><sup>&#160;&#160;&#160;<xsl:value-of select="@index"/>&#160;</sup><xsl:apply-templates />
</xsl:template>


<!-- ==================== show an RSS feed ==================== -->
<xsl:template match="ow:feed">
    <xsl:apply-templates/>
    <small>
    <br />
    last update: <xsl:value-of select="string(@last)"/>
    <br />
    <a href="{@href}" target="_blank"><img src="ow/images/xml.gif" width="36" height="14" border="0" alt="" /></a> |
    <a href="{/ow:wiki/ow:scriptname}?p={/ow:wiki/ow:page/ow:link/@name}&amp;a=refresh&amp;refreshurl={string(@href)}">refresh</a> |
    <a href="{/ow:wiki/ow:scriptname}?p={/ow:wiki/ow:page/ow:link/@name}&amp;a=refresh">refresh all</a>
    </small>
</xsl:template>

<!-- ==================== show an aggregated RSS feed ==================== -->
<xsl:template match="ow:aggregation">
    <xsl:apply-templates/>
    <small>
    <br />
    last update: <xsl:value-of select="string(@last)"/>
    <br />
    <a href="{@href}" target="_blank"><img src="ow/images/xml.gif" width="36" height="14" border="0" alt="" /></a> |
    <a href="{@refreshURL}">refresh</a>
    </small>
</xsl:template>


<xsl:template match="ow:interlinks">
    <script language="javascript" type="text/javascript" charset="{/ow:wiki/@encoding}">
      <xsl:text disable-output-escaping="yes">&lt;!--
        function ask(pURL) {
            var x = prompt("Enter the word you're searching for:", "");
            if (x != null) {
                var pos = pURL.indexOf("$1");
                if (pos > 0) {
                    top.location.assign(pURL.substring(0, pos) + x + pURL.substring(pos + 2, pURL.length));
                } else {
                    top.location.assign(pURL + x);
                }
            }
        }
    //--&gt;</xsl:text>
    </script>
    <table cellspacing="0" cellpadding="2" border="0">
      <xsl:for-each select="ow:interlink">
        <tr><td class="n"><li><xsl:value-of select="text()"/></li> &#160;&#160;</td><td class="n"><a href="#" onclick="javascript:ask('{@href}');"><xsl:value-of select="@href"/></a></td></tr>
      </xsl:for-each>
    </table>
</xsl:template>


<xsl:template match="/ow:wiki" mode="edit">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff">
        <xsl:attribute name="onload">document.f.text.focus();</xsl:attribute>

        <script language="javascript" type="text/javascript" charset="{@encoding}">
          <xsl:text disable-output-escaping="yes">&lt;!--
            function openw(pURL)
            {
                var w = window.open(pURL, "openw", "width=680,height=560,resizable=1,statusbar=1,scrollbars=1");
                w.focus();
            }

            function preview()
            {
                var w = window.open("", "preview", "width=680,height=560,resizable=1,statusbar=1,scrollbars=1");
                w.focus();

                var body = '&lt;html&gt;&lt;head&gt;&lt;meta http-equiv="Content-Type" content="text/html; charset=</xsl:text><xsl:value-of select="@encoding"/><xsl:text disable-output-escaping="yes">;" />&lt;/head&gt;&lt;body&gt;&lt;form name="pvw" method="post" action="</xsl:text><xsl:value-of select="/ow:wiki/ow:location"/><xsl:value-of select="/ow:wiki/ow:scriptname"/><xsl:text disable-output-escaping="yes">" /&gt;';
                body += '&lt;input type="hidden" name="a" value="preview" /&gt;';
                body += '&lt;input type="hidden" name="p" value="</xsl:text><xsl:value-of select="$name"/><xsl:text disable-output-escaping="yes">" /&gt;';
                body += '&lt;input id="text" type="hidden" name="text"/&gt;&lt;/form&gt;&lt;/body&gt;&lt;/html&gt;';

                w.document.open();
                w.document.write(body);
                w.document.close();

                w.document.forms[0].elements['text'].value = window.document.forms[0].elements['text'].value;
                w.document.forms[0].submit();
            }

            function saveDocumentCheck(evt) {
                    var desiredKeyState = evt.ctrlKey &amp;&amp; !evt.altKey &amp;&amp; !evt.shiftKey;
                    var key = evt.keyCode;
                    var charS = 83;
                    if ( desiredKeyState &amp;&amp; key == charS ) {
                            window.document.forms[0].elements['save'][0].click();
                            evt.returnValue = false;
                    }
            }

            function theTextAreaValue() {
                return window.document.forms[0].elements['text'].value;
            }

            savedValue = 'Empty';
            function checkChanged() {
                    currentValue = theTextAreaValue();
                    if (currentValue != savedValue) {
                            event.returnValue = 'Text changed without saving.';
                    }
            }
            function saveText(v) {
                    if (savedValue == 'Empty') {
                            setText(v);
                    }
                    window.onbeforeunload = checkChanged;
            }
            function setText(v) {
                    savedValue = v;
            }

          //--&gt;</xsl:text>
        </script>

        <h1>Editing <xsl:if test="ow:page/@revision">revision <xsl:value-of select="ow:page/@revision"/> of </xsl:if><xsl:value-of select="ow:page/@name"/></h1>
        <hr size="1" />
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=Help" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=Help&amp;a=print'); return false;">Help</a>
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=Help" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=Help&amp;a=print'); return false;"><img src="ow/images/popup.gif" width="15" height="9" border="0" alt="" /></a>
        |
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=HelpOnFormatting" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=HelpOnFormatting&amp;a=print'); return false;">Help On Formatting</a>
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=HelpOnFormatting" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=HelpOnFormatting&amp;a=print'); return false;"><img src="ow/images/popup.gif" width="15" height="9" border="0" alt="" /></a>
        |
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=HelpOnEditing" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=HelpOnEditing&amp;a=print'); return false;">Help On Editing</a>
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=HelpOnEditing" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=HelpOnEditing&amp;a=print'); return false;"><img src="ow/images/popup.gif" width="15" height="9" border="0" alt="" /></a>
        |
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=HelpOnEmoticons" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=HelpOnEmoticons&amp;a=print'); return false;">Help On Emoticons</a>
        <a class="same" href="{/ow:wiki/ow:scriptname}?p=HelpOnEmoticons" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=HelpOnEmoticons&amp;a=print'); return false;"><img src="ow/images/popup.gif" width="15" height="9" border="0" alt="" /></a>
        <br />
        <br />
        <xsl:if test="ow:page/@revision">
            <b>Editing old revision <xsl:value-of select="ow:page/@revision"/>. Saving this page will replace the latest revision with this text.</b>
        </xsl:if>
        <xsl:apply-templates select="ow:error"/>

        <xsl:if test="ow:textedits">
            <p>
                The text you edited is shown below.
                The text in the textarea box shows the latest version of this page.
            </p>
            <hr size="1" />
            <pre><xsl:value-of select="ow:textedits"/></pre>
            <hr size="1" />
        </xsl:if>

        <form name="f" method="post" onsubmit="setText(theTextAreaValue()); return true;">
            <xsl:attribute name="action"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?a=edit#preview</xsl:attribute>
            <input type="submit" name="save" value="Save" />
            &#160;
            <input type="button" name="prev1" value="Preview" onclick="javascript:preview();" />
            <!-- <input type="submit" name="preview" value="Preview" /> -->
            &#160;
            <input type="button" name="cancel" value="Cancel" onClick="javascript:window.location='{/ow:wiki/ow:scriptname}?p={$name}';" />
            <br />
            <br />
            <textarea id="text" name="text" wrap="virtual" onfocus="saveText(this.value)" onkeydown="saveDocumentCheck(event);"><xsl:attribute name="rows"><xsl:value-of select="/ow:wiki/ow:userpreferences/ow:rows"/></xsl:attribute><xsl:attribute name="cols"><xsl:value-of select="/ow:wiki/ow:userpreferences/ow:cols"/></xsl:attribute><xsl:value-of select="ow:page/ow:raw/text()"/></textarea><br />
            <input type="checkbox" name="rc" value="1">
              <xsl:if test="ow:page/ow:change/@minor='false' and not(starts-with(ow:page/ow:raw/text(), '#MINOREDIT'))">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
            </input>
            Include page in
            <a href="{/ow:wiki/ow:scriptname}?p=RecentChanges" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=RecentChanges&amp;a=print'); return false;">Recent Changes</a>
            <a href="{/ow:wiki/ow:scriptname}?p=RecentChanges" onclick="javascript:openw('{/ow:wiki/ow:scriptname}?p=RecentChanges&amp;a=print'); return false;"><img src="ow/images/popup.gif" width="15" height="9" border="0" alt="" /></a>
            list.
            <br />
            <br />
            Optional comment about this change:
            <br />
            <input type="text" name="comment" style="color:#333333; width:100%" maxlength="1000"><xsl:attribute name="size"><xsl:value-of select="/ow:wiki/ow:userpreferences/ow:cols"/></xsl:attribute><xsl:attribute name="value"><xsl:value-of select="ow:page/ow:change/ow:comment/text()"/></xsl:attribute></input>
            <br />
            <input type="hidden" name="revision" value="{ow:page/@revision}" />
            <input type="hidden" name="newrev" value="{ow:page/ow:change/@revision}" />
            <input type="hidden" name="p" value="{$name}" />
            <input type="submit" name="save" value="Save" />
            &#160;
            <input type="button" name="prev2" value="Preview" onclick="javascript:preview();" />
            <!-- <input type="submit" name="preview" value="Preview" /> -->
            &#160;
            <input type="button" name="cancel" value="Cancel" onClick="javascript:window.location='{/ow:wiki/ow:scriptname}?p={$name}';" />
        </form>

        <xsl:if test="ow:page/ow:body">
          <!-- this shows the preview, pre 0.74 versions -->
             <a name="preview"/>
             <hr size="1" />
             <h1>Preview</h1>
             <hr size="1" />
             <xsl:apply-templates select="ow:page/ow:body"/>
             <hr size="1" />
          <!-- end preview -->
        </xsl:if>
    </body>
  </html>
</xsl:template>



<xsl:template match="/ow:wiki" mode="print">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
      <h2>
        <a name="h0" class="same"><xsl:value-of select="ow:page/ow:link"/></a>
      </h2>
      <xsl:apply-templates select="ow:page/ow:body"/>
    </body>
  </html>
</xsl:template>



<xsl:template match="/ow:wiki" mode="naked">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
      <xsl:attribute name="ondblclick">location.href='<xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=edit<xsl:if test='ow:page/@revision'>&amp;revision=<xsl:value-of select="ow:page/@revision"/></xsl:if>'</xsl:attribute>
      <h2>
        <a name="h0" class="same"><xsl:value-of select="ow:page/ow:link"/></a>
      </h2>
      <xsl:apply-templates select="ow:page/ow:body"/>
    </body>
  </html>
</xsl:template>


<xsl:template match="/ow:wiki" mode="embedded">
    <xsl:apply-templates select="ow:page/ow:body"/>
</xsl:template>



<xsl:template match="ow:diff">
    <pre class="diff">
        <xsl:apply-templates/>
    </pre>
</xsl:template>

<xsl:template match="/ow:wiki" mode="diff">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
        <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
        <xsl:call-template name="brandingImage"/>
        <h1>
          <a class="same" href="{ow:scriptname}?a=fullsearch&amp;txt={$name}&amp;fromtitle=true" title="Do a full text search for {ow:page/ow:link/text()}">
            <xsl:value-of select="ow:page/ow:link/text()"/>
          </a>
        </h1>
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <hr noshade="noshade" size="1" />

        <xsl:choose>
            <xsl:when test="ow:diff = ''">
                <b>No difference available. This is the first <xsl:value-of select="ow:diff/@type"/> revision.</b>
                <hr noshade="noshade" size="1"/>
                <xsl:apply-templates select="ow:trail"/>
                <xsl:if test='ow:page/@revision'>
                    <b>Showing revision <xsl:value-of select="ow:page/@revision"/></b>
                    <p></p>
                </xsl:if>
                <xsl:apply-templates select="ow:page/ow:body"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not(ow:diff/@type='selected')">
                    <b>Difference from prior <xsl:value-of select="ow:diff/@type"/>
                    revision<xsl:if test="not(ow:diff/@to = ow:page/@lastminor)"> relative to revision
                    <xsl:value-of select="ow:diff/@to"/>
                    </xsl:if>.</b>
                </xsl:if>
                <xsl:if test="ow:diff/@type='selected'">
                    <b>Difference from revision <xsl:value-of select="ow:diff/@from"/> to
                    <xsl:choose>
                        <xsl:when test="ow:diff/@to = ow:page/@lastminor">
                            the current revision.
                        </xsl:when>
                        <xsl:otherwise>
                            revision <xsl:value-of select="ow:diff/@to"/>.
                        </xsl:otherwise>
                    </xsl:choose>
                    </b>
                </xsl:if>
                <br />
                <xsl:if test="not(ow:diff/@type='major')">
                    <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=diff</xsl:attribute>major diff</a>
                    <xsl:text> </xsl:text>
                </xsl:if>
                <xsl:if test="not(ow:diff/@type='minor')">
                    <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=diff&amp;diff=1</xsl:attribute>minor diff</a>
                    <xsl:text> </xsl:text>
                </xsl:if>
                <xsl:if test="not(ow:diff/@type='author')">
                    <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=diff&amp;diff=2</xsl:attribute>author diff</a>
                    <xsl:text> </xsl:text>
                </xsl:if>
                <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/><xsl:if test="ow:diff/@to">&amp;revision=<xsl:value-of select="ow:diff/@to"/></xsl:if></xsl:attribute>hide diff</a>
                <p></p>
                <xsl:apply-templates select="ow:diff"/>
            </xsl:otherwise>
        </xsl:choose>

        <form name="f" method="get">
        <xsl:attribute name="action"><xsl:value-of select="/ow:wiki/ow:scriptname"/></xsl:attribute>
        <hr size="1" />
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <br />
        <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=edit<xsl:if test='ow:page/@revision'>&amp;revision=<xsl:value-of select="ow:page/@revision"/></xsl:if></xsl:attribute>Edit <xsl:if test='ow:page/@revision'>revision <xsl:value-of select="ow:page/@revision"/> of</xsl:if> this page</a>
        <xsl:if test="ow:page/@revision or (ow:page/ow:change and not(ow:page/ow:change/@revision = 1))">
            |
            <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=changes</xsl:attribute>View other revisions</a>
        </xsl:if>
        <xsl:if test='ow:page/@revision'>
            |
            <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/></xsl:attribute>View current revision</a>
        </xsl:if>
        <br />
        <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=print&amp;revision=<xsl:value-of select="ow:page/ow:change/@revision"/></xsl:attribute>Print this page</a>
        |
        <a class="same"><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=diff&amp;revision=<xsl:value-of select="ow:change/@revision"/>&amp;xml=1</xsl:attribute>View XML</a>
        <br />
        <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=FindPage&amp;txt=<xsl:value-of select="$name"/></xsl:attribute>Find page</a> by browsing, searching or an index
        <br />
        <xsl:if test="not(ow:page/@changes='0')">
            Edited <xsl:value-of select="string(ow:page/ow:change/ow:date)"/>
            <xsl:text> </xsl:text>
            <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/><xsl:if test="ow:diff/@to">&amp;revision=<xsl:value-of select="ow:diff/@to"/></xsl:if></xsl:attribute>(hide diff)</a>
            <br />
        </xsl:if>
        <input type="hidden" name="a" value="fullsearch"/>
        <input type="text" name="txt" size="30"/> <input type="submit" value="Search"/>
        </form>
        </body>
  </html>
</xsl:template>





<xsl:template match="ow:recentchanges" mode="shortversion">
    <table cellspacing="0" cellpadding="2" border="0">
    <xsl:for-each select="ow:page">
        <tr>
        <xsl:choose>
            <xsl:when test='not(substring-before(./preceding-sibling::*[position()=1]/ow:change/ow:date, "T") = substring-before(ow:change/ow:date, "T"))'>
                <td width="1%" class="rc" nowrap="nowrap"><xsl:value-of select="ow:formatShortDate(string(ow:change/ow:date))"/></td>
            </xsl:when>
            <xsl:otherwise>
                <td width="1%" class="rc">&#160;</td>
            </xsl:otherwise>
        </xsl:choose>
        <td class="rc">
        <xsl:value-of select="ow:formatTime(string(ow:change/ow:date))"/>
        -
        <xsl:apply-templates select="ow:link"/>&#160;<xsl:if test="ow:change/@status='new'"><span class="new">new</span></xsl:if><xsl:if test="ow:change/@status='deleted'"><span class="deprecated">deprecated</span></xsl:if>
        </td>
        </tr>
    </xsl:for-each>
    </table>
</xsl:template>

<xsl:template match="ow:recentchanges">
    <xsl:choose>
        <xsl:when test="@short='true'">
            <xsl:apply-templates select="." mode="shortversion"/>
        </xsl:when>
        <xsl:otherwise>
            <table cellspacing="0" cellpadding="2" width="100%" border="0">
            <xsl:for-each select="ow:page">
                <xsl:if test='not(substring-before(./preceding-sibling::*[position()=1]/ow:change/ow:date, "T") = substring-before(ow:change/ow:date, "T"))'>
                    <tr class="rc">
                        <td colspan="4">&#160;</td>
                    </tr>
                    <tr class="rc">
                        <td colspan="4"><b><xsl:value-of select="string(ow:change/ow:date)"/></b></td>
                    </tr>
                </xsl:if>
                <tr class="rc">
                    <td align="left" width="1%"><xsl:value-of select="ow:formatTime(string(ow:change/ow:date))"/></td>
                    <td align="left" width="25%" nowrap="nowrap"><xsl:if test="@changes > 1">[<a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="string(@name)"/>&amp;a=diff</xsl:attribute>diff</a>] <xsl:text> </xsl:text> [<xsl:value-of select="@changes"/>&#160;<a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="string(@name)"/>&amp;a=changes</xsl:attribute>changes</a>]</xsl:if>&#160;</td>
                    <td align="left"><a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="string(@name)"/></xsl:attribute><xsl:value-of select="ow:link/text()"/></a>&#160;<xsl:if test="ow:change/@status='new'"><span class="new">new</span></xsl:if><xsl:if test="ow:change/@status='deleted'"><span class="deprecated">deprecated</span></xsl:if></td>

                    <xsl:choose>
                      <xsl:when test="ow:change/ow:by/@alias">
                        <td align="left"><a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="string(ow:change/ow:by/@alias)"/></xsl:attribute><xsl:value-of select="ow:change/ow:by/text()"/></a></td>
                      </xsl:when>
                      <xsl:otherwise>
                        <td align="left"><xsl:value-of select="ow:change/ow:by/@name"/></td>
                      </xsl:otherwise>
                    </xsl:choose>

                </tr>
                <xsl:if test="ow:change/ow:comment">
                    <tr class="rc">
                        <td align="left" colspan="2">&#160;</td>
                        <td align="left" colspan="2" class="comment"><xsl:value-of select="ow:change/ow:comment"/></td>
                    </tr>
                </xsl:if>

                <xsl:for-each select="ow:change/ow:attachmentchange">
                    <tr class="rc">
                        <td colspan="4">
                            <xsl:apply-templates select="."/>
                        </td>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
            </table>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<xsl:template match="ow:recentchanges_original">
    <ul>
    <xsl:for-each select="ow:page">
        <xsl:if test='not(substring-before(./preceding-sibling::*[position()=1]/ow:change/ow:date, "T") = substring-before(ow:change/ow:date, "T"))'>
          <xsl:text disable-output-escaping="yes">&lt;/ul&gt;</xsl:text>
            <b><xsl:value-of select="string(ow:change/ow:date)"/></b>
          <xsl:text disable-output-escaping="yes">&lt;ul&gt;</xsl:text>
        </xsl:if>
        <li>
            <xsl:value-of select="ow:formatTime(string(ow:change/ow:date))"/>
            -
            <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="@name"/></xsl:attribute><xsl:value-of select="ow:link/text()"/></a>
            <xsl:if test="ow:change/@status='new'">
              <xsl:text> </xsl:text>
              <span class="new">new</span>
            </xsl:if>
            <xsl:text> </xsl:text>
            <xsl:if test="@changes > 1">
                (<a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="@name"/>&amp;a=diff</xsl:attribute>diff</a>)
                (<xsl:value-of select="@changes"/>&#160;<a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="@name"/>&amp;a=changes</xsl:attribute>changes</a>)
            </xsl:if>
            <xsl:if test="ow:change/ow:comment">
                <xsl:text> </xsl:text>
                <b>[<xsl:value-of select="ow:change/ow:comment"/>]</b>
            </xsl:if>
            . . . . . .
            <xsl:choose>
              <xsl:when test="ow:change/ow:by/@alias">
                <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="ow:change/ow:by/@alias"/></xsl:attribute><xsl:value-of select="ow:change/ow:by/text()"/></a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="ow:change/ow:by/@name"/>
              </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:for-each>
    </ul>
</xsl:template>


<xsl:template match="ow:wiki" mode="changes">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
        <h1>History of "<xsl:value-of select="ow:page/ow:link/text()"/>"</h1>
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <hr size="1" />
        <ul>
        <xsl:for-each select="ow:page/ow:change">
            <li>
                Revision:
                <xsl:value-of select="@revision"/>
                . .
                <xsl:value-of select="string(ow:date)"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="ow:formatTime(string(ow:date))"/>
                <xsl:text> </xsl:text>
                <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;revision=<xsl:value-of select="@revision"/></xsl:attribute>View</a>
                <xsl:if test="position() > 1">
                    (<a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?p=<xsl:value-of select="$name"/>&amp;a=diff&amp;difffrom=<xsl:value-of select="@revision"/></xsl:attribute>diff</a>)
                </xsl:if>
                . . . . . .
                <xsl:choose>
                  <xsl:when test="ow:by/@alias">
                    <a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="string(ow:by/@alias)"/></xsl:attribute><xsl:value-of select="ow:by/text()"/></a>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="ow:by/@name"/>
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="ow:comment">
                    <br />
                    <xsl:text> </xsl:text>
                    <span class="comment"><xsl:value-of select="ow:comment"/></span>
                </xsl:if>
            </li>
        </xsl:for-each>
        </ul>
        <form name="f" method="get">
        <xsl:attribute name="action"><xsl:value-of select="/ow:wiki/ow:scriptname"/></xsl:attribute>
        <hr size="1" />
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <br />
        <input type="hidden" name="a" value="fullsearch"/>
        <input type="text" name="txt" size="30"><xsl:attribute name="value"><xsl:value-of select="ow:titlesearch/@value"/></xsl:attribute></input> <input type="submit" value="Search"/>
        </form>
      </body>
  </html>
</xsl:template>


<xsl:template match="ow:titleindex">
    <center>
    <xsl:for-each select="ow:page">
        <xsl:if test="not(substring(./preceding-sibling::*[position()=1]/@name, 1, 1) = substring(@name, 1, 1))">
            <a><xsl:attribute name="href">#<xsl:value-of select="substring(@name, 1, 1)"/></xsl:attribute><xsl:value-of select="substring(@name, 1, 1)"/></a>
            <xsl:text> </xsl:text>
        </xsl:if>
    </xsl:for-each>
    </center>
    <xsl:for-each select="ow:page">
        <xsl:if test="not(substring(./preceding-sibling::*[position()=1]/@name, 1, 1) = substring(@name, 1, 1))">
            <br />
            <a><xsl:attribute name="name"><xsl:value-of select="substring(@name, 1, 1)"/></xsl:attribute></a>
            <b><xsl:value-of select="substring(@name, 1, 1)"/></b>
            <br />
        </xsl:if>
        <xsl:apply-templates select="ow:link"/>
        <br />
    </xsl:for-each>
</xsl:template>


<xsl:template match="ow:wordindex">
    <center>
    <xsl:for-each select="ow:word">
        <xsl:if test="not(substring(./preceding-sibling::*[position()=1]/@value, 1, 1) = substring(@value, 1, 1))">
            <a><xsl:attribute name="href">#<xsl:value-of select="substring(@value, 1, 1)"/></xsl:attribute><xsl:value-of select="substring(@value, 1, 1)"/></a>
        </xsl:if>
        <xsl:text> </xsl:text>
    </xsl:for-each>
    </center>
    <xsl:text disable-output-escaping="yes">&lt;ul&gt;</xsl:text>
    <xsl:for-each select="ow:word">
        <xsl:if test="not(substring(./preceding-sibling::*[position()=1]/@value, 1, 1) = substring(@value, 1, 1))">
            <xsl:text disable-output-escaping="yes">&lt;/ul&gt;</xsl:text>
            <a><xsl:attribute name="name"><xsl:value-of select="substring(@value, 1, 1)"/></xsl:attribute></a>
            <b><xsl:value-of select="substring(@value, 1, 1)"/></b>
            <xsl:text disable-output-escaping="yes">&lt;ul&gt;</xsl:text>
        </xsl:if>
        <xsl:if test="not(./preceding-sibling::*[position()=1]/@value = @value)">
            <xsl:text disable-output-escaping="yes">&lt;/ul&gt;</xsl:text>
            <b><xsl:value-of select="@value"/></b>
            <xsl:text disable-output-escaping="yes">&lt;ul&gt;</xsl:text>
        </xsl:if>
        <li><xsl:apply-templates select="ow:page/ow:link"/></li>
    </xsl:for-each>
    <xsl:text disable-output-escaping="yes">&lt;/ul&gt;</xsl:text>
</xsl:template>


<xsl:template match="ow:randompages">
    <xsl:choose>
        <xsl:when test='count(ow:page)=1'>
            <xsl:apply-templates select="ow:page/ow:link"/>
        </xsl:when>
        <xsl:otherwise>
            <ul>
            <xsl:for-each select="ow:page">
                <li><xsl:apply-templates select="ow:link"/></li>
            </xsl:for-each>
            </ul>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<xsl:template match="ow:titlesearch">
    <ul>
    <xsl:for-each select="ow:page">
      <li>
        <xsl:if test="contains(@name, '/')">
            ....
        </xsl:if>
        <xsl:apply-templates select="ow:link"/>
      </li>
    </xsl:for-each>
    </ul>
</xsl:template>


<xsl:template match="/ow:wiki" mode="titlesearch">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
        <xsl:call-template name="brandingImage"/>
        <h1>Title search for "<xsl:value-of select="ow:titlesearch/@value"/>"</h1>
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <hr size="1" />
        <xsl:apply-templates select="ow:titlesearch"/>
        <xsl:value-of select="count(ow:titlesearch/ow:page)"/> hits out of
        <xsl:value-of select="ow:titlesearch/@pagecount"/> pages searched.

        <form name="f" method="get">
        <xsl:attribute name="action"><xsl:value-of select="/ow:wiki/ow:scriptname"/></xsl:attribute>
        <hr size="1" />
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <br />
        <input type="hidden" name="a" value="fullsearch"/>
        <input type="text" name="txt" size="30"><xsl:attribute name="value"><xsl:value-of select="ow:titlesearch/@value"/></xsl:attribute></input> <input type="submit" value="Search"/>
        </form>
    </body>
  </html>
</xsl:template>


<xsl:template match="ow:fullsearch">
    <ul>
    <xsl:for-each select="ow:page">
      <li>
        <xsl:if test="contains(@name, '/')">
            ....
        </xsl:if>
        <xsl:apply-templates select="ow:link"/>
      </li>
    </xsl:for-each>
    </ul>
</xsl:template>


<xsl:template match="/ow:wiki" mode="fullsearch">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
        <xsl:call-template name="brandingImage"/>
        <h1>Full text search for "<xsl:value-of select="ow:fullsearch/@value"/>"</h1>
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <hr size="1" />
        <xsl:apply-templates select="ow:fullsearch"/>
        <xsl:value-of select="count(ow:fullsearch/ow:page)"/> hits out of
        <xsl:value-of select="ow:fullsearch/@pagecount"/> pages searched.

        <form name="f" method="get">
        <xsl:attribute name="action"><xsl:value-of select="/ow:wiki/ow:scriptname"/></xsl:attribute>
        <hr size="1" />
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <br />
        <input type="hidden" name="a" value="fullsearch"/>
        <input type="text" name="txt" size="30"><xsl:attribute name="value"><xsl:value-of select="ow:fullsearch/@value"/></xsl:attribute></input> <input type="submit" value="Search"/>
        </form>
    </body>
  </html>
</xsl:template>


<xsl:template match="ow:message">
    <xsl:if test="@code='userpreferences_saved'">
      <b>User preferences saved successfully.</b>
    </xsl:if>
    <xsl:if test="@code='userpreferences_cleared'">
      <b>User preferences cleared successfully.</b>
    </xsl:if>
</xsl:template>


<xsl:template match="ow:userpreferences">
    <form name="f" method="post">
      <xsl:attribute name="action"><xsl:value-of select="/ow:wiki/ow:scriptname"/></xsl:attribute>
      <table>
        <tr><td>Username:</td><td><input type="text" name="username" ondblclick="event.cancelBubble=true;"><xsl:attribute name="value"><xsl:value-of select="/ow:wiki/ow:userpreferences/ow:username"/></xsl:attribute></input></td></tr>
        <tr><td>Bookmarks:</td><td><input type="text" name="bookmarks" size="60" ondblclick="event.cancelBubble=true;"><xsl:attribute name="value"><xsl:for-each select="/ow:wiki/ow:userpreferences/ow:bookmarks/ow:link"><xsl:value-of select="@name"/><xsl:text> </xsl:text></xsl:for-each></xsl:attribute></input></td></tr>
        <tr><td colspan="2">Edit form columns: <input type="text" name="cols" size="3" ondblclick="event.cancelBubble=true;"><xsl:attribute name="value"><xsl:value-of select="/ow:wiki/ow:userpreferences/ow:cols"/></xsl:attribute></input> rows: <input type="text" name="rows" size="3" ondblclick="event.cancelBubble=true;"><xsl:attribute name="value"><xsl:value-of select="/ow:wiki/ow:userpreferences/ow:rows"/></xsl:attribute></input></td></tr>
        <tr>
          <td colspan="2">
            <input type="checkbox" name="prettywikilinks" value="1">
              <xsl:if test="/ow:wiki/ow:userpreferences/ow:prettywikilinks"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            Show pretty wiki links
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="checkbox" name="bookmarksontop" value="1">
              <xsl:if test="/ow:wiki/ow:userpreferences/ow:bookmarksontop"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            Show bookmarks on top
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="checkbox" name="editlinkontop" value="1">
              <xsl:if test="/ow:wiki/ow:userpreferences/ow:editlinkontop"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            Show edit link on top
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="checkbox" name="trailontop" value="1">
              <xsl:if test="/ow:wiki/ow:userpreferences/ow:trailontop"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            Show trail on top
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="checkbox" name="opennew" value="1">
              <xsl:if test="/ow:wiki/ow:userpreferences/ow:opennew"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            Open external links in new window
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="checkbox" name="emoticons" value="1">
              <xsl:if test="/ow:wiki/ow:userpreferences/ow:emoticons"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            Show emoticons in text <small>(goto <a href="?HelpOnEmoticons">HelpOnEmoticons</a>)</small>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="submit" name="save" value="Save Preferences"/>
            &#160;&#160;
            <input type="submit" name="clear" value="Clear Preferences"/>
          </td>
        </tr>
      </table>
      <input type="hidden" name="p"><xsl:attribute name="value"><xsl:value-of select="/ow:wiki/ow:page/@name"/></xsl:attribute></input>
      <input type="hidden" name="a" value="userpreferences"/>
    </form>
</xsl:template>


<xsl:template match="/ow:wiki" mode="login">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="this.document.f.pwd.focus();">
        <table width="100%" height="100%">
          <tr>
            <td align="center" valign="center">
                <table border="0" cellspacing="0" cellpadding="70" bgcolor="#eeeeee">
                  <tr><td>
                    <xsl:if test="ow:login/@mode='edit'">
                      <b>Enter password to edit content</b>
                      <br />
                      <br />
                    </xsl:if>
                    <xsl:apply-templates select="ow:error"/>
                    <table>
                    <form name="f" method="post" action="{/ow:wiki/ow:scriptname}?a=login&amp;mode={ow:login/@mode}">
                    <tr><td>password</td><td><input type="password" name="pwd" size="10"/>
                    <xsl:text> </xsl:text>
                    <input type="submit" name="submit" value="let me in!"/>
                    </td></tr>
                    <tr><td>&#160;</td><td>
                    <input type="checkbox" name="r" value="1">
                      <xsl:if test="ow:login/ow:rememberme='false'">
                        <xsl:attribute name="checked">checked</xsl:attribute>
                      </xsl:if>
                    </input>
                    Remember me
                    </td></tr>
                    <input type="hidden" name="backlink">
                      <xsl:attribute name="value"><xsl:value-of select="ow:login/ow:backlink"/></xsl:attribute>
                    </input>
                    </form>
                    </table>
                  </td></tr>
                </table>
            </td>
          </tr>
        </table>
    </body>
  </html>
</xsl:template>

</xsl:stylesheet>
