<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:dir="http://apache.org/cocoon/directory/2.0">

<xsl:template match="/">
    <html>
        <head>
            <xsl:if test="not(//dir)">
                <META HTTP-EQUIV="refresh" content="2;URL=index-list.html" /> 
            </xsl:if>
            <title></title>
            <style type="text/css">
                td, th { vertical-align: top }
                pre.example { color: blue }
            </style>
        </head>
        <body>
            <xsl:copy-of select="/introhtml|/*/introhtml"/>
            <hr/>       
            <xsl:variable name="dircnthalf" select="count(.//dir)  div 2"/>
            <table>
                <tbody>
                    <tr>
                        <td>
                            <xsl:apply-templates select=".//dir[text()!='..' and position() &lt;= $dircnthalf]"/>
                        </td>
                        <td>
                            <xsl:apply-templates select=".//dir[text()!='..' and position() &gt; $dircnthalf]"/>
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
</xsl:template>

<xsl:template match="dir">
    <li>
        <a href="{.}"><xsl:value-of select="."/></a>
    </li>
</xsl:template>


</xsl:stylesheet>
