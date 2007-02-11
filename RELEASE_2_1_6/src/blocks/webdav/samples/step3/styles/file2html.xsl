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

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="file"></xsl:param>
<xsl:param name="sitemapURI"></xsl:param>
<xsl:param name="requestURI"></xsl:param>

<xsl:template match="/page">
  <html>
    <body>
      <form method="get">
        <xsl:attribute name="action"><xsl:value-of select="substring-before($requestURI, $sitemapURI)"/>write/<xsl:value-of select="$file"/></xsl:attribute>
        <p>Title:<br />
        <input name="title" type="text" size="30" maxlength="30" value="{title}" />
        </p>
        <xsl:apply-templates select="content/para"/>
        <input type="submit" value="Submit" />
        <input type="reset" value="Reset" />
      </form>
    </body>
  </html>
</xsl:template>

<xsl:template match="content/para">
        <p>para:<br />
        <textarea name="para" cols="50" rows="10">
        <!--xsl:value-of select="normalize-space(.)"/-->
        <xsl:copy-of select="node()"/>
        </textarea>
        </p>
</xsl:template>

</xsl:stylesheet>
