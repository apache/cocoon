<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:param name="stylebook.project"/>
  <xsl:param name="copyright"/>
  <xsl:param name="id"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="s1">
    <html>
      <head>
        <script language="JavaScript" type="text/javascript" src="resources/script.js"/>
        <title><xsl:value-of select="@title"/></title>
      </head>
      <body text="#000000" link="#0000ff" vlink="#0000aa" alink="#ff0000"
            topmargin="4" leftmargin="4" marginwidth="4" marginheight="4"
            bgcolor="#ffffff">
        <!-- THE TOP BAR (HEADER) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td width="135" height="60" rowspan="3" valign="top" align="left">
              <img width="135" height="60" src="resources/logo.gif" hspace="0" vspace="0" border="0"/>
            </td>
            <td width="100%" height="5" valign="top" align="left" colspan="2" background="resources/line.gif">
              <img width="1" height="5" src="resources/line.gif" hspace="0" vspace="0" border="0" align="left"/>
            </td>
            <td width="29" height="60"  rowspan="3" valign="top" align="left">
              <img width="29" height="60" src="resources/right.gif" hspace="0" vspace="0" border="0"/>
            </td>
          </tr>
          <tr>
            <td width="100%" height="35" valign="top" align="left" colspan="2" bgcolor="#0086b2">
              <img src="graphics/{$id}-header.jpg" width="456" height="35" hspace="0" vspace="0" border="0" alt="{s1/@title}" align="right"/>
            </td>
          </tr>
          <tr>
            <td width="100%" height="20" valign="top" align="left" bgcolor="#0086b2" background="resources/bottom.gif">
              <img width="3" height="20" src="resources/bottom.gif" hspace="0" vspace="0" border="0" align="left"/>
            </td>
            <td align="right" bgcolor="#0086b2" height="20" valign="top" width="288" background="resources/bottom.gif">
              <table border="0" cellpadding="0" cellspacing="0" width="288">
                <tr>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://xml.apache.org/" onMouseOver="rolloverOn('xml');" onMouseOut="rolloverOff('xml');" target="new">
                      <img alt="http://xml.apache.org/" width="96" height="20" src="resources/button-xml-lo.gif"
                           name="xml" hspace="0" vspace="0" border="0"
                           onLoad="rolloverLoad('xml','resources/button-xml-hi.gif','resources/button-xml-lo.gif');"/>
                    </a>
                  </td>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://www.apache.org/" onMouseOver="rolloverOn('asf');" onMouseOut="rolloverOff('asf');" target="new">
                      <img alt="http://www.apache.org/" width="96" height="20" src="resources/button-asf-lo.gif"
                           name="asf" hspace="0" vspace="0" border="0"
                           onLoad="rolloverLoad('asf','resources/button-asf-hi.gif','resources/button-asf-lo.gif');"/>
                    </a>
                  </td>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://www.w3.org/" onMouseOver="rolloverOn('w3c');" onMouseOut="rolloverOff('w3c');" target="new">
                      <img alt="http://www.w3.org/" width="96" height="20" src="resources/button-w3c-lo.gif"
                           name="w3c" hspace="0" vspace="0" border="0"
                           onLoad="rolloverLoad('w3c','resources/button-w3c-hi.gif','resources/button-w3c-lo.gif');"/>
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
              <img width="120" height="14" src="resources/join.gif" hspace="0" vspace="0" border="0"/><br/>
                <xsl:apply-templates select="document($stylebook.project)"/>
              <img width="120" height="14" src="resources/close.gif" hspace="0" vspace="0" border="0"/><br/>
            </td>
            <!-- THE CONTENT PANEL -->
            <td width="*" valign="top" align="left">
              <table border="0" cellspacing="0" cellpadding="3">
                <tr><td><xsl:apply-templates/></td></tr>
              </table>
            </td>
          </tr>
        </table><br/>
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
  </xsl:template>

