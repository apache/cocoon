<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:source="http://xml.apache.org/cocoon/source/2.0"
                xmlns:dav="DAV:">

 <xsl:output indent="yes"/>

 <xsl:template match="/">
  <html>
   <head>
    <title>Apache Cocoon @version@</title>
    <link rel="SHORTCUT ICON" href="favicon.ico"/>
   </head>
   <body bgcolor="#ffffff" link="#0086b2" vlink="#00698c" alink="#743e75">
    <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
     <tr>
      <td width="*"><font face="arial,helvetica,sanserif" color="#000000">The Apache Software Foundation is proud to present...</font></td>
      <td width="40%" align="center"><img border="0" src="/cocoon/samples/images/cocoon.gif"/></td>
      <td width="30%" align="center"><font face="arial,helvetica,sanserif" color="#000000"><b>version @version@</b></font></td>
     </tr>
     <tr>
       <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
         <tr>
            <td width="90%" align="left" bgcolor="#0086b2"><font size="+1" face="arial,helvetica,sanserif"
    color="#ffffff"><xsl:value-of select="source:source/@uri"/></font></td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/users/">
             <i>users</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/content/{substring-after(source:source/@uri,'://')}">
             <i>content</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/properties/{substring-after(source:source/@uri,'://')}">
             <i>properties</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/permissions/{substring-after(source:source/@uri,'://')}">
             <i>permissions</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/locks/{substring-after(source:source/@uri,'://')}">
             <i>locks</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/logout.html">
             <i>logout</i></a>
            </td>
         </tr>
       </table>
     </tr>
    </table>

    <xsl:apply-templates select="source:source"/>

    <p align="center">
     <font size="-1">
      Copyright &#169; @year@ <a href="http://www.apache.org/">The Apache Software Foundation</a>.<br/>
      All rights reserved.
     </font>
    </p>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="source:source">

  <table width="100%">
   <tr>
    <td width="200" valign="top">
     <table border="0" bgcolor="#000000" cellpadding="0" cellspacing="0" width="97%">
      <tbody>
       <tr>
        <td>
         <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
          <tr>
           <td bgcolor="#0086b2" width="100%" align="left">
            <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">Navigation</font>
           </td>
          </tr>
          <tr>
           <td width="100%" bgcolor="#ffffff" align="left">
            <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">
             <xsl:if test="@parent">
              <tr>
               <td width="100%" bgcolor="#ffffff" align="left">
                <a href="/cocoon/samples/slide/locks/{substring-after(@parent,'://')}">Back</a>
               </td>
              </tr>
             </xsl:if>
             <tr>
              <td width="100%" bgcolor="#ffffff" align="left">
               <br/>
              </td>
             </tr>
             <xsl:for-each select="source:children/source:source">
              <tr>
               <td width="100%" bgcolor="#ffffff" align="left">
                <font size="+0" face="arial,helvetica,sanserif" color="#000000">
                 <a href="/cocoon/samples/slide/locks/{substring-after(@uri,'://')}"
                  ><xsl:value-of select="@name"/></a>
                </font>
               </td>
              </tr>
             </xsl:for-each>
            </table>
           </td>
          </tr>
         </table>

        </td>
       </tr> 
      </tbody>
     </table>

     <br/>
    </td>

    <td valign="top">
     <table border="0" bgcolor="#000000" cellpadding="0" cellspacing="0" width="97%">
      <tbody>
       <tr>
        <td>
         <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
          <tr>
           <td bgcolor="#0086b2" width="100%" align="left">
            <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">Locks</font>
           </td>
          </tr>
          <tr>
           <td width="100%" bgcolor="#ffffff" align="left">
            <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">
             <font size="+0" face="arial,helvetica,sanserif" color="#000000">
              <tr>
               <td align="left"><b>Subject</b></td>
               <td align="left"><b>Type</b></td>
               <td align="left"><b>Expiration</b></td>
               <td align="left"><b>Inheritable</b></td>
               <td align="left"><b>Exclusive</b></td>
               <td align="right"></td>
              </tr>

              <xsl:for-each select="source:locks/source:lock">
               <tr bgcolor="#eeeeee">
                <td align="left"><xsl:value-of select="@subject"/></td>
                <td align="left"><xsl:value-of select="@type"/></td>
                <td align="left"><xsl:value-of select="@expiration"/></td>
                <td align="left"><xsl:value-of select="@inheritable"/></td>
                <td align="left"><xsl:value-of select="@exclusive"/></td>
                <td align="right">
                 <form action="" method="post">
                  <input type="hidden" name="method" value="doDeleteLock"/>
                  <input type="hidden" name="cocoon-source-uri" value="{../../@uri}"/>
                  <input type="hidden" name="cocoon-lock-subject" value="{@subject}"/>

                  <input type="submit" name="cocoon-action-deletelock" value="Delete"/>
                 </form>
                </td>
               </tr>
              </xsl:for-each>

              <tr>
               <form action="" method="post">
                <input type="hidden" name="method" value="doAddLock"/>
                <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
                <td align="left">
                 <input name="cocoon-lock-subject" type="text" size="20" maxlength="40"/>
                </td>
                <td align="left">
                 <input name="cocoon-lock-type" type="text" size="15" maxlength="40"/>
                </td>
                <td align="left">
                 <input name="cocoon-lock-expiration" type="text" size="15" maxlength="40"/>
                </td>
                <td align="left">
                 <select name="cocoon-lock-inheritable">
                  <option>true</option>
                  <option>false</option>
                 </select>
                </td>
                <td align="left">
                 <select name="cocoon-lock-exclusive">
                  <option>true</option>
                  <option>false</option>
                 </select>
                </td>
                <td align="right">
                 <input type="submit" name="cocoon-action-addlock" value="Add/Modify"/>
                </td>
               </form>
              </tr>
             </font>
            </table>
           </td>
          </tr>
         </table>

        </td>
       </tr> 
      </tbody>
     </table>

     <br/>
    </td>
   </tr>
  </table>

 </xsl:template>

</xsl:stylesheet>
