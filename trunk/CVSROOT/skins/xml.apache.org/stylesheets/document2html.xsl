<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:param name="stylebook.project"/>
  <xsl:param name="copyright"/>
  <xsl:param name="id"/>

<!-- ====================================================================== -->
<!-- document section -->
<!-- ====================================================================== -->

 <xsl:template match="/">
  <!-- checks if this is the included document to avoid neverending loop -->
  <xsl:if test="not(book)">
    <html>
      <head>
        <title><xsl:value-of select="document/header/title"/></title>
<STYLE type="text/css">
a.menu {
  color: #FFFFFF;
  text-align:left;               
  font-size:12px;
  font-family: Verdana, Arial, Helvetica, sans-serif;
  font-weight:plain;
  text-decoration:none;
  padding-left: 14px
}

A.menu:hover {
  color: #FFCC00
}
.menutitle {
  color: #000000;
  text-align:left;               
  font-size:10px;
  font-family: Verdana, Arial, Helvetica, sans-serif;
  font-weight:bold;
  padding-left: 8px
}
.menuselected {
  color: #FFCC00;
  text-align:left;               
  font-size:12px;
  font-family: Verdana, Arial, Helvetica, sans-serif;
  font-weight:bold;
  padding-left: 14px
}
</STYLE>     
      </head>
      <body text="#000000" link="#039acc" vlink="#0086b2" alink="#cc0000"
            topmargin="4" leftmargin="4" marginwidth="4" marginheight="4"
            bgcolor="#ffffff">
        <!-- THE TOP BAR (HEADER) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td width="135" height="60" rowspan="3" valign="top" align="left">
              <img width="135" height="60" src="resources/logo.gif" hspace="0" vspace="0" border="0"/>
            </td>
            <td width="100%" height="0" valign="top" align="left" colSpan="2" rowspan="1" background="resources/line.gif">
            </td>
            <td width="29" height="60"  rowspan="3" valign="top" align="left">
              <img width="29" height="60" src="resources/right.gif" hspace="0" vspace="0" border="0"/>
            </td>
          </tr>
          <tr>
            <td width="100%" height="35" valign="top" align="right" colspan="2" bgcolor="#0086b2">
              <font size="5" face="Verdana, Arial, Helvetica, sans-serif" color="#ffffff"><xsl:value-of select="document/header/title"/></font>
            </td>
          </tr>
          <tr>
            <td align="right" bgcolor="#0086b2" height="20" valign="top" width="100%" colspan="2" background="resources/bottom.gif">
               <table border="0" cellpadding="0" cellspacing="0" width="288">
                <tr>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://xml.apache.org/" target="new">
                      <img alt="http://xml.apache.org/" width="96" height="20" src="resources/button-xml-lo.gif" name="xml" hspace="0" vspace="0" border="0"/>
                    </a>
                  </td>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://www.apache.org/" target="new">
                      <img alt="http://www.apache.org/" width="96" height="20" src="resources/button-asf-lo.gif" name="asf" hspace="0" vspace="0" border="0"/>
                    </a>
                  </td>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://www.w3.org/" target="new">
                      <img alt="http://www.w3.org/" width="96" height="20" src="resources/button-w3c-lo.gif" name="w3c" hspace="0" vspace="0" border="0"/>
                    </a>
                  </td>
                </tr>
              </table>              
            </td>
          </tr>
        </table>
        
        <!-- THE MAIN PANEL (SIDEBAR AND CONTENT) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <!-- THE SIDE BAR -->
            <td width="120" valign="top" align="left">
              <table cellpadding="0" cellspacing="0">
                <tr>
                  <td valign="top" align="left">
              <img width="120" height="14" src="resources/join.gif" hspace="0" vspace="0" border="0"/><br/>
                  </td>
                </tr>
              <xsl:apply-templates select="document($stylebook.project)"/>
                <tr>
                  <td valign="top" align="left">
              <img width="120" height="14" src="resources/close.gif" hspace="0" vspace="0" border="0"/><br/>
                  </td>
                </tr>
              </table>
            </td>
            <!-- THE CONTENT PANEL -->
            <td width="*" valign="top" align="left">
              <table border="0" cellspacing="0" cellpadding="3">
                <tr><td><br/><xsl:apply-templates/></td></tr>
              </table>
            </td>
          </tr>
        </table>
        
        <br/>
        
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr><td bgcolor="#0086b2"><img src="images/dot.gif" width="1" height="1"/></td></tr>
          <tr>
            <td align="center"><font face="arial,helvetica,sanserif" size="-1" color="#0086b2"><i>
              Copyright &#169; <xsl:value-of select="$copyright"/>.
              All Rights Reserved.
            </i></font></td>
          </tr>
        </table>
      </body>
    </html>
   </xsl:if>
   
   <xsl:if test="book">
    <xsl:apply-templates/>
   </xsl:if>
  </xsl:template>

