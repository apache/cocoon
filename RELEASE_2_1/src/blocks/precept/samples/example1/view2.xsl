<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:i="http://www.dff.st/ns/desire/instance/1.0">
   <xsl:template match="root">
     <html><body><form method="POST"><xsl:apply-templates/></form></body></html>
   </xsl:template>
   
   <xsl:template match="cocoon-installation">
     <table border="1">
     <tr>
       <td>Installation number</td>
       <td><input type="textbox" name="cocoon-installation/number" value="{number/text()}"/></td>
     </tr>
     <tr>
       <td>Live URL</td>
       <td><input type="textbox" name="cocoon-installation/live-url" value="{live-url/text()}"/></td>
     </tr>
     <tr>
       <td>Publish this URL</td>
       <td><input type="checkbox" name="cocoon-installation/publish" value="true">
              <xsl:if test="publish/text() = 'true'">
                <xsl:attribute name="checked"/>
              </xsl:if>
           </input>
       </td>
     </tr>
     </table>
     <input type="submit" name="cocoon-method-prev1" value="Prev Page"/>
     <input type="submit" name="cocoon-method-next3" value="Next Page"/>
   </xsl:template>
   
   <xsl:template match="/|*">
      <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates /></xsl:copy>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:value-of select="." />
   </xsl:template>
</xsl:stylesheet>

