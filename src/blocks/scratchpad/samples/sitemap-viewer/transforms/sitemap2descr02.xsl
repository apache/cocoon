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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">


<xsl:template match="reference">
    <xsl:param name="depth"/>
    
    <xsl:variable name="thepattern" select="@thepattern"/>
    <xsl:variable name="src" select="@src"/>
    <xsl:variable name="src0" select="@src0"/>
    
    <xsl:choose>
        <xsl:when test="$depth &gt; 100">
        </xsl:when>

        <xsl:when test="count(/*/match[@pattern=$thepattern])&gt;0">
            <xsl:apply-templates select="/*/match[@pattern=$thepattern]" >
                <xsl:with-param name="depth" select="$depth+1"/>
            </xsl:apply-templates>
        </xsl:when>

        <xsl:when test="count(/*/*/match[@pattern=$thepattern])&gt;0">
            <xsl:apply-templates select="/*/*/match[@pattern=$thepattern]" >
                <xsl:with-param name="depth" select="$depth+1"/>
            </xsl:apply-templates>
        </xsl:when>

        <xsl:otherwise>
            <xsl:variable name="empty">
                <!--xsl:call-template name="find-match">
                    <xsl:with-param name="request" select="$src0"/>
                </xsl:call-template-->
            </xsl:variable>
            <LEGEPIPELINE src="{$src}">
                <pattern>
                    <xsl:copy-of select="$thepattern"/>
                </pattern>

                <match ref="{substring-after($src,'cocoon:/')}" pattern="{concat(substring-before(substring-after($src,'cocoon:/'),'{'),substring-after($src,'}'))}"/>
            </LEGEPIPELINE>
        </xsl:otherwise>
    </xsl:choose>

</xsl:template>

<xsl:template match="*">
    <xsl:param name="depth" select="0"/>
    
    <xsl:if test="$depth &lt; 100">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates>
                <xsl:with-param name="depth" select="$depth+1"/>
            </xsl:apply-templates>
        </xsl:copy>

    </xsl:if>

</xsl:template>

</xsl:stylesheet>
