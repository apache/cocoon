<?xml version="1.0" encoding="iso-8859-1" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i="http://www.dff.st/ns/desire/instance/1.0">
   <xsl:template match="/">
      <html>
         <body>
         <form method="POST">
            <xsl:apply-templates />
         </form>
         </body>
      </html>
   </xsl:template>

   <xsl:template match="rows">
       <table border="1">
           <xsl:apply-templates/>
       </table>
   </xsl:template>

   <xsl:template match="row">
       <tr><td><xsl:apply-templates select="label"/></td><td><xsl:apply-templates select="i:*"/></td></tr>
   </xsl:template>

   <xsl:template match="i:output">
      [<xsl:value-of select="i:value/text()"/>]
   </xsl:template>

   <xsl:template match="i:textbox">
      <input name="{@ref}" type="textbox" value="{i:value/text()}" />
   </xsl:template>

   <xsl:template match="i:password">
      <input name="{@ref}" type="password" value="{i:value/text()}" />
   </xsl:template>

   <xsl:template match="i:selectBoolean">
      <input name="{@ref}" type="checkbox" value="true">
        <xsl:if test="i:value/text() = 'true'">
          <xsl:attribute name="checked"/>
        </xsl:if>
      </input>
   </xsl:template>

   <xsl:template match="i:selectOne">
     <select name="{@ref}">
       <xsl:variable name="selected" select="i:value/text()"/>
       <xsl:for-each select="constraint[@type = 'choice']/choice">
         <option value="{@value}">
           <xsl:if test="$selected = @value">
             <xsl:attribute name="selected"/>
           </xsl:if>
           <xsl:value-of select="."/>
         </option>
       </xsl:for-each>
     </select>
   </xsl:template>

   <xsl:template match="i:selectMany">
   </xsl:template>

   <xsl:template match="i:button">
      <input name="cocoon-method-{@method}" type="submit" value="{i:caption/text()}" />
   </xsl:template>

   <xsl:template match="*">
      <xsl:copy><xsl:copy-of select="@*" /><xsl:apply-templates /></xsl:copy>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:value-of select="." />
   </xsl:template>
</xsl:stylesheet>

