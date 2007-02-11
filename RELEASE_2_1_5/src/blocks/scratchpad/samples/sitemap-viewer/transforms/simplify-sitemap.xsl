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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:map="http://apache.org/cocoon/sitemap/1.0">
    
    <xsl:output indent="yes"/>
    
    <xsl:variable name="globals" select="0"/>
    <xsl:variable name="nomatch" select="'::::*****:::::'"/>
    
    <!--
      ! skipping some 
      ! -->
    
    <xsl:template match="map:components|map:views|map:view|map:component-configurations|map:parameter">
    </xsl:template>
    
    
    <xsl:template match="map:match" mode="print">
        <xsl:param name="use_cnt" select="-1"/>
        <xsl:param name="depth"  select="0"/>

        <match pattern="{@pattern}">
            <xsl:if test="$use_cnt>0">
                <xsl:attribute name="used"><xsl:value-of select="$use_cnt"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates>
                <xsl:with-param name="depth"  select="$depth+1"/>        
            </xsl:apply-templates>
        </match>
    </xsl:template>
    
    
    <xsl:template match="map:call" mode="children">
        <xsl:param name="depth"  select="0"/>

        <xsl:apply-templates select="//map:resource[@name=current()/@resource]">
            <xsl:with-param name="depth" select="$depth+1"/>
        </xsl:apply-templates>
        
    </xsl:template>
         
         
     <xsl:template match="map:redirect-to" mode="children">
        <xsl:param name="depth"  select="0"/>

        <xsl:variable name="redirectname">
            <xsl:call-template name="redirectpath">
                <xsl:with-param name="file" select="@uri"/>
                <xsl:with-param name="parentfile" select="../@pattern"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:attribute name="to-uri"><xsl:value-of select="$redirectname"/></xsl:attribute>
        
            <xsl:apply-templates mode="pipeline" select=".">
                <xsl:with-param name="depth" select="$depth+1"/>
            </xsl:apply-templates>
        
    </xsl:template>

            
    <xsl:template match="map:match" mode="pipeline">
        <xsl:param name="depth"  select="0"/>

        <xsl:choose>
            <xsl:when test="not($globals)">
                <xsl:apply-templates select="." mode="print">
                            <xsl:with-param name="depth"  select="$depth+1"/>        
                </xsl:apply-templates>
            </xsl:when>
            
            <xsl:otherwise>


                    <xsl:apply-templates select="." mode="print">
                        <xsl:with-param name="use_cnt" select="$cnt_direct+$cnt_indirect"/>
                        <xsl:with-param name="depth"  select="$depth+1"/>        
                    </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    
    
    <xsl:template match="map:redirect-to">
        <xsl:param name="depth"  select="0"/>

        <xsl:variable name="redirectname">
            <xsl:call-template name="redirectpath">
                <xsl:with-param name="file" select="@uri"/>
                <xsl:with-param name="parentfile" select="../@pattern"/>
            </xsl:call-template>
        </xsl:variable>
        <redirect to="{@uri}" src="{@uri}">
            <xsl:apply-templates mode="pipeline" select=".">
                <xsl:with-param name="depth"  select="$depth+1"/>        
                <xsl:with-param name="redirectname" select="$redirectname"/>
            </xsl:apply-templates>
        </redirect>
    </xsl:template>
    
    
    <xsl:template match="*" mode="pipeline">
        <xsl:param name="redirectname"/>
        <xsl:param name="depth"  select="0"/>
        
        <xsl:variable name="src">
            <xsl:if test="not(contains(@src,'cocoon:/'))">cocoon:/</xsl:if>
            <xsl:choose>
                <xsl:when test="$redirectname!=''">
                    <xsl:value-of select="$redirectname"/>
                </xsl:when>
                <xsl:when test="contains(concat(@src,@uri),'?')">
                    <xsl:value-of select="substring-before(concat(@src,@uri),'?')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat(@src,@uri)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:if test="$depth &lt; 100">
            <pipeline src="{concat(@src,@uri)}">
                <xsl:copy-of select="@element"/>
                <xsl:variable name="src0">
                    <xsl:call-template name="skip-interparam-slashes">
                        <xsl:with-param name="str" select="substring-after($src,'cocoon:/')"/>
                    </xsl:call-template>
                </xsl:variable> 
                <xsl:variable name="thepattern">
                    <xsl:call-template name="find-match">
                        <xsl:with-param name="request" select="$src0"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:message>- found <xsl:value-of select="$thepattern"/></xsl:message>
                <xsl:choose>
                    <xsl:when test="count(//map:match[@pattern=$thepattern])&gt;0">
                        <xsl:apply-templates select="//map:match[@pattern=$thepattern]" mode="pipeline">
                            <xsl:with-param name="depth" select="$depth+1"/>
                        </xsl:apply-templates>
                    </xsl:when>

                    <xsl:otherwise>
                        <xsl:call-template name="find-match">
                            <xsl:with-param name="request" select="$src0"/>
                        </xsl:call-template>
                        <LEGEPIPELINE src="{$src}">
                            <pattern>
                                <xsl:copy-of select="$thepattern"/>
                            </pattern>
                            <match ref="{substring-after($src,'cocoon:/')}" pattern="{concat(substring-before(substring-after($src,'cocoon:/'),'{'),substring-after($src,'}'))}"/>
                        </LEGEPIPELINE>
                    </xsl:otherwise>
                </xsl:choose>

            </pipeline>
        </xsl:if>
    </xsl:template>
    
    <!--xsl:template match="map:*" mode="pipeline">
        <xsl:param name="depth"  select="0"/>

        <xsl:element name="{substring-after(name(),'map:')}">
            <xsl:copy-of select="@*"/>

            <xsl:apply-templates select="." mode="children">
                <xsl:with-param name="depth"  select="$depth"/>        
            </xsl:apply-templates>
            
        </xsl:element>
        
    </xsl:template-->

    
    <xsl:template name="redirectpath">
        <xsl:param name="file" select="@uri"/>
        <xsl:param name="parentfile" select="../@pattern"/>
        <xsl:choose>
            <xsl:when test="contains($parentfile,'/')">
                <xsl:variable name="first-part" select="substring-before($parentfile,'/')"/>
                <xsl:variable name="rest-part">
                    <xsl:call-template name="redirectpath">
                        <xsl:with-param name="file" select="$file"/>
                        <xsl:with-param name="parentfile" select="substring-after($parentfile,'/')"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="concat($first-part,'/',$rest-part)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$file"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    
    <xsl:template name="find-match">
        <xsl:param name="request"/>
        <xsl:message>find-match for <xsl:value-of select="$request"/></xsl:message>
        <xsl:choose>
            <xsl:when test="//map:match[@pattern=$request]">
                <xsl:value-of select="//map:match[@pattern=$request]/@pattern"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="find-a-match">
                    <xsl:with-param name="request" select="concat('',$request)"/>
                    <xsl:with-param name="matches" select="//map:match[contains(@pattern,'*')]"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template name="find-a-match">
        <xsl:param name="request"/>
        <xsl:param name="matches"/>
        <xsl:choose>
            <xsl:when test="count($matches)=0">
                <xsl:value-of select="$nomatch"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="first-test-result">
                    <xsl:call-template name="test-match">
                        <xsl:with-param name="pattern" select="concat('',$matches[1]/@pattern)"/>
                        <xsl:with-param name="request" select="$request"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$first-test-result=1">
                        <xsl:value-of select="$matches[1]/@pattern"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="find-a-match">
                            <xsl:with-param name="request" select="$request"/>
                            <xsl:with-param name="matches" select="$matches[position()>1]"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="test-match">
        <xsl:param name="pattern"/>
        <xsl:param name="request"/>

        <xsl:choose>
            <xsl:when test="$pattern=$request">1</xsl:when>
            <xsl:when test="not(contains($pattern,'*'))">0</xsl:when>
            <xsl:when test="$request=''">0</xsl:when>
            <xsl:when test="substring($pattern,1,1)='*'">

                <xsl:variable name="subtext">
                    <xsl:choose>
                        <xsl:when test="substring($pattern,1,2)='**' and contains(substring($pattern,3),'*')">
                            <xsl:value-of select="substring-before(substring($pattern,3),'*')"/>
                        </xsl:when>
                        <xsl:when test="substring($pattern,1,2)='**' ">
                            <xsl:value-of select="substring($pattern,3)"/>
                        </xsl:when>
                        <xsl:when test="contains(substring($pattern,2),'*')">
                            <xsl:value-of select="substring-before(substring($pattern,2),'*')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="substring($pattern,2)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="$subtext=''">
                        <xsl:value-of select="number(substring($pattern,2,2)='*' or not(contains($request,'/')))"/>
                    </xsl:when>
                    <xsl:when test="not(contains($request,$subtext))">0</xsl:when>
                    <xsl:when test="not(substring($pattern,2,1)='*') and contains(substring-before($request,$subtext),'/')">0</xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="test-match-anybefore">
                            <xsl:with-param name="pattern" select="substring-after($pattern,$subtext)"/>
                            <xsl:with-param name="request" select="substring-after($request,$subtext)"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>

            <xsl:when test="substring($pattern,1,1)=substring($request,1,1)">
                <xsl:call-template name="test-match">
                    <xsl:with-param name="pattern" select="substring($pattern,2)"/>
                    <xsl:with-param name="request" select="substring($request,2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                0                
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template name="test-match-anybefore">
        <xsl:param name="pattern"/>
        <xsl:param name="request"/>
        <xsl:variable name="isok">
            <xsl:call-template name="test-match">
                <xsl:with-param name="pattern" select="$pattern"/>
                <xsl:with-param name="request" select="$request"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$isok=1">1</xsl:when>
            <xsl:when test="string-length($request)=0">
                <xsl:choose>
                    <xsl:when test="$pattern=''">1</xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="test-match-anybefore">
                    <xsl:with-param name="pattern" select="$pattern"/>
                    <xsl:with-param name="request" select="substring($request,2)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="skip-interparam-slashes">
        <xsl:param name="str"/>

        <xsl:choose>
            <xsl:when test="contains(substring-after($str,'{'),'/')">
                <xsl:value-of select="concat(substring-before($str,'{'),'{',translate(substring-before(substring-after($str,'{'),'}'),'/','|'),'}')"/>
                <xsl:call-template name="skip-interparam-slashes">
                    <xsl:with-param name="str" select="substring-after($str,'}')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$str"/>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>


</xsl:stylesheet>
