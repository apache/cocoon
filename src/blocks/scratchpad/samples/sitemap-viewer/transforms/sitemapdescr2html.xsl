<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    
    <xsl:param name="browser" select="'ie'"/>

    <xsl:param name="showsources" select="0"/>

    <xsl:variable name="nomatch" select="'::::*****:::::'"/>
    <xsl:variable name="debug" select="0"/>
    
    <xsl:variable name="files" select="/*/files"/>
    
    <xsl:variable name="display">
        <xsl:choose>
            <xsl:when test="$browser='ie'">display:none;</xsl:when>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:template match="sitemap">

        <html>
            <head>
                <title>Sitemap Viewer - <xsl:value-of select="@location"/></title>
                <link href="SitemapInfo.css" type="text/css" rel="stylesheet" />
            </head>
            <body>
                <script src="ie.js" type="text/javascript"/>
                <table class="header">
                        <tr>
                            <td>
                                <xsl:if test="$showsources=1">
                                    <pre><a>
                                            <xsl:call-template name="popup-attrs">
                                                <xsl:with-param name="url" select="concat('src/',substring-before(@filename,'.xmap'),'._xmap?location=',@location)"/>
                                                <xsl:with-param name="target" select="'sitemapSrc'"/>
                                            </xsl:call-template>
                                            <xsl:value-of select="concat(@location,'/',@filename)"/></a>
                                    <xsl:if test="$debug">
                                        <a href="{concat('descr?location=',@location)}">.</a>
                                    </xsl:if>
                                    </pre>
                                </xsl:if>
                            </td>
                            <td id="othersites">
                                <p id="link-to-others"><a href="sitemap-list.html">Other sitemaps</a></p>
                            </td>
                        </tr>
                </table>
                
                <xsl:apply-templates select="*"/>
                <script type="text/javascript">
//                    alert("done");
//                    test();
                </script>"
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
    
        
    
    <xsl:template match="pipeline" mode="tablestart">

                <xsl:if test="name(..)='sitemap'">
                    <xsl:attribute name="class">pipe</xsl:attribute>
                </xsl:if>
                <tr><td colspan="5"> 
                    <xsl:apply-templates select="." mode="showattrs"/>
                </td></tr>
    </xsl:template>
        
        
    <xsl:template match="*" mode="tablestart">

         <xsl:attribute name="class">subtable</xsl:attribute>
     </xsl:template>

    <xsl:template match="*" mode="tablerowstart">
                <td class="preimg"><img class="1" src ="{concat(name(),'.gif')}" alt="{name()}"  width="18" height="16" /></td>
    </xsl:template>

    <xsl:template match="select|aggregate" mode="tablerowstart">
        <td class="preimg"> 
                <img class="2"  src ="{concat(name(),'_pre.gif')}" alt="{name()}" width="18" height="16" />
        </td>
    </xsl:template>
    
    <xsl:template match="pipeline" mode="tablerowstart">
            <td class="preimg"> 
                    <xsl:choose>
                        <xsl:when test="@internal-only='yes'">
                            <img class="2"  src ="{concat('internal-',name(),'.gif')}" alt="{name()}" height="21" width="23"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <img class="3" src ="{concat(name(),'.gif')}" alt="{name()}" height="21" width="23"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
    </xsl:template>
    
    

<xsl:template match="aggregate/*|select/*">
        <xsl:param name="refinfo"/>

<table>
        <tr>
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug-part"/></xsl:if>

        <td class="partline"><img class="4" src="{concat(name(),'_pre.gif')}" alt="{concat(name(..),'-',name())}" width="9" height="6" /> </td>
        <td class="part">
            <pre class="comment">
                <xsl:value-of select="name()"/>
            </pre>
        </td>
        
        <td class="stream"  >
            <xsl:apply-templates select="." mode="special">
                <xsl:with-param name="refinfo" select="$refinfo"/>
            </xsl:apply-templates>
        </td>
        <td class="filler"></td>
        </tr>
</table>

</xsl:template>




    <xsl:template match="ZZ-aggregate|ZZ-select">

        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>

        <table  ccellpadding="0" ccellspacing="0" class="subtable"><!--class="{name()}"-->
            <tr>
                        <xsl:apply-templates select="." mode="tablerowstart"/>

                        <xsl:apply-templates select="." mode="infotable">
                            <xsl:with-param name="name" select="name()"/>
                        </xsl:apply-templates>                            
     
                 <td><table cellpadding="0" cellspacing="0" ><tr>
                <td class="{name()}">
                    <xsl:value-of select="name()"/>
                    <pre class="comment">
                        <xsl:value-of select="@element|@test|@parameter"/>
                        <xsl:value-of select="@type"/>
                    </pre>
                </td>

                
                <xsl:apply-templates select="." mode="td_button"/>
                
                <td class="{concat(name(),'_elements')}" style="{$display}">
                    <table>
                            <tr>
                                <td><img  class="5"  src="{concat(name(),'.gif')}" alt="{name()}" width="23" height="21"/></td>
                                <td>
                                    <table class="{concat(name(),'_table')}" cellpadding="0" cellspacing="0">
                                        <xsl:for-each select="*[name()!='parameter']">
                                            <tr>
                                              
                                                    <xsl:apply-templates select="."/>
                                            
                                            </tr>
                                        </xsl:for-each>
                                    </table>
                                </td>
                            </tr>
                    </table>
                    
                
               
                </td>
        <td class="filler"/>
            </tr></table></td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="LEGEPIPELINE">
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>
        <xsl:text>[unresolved]</xsl:text>
    </xsl:template>
    
    
    

    <xsl:template match="call">
        <xsl:if test="$debug"><xsl:apply-templates select="." mode="debug"/></xsl:if>
        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="*">
        <xsl:param name="refinfo"/>

                <xsl:apply-templates select="." mode="special">
                    <xsl:with-param name="refinfo" select="concat($refinfo,@name)"/>
                </xsl:apply-templates>
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

                <table class="{name(..)}" >
                    <xsl:apply-templates select="." mode="tablestart"/>
                    <tr>
                        <xsl:apply-templates select="." mode="tablerowstart"/>

                        <xsl:apply-templates select="." mode="infotable">
                            <xsl:with-param name="afterslashes" select="$afterslashes"/>
                            <xsl:with-param name="name" select="$name"/>
                            <xsl:with-param name="refinfo" select="$refinfo"/>
                        </xsl:apply-templates>                            
     
                        
                        
                        <xsl:choose>
                            <xsl:when test="name()='pipeline'">
                                <td class="pipeline" >
                                    <xsl:apply-templates select="*[name()!='parameter']"/>
                                </td>
                                <td class="filler"></td>
                            </xsl:when>
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
                            
        <td class="filler"/>
                    </tr>
                </table>

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
                <img class="6" src="{concat($button_name,'_op.gif')}" alt="{$button_name}" width="9" height="9"/>
            </a>
        </td>
    </xsl:template>


    <xsl:template match="*" mode="showattrs">
        <xsl:for-each select="@*">
            <pre class="comment"><xsl:value-of select="name()"/>=<xsl:value-of select="."/></pre>
        </xsl:for-each>
    </xsl:template>


