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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:i="http://www.dff.st/ns/desire/instance/1.0">

  <xsl:template match="root">
    <html>
      <body>
        <form method="POST">
          <xsl:apply-templates/>
        </form>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="cocoon-installation">
    <table border="1">
      <tr>
        <td>os</td>
        <td>
          <select name="cocoon-installation/system/os">
            <option value="linux">
              <xsl:if test="system/os = 'linux'">
                <xsl:attribute name="selected"/>
              </xsl:if>
              Linux
            </option>
            <option value="w2k">
              <xsl:if test="system/os = 'w2k'">
                <xsl:attribute name="selected"/>
              </xsl:if>
              Windows 2k
            </option>
          </select>
        </td>
        <td>
          <xsl:apply-templates select="system/os/constraint"/>
        </td>
      </tr>
      <tr>
        <td>processor</td>
        <td>
          <input type="textbox" name="cocoon-installation/system/processor" value="{system/processor/text()}"/>
        </td>
        <td>
          <xsl:apply-templates select="system/processor/constraint"/>
        </td>
      </tr>
      <tr>
        <td>ram</td>
        <td>
          <input type="textbox" name="cocoon-installation/system/ram" value="{system/ram/text()}"/>
        </td>
        <td>
          <xsl:apply-templates select="system/ram/constraint"/>
        </td>
      </tr>
      <tr>
        <td>servlet engine</td>
        <td>
          <input type="textbox" name="cocoon-installation/system/servlet-engine" value="{system/servlet-engine/text()}"/>
        </td>
        <td>
          <xsl:apply-templates select="system/servlet-engine/constraint"/>
        </td>
      </tr>
      <tr>
        <td>java version</td>
        <td>
          <input type="textbox" name="cocoon-installation/system/java-version" value="{system/java-version/text()}"/>
        </td>
        <td>
          <xsl:apply-templates select="system/java-version/constraint"/>
        </td>
      </tr>
    </table>
    <input type="submit" name="cocoon-method-prev2" value="Prev Page"/>
    <input type="submit" name="cocoon-method-next4" value="Next Page"/>
  </xsl:template>

  <xsl:template match="/|*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>
</xsl:stylesheet>

