<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:pl="http://xml.apache.org/cocoon/PrincipalListGenerator">

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
    color="#ffffff">User management</font></td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/users/">
             <i>users</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/content/">
             <i>content</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/properties/">
             <i>properties</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/permissions/">
             <i>permissions</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/locks/">
             <i>locks</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/logout.html">
             <i>logout</i></a>
            </td>
         </tr>
       </table>
     </tr>
    </table>

    <xsl:apply-templates select="pl:list"/>

    <p align="center">
     <font size="-1">
      Copyright &#169; @year@ <a href="http://www.apache.org/">The Apache Software Foundation</a>.<br/>
      All rights reserved.
     </font>
    </p>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="pl:list">

  <table width="100%">
   <tr>

    <td width="60%" valign="top">
     <table border="0" bgcolor="#000000" cellpadding="0" cellspacing="0" width="97%">
      <tbody>
       <tr>
        <td>
         <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
          <tr>
           <td bgcolor="#0086b2" width="100%" align="left">
            <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">Users</font>
           </td>
          </tr>
          <tr>
           <td width="100%" bgcolor="#ffffff" align="left">
            <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">

             <font size="+0" face="arial,helvetica,sanserif" color="#000000">

              <tr>
               <td align="left"><b>User</b></td>
               <td align="left"></td>
               <td align="left"><b>Role</b></td>
               <td align="left"><b>Groups</b></td>
               <td align="left"></td>
               <td align="right"></td>
              </tr>
 
              <xsl:for-each select="pl:principal">
               <tr>
  
                <form action="" method="post">
                <input type="hidden" name="cocoon-principal-name" value="{@name}"/>
                <input type="hidden" name="method" value="doRemovePrincipal"/>
 
                 <td align="left">
                  <xsl:value-of select="@name"/>
                 </td>
 
                 <td align="left">
                  <input type="submit" name="cocoon-action-deleteprincipal" value="Delete user"/>
                 </td>
 
                </form>
 
                <td align="left"><xsl:value-of select="@role"/></td>
 
                <form action="" method="post">
                <input type="hidden" name="cocoon-principal-name" value="{@name}"/>
                <input type="hidden" name="method" value="doRemovePrincipalGroupMember"/>
 
                 <td align="left">
                  <xsl:variable name="name" select="@name"/>
                  <select name="cocoon-principal-group-name" 
                          size="{count(../pl:group/pl:principal[@name=$name])}">
                   <xsl:for-each select="../pl:group/pl:principal[@name=$name]">
                    <option><xsl:value-of select="../@name"/></option>
                   </xsl:for-each>
                  </select>
                 </td>
 
                 <td align="left">
                  <input type="submit" name="cocoon-action-removemember" value="Remove group"/>
                 </td>
                </form>
 
               </tr>
 
               <tr>
                <td align="left"></td>
                <td align="left"></td>
                <td align="left"></td>
 
                <form action="" method="post">
                <input type="hidden" name="cocoon-principal-name" value="{@name}"/>
                <input type="hidden" name="method" value="doAddPrincipalGroupMember"/>
 
                 <td align="left">
                  <xsl:variable name="name" select="@name"/>
                  <select name="cocoon-principal-group-name" size="1">
                   <xsl:for-each select="../pl:group">
                    <option><xsl:value-of select="@name"/></option>
                   </xsl:for-each>
                  </select>
                 </td>
 
                 <td align="left">
                  <input type="submit" name="cocoon-action-addmember" value="Add group"/>
                 </td>
                </form>
 
               </tr>
              </xsl:for-each>
 
              <tr>
               <form action="" method="post">
                <input type="hidden" name="method" value="doAddPrincipal"/>
                <td align="left">
                 <input name="cocoon-principal-name" type="text" size="10" maxlength="40"/>
                </td>
                <td align="left"></td>
                <td align="left">
                 <input name="cocoon-principal-role" type="text" size="10" maxlength="40"/>
                </td>
                <td align="left">Password:</td>
                <td align="left">
                 <input name="cocoon-principal-password" type="text" size="10" maxlength="40"/>
                </td>
                <td align="right">
                 <input type="submit" name="cocoon-action-addprincipal" value="Add user"/>
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

    <td valign="top">
     <table border="0" bgcolor="#000000" cellpadding="0" cellspacing="0" width="97%">
      <tbody>
       <tr>
        <td>
         <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
          <tr>
           <td bgcolor="#0086b2" width="100%" align="left">
            <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">Groups</font>
           </td>
          </tr>
          <tr>
           <td width="100%" bgcolor="#ffffff" align="left">
            <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">

             <font size="+0" face="arial,helvetica,sanserif" color="#000000">

              <tr>
               <td align="left"><b>Group</b></td>
               <td align="right"></td>
              </tr>

              <xsl:for-each select="pl:group">
               <tr>

                <form action="" method="post">
                 <input type="hidden" name="cocoon-principal-group-name" value="{@name}"/>
                 <input type="hidden" name="method" value="doRemovePrincipalGroup"/>
 
                 <td align="left">
                  <xsl:value-of select="@name"/>
                 </td>

                 <td align="right">
                  <input type="submit" name="cocoon-action-deletegroup" value="Delete group"/>
                 </td>

                </form>
               </tr>
              </xsl:for-each>

              <tr>
               <form action="" method="post">
                <input type="hidden" name="method" value="doAddPrincipalGroup"/>
                <td align="left">
                 <input name="cocoon-principal-group-name" type="text" size="15" maxlength="40"/>
                </td>
                <td align="right">
                 <input type="submit" name="cocoon-action-addgroup" value="Add group"/>
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
