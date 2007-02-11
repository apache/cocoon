<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    
    <xsl:param name="browser" select="'ie'"/>

    <xsl:param name="showsources" select="0"/>

    <xsl:variable name="nomatch" select="'::::*****:::::'"/>
    <xsl:variable name="debug" select="0"/>
    
    <xsl:variable name="display">
        <xsl:choose>
            <xsl:when test="$browser='ie'">display:none;</xsl:when>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:template match="sitemap">
        <!--xsd:schema elementFormDefault="qualified" attributeFormDefault="unqualified"-->
        <html>
            <head>
                <title>Sitemap Viewer - <xsl:value-of select="@location"/></title>
                <link href="SitemapInfo.css" type="text/css" rel="stylesheet" />
            </head>
            <body>
                <xsl:if test="$showsources=1">
                    <pre><a href="{concat('sitemap.xmap?location=',@location)}" target="sitemapSrc"><xsl:value-of select="@location"/>/sitemap.xmap</a>
                    <xsl:if test="$debug">
                        <a href="{concat('descr?location=',@location)}">.</a>
                    </xsl:if>
                    </pre>
                </xsl:if>
                
                <p id="link-to-others"><a href="sitemap-list.html">Other sitemaps</a></p>
                <xsl:apply-templates/>
                <script src="ie.js" type="text/javascript"/>
            </body>
        </html>
    
    </xsl:template>
    
    
    <xsl:template match="info">
    </xsl:template>

    <xsl:template match="redirect">
        <xsl:param name="refinfo"/>
        
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>

        <xsl:choose>
            <xsl:when test="*[name()!='parameter']">
                <xsl:apply-templates select="*[name()!='parameter']"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="special">
                    <xsl:with-param name="refinfo" select="$refinfo"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template match="*" mode="tablestart">
        
        <xsl:choose>
            <xsl:when test="*">
                <xsl:attribute name="class">stream</xsl:attribute>
            </xsl:when>
            <xsl:when test="@ref">
                <xsl:attribute name="class">special-ref</xsl:attribute>
           </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="class">special-other</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
         <xsl:attribute name="id">subtable</xsl:attribute>
     </xsl:template>

    <xsl:template match="*" mode="tablerowstart">
                <td class="{concat(name(),'_img')}" id="preimg"><img src ="{concat(name(),'.gif')}" alt="{name()}"/></td>
    </xsl:template>
    
    

<xsl:template match="aggregate/*|select/*">
        <xsl:param name="refinfo"/>


        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug-part"/></xsl:if>

        <td class="partline"><img src="{concat(name(),'_pre.gif')}" alt="{concat(name(..),'_',name())}"/> </td>
        <td class="part">
            <pre class="comment">
                <xsl:value-of select="name()"/>
            </pre>
        </td>
        
<!--
        <td class="part">
            <pre class="comment">
                <xsl:value-of select="@element"/>
                <xsl:value-of select="@test"/>
                <xsl:value-of select="@parameter"/>
                <xsl:value-of select="@type"/>
                <xsl:value-of select="@refinfo"/>
            </pre>
        </td>
        <td >
                    <xsl:variable name="button_name">
                        <xsl:choose>
                            <xsl:when test="(name()='match' or name()='part') and count(*)=1"><xsl:value-of select="name(*)"/></xsl:when>
                            <xsl:when test="(name()='match' or name()='part') ">stream</xsl:when>
                            <xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
        
                    <a oncontextmenu="return ToggleAll(this)" onclick="Toggle(this)">
                        <img src="{concat($button_name,'_op.gif')}" alt="{concat('[',$button_name,']')}"/>
                    </a>
        </td>
-->
        <td class="stream"  >
            <xsl:apply-templates select="." mode="special">
                <xsl:with-param name="refinfo" select="$refinfo"/>
            </xsl:apply-templates>
        </td>
        <td class="filler"></td>
</xsl:template>




    <xsl:template match="aggregate|select">

        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>

        <table class="{name()}" cellpadding="0" cellspacing="0" id="subtable">
            <tr>
                <td class="{name()}">
                    <pre class="comment">
                        <xsl:value-of select="@element|@test|@parameter"/>
                        <xsl:value-of select="@type"/>
                    </pre>
                </td>
                <!--td height="100%"><table cellpadding="0" cellspacing="0" style="height:100%">
                        <tr><td valign="top"><img src="aggregate_end.gif"/></td></tr>
                        <tr><td valign="middle" style="height:50%;background-image:url('aggregate_vline.gif');background-repeat:repeat-y;"><img src="aggregate_vline.gif"/></td></tr>
                        <tr><td valign="middle"><img src="aggregate_mid.gif"/></td></tr>
                        <tr><td valign="middle" style="height:50%;background-image:url('aggregate_vline.gif');background-repeat:repeat-y;"><img src="aggregate_vline.gif"/></td></tr>
                        <tr><td valign="bottom"><img src="aggregate_end.gif"/></td></tr>
                </table></td-->
                
                <xsl:apply-templates select="." mode="td_button"/>
                
                <td class="{concat(name(),'_elements')}" style="{$display}">
                    <table>
                            <tr>
                                <td><img src ="{concat(name(),'.gif')}" alt="{name()}"/></td>
                                <td>
                                    <table class="{concat(name(),'_table')}" cellpadding="0" cellspacing="0">
                                        <xsl:for-each select="*[name()!='parameter']">
                                            <tr>
                                                <td>
                                                    <xsl:apply-templates select="."/>
                                                </td>
                                            </tr>
                                        </xsl:for-each>
                                    </table>
                                </td>
                            </tr>
                    </table>
                    
                
                