<!-- ====================================================================== -->
<!-- book section -->
<!-- ====================================================================== -->

  <xsl:template match="page|faqs|changes|todo|spec">
    <tr>
    <td valign="top" align="left" bgcolor="#959595" background="resources/label-background_a.gif">
    <xsl:if test="@id=$id">
      <span class="menuselected"><xsl:value-of select="@label"/></span>
    </xsl:if>
    <xsl:if test="@id!=$id">
      <a href="{@id}.html" class="menu"><xsl:value-of select="@label"/></a>
    </xsl:if>
    <br/>
    </td>
    </tr>
  </xsl:template>

  <xsl:template match="external">
    <xsl:variable name="extid" select="concat('ext-',position())"/>
    <tr>
    <td valign="top" align="left" bgcolor="#959595" background="resources/label-background_a.gif">
    <a href="{@href}" class="menu"><xsl:value-of select="@label"/></a>
    <br/>
    </td>
    </tr>
  </xsl:template>

  <xsl:template match="separator">
    <tr>
    <td valign="top" align="left" bgcolor="#959595">
    <img src="resources/separator.gif" width="120" height="6" hspace="0" vspace="0" border="0"/><br/>
    </td>
    </tr>
  </xsl:template>
  
<!-- ====================================================================== -->
<!-- header section -->
<!-- ====================================================================== -->

 <xsl:template match="header">
  <!-- ignore on general document -->
 </xsl:template>

<!-- ====================================================================== -->
<!-- body section -->
<!-- ====================================================================== -->

  <xsl:template match="s1">
    <font color="#0086b2" size="+2" face="verdana, helvetica, sans serif">
      <xsl:value-of select="@title"/>
    </font>
    <hr size="1" style="color: #0086b2"/>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="s2">
    <font color="#0086b2" size="+1" face="verdana, helvetica, sans serif">
      <b><xsl:value-of select="@title"/></b>
    </font>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="s3">
    <font color="#0086b2" size="+1" face="verdana, helvetica, sans serif">
      <xsl:value-of select="@title"/>
    </font>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="s4">
    <font color="#0086b2" face="verdana, helvetica, sans serif">
      <b><xsl:value-of select="@title"/></b>
    </font>
    <xsl:apply-templates/>
  </xsl:template>

<!-- ====================================================================== -->
<!-- footer section -->
<!-- ====================================================================== -->

 <xsl:template match="footer">
  <!-- ignore on general documents -->
 </xsl:template>

<!-- ====================================================================== -->
<!-- paragraph section -->
<!-- ====================================================================== -->
  <xsl:template match="p">
    <p><font face="verdana, helvetica, sans serif" color="black">
    <xsl:apply-templates/></font></p>
  </xsl:template>
  <xsl:template match="note">
   <p>
    <table width="100%" cellspacing="3" cellpadding="0" border="0">
      <tr>
        <td width="28" valign="top">
          <img src="resources/note.gif" width="28" height="29" vspace="0" hspace="0" border="0" alt="Note"/>
        </td>
        <td valign="top">
          <font size="-1" face="verdana, helvetica, sans serif" color="#000000">
            <i>
              <xsl:apply-templates/>
            </i>
          </font>
        </td>
      </tr>  
    </table>
   </p>
  </xsl:template>
  <xsl:template match="source">
   <div align="center">
    <table cellspacing="4" cellpadding="0" border="0">
    <tr>
      <td bgcolor="#0086b2" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    <tr>
      <td bgcolor="#0086b2" width="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#ffffff"><pre><xsl:apply-templates/></pre></td>
      <td bgcolor="#0086b2" width="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    <tr>
      <td bgcolor="#0086b2" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    </table>
   </div>
  </xsl:template>
  <xsl:template match="fixme">
    <!-- ignore on documentation -->
  </xsl:template>
  
