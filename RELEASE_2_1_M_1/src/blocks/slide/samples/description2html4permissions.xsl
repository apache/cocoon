<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:source="http://xml.apache.org/cocoon/source/2.0"
                xmlns:dav="DAV:"
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
    color="#ffffff"><xsl:value-of select="document/source:source/@uri"/></font></td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/users/">
             <i>users</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/content/{substring-after(document/source:source/@uri,'://')}">
             <i>content</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/properties/{substring-after(document/source:source/@uri,'://')}">
             <i>properties</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/permissions/{substring-after(document/source:source/@uri,'://')}">
             <i>permissions</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a
    href="/cocoon/samples/slide/locks/{substring-after(document/source:source/@uri,'://')}">
             <i>locks</i></a>
            </td>
            <td nowrap="nowrap" bgcolor="#ffffff"><a href="/cocoon/samples/slide/logout.html">
             <i>logout</i></a>
            </td>
         </tr>
       </table>
     </tr>
    </table>

    <xsl:apply-templates select="document/source:source"/>

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
                <a href="/cocoon/samples/slide/permissions/{substring-after(@parent,'://')}">Back</a>
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
                 <a href="/cocoon/samples/slide/permissions/{substring-after(@uri,'://')}"
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
            <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">User permissions</font>
           </td>
          </tr>
          <tr>
           <td width="100%" bgcolor="#ffffff" align="left">
            <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">
             <font size="+0" face="arial,helvetica,sanserif" color="#000000">
              <tr>
               <td align="left"><b>Principal</b></td>
               <td align="left"><b>Privilege</b></td>
               <td align="left"><b>Inheritable</b></td>
               <td align="left"><b>Deny</b></td>
               <td align="right"></td>
              </tr>

              <xsl:for-each select="source:permissions/source:permission[@principal]">
               <tr>
                <td align="left"><xsl:value-of select="@principal"/></td>
                <td align="left"><xsl:value-of select="@privilege"/></td>
                <td align="left"><xsl:value-of select="@inheritable"/></td>
                <td align="left"><xsl:value-of select="@negative"/></td>
                <td align="right">
                 <form action="" method="post">
                  <input type="hidden" name="method" value="doRemovePrincipalPermission"/>
                  <input type="hidden" name="cocoon-source-uri" value="{../../@uri}"/>
                  <input type="hidden" name="cocoon-source-permission-principal" value="{@principal}"/>
                  <input type="hidden" name="cocoon-source-permission-privilege" value="{@privilege}"/>
                  <input type="hidden" name="cocoon-source-permission-inheritable" value="{@inheritable}"/>
                  <input type="hidden" name="cocoon-source-permission-negative" value="{@negative}"/>

                  <input type="submit" name="doRemovePrincipalPermission" value="Delete"/>
                 </form>
                </td>
               </tr>
              </xsl:for-each>

              <tr>
               <form action="" method="post">
                <input type="hidden" name="method" value="doAddPrincipalPermission"/>
                <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
                <td align="left">
                 <select name="cocoon-source-permission-principal">
                  <option>ALL</option>
                  <option>SELF</option>
                  <option>GUEST</option>
                  <xsl:for-each select="/document/pl:list/pl:principal">
                   <option><xsl:value-of select="@pl:name"/></option>
                  </xsl:for-each>
                 </select>
                </td>
                <td align="left">
                 <select name="cocoon-source-permission-privilege">
                  <option>all</option>
                  <option>read</option>
                  <option>write</option>
                  <option>read-acl</option>
                  <option>write-acl</option>
                  <option>read-source</option>
                  <option>create-source</option>
                  <option>remove-source</option>
                  <option>lock-source</option>
                  <option>read-locks</option>
                  <option>read-property</option>
                  <option>create-property</option>
                  <option>modify-property</option>
                  <option>remove-property</option>
                  <option>read-content</option>
                  <option>create-content</option>
                  <option>modify-content</option>
                  <option>remove-content</option>
                  <option>grant-permission</option>
                  <option>revoke-permission</option>
                 </select>
                </td>
                <td align="left">
                 <select name="cocoon-source-permission-inheritable">
                  <option>true</option>
                  <option>false</option>
                 </select>
                </td>
                <td align="left">
                 <select name="cocoon-source-permission-negative">
                  <option>true</option>
                  <option>false</option>
                 </select>
                </td>
                <td align="right">
                 <input type="submit" name="doAddPrincipalPermission" value="Add/Modify"/>
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

     <table border="0" bgcolor="#000000" cellpadding="0" cellspacing="0" width="97%">
      <tbody>
       <tr>
        <td>
         <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
          <tr>
           <td bgcolor="#0086b2" width="100%" align="left">
            <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">Group permissions</font>
           </td>
          </tr>
          <tr>
           <td width="100%" bgcolor="#ffffff" align="left">
            <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">
             <tr>
              <td align="left"><b>Group</b></td>
              <td align="left"><b>Privilege</b></td>
              <td align="left"><b>Inheritable</b></td>
              <td align="left"><b>Deny</b></td>
              <td align="right"></td>
             </tr>

             <xsl:for-each select="source:permissions/source:permission[@group]">
              <tr>
               <td align="left"><xsl:value-of select="@group"/></td>
               <td align="left"><xsl:value-of select="@privilege"/></td>
               <td align="left"><xsl:value-of select="@inheritable"/></td>
               <td align="left"><xsl:value-of select="@negative"/></td>
               <td align="right">
                <form action="" method="post">
                 <input type="hidden" name="method" value="doRemovePrincipalGroupPermission"/>
                 <input type="hidden" name="cocoon-source-uri" value="{../../@uri}"/>
                 <input type="hidden" name="cocoon-source-permission-principal-group" value="{@group}"/>
                 <input type="hidden" name="cocoon-source-permission-privilege" value="{@privilege}"/>
                 <input type="hidden" name="cocoon-source-permission-inheritable" value="{@inheritable}"/>
                 <input type="hidden" name="cocoon-source-permission-negative" value="{@negative}"/>

                 <input type="submit" name="doRemovePrincipalGroupPermission" value="Delete"/>
                </form>
               </td>
              </tr>
             </xsl:for-each>

             <tr>
              <form action="" method="post">
               <input type="hidden" name="method" value="doAddPrincipalGroupPermission"/>
               <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
               <td align="left">
                <select name="cocoon-source-permission-principal-group">
                 <xsl:for-each select="/document/pl:list/pl:group">
                  <option><xsl:value-of select="@pl:name"/></option>
                 </xsl:for-each>
                </select>
               </td>
               <td align="left">
                <select name="cocoon-source-permission-privilege">
                 <option>all</option>
                 <option>read</option>
                 <option>write</option>
                 <option>read-acl</option>
                 <option>write-acl</option>
                 <option>read-source</option>
                 <option>create-source</option>
                 <option>remove-source</option>
                 <option>lock-source</option>
                 <option>read-locks</option>
                 <option>read-property</option>
                 <option>create-property</option>
                 <option>modify-property</option>
                 <option>remove-property</option>
                 <option>read-content</option>
                 <option>create-content</option>
                 <option>modify-content</option>
                 <option>remove-content</option>
                 <option>grant-permission</option>
                 <option>revoke-permission</option>
                </select>
               </td>
               <td align="left">
                <select name="cocoon-source-permission-inheritable">
                 <option>true</option>
                 <option>false</option>
                </select>
               </td>
               <td align="left">
                <select name="cocoon-source-permission-negative">
                 <option>true</option>
                 <option>false</option>
                </select>
               </td>
               <td align="right">
                <input type="submit" name="doAddPrincipalPermission" value="Add/Modify"/>
               </td>
              </form>
             </tr>
            </table>
           </td>
          </tr>
         </table>
        </td>
       </tr>
      </tbody>
     </table>
    </td>
   </tr>
  </table>

 </xsl:template>

</xsl:stylesheet>
