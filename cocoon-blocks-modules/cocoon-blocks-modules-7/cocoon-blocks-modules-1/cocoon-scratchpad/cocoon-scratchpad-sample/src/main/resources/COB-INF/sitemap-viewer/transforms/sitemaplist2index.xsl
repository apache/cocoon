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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:dir="http://apache.org/cocoon/directory/2.0">

<xsl:template match="/">
    <html>
        <head>
            <xsl:if test="not(//dir)">
                <META HTTP-EQUIV="refresh" content="2;URL=index-list.html" /> 
            </xsl:if>
            <title>Cocoon Sitemap Viewer</title>
            <style type="text/css">
                td, th { vertical-align: top }
                pre.example { color: blue }
            </style>
        </head>
        <body>
            <xsl:copy-of select="/introhtml|/*/introhtml"/>
            <hr/>       
            <xsl:variable name="filecnthalf" select="(count(.//file)+1)  div 2"/>
            <table>
                <tbody>
                    <tr>
                        <td>
                            <xsl:apply-templates select=".//file[position() &lt;= $filecnthalf]"/>
                        </td>
                        <td>
                            <xsl:apply-templates select=".//file[position() &gt; $filecnthalf]"/>
                        </td>
                    </tr>
                </tbody>
            </table>

            <xsl:if test="not(//dir)">
                <b>Loading list of sitemaps.. . </b>
                <a href="index-list.html">click here if list won't load automatically    </a>
                <script type="text/javascript">
                    // open the list of sitemaps when this page is loaded...
                    //      window.location.href = "index-list.html"; 
                </script>
            </xsl:if>

        </body>
    </html>
</xsl:template>

<xsl:template match="file">
    <li>
        <a href="{concat(substring-before(@filename,'.xmap'),'._xmap','?location=',@path)}"><xsl:value-of select="."/></a>
    </li>
</xsl:template>

<xsl:template match="dir">
    <!--li>
        <a href="{.}"><xsl:value-of select="."/></a>
    </li-->
</xsl:template>


</xsl:stylesheet>