<!-- ====================================================================== -->
<!-- list section -->
<!-- ====================================================================== -->

 <xsl:template match="ul|ol">
   <blockquote>
     <xsl:copy>
       <xsl:apply-templates/>
     </xsl:copy>
   </blockquote>
 </xsl:template>
 
 <xsl:template match="li">
   <xsl:copy><font face="verdana, helvetica, sans serif">
     <xsl:apply-templates/>
   </font></xsl:copy>
 </xsl:template>

 <xsl:template match="dl">
   <blockquote><font face="verdana, helvetica, sans serif">
     <xsl:copy>
       <xsl:apply-templates/>
     </xsl:copy>
   </font></blockquote>
 </xsl:template>

 <xsl:template match="sl">
   <ul>
     <xsl:apply-templates/>
   </ul>
 </xsl:template>

 <xsl:template match="dt">
   <li>
     <strong><xsl:value-of select="."/></strong>
     <xsl:text> - </xsl:text>
     <xsl:apply-templates select="dd"/>   
   </li>
 </xsl:template>
 
<!-- ====================================================================== -->
<!-- table section -->
<!-- ====================================================================== -->

  <xsl:template match="table">
    <table width="100%" border="0" cellspacing="2" cellpadding="2">
      <caption><xsl:value-of select="caption"/></caption>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="tr">
    <tr><xsl:apply-templates/></tr>
  </xsl:template>

  <xsl:template match="th">
    <td bgcolor="#039acc" colspan="{@colspan}" rowspan="{@rowspan}" valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
        <b><xsl:apply-templates/></b>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="td">
    <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
      <font color="#000000" size="-1" face="arial,helvetica,sanserif">
        <xsl:apply-templates/>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="tn">
    <td bgcolor="#ffffff" colspan="{@colspan}" rowspan="{@rowspan}">
      &#160;
    </td>
  </xsl:template>
  
  <xsl:template match="caption">
    <!-- ignore since already used -->
  </xsl:template>

<!-- ====================================================================== -->
<!-- markup section -->
<!-- ====================================================================== -->

 <xsl:template match="strong">
   <b><xsl:apply-templates/></b>
 </xsl:template>

 <xsl:template match="em">
    <i><xsl:apply-templates/></i>
 </xsl:template>

 <xsl:template match="code">
    <code><font face="courier, monospaced"><xsl:apply-templates/></font></code>
 </xsl:template>
 
<!-- ====================================================================== -->
<!-- images section -->
<!-- ====================================================================== -->

 <xsl:template match="figure">
  <p align="center"><img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4"/></p>
 </xsl:template>
 
 <xsl:template match="img">
   <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4" align="right"/>
 </xsl:template>

 <xsl:template match="icon">
   <img src="{@src}" alt="{@alt}" border="0" align="absmiddle"/>
 </xsl:template>

<!-- ====================================================================== -->
<!-- links section -->
<!-- ====================================================================== -->

 <xsl:template match="link">
   <a href="{@href}"><xsl:apply-templates/></a>
 </xsl:template>

 <!-- hacky hacky! :-) -->
 <xsl:template match="connect">

  <xsl:variable name="cref"><xsl:value-of select="@href"/></xsl:variable>

  <!-- look up the soft link in the index to try and convert it to a hard link -->
  <xsl:variable name="converted"><xsl:value-of select="document('sbk:/sources/book.xml')//*[@source=$cref]/@id"/></xsl:variable>

  <xsl:variable name="htmlref"><xsl:choose>
    <xsl:when test="$converted"><xsl:value-of select="$converted"/>.html</xsl:when>
    <xsl:when test="substring(@href,string-length(@href)-3)='.xml'">
     <!-- if not in the index, guess -->
     <xsl:value-of select="substring(@href,1,string-length(@href)-4)"/>.html
    </xsl:when>
    <xsl:otherwise>
     <!-- give up -->
     <xsl:value-of select="@href"/>
    </xsl:otherwise>
   </xsl:choose></xsl:variable>

  <a href="{$htmlref}"><xsl:apply-templates/></a>
 </xsl:template>

 <xsl:template match="jump">
   <a href="{@href}#{@anchor}"><xsl:apply-templates/></a>
 </xsl:template>

 <xsl:template match="fork">
   <a href="{@href}" target="_blank"><xsl:apply-templates/></a>
 </xsl:template>

 <xsl:template match="anchor">
   <a name="{@id}"><xsl:comment>anchor</xsl:comment></a>
 </xsl:template>  

<!-- ====================================================================== -->
<!-- specials section -->
<!-- ====================================================================== -->

 <xsl:template match="br">
  <br/>
 </xsl:template>

</xsl:stylesheet>
