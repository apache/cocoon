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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                              xmlns:n="http://www.betaversion.org/linotype/news/1.0"
                              xmlns="http://www.w3.org/1999/xhtml">

  <xsl:param name="home"/>

  <xsl:template match="/">
    <html>
      <head>
        <title><xsl:value-of select="n:news/n:title"/></title> 
        <link rel="stylesheet" type="text/css" href="{$home}/styles/main.css"/>
        <link rel="stylesheet" type="text/css" href="{$home}/styles/editor.css"/>
        <script type="text/javascript"><![CDATA[

            prefixImageSrc="image-";

            function doAction(action) {
                var form = document.forms[0];

                if (action == "delete") {
                    var agree = confirm("Are you sure you want to delete this news?");
                    if (!agree) return;
                } else if (action == "revert") {
                    var content = document.getElementById("previous_innerHTML").firstChild.nodeValue;
                    if (content != "") {
                        var agree = confirm("Are you sure you want to restore content to the previous version?");
                        if (agree) loadContent(content);
                    }
                    return;
                } else {
                    form.elements['date'].value = getDate();
                    form.elements['time'].value = getTime();
                    form.elements['fulldate'].value = getFullDate();
                    form.elements['innerHTML'].value = getInnerHTML();
                    form.elements['xml:content'].value = getContent();
                }

                form.elements['action'].value = action;
                form.submit();
            }
	 ]]></script>
        <script src="{$home}/scripts/editor.js" type="text/javascript"></script>
        <noscript>
          <h1>Dude, you don't go anywhere around here without Javascript enabled. ;-)</h1>
        </noscript>
      </head>

      <body onload="start(event)" onunload="stop(event)">
        <!--form name="data" action="/request" method="POST" enctype="multipart/form-data"-->
        <form name="data" action="{string('#{$continuation/id}')}.kont" method="POST" enctype="multipart/form-data">
          <input type="hidden" name="action"/>
          <input type="hidden" name="author" value="{string('#{userid}')}"/>
          <input type="hidden" name="date"/>
          <input type="hidden" name="time"/>
          <input type="hidden" name="fulldate"/>
          <input type="hidden" name="xml:content"/>
          <input type="hidden" name="innerHTML"/>

          <div id="toolbar">
            <table cellpadding="0" cellspacing="0">
              <tr>
                <td><div class="imagebutton" id="bold"><img src="{$home}/images/icons/bold.gif" alt="Strong" title="Strong"/></div></td>
                <td><div class="imagebutton" id="italic"><img src="{$home}/images/icons/italic.gif" alt="Emphasis" title="Emphasis"/></div></td>
                <td><div class="imagebutton" id="strikethrough"><img src="{$home}/images/icons/strikethrough.gif" alt="Error" title="Error"/></div></td>
                <td><div class="imagebutton" id="quote"><img src="{$home}/images/icons/quote.gif" alt="Quote" title="Quote"/></div></td>
                <td><div class="spacer"/></td>
                <td><div class="imagebutton" id="removeformat"><img src="{$home}/images/icons/removeformat.gif" alt="Remove Format" title="Remove Format"/></div></td>
                <td><div class="spacer"/></td>
                <td><div class="imagebutton" id="createlink"><img src="{$home}/images/icons/link.gif" alt="Link Selection" title="Link Selection"/></div></td>
                <td><div class="imagebutton" id="unlink"><img src="{$home}/images/icons/unlink.gif" alt="Unlink Selection" title="Unlink Selection"/></div></td>
                <td><div class="spacer"/></td>
                <td><div class="imagebutton" id="insertimage"><img src="{$home}/images/icons/image.gif" alt="Add Image" title="Add Image"/></div></td>
                <td><div class="imagebutton" id="inserthorizontalrule"><img src="{$home}/images/icons/horizontalrule.gif" alt="Add Horizontal Rule" title="Add Horizontal Rule"/></div></td>
                <td><div class="spacer"/></td>
                <td><div class="imagebutton" id="insertorderedlist"><img src="{$home}/images/icons/orderedlist.gif" alt="Ordered List" title="Ordered List"/></div></td>
                <td><div class="imagebutton" id="insertunorderedlist"><img src="{$home}/images/icons/unorderedlist.gif" alt="Unordered List" title="Unordered List"/></div></td>
                <td><div class="spacer"/></td>
                <td><div class="imagebutton" id="outdent"><img src="{$home}/images/icons/outdent.gif" alt="Outdent" title="Outdent"/></div></td>
                <td><div class="imagebutton" id="indent"><img src="{$home}/images/icons/indent.gif" alt="Indent" title="Indent"/></div></td>
                <td width="100%" align="center" style="white-space: nowrap;">
                  <span id="block_selector">
                    <xsl:text>Block: </xsl:text>
                    <select id="formatblock">
                      <option value="">Normal</option>
                      <option value="p">Paragraph</option>
                      <option value="h1">Heading 1</option>
                      <option value="h2">Heading 2</option>
                      <option value="h3">Heading 3</option>
                      <option value="h4">Heading 4</option>
                    </select>
                  </span>
                  <span id="class_selector">
                    <xsl:text>Class: </xsl:text>
                    <select id="alternatives">
                      <option>whatever</option>
                    </select>
                  </span>
                </td>
                <td style="white-space: nowrap;">
                  <input type="checkbox" name="online">
                    <xsl:if test="n:news/@online='on'">
                      <xsl:attribute name="checked">true</xsl:attribute>
                    </xsl:if>
                  </input>
                  <label for="online">published</label>
                </td>
              </tr>
            </table>
          </div>

          <xsl:apply-templates/>

          <div id="controls">
            <!--Version: <select name="version">
              <t:forEach select="{string('#{versions}')}" xmlns:t="http://apache.org/cocoon/templates/jx/1.0">
                <option>#{.}</option>
              </t:forEach>
            </select>
            <input type="button" value="Restore" onclick="doAction('restore')"/--> 
            <input type="button" value="Revert" onclick="doAction('revert')"/> 
            <input type="button" value="Delete" onclick="doAction('delete')"/> 
            <input type="button" value="Save" onclick="doAction('save')"/>
            <input type="button" value="Finish" onclick="doAction('finish')"/>
          </div>

        </form>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="n:news">
    <div id="page">
      <div class="field">Title:<br/><input type="text" name="title" size="40" value="{n:title}"/></div>
      <div id="image_inputs"/>
      <div class="field">
        <table cellpadding="0" cellspacing="0">
          <tr>
            <td align="left">Content:</td>
            <td align="right" width="100%"><input type="checkbox" id="wysiwyg-checkbox" onclick="wysiwyg(!this.checked)"/>WYSIWYG</td>
          </tr>
        </table>
        <iframe id="edit" src="content" width="100%" height="400px" scrolling="auto" frameborder="0">Get a modern browser</iframe>
        <div id="path">...</div>
      </div>
      <div id="previous_innerHTML">#{innerHTML}</div>
    </div>
  </xsl:template>

</xsl:stylesheet>
