<?xml version="1.0" encoding="UTF-8"?>
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

<!--+
    | Transforms menu.xml to the index page
    | CVS $Id: menu2content.xsl,v 1.1 2004/06/16 20:00:07 vgritsenko Exp $
    +-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="book">
    <page>
      <title>Input Modules</title>
      <table>
        <tr>
          <td>
            <h3>Input Modules</h3>
            <p>Modules are used to access input and output data in 
               modular way. Below there are samples for some of the
               available input modules.
            </p>
            <table class="table">
              <xsl:apply-templates select="menu/menu-item[@desc]"/>
            </table>
          </td>
        </tr>
      </table>
    </page>
  </xsl:template>

  <xsl:template match="menu-item[@desc]">
    <tr>
      <td>
        <a href="{@href}"><xsl:value-of select="@label" /></a>
      </td>
      <td>
        <xsl:value-of select="@desc" />
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
