<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:map="http://apache.org/cocoon/sitemap/1.0">
    
    <xsl:output indent="yes"/>
    
    <xsl:param name="location"/>
    <xsl:param name="filename"/>
    
    <xsl:variable name="sitemapInfo" select="//SitemapInfo"/>
    <xsl:variable name="globals" select="0"/>
    <xsl:variable name="nomatch" select="'::::*****:::::'"/>
    
    <xsl:template match="/">
        <sitemap location="{$location}" filename="{$filename}" >
            <xsl:apply-templates/>
        </sitemap>
    </xsl:template>
    
    
    <xsl:template match="map:components|map:views|map:component-configurations">
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
    
    
    <xsl:template match="map:match">
        <xsl:param name="depth"  select="0"/>

            <xsl:apply-templates select="." mode="print">
                <xsl:with-param name="use_cnt" select="0"/>
                <xsl:with-param name="depth"  select="$depth+1"/>        
            </xsl:apply-templates>

    </xsl:template>
    
    
    <xsl:template match="map:resource" mode="pipeline">
        <xsl:param name="depth"  select="0"/>

        <xsl:element name="{substring-after(name(),'map:')}">
            <xsl:copy-of select="@*"/>
             <xsl:apply-templates />
        </xsl:element>
        
        
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
                <!-- kijken hoe vaak deze match gebruikt wordt... -->
                <xsl:variable name="cnt_direct" select="count(//*[@src=concat('cocoon:/',current()/@pattern)])"/>
                <xsl:variable name="indirects" select="$sitemapInfo//*[@pattern=current()/@pattern]"/>
                <xsl:variable name="cnt_indirect">
                    <xsl:call-template name="cnt_indirect">
                        <xsl:with-param name="indirects" select="$indirects"/>
                    </xsl:call-template>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="$cnt_direct+$cnt_indirect &lt; 2">
                        <xsl:apply-templates select="." mode="print">
                            <xsl:with-param name="use_cnt" select="$cnt_direct+$cnt_indirect"/>
                            <xsl:with-param name="depth"  select="$depth+1"/>        
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <match ref="{@pattern}"/>
                    </xsl:otherwise>
                </xsl:choose>

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
    
    
    <xsl:template match="map:view"/>
    
    
    <xsl:template match="map:serialize">
        <serialize>
            <xsl:copy-of select="@*"/>
        </serialize>
    </xsl:template>
    
    
    <xsl:template match="map:aggregate">
        <aggregate>
            <xsl:copy-of select="@element"/>
            <xsl:apply-templates/>
        </aggregate>
    </xsl:template>
    
    
    
    
    <xsl:template match="*" mode="pipeline">
        <xsl:param name="redirectname"/>
        <xsl:param name="depth"  select="0"/>
        
        <xsl:variable name="src">
            <xsl:if test="not(contains(@src,'cocoon:/'))">cocoon:/</xsl:if>
            <xsl:choose>
                <xsl:when test="contains($redirectname,'?')">
                    <xsl:value-of select="substring-before($redirectname,'?')"/>
                </xsl:when>
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

            <xsl:element name="{substring-after(name(),'map:')}">
            <xsl:attribute name="src"><xsl:value-of select="concat(@src,@uri)"/></xsl:attribute>
                <xsl:copy-of select="@element|@type|@mime-type"/>
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

                <reference thepattern="{$thepattern}" src="{$src}" src0="{$src0}"/>
    
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="map:pipeline">
        <xsl:element name="{substring-after(name(),'map:')}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="Sitemap|map:sitemap|map:pipelines">
        <xsl:param name="depth" select="0"/>
        <xsl:apply-templates select="*">
            <xsl:with-param name="depth" select="$depth+1"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="map:resources">
    </xsl:template>
   
   
     <xsl:template match="map:call">
        <xsl:element name="{substring-after(name(),'map:')}">
            <xsl:copy-of select="@*"/>
             <xsl:apply-templates select="//map:resource[@name=current()/@resource]" mode="pipeline"/>
        </xsl:element>
    </xsl:template>
   


    <xsl:template match="map:generate|map:read|map:transform|map:part">
        <xsl:param name="depth"  select="0"/>

        <xsl:choose>
            <xsl:when test="contains(@src,'cocoon:/')">
                <xsl:apply-templates select="." mode="pipeline" >
                    <xsl:with-param name="depth"  select="$depth+1"/>        
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{substring-after(name(),'map:')}">
                    <xsl:copy-of select="@*"/>
                    <xsl:apply-templates />
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="map:*">
        <xsl:param name="depth"  select="0"/>

        <xsl:element name="{substring-after(name(),'map:')}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates>
                <xsl:with-param name="depth" select="$depth +1"/>
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="text()">
        <xsl:apply-templates/>
    </xsl:template>
    
    
    <xsl:template name="cnt_indirect">
        <xsl:param name="indirects"/>
        <xsl:choose>
            <xsl:when test="$indirects[1]">
                <xsl:variable name="count_this" select="count(//*[@src=concat('cocoon:/',$indirects[1]//@ref)])"/>
                <xsl:variable name="count_rest">
                    <xsl:call-template name="cnt_indirect">
                        <xsl:with-param name="indirects" select="$indirects[position()>1]"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="$count_this + $count_rest"/>
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
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
        
        <xsl:variable name="norm-req">
            <xsl:call-template name="normalize-path">
                <xsl:with-param name="path" select="$request"/>
            </xsl:call-template>
        </xsl:variable>
        <!--xsl:message>find-match for <xsl:value-of select="$request"/> or <xsl:value-of select="$norm-req"/></xsl:message-->
        
        
        <xsl:choose>
            <xsl:when test="//map:match[@pattern=$norm-req]">
                <xsl:value-of select="//map:match[@pattern=$norm-req]/@pattern"/>
            </xsl:when>
            <xsl:when test="substring($norm-req,1,3)='../'">
                <!-- try to find the request in the local dir, just in case -->
                <xsl:call-template name="find-match">
                    <xsl:with-param name="request" select="substring-after($norm-req,'../')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($norm-req,'..')">
                <xsl:value-of select="$nomatch"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="find-a-match">
                    <xsl:with-param name="request" select="concat('',$norm-req)"/>
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
        <!--xsl:message>test-match</xsl:message>        
    <xsl:message>- pattern: <xsl:value-of select="$pattern"/></xsl:message>        
    <xsl:message>- request: <xsl:value-of select="$request"/></xsl:message-->

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



    <xsl:template name="normalize-path">
        <xsl:param name="path"/>
        <!-- this will take care of paths like "/usr/local/../program/../../tmp" and change it into "/tmp" -->
        <xsl:choose>
            <xsl:when test="contains($path,'/../')">
                <xsl:variable name="first-part">
                        <xsl:call-template name="strip-last-dir">
                            <xsl:with-param name="path" select="substring-before($path,'/../')"/>
                        </xsl:call-template>
                </xsl:variable>
                <xsl:call-template name="normalize-path">
                    <xsl:with-param name="path" select="concat($first-part,substring-after($path,'/../'))"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($path,'/..') and substring-after($path,'/..')='' ">
                <xsl:variable name="first-part">
                        <xsl:call-template name="strip-last-dir">
                            <xsl:with-param name="path" select="substring-before($path,'/..')"/>
                        </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="substring($first-part,1,string-length($first-part)-1)"/>
            </xsl:when>
            <xsl:when test="contains($path,'/./')">
                <xsl:call-template name="normalize-path">
                        <xsl:with-param name="path" select="concat(substring-before($path,'/./'),'/',substring-after($path,'/./'))"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="$path"/></xsl:otherwise>
        </xsl:choose>

    </xsl:template>
    
    <xsl:template name="strip-last-dir">
        <xsl:param name="path"/>

        <!-- this will change "/usr/local/me" into "/usr/local/", 
            "usr/him" into "usr/" 
            and "foo" into "" -->
        
        <xsl:if test="contains($path,'/')">
            <xsl:value-of select="concat(substring-before($path,'/'),'/')"/>
            <xsl:call-template name="strip-last-dir">
                <xsl:with-param name="path" select="substring-after($path,'/')"/>
            </xsl:call-template>
        </xsl:if>

    </xsl:template>
</xsl:stylesheet>
