<?xml version="1.0"?>
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
<xsl:output method="xml"/>


<!-- this shows the table of files -->
<xsl:template match="ow:attachments">
    <xsl:param name="showhidden"/>
    <xsl:param name="showactions"/>
        <xsl:if test="$showhidden='true' or count(ow:attachment[@hidden='false' and @deprecated='false']) &gt; 0">
        <p />
        <table cellspacing="0" cellpadding="2" border="0" width="100%">
          <tr bgcolor="#eeeeee">
            <td width="1%">&#160;</td>
            <td class="fileattr" width="18%">Filename</td>
            <td class="fileattr" width="5%" align="right">Size&#160;&#160;&#160;</td>
            <td class="fileattr" width="10%">Modified</td>
            <td class="fileattr" width="15%">By</td>
            <td class="fileattr" width="50%">Comment</td>
            <td class="fileattr" width="1%">&#160;</td>
          </tr>
          <xsl:apply-templates select="ow:attachment">
              <xsl:with-param name="showhidden"><xsl:value-of select="$showhidden"/></xsl:with-param>
              <xsl:with-param name="showactions"><xsl:value-of select="$showactions"/></xsl:with-param>
          </xsl:apply-templates>
        </table>
    </xsl:if>
</xsl:template>


<!-- this shows one line in the table of files -->
<xsl:template match="ow:attachments/ow:attachment">
    <xsl:param name="showhidden"/>
    <xsl:param name="showactions"/>
    <xsl:if test="$showhidden='true' or (@hidden='false' and @deprecated='false')">
        <tr bgcolor="#ffffff" valign="top">
            <td nowrap="nowrap" class="fileattr">
                <a href="{ow:file/@href}" target="_blank"><img src="{/ow:wiki/ow:location}{/ow:wiki/ow:iconpath}/doc/{ow:file/@icon}.gif" border="0" hspace="2" width="16" height="16" /></a>
            </td>
            <td nowrap="nowrap" class="fileattr">
                <a href="{ow:file/@href}"><xsl:value-of select="ow:file"/></a> (v<xsl:value-of select="@revision"/>)&#160;
            </td>
            <td nowrap="nowrap" align="right" class="fileattr"><xsl:value-of select="ow:file/@size" /> KB&#160;&#160;&#160;</td>
            <td nowrap="nowrap" class="fileattr"><xsl:value-of select="ow:formatShortDateTime2(string(ow:date))"/>&#160;&#160;</td>

            <xsl:choose>
              <xsl:when test="ow:by/@alias">
                <td align="left" class="fileattr"><a><xsl:attribute name="href"><xsl:value-of select="/ow:wiki/ow:scriptname"/>?<xsl:value-of select="ow:urlencode(string(ow:by/@alias))"/></xsl:attribute><xsl:value-of select="ow:by/text()"/></a></td>
              </xsl:when>
              <xsl:otherwise>
                <td align="left" class="fileattr"><xsl:value-of select="ow:by/@name"/></td>
              </xsl:otherwise>
            </xsl:choose>

            <td class="fileattr"><xsl:if test="@deprecated='true'"><font color="#ff0000"><b>This file will be permanently destroyed.</b></font><xsl:if test="ow:comment"><br /></xsl:if></xsl:if> <xsl:value-of select="ow:comment"/>&#160;</td>
            <td class="fileattr" nowrap="nowrap" align="right">
                &#160;
                <xsl:if test="$showactions='true'">
                    <xsl:choose>
                        <xsl:when test="@deprecated='true'">
                            &#160;
                        </xsl:when>
                        <xsl:when test="@hidden='true'">
                            <a href="{/ow:wiki/ow:scriptname}?p={$name}&amp;a=undohidefile&amp;file={ow:file}&amp;rev={@revision}" title="make visible on wikipage"><img src="ow/images/hidden_on.gif" border="0" hspace="2" width="12" height="12" alt="make visible on wikipage" /></a>
                        </xsl:when>
                        <xsl:otherwise>
                            <a href="{/ow:wiki/ow:scriptname}?p={$name}&amp;a=hidefile&amp;file={ow:file}&amp;rev={@revision}" title="hide file from wikipage"><img src="ow/images/hidden_off.gif" border="0" hspace="2" width="12" height="12" alt="hide file from wikipage" /></a>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="@deprecated='true'">
                            <a href="{/ow:wiki/ow:scriptname}?p={$name}&amp;a=undotrashfile&amp;file={ow:file}&amp;rev={@revision}" title="restore file"><img src="ow/images/undo.gif" border="0" hspace="2" width="16" height="12" alt="restore" /></a>
                        </xsl:when>
                        <xsl:otherwise>
                            <a href="{/ow:wiki/ow:scriptname}?p={$name}&amp;a=trashfile&amp;file={ow:file}&amp;rev={@revision}" title="trash file"><img src="ow/images/delico.gif" border="0" hspace="4" width="12" height="12" alt="trash" /></a>
                        </xsl:otherwise>
                    </xsl:choose>
                    <a href="{/ow:wiki/ow:scriptname}?p={$name}&amp;a=attachchanges&amp;file={ow:file}" title="view other revisions"><img src="ow/images/revs.gif" border="0" width="12" height="12" alt="view other revisions" /></a>
                </xsl:if>
            </td>
        </tr>
    </xsl:if>
</xsl:template>


