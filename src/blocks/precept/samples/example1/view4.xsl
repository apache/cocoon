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
        <td>Firstname</td>
        <td>
          <xsl:value-of select="user/firstname/text()"/>
        </td>
      </tr>
      <tr>
        <td>Lastname</td>
        <td>
          <xsl:value-of select="user/lastname/text()"/>
        </td>
      </tr>
      <tr>
        <td>Email</td>
        <td>
          <xsl:value-of select="user/email/text()"/>
        </td>
      </tr>
      <tr>
        <td>Age</td>
        <td>
          <xsl:value-of select="user/age/text()"/>
        </td>
      </tr>
      <tr>
        <td>Installation number</td>
        <td>
          <xsl:value-of select="number/text()"/>
        </td>
      </tr>
      <tr>
        <td>Live URL</td>
        <td>
          <xsl:value-of select="live-url/text()"/>
        </td>
      </tr>
      <tr>
        <td>Please publish it as cocoon live-site example</td>
        <td>
          <xsl:value-of select="publish/text()"/>
        </td>
      </tr>
      <tr>
        <td>os</td>
        <td>
          <xsl:value-of select="system/os/text()"/>
        </td>
      </tr>
      <tr>
        <td>processor</td>
        <td>
          <xsl:value-of select="system/processor/text()"/>
        </td>
      </tr>
      <tr>
        <td>ram</td>
        <td>
          <xsl:value-of select="system/ram/text()"/>
        </td>
      </tr>
      <tr>
        <td>servlet engine</td>
        <td>
          <xsl:value-of select="system/servlet-engine/text()"/>
        </td>
      </tr>
      <tr>
        <td>java version</td>
        <td>
          <xsl:value-of select="system/java-version/text()"/>
        </td>
      </tr>
    </table>
    <input type="submit" name="cocoon-method-prev3" value="Prev Page"/>
    <input type="submit" name="cocoon-method-submit" value="Submit"/>
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