<xsl:template match="*" mode="infotable">
    <xsl:param name="afterslashes"  select="''"/>
    <xsl:param name="name"  />
    <xsl:param name="refinfo"  select="''"/>

    <xsl:if test=" $afterslashes=$nomatch or 
            name()='serialize' or name()='act' or name()='transform' or $afterslashes!='' or
             concat(substring-before($name,$afterslashes),$refinfo,@element,@test, @mime-type,@type,@parameter)!='' or
             count(parameter)>0">
             
            
        <td><table><tr>
            <td colspan="2">

                <xsl:variable name="url">
                    <xsl:call-template name="make-url">
                        <xsl:with-param name="location" select="/*/sitemap/@location"/>
                        <xsl:with-param name="subloc" select="$afterslashes"/>
                    </xsl:call-template>
                </xsl:variable> 

                <xsl:choose>
                    <xsl:when test="$afterslashes=$nomatch">
                        <pre>[unknown]</pre>
                    </xsl:when>
                    <xsl:when test="name()='mount' and $afterslashes!='' and $files/*[.=$afterslashes]">
                        <pre><a href="{$url}"><xsl:value-of select="$afterslashes"/> </a></pre>
                    </xsl:when>
                    <xsl:when test="$afterslashes!='' and $showsources=1 and $files/*[.=$afterslashes]">
                        <!--xsl:variable name="url" select="concat('src/',$afterslashes)"/>
                        <xsl:variable name="target" select="srcpopup"/-->
                        <pre><a>
                            <xsl:call-template name="popup-attrs">
                                <xsl:with-param name="url" select="$url"/> 
                              </xsl:call-template>

                            <xsl:value-of select="$afterslashes"/> 
                        </a></pre>

                    </xsl:when>
                    <xsl:when test="$afterslashes!='' ">
                        <pre><xsl:value-of select="$afterslashes"/></pre>
                    </xsl:when>
                    <xsl:otherwise>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="name()='serialize' or name()='act'"><xsl:value-of select="name()"/></xsl:when>
                    <xsl:when test="name()='transform'">xlst</xsl:when>
                </xsl:choose>
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
            <xsl:if test="count(parameter)>0">
                <td class="parameters">
                    <xsl:for-each select="parameter">
                        <pre class="comment"><xsl:value-of select="@name"/>=<xsl:value-of select="@value"/></pre>
                    </xsl:for-each>
                </td>
            </xsl:if>
            
        </tr></table></td>
        
    </xsl:if>
</xsl:template>


<xsl:template match="sitemapandfiles">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="files">
</xsl:template>

<xsl:template name="popup-attrs">
    <xsl:param name="url"/>
    <xsl:param name="target" select="'srcpopup'"/>

    <xsl:variable name="urlscript" select="concat('popupwin=window.open(&quot;', $url,'&quot; ,&quot;',$target,'&quot;,&quot;toolbar=0,scrollbars=1,location=0,statusbar=0,menubar=0,resizable=1,width=690,height=670,top=10,left=10&quot;);popupwin.focus();')"/>
    <xsl:attribute name="href"><xsl:value-of select="$url"/></xsl:attribute>
    <xsl:attribute name="target"><xsl:value-of select="$target"/></xsl:attribute>
    <xsl:attribute name="onclick"><xsl:value-of select="$urlscript"/></xsl:attribute>
</xsl:template>

<xsl:template name="make-url">
    <xsl:param name="location"/>
    <xsl:param name="subloc"/>
    <xsl:choose>
        <xsl:when test="contains($subloc,'/')">
            <xsl:call-template name="make-url">
                <xsl:with-param name="location">
                    <xsl:choose>
                        <xsl:when test="$location!=''"><xsl:value-of select="concat($location,'/',substring-before($subloc,'/'))"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="substring-before($subloc,'/')"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="subloc" select="substring-after($subloc,'/')"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="contains($subloc,'.xmap')">
            <xsl:value-of select="concat(substring-before($subloc,'.xmap'),'._xmap','?location=',$location)"/>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="concat('src/',$subloc,'?location=',$location)"/></xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