<!-- this will show when you link the attachment in wiki pages -->
<xsl:template match="ow:attachment">
    <xsl:choose>
        <xsl:when test="ow:file/@image='true'">
            <img src="{ow:file/@href}"><xsl:attribute name="title">Last changed: <xsl:value-of select="ow:formatLongDate(string(ow:date))"/></xsl:attribute></img>
        </xsl:when>
        <xsl:otherwise>
            <!-- <a href="{ow:file/@href}" target="_blank"><img src="{/ow:wiki/ow:location}{/ow:wiki/ow:iconpath}/doc/{ow:file/@icon}.gif" border="0" hspace="2" width="16" height="16" /></a> -->
            <a href="{ow:file/@href}"><xsl:attribute name="title">Last changed: <xsl:value-of select="ow:formatLongDate(string(ow:date))"/></xsl:attribute><xsl:choose><xsl:when test="not(text()='')"><xsl:value-of select="text()"/></xsl:when><xsl:otherwise><xsl:value-of select="ow:file"/></xsl:otherwise></xsl:choose></a>
            <small>&#160;(<xsl:value-of select="ow:file/@size" /> KB)</small>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<!-- this is the line that will appear in the RecentChanges page -->
<xsl:template match="ow:attachmentchange">
    <div align="right" class="fileattr">
            (<xsl:value-of select="ow:formatTime(string(ow:date))"/>)
            &#160;
            <xsl:value-of select="@name" /> (v<xsl:value-of select="@revision"/>)
            <xsl:value-of select="ow:action"/>
            by
            <xsl:choose>
              <xsl:when test="ow:by/@alias">
                <a href="{/ow:wiki/ow:scriptname}?{ow:urlencode(string(ow:by/@alias))}"><xsl:value-of select="ow:by/text()"/></a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="ow:by/@name"/>
              </xsl:otherwise>
            </xsl:choose>
    </div>
</xsl:template>


<!-- this shows the page when you click on the "Attachment" link -->
<xsl:template match="/ow:wiki" mode="attach">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
        <xsl:call-template name="brandingImage"/>
        <h1>Attachments for <xsl:value-of select="ow:page/@name"/></h1>
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <hr size="1" />

        <small>
            Back to <a href="{ow:wiki/ow:scriptname}?{$name}"><xsl:value-of select="ow:page/@name" /></a>.
        </small>

        <xsl:if test="ow:page/ow:attachments">
            <hr size="1" />
            <xsl:apply-templates select="ow:page/ow:attachments">
                <xsl:with-param name="showhidden">true</xsl:with-param>
                <xsl:with-param name="showactions">true</xsl:with-param>
            </xsl:apply-templates>
        </xsl:if>

        <hr size="1" />
        To upload a document, enter the full path to a file stored on
        your computer, or select "Browse" or "Choose" to find and select a file.

        <form name="fup" method="post" action="{/ow:wiki/ow:scriptname}?p={$name}&amp;a=upload" enctype="multipart/form-data">
          <table cellspacing="0" cellpadding="2" border="0">
            <tr>
              <td>File:</td>
              <td><input type="file" name="file" size="60" /></td>
            </tr>
            <tr>
              <td>Comment:</td>
              <td><input type="text" name="comment" size="60" /></td>
            </tr>
            <tr>
              <td>&#160;</td>
              <td><input type="checkbox" name="link" value="1" />Create a link to the attached file at the end of the WikiPage.</td>
            </tr>
            <tr>
              <td>&#160;</td>
              <td><input type="checkbox" name="hide" value="1" checked="checked" />Hide the attached file in normal view.</td>
            </tr>
            <tr>
              <td>&#160;</td>
              <td>
                <input type="submit" value="Upload"/>
                &#160;
                <input type="button" name="cancel" value="Cancel" onClick="javascript:window.location='{/ow:wiki/ow:scriptname}?p={$name}';" />
              </td>
            </tr>
          </table>
        </form>
        <br />

        <form name="f" method="get" action="{/ow:wiki/ow:scriptname}">
            <hr size="1" />
            <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
            <br />
            <input type="hidden" name="a" value="fullsearch"/>
            <input type="text" name="txt" size="30"><xsl:attribute name="value"><xsl:value-of select="ow:fullsearch/@value"/></xsl:attribute></input> <input type="submit" value="Search"/>
        </form>
    </body>
  </html>
</xsl:template>


<!-- this shows the history of changes to a file -->
<xsl:template match="/ow:wiki" mode="attachchanges">
  <xsl:call-template name="pi"/>
  <html>
  <xsl:call-template name="head"/>
    <body bgcolor="#ffffff" onload="window.defaultStatus='{$brandingText}'">
        <xsl:call-template name="brandingImage"/>
        <h1>History of <xsl:value-of select="ow:page/ow:attachments/ow:attachment/@name"/></h1>
        <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
        <hr size="1" />

        <xsl:apply-templates select="ow:page/ow:attachments">
            <xsl:with-param name="showhidden">true</xsl:with-param>
            <xsl:with-param name="showactions">false</xsl:with-param>
        </xsl:apply-templates>

        <form name="f" method="get" action="{/ow:wiki/ow:scriptname}">
            <hr size="1" />
            <xsl:apply-templates select="ow:userpreferences/ow:bookmarks"/>
            <br />
            <input type="hidden" name="a" value="fullsearch"/>
            <input type="text" name="txt" size="30"><xsl:attribute name="value"><xsl:value-of select="ow:fullsearch/@value"/></xsl:attribute></input> <input type="submit" value="Search"/>
        </form>
    </body>
  </html>
</xsl:template>


</xsl:stylesheet>