<!--
                    <xsl:apply-templates select="." mode="special"/>
-->
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="LEGEPIPELINE">
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>
        <xsl:text>[unresolved]</xsl:text>
    </xsl:template>
    
    
    
    
    <!--xsl:template match="*">
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>
        <UNKNOWN>
              <xsl:value-of select="name()"/>
        </UNKNOWN>
    </xsl:template-->



    <xsl:template match="call">
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>
        <xsl:apply-templates/>
    </xsl:template>


    
    <!--xsl:template match="match|part|transform|generate|read|serialize|resource|*"-->

    <xsl:template match="*">
        <xsl:param name="refinfo"/>

                <xsl:apply-templates select="." mode="special">
                    <xsl:with-param name="refinfo" select="concat($refinfo,@name)"/>
                </xsl:apply-templates>
<!--
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>

        <xsl:choose>
            <xsl:when test="@element or name(..)='aggregate' or name(..)='select'">
                <td class="partline"><img src="{concat(name(),'_pre.gif')}" alt="{concat(name(..),'_',name())}"/></td>
                <td class="part">
                    <pre class="comment">
                        <xsl:value-of select="@element"/>
                        <xsl:value-of select="@test"/>
                        <xsl:value-of select="@parameter"/>
                        <xsl:value-of select="@type"/>
                        <xsl:value-of select="@refinfo"/>
                    </pre>
                </td>
                <td >
                            <xsl:variable name="name">
                                <xsl:choose>
                                    <xsl:when test="(name()='match' or name()='part') and count(*)=1"><xsl:value-of select="name(*)"/></xsl:when>
                                    <xsl:when test="(name()='match' or name()='part') ">stream</xsl:when>
                                    <xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                
                            <a oncontextmenu="return ToggleAll(this)" onclick="Toggle(this)">
                                <img src="{concat($name,'_op.gif')}" alt="{concat('[',$name,']')}"/>
                            </a>
                </td>
                <td class="stream" style="{$display}">
                    <xsl:apply-templates select="." mode="special">
                        <xsl:with-param name="refinfo" select="$refinfo"/>
                    </xsl:apply-templates>
                </td>
                <td class="filler"></td>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="special">
                    <xsl:with-param name="refinfo" select="concat($refinfo,@name)"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
-->
    </xsl:template>
    

    
    
    <xsl:template match="*" mode="special">
        <xsl:param name="refinfo"/>

        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug-special"/></xsl:if>
        
        <xsl:variable name="name" select="concat(@pattern,@src,@ref)"/>
        <xsl:variable name="afterslashes">
            <xsl:call-template name="after_slashes">
                <xsl:with-param name="str" select="$name"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:if test="serialize">
            <xsl:attribute name="class">serialize</xsl:attribute>
        </xsl:if>

                <table id="{name(..)}">
                    <xsl:apply-templates select="." mode="tablestart"/>
                    <tr>
                        <xsl:apply-templates select="." mode="tablerowstart"/>
                        <td>
                            <table><tr>
                                <td colspan="2">
                                    <xsl:choose>
                                        <xsl:when test="$afterslashes=$nomatch">
                                            <pre>[unknown]</pre>
                                        </xsl:when>
                                        <xsl:when test="$afterslashes!=''">
                                            <pre><xsl:value-of select="$afterslashes"/></pre>
                                        </xsl:when>
                                        <xsl:otherwise>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:if test="name()='serialize' or name()='act'"><xsl:value-of select="name()"/></xsl:if>
                                </td></tr>
                              <tr class="comments">
                                <td>
                                    <pre class="comment">
                                        <xsl:choose>
                                            <xsl:when test="substring-before($name,$afterslashes)!=''">
                                                <xsl:value-of select="substring-before($name,$afterslashes)"/>
                                            </xsl:when>
                                            <xsl:when test="$refinfo!=''">
                                                <xsl:value-of select="$refinfo"/>
                                            </xsl:when>
                                            <xsl:when test="name()='transform'">xslt</xsl:when>
                                            <xsl:when test="@element">
                                                <xsl:value-of select="@element"/>
                                            </xsl:when>
                                            <xsl:when test="@test">
                                                <xsl:value-of select="@test"/>
                                            </xsl:when>
                                            <xsl:when test="@type">
                                                <xsl:value-of select="@type"/>
                                            </xsl:when>
                                            <xsl:when test="@mime-type">
                                                <xsl:value-of select="@mime-type"/>
                                            </xsl:when>
                                            <xsl:when test="@parameter">
                                                <xsl:value-of select="@parameter"/>
                                            </xsl:when>
                                        </xsl:choose>
                                    </pre>
                                </td>
                                <td class="parameters">
                                    <xsl:for-each select="parameter">
                                        <pre class="comment"><xsl:value-of select="@name"/>=<xsl:value-of select="@value"/></pre>
                                    </xsl:for-each>
                                </td>
                                
                            </tr></table>                        
                            
     
                        </td>
                        
                        
                        <xsl:choose>
                            <xsl:when test="*[name()!='parameter']">
                                <xsl:apply-templates select="." mode="td_button"/>
                                <td class="stream" style="{$display}">
                                    <xsl:apply-templates select="*[name()!='parameter']"/>
                                </td>
                                <td class="filler"></td>
                            </xsl:when>
                            <xsl:when test="@ref">
                                <xsl:apply-templates select="//*[@pattern=current()/@ref]"/>
                            </xsl:when>
                            <xsl:otherwise>
                            </xsl:otherwise>
                        </xsl:choose>
                            
                    </tr>
                </table>

