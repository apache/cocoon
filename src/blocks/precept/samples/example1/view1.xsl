<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:i="http://www.dff.st/ns/desire/instance/1.0">
   <xsl:template match="root">
     <html><body><form method="POST"><xsl:apply-templates/></form></body></html>
   </xsl:template>
   
   <xsl:template match="cocoon-installation">
     <table border="1">
     <tr>
       <td>Firstname</td>
       <td><input type="textbox" name="cocoon-installation/user/firstname" value="{user/firstname/text()}"/></td>
       <td><xsl:apply-templates select="user/firstname/constraint"/></td>
     </tr>
     <tr>
       <td>Lastname</td>
       <td><input type="textbox" name="cocoon-installation/user/lastname" value="{user/lastname/text()}"/></td>
       <td><xsl:apply-templates select="user/lastname/constraint"/></td>
     </tr>
     <tr>
       <td>Email</td>
       <td><input type="textbox" name="cocoon-installation/user/email" value="{user/email/text()}"/></td>
       <td><xsl:apply-templates select="user/email/constraint"/></td>
     </tr>
     <tr>
       <td>Age</td>
       <td><input type="textbox" name="cocoon-installation/user/age" value="{user/age/text()}"/></td>
       <td><xsl:apply-templates select="user/age/constraint"/></td>
     </tr>
     </table>
     <input type="submit" name="cocoon-method-next2" value="Next Page"/>
   </xsl:template>
   
   <xsl:template match="/|*">
      <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates /></xsl:copy>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:value-of select="." />
   </xsl:template>
</xsl:stylesheet>