<!-- ###################################################################### -->
<!-- book -->

  <xsl:template match="book">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="document|faqs|changes|group">
    <xsl:if test="@id=$id">
      <img src="graphics/{@id}-label-1.jpg" width="120" height="12" hspace="0" vspace="0" border="0" alt="{@label}"/>
    </xsl:if>
    <xsl:if test="@id!=$id">
      <a href="{@id}.html" onMouseOver="rolloverOn('side-{@id}');" onMouseOut="rolloverOff('side-{@id}');">
        <img onLoad="rolloverLoad('side-{@id}','graphics/{@id}-label-2.jpg','graphics/{@id}-label-3.jpg');"
             name="side-{@id}" src="graphics/{@id}-label-3.jpg" width="120" height="12" hspace="0" vspace="0" border="0" alt="{@label}"/>
      </a>
    </xsl:if>
    <br/>
  </xsl:template>

  <xsl:template match="external">
    <xsl:variable name="extid" select="concat('ext-',position())"/>
    <a href="{@href}" onMouseOver="rolloverOn('side-{$extid}');" onMouseOut="rolloverOff('side-{$extid}');">
      <img onLoad="rolloverLoad('side-{$extid}','graphics/{$extid}-label-2.jpg','graphics/{$extid}-label-3.jpg');"
           name="side-{$extid}" src="graphics/{$extid}-label-3.jpg" width="120" height="12" hspace="0" vspace="0" border="0" alt="{@label}"/>
    </a>
    <br/>
  </xsl:template>

  <xsl:template match="separator">
    <img src="resources/separator.gif" width="120" height="6" hspace="0" vspace="0" border="0"/><br/>
  </xsl:template>


<!-- ###################################################################### -->
<!-- document -->

  <xsl:template match="s2">
   <div align="right">
    <table border="0" width="100%" cellspacing="0" cellpadding="0">
      <tr>
        <td width="9" height="7" valign="bottom" align="right"><img src="resources/bar-top-left.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-top.gif"><img src="resources/void.gif" width="1" height="5" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="7" valign="bottom" align="left"><img src="resources/bar-top-right.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" background="resources/bar-border-left.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
        <td width="100%" bgcolor="#0086b2">
          <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/><b><xsl:value-of select="@title"/></b></font>
         </td>
        <td width="9" background="resources/bar-border-right.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" height="12" valign="top" align="right"><img src="resources/bar-bottom-left.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-bottom.gif"><img src="resources/void.gif" height="12" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="12" valign="top" align="left"><img src="resources/bar-bottom-right.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
      </tr>
     </table>
     <table border="0" width="100%" cellspacing="0" cellpadding="0">
      <tr>
       <td>
        <font face="arial,helvetica,sanserif" color="#000000"><xsl:apply-templates/></font>
       </td>
      </tr>
    </table>
   </div>
   <br/>
  </xsl:template>

  <xsl:template match="s3">
   <br/>
   <div align="right">
    <table border="0" width="95%" cellspacing="0" cellpadding="0">
      <tr>
        <td width="9" height="7" valign="bottom" align="right"><img src="resources/bar-top-left.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-top.gif"><img src="resources/void.gif" width="1" height="5" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="7" valign="bottom" align="left"><img src="resources/bar-top-right.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" background="resources/bar-border-left.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
        <td width="100%" bgcolor="#0086b2">
          <font face="arial,helvetica,sanserif" color="#ffffff">
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/><b><xsl:value-of select="@title"/></b></font>
         </td>
        <td width="9" background="resources/bar-border-right.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" height="12" valign="top" align="right"><img src="resources/bar-bottom-left.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-bottom.gif"><img src="resources/void.gif" width="1" height="12" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="12" valign="top" align="left"><img src="resources/bar-bottom-right.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
      </tr>
     </table>
     <table border="0" width="95%" cellspacing="0" cellpadding="0">
      <tr>
       <td>
        <font size="-1" face="arial,helvetica,sanserif" color="#000000"><xsl:apply-templates/></font>
       </td>
      </tr>
    </table>
   </div>
   <br/>
  </xsl:template>

  <xsl:template match="s4">
   <br/>
   <div align="right">
    <table border="0" width="90%" cellspacing="0" cellpadding="0">
      <tr>
        <td width="9" height="7" valign="bottom" align="right"><img src="resources/bar-top-left.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-top.gif"><img src="resources/void.gif" width="1" height="5" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="7" valign="bottom" align="left"><img src="resources/bar-top-right.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" background="resources/bar-border-left.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
        <td width="100%" bgcolor="#0086b2">
          <font size="-1" face="arial,helvetica,sanserif" color="#ffffff">
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/><b><xsl:value-of select="@title"/></b></font>
         </td>
        <td width="9" background="resources/bar-border-right.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" height="12" valign="top" align="right"><img src="resources/bar-bottom-left.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-bottom.gif"><img src="resources/void.gif" width="1" height="12" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="12" valign="top" align="left"><img src="resources/bar-bottom-right.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
      </tr>
     </table>
     <table border="0" width="90%" cellspacing="0" cellpadding="0">
      <tr>
       <td>
        <font size="-2" face="arial,helvetica,sanserif" color="#000000"><xsl:apply-templates/></font>
       </td>
      </tr>
    </table>
   </div>
   <br/>
  </xsl:template>