<!--
                <table class="special-ref" id="{name()}">
                    <xsl:apply-templates select="." mode="tablestart"/>
                    <tr>
                        <xsl:apply-templates select="." mode="tablerowstart"/>
                        <td>


                            <pre class="comment">
                                <xsl:value-of select="substring-before($name,$afterslashes)"/>
                                <xsl:value-of select="$refinfo"/>
                            </pre>
                            <xsl:apply-templates select="//*[@pattern=current()/@ref]"/>
                        </td>
                    </tr>
                </table>
            </xsl:when>
            <xsl:otherwise>
                <table class="special-other" id="{name()}">
                    <xsl:apply-templates select="." mode="tablestart"/>
                    <tr>
                        <xsl:apply-templates select="." mode="tablerowstart"/>
                        <td >


                            <pre class="name">
                                <xsl:choose>
                                    <xsl:when test="$afterslashes=$nomatch">
                                        <xsl:text>[unknown]</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$afterslashes"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:if test="name()='serialize'">serialize</xsl:if>
                            </pre>
     

                            <pre class="comment">
                                <xsl:choose>
                                    <xsl:when test="substring-before($name,$afterslashes)!=''">
                                        <xsl:value-of select="substring-before($name,$afterslashes)"/>
                                    </xsl:when>
                                    <xsl:when test="$refinfo!=''">
                                        <xsl:value-of select="$refinfo"/>
                                    </xsl:when>
                                    <xsl:when test="name()='transform'">xslt</xsl:when>
                                    <xsl:when test="@element">
                                        <xsl:value-of select="@element"/>
                                    </xsl:when>
                                    <xsl:when test="@test">
                                        <xsl:value-of select="@test"/>
                                    </xsl:when>
                                    <xsl:when test="@type">
                                        <xsl:value-of select="@type"/>
                                    </xsl:when>
                                    <xsl:when test="@mime-type">
                                        <xsl:value-of select="@mime-type"/>
                                    </xsl:when>
                                    <xsl:when test="@parameter">
                                        <xsl:value-of select="@parameter"/>
                                    </xsl:when>
                                </xsl:choose>
     
                            </pre>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>
-->
        <!--/td></tr></table-->
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
                    <xsl:with-param name="str" select="substring-after($str,'/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$str"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="debug-special">
        <xsl:apply-templates select="." mode="debug">
            <xsl:with-param name="special" select="'special'"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="*" mode="debug-part">
        <xsl:apply-templates select="." mode="debug">
            <xsl:with-param name="special" select="'part'"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="*" mode="debug">
        <xsl:param name="special" select="''"/>
        
        <table  >
            <xsl:attribute name="class">debug<xsl:if test="$special!=''">-<xsl:value-of select="$special"/></xsl:if></xsl:attribute>
            <tbody>
                <tr>
                    <th colspan="{count(@*)}"><xsl:value-of select="name()"/></th>
                </tr>
                <tr>
                    <xsl:for-each select="@*">
                        <td><xsl:value-of select="name()"/>="<xsl:value-of select="."/>"</td>
                    </xsl:for-each>
                </tr>
            </tbody>
        </table>
    </xsl:template>
    

    <xsl:template match="*" mode="td_button">
        <td >
            <xsl:variable name="button_name">
                <xsl:choose>
                    <xsl:when test="(name()='match' or name()='part') and count(*)=1"><xsl:value-of select="name(*)"/></xsl:when>
                    <xsl:when test="(name()='match' or name()='part') ">stream</xsl:when>
                    <xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <a oncontextmenu="return ToggleAll(this)" onclick="Toggle(this)">
                <img src="{concat($button_name,'_op.gif')}" alt="{$button_name}"/>
            </a>
        </td>
    </xsl:template>
    
</xsl:stylesheet>
