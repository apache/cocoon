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
<xsl:stylesheet version="1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format"  xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:template match="sitemap">
    <!--xsd:schema elementFormDefault="qualified" attributeFormDefault="unqualified"-->
    <xsl:element name="xsd:schema">
        <xsl:attribute name="elementFormDefault">qualified</xsl:attribute>
        <xsl:attribute name="attributeFormDefault">unqualified</xsl:attribute>
        <xsl:apply-templates/>
    </xsl:element>
    <!--xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
        <xsl:apply-templates/>
    </xsd:schema-->
</xsl:template>

<xsl:template match="info">
</xsl:template>

<xsl:template match="redirect">
    <xsl:param name="refinfo"/>

    <xsl:choose>
        <xsl:when test="pipeline">
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates select="." mode="special"><xsl:with-param name="refinfo" select="$refinfo"/></xsl:apply-templates>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="matcher|part|transform|generate|read">
    <xsl:param name="refinfo"/>

    <xsl:choose>
        <xsl:when test="@element">
            <xsd:sequence>
                <xsd:annotation>
                    <xsd:documentation><xsl:value-of select="@element"/>
                    </xsd:documentation>
                </xsd:annotation>
                <xsl:apply-templates select="." mode="special"><xsl:with-param name="refinfo" select="$refinfo"/></xsl:apply-templates>
            </xsd:sequence>
        </xsl:when>
        <xsl:otherwise>
                <xsl:apply-templates select="." mode="special"><xsl:with-param name="refinfo" select="$refinfo"/></xsl:apply-templates>
        </xsl:otherwise>
    </xsl:choose>    
    
</xsl:template>


<xsl:template match="*" mode="special">
    <xsl:param name="refinfo"/>


    <xsl:variable name="name" select="concat(@pattern,@src,@ref)"/>
    <xsl:variable name="afterslashes">
        <xsl:call-template name="after_slashes">
            <xsl:with-param name="str" select="$name"/>
        </xsl:call-template> 
    </xsl:variable>

    <xsd:element>
        <xsl:choose>
            <xsl:when test="*">
                <xsl:attribute name="name"><xsl:value-of select="$afterslashes"/></xsl:attribute>
                <xsd:annotation>
                    <xsd:documentation><xsl:value-of select="substring-before($name,$afterslashes)"/> <xsl:value-of select="$refinfo"/></xsd:documentation>
                </xsd:annotation>
                <xsd:complexType>
                    <xsd:sequence>
                        <xsd:annotation>
                            <xsd:documentation><xsl:value-of select="serialize/@*"/></xsd:documentation>
                        </xsd:annotation>
                        <xsl:apply-templates/>
                    </xsd:sequence>
                </xsd:complexType>
            </xsl:when>
            <xsl:when test="@ref">
                <xsl:attribute name="ref"><xsl:value-of select="$afterslashes"/></xsl:attribute>
                        <xsd:annotation>
                            <xsd:documentation><xsl:value-of select="substring-before($name,$afterslashes)"/> <xsl:value-of select="$refinfo"/></xsd:documentation>
                            <!--xsl:copy-of select="."/-->
                        </xsd:annotation>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="name"><xsl:value-of select="$afterslashes"/></xsl:attribute>
                <xsl:attribute name="type">xsd:string</xsl:attribute>
                        <xsd:annotation>
                            <xsd:documentation><xsl:value-of select="substring-before($name,$afterslashes)"/> <xsl:value-of select="$refinfo"/>
                                <xsl:if test="name()='transform'">xslt</xsl:if>
                            </xsd:documentation>
                            <!--xsl:copy-of select="."/-->
                        </xsd:annotation>
            </xsl:otherwise>
        </xsl:choose>
    </xsd:element>
    
</xsl:template>

<xsl:template match="serialize">
</xsl:template>


<xsl:template match="aggregate">
    <xsd:choice>
            <xsd:annotation>
                <xsd:documentation><xsl:value-of select="@element"/></xsd:documentation>
            </xsd:annotation>
            <xsl:apply-templates/>
    </xsd:choice>
</xsl:template>


<xsl:template match="LEGEPIPELINE">
    <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="*">
    <UNKNOWN><xsl:value-of select="name()"/></UNKNOWN>
</xsl:template>


<xsl:template match="pipeline">
    <xsl:choose>
        <xsl:when test="@element">
            <xsd:sequence>
                <xsd:annotation>
                    <xsd:documentation><xsl:value-of select="@element"/></xsd:documentation>
                </xsd:annotation>
                <xsl:apply-templates><xsl:with-param name="refinfo" select="@src"/></xsl:apply-templates>
            </xsd:sequence>
        </xsl:when>
        <xsl:when test="count(*)=0">
            <xsd:element name="{@src}"/>
        </xsl:when>
        <xsl:otherwise>
                <xsl:apply-templates><xsl:with-param name="refinfo" select="@src"/></xsl:apply-templates>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<xsl:template name="no_slashes">
    <xsl:param name="str"/>
    <xsl:value-of select="translate($str,'/*','--')"/>
    
</xsl:template>


<xsl:template name="after_slashes">
    <xsl:param name="str"/>
    <xsl:choose>
        <xsl:when test="contains($str,'/') and 0">
            <xsl:call-template name="after_slashes">
                <xsl:with-param name="str" select="substring-after($str,'/')" />
            </xsl:call-template> 
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$str"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