<!-- ###################################################################### -->
<!-- blocks -->

  <xsl:template match="p">
    <p align="justify"><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="note">
   <p>
    <table width="100%" cellspacing="3" cellpadding="0" border="0">
      <tr>
        <td width="28" valign="top">
          <img src="resources/note.gif" width="28" height="29" vspace="0" hspace="0" border="0" alt="Note"/>
        </td>
        <td valign="top">
          <font size="-1" face="arial,helvetica,sanserif" color="#000000">
            <i>
              <xsl:apply-templates/>
            </i>
          </font>
        </td>
      </tr>  
    </table>
   </p>
  </xsl:template>

  <xsl:template match="ul">
    <ul><xsl:apply-templates/></ul>
  </xsl:template>

  <xsl:template match="ol">
    <ol><xsl:apply-templates/></ol>
  </xsl:template>

  <xsl:template match="li">
    <li><xsl:apply-templates/></li>
  </xsl:template>

  <xsl:template match="source">
  <div align="center">
  <table cellspacing="4" cellpadding="0" border="0">
    <tr>
      <td bgcolor="#0086b2" width="1"   height="1"><img src="resources/void.gif" width="1"   height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" width="1"   height="1"><img src="resources/void.gif" width="1"   height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    <tr>
      <td bgcolor="#0086b2" width="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#ffffff"><pre><xsl:apply-templates/></pre></td>
      <td bgcolor="#0086b2" width="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    <tr>
      <td bgcolor="#0086b2" width="1"   height="1"><img src="resources/void.gif" width="1"   height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#0086b2" width="1"   height="1"><img src="resources/void.gif" width="1"   height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
  </table>
  </div>
  </xsl:template>

  <xsl:template match="table">
    <table width="100%" border="0" cellspacing="2" cellpadding="2">
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

<!-- ###################################################################### -->
<!-- markup -->

  <xsl:template match="em">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="ref">
    <i><xsl:apply-templates/></i>
  </xsl:template>
  
  <xsl:template match="code">
    <code><font face="courier, monospaced"><xsl:apply-templates/></font></code>
  </xsl:template>
  
  <xsl:template match="br">
    <br/>
  </xsl:template>
  
<!-- ###################################################################### -->
<!-- links -->

  <xsl:template match="link">
    <xsl:if test="string-length(@anchor)=0">
      <xsl:if test="string-length(@idref)=0">
        <xsl:apply-templates/>
      </xsl:if>
      <xsl:if test="string-length(@idref)>0">
        <a href="{@idref}.html"><xsl:apply-templates/></a>
      </xsl:if>
    </xsl:if>

    <xsl:if test="string-length(@anchor)>0">
      <xsl:if test="string-length(@idref)=0">
        <a href="#{@anchor}"><xsl:apply-templates/></a>
      </xsl:if>
      <xsl:if test="string-length(@idref)>0">
        <a href="{@idref}.html#{@anchor}"><xsl:apply-templates/></a>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="anchor">
    <a name="{@name}"><xsl:comment>anchor</xsl:comment></a>
  </xsl:template>

  <xsl:template match="jump">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="img">
    <img src="images/{@src}" border="0" vspace="4" hspace="4" align="right"/>
  </xsl:template>

<!-- ###################################################################### -->
<!-- copy

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
-->
</xsl:stylesheet>