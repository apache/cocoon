<?xml version="1.0"?>

<!-- CVS: $Id: pagedlist2html.xsl,v 1.1 2003/06/27 20:10:41 stefano Exp $ -->

<!DOCTYPE xsl:stylesheet [
 <!ENTITY laquo "&#xAB;" >
 <!ENTITY raquo "&#xBB;" >
 <!ENTITY nbsp "&#160;" >
]>

<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:page="http://apache.org/cocoon/paginate/1.0"
>

  <xsl:template match="/">
   <html>
    <head>
     <title>Paged List</title>
    </head>
    <body bgcolor="white" alink="red" link="blue" vlink="blue">
     <h3>Paged List</h3>
     <xsl:apply-templates/>
     <a href="{//page:page/@clean-uri}">single page version</a>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="list">
   <ul>
    <xsl:apply-templates/>
   </ul>
  </xsl:template>

  <xsl:template match="item">
   <li><xsl:apply-templates/></li>
  </xsl:template>

  <xsl:template match="page:page">
   <xsl:if test="@total &gt; 1">

      <!-- page navigation table -->
      <table border="0">
       <tr>

        <!-- td prev -->
        <td align="right">&nbsp;
         <xsl:if test="page:link[@type='prev']">
          <xsl:variable name="previous" select="@current - 1"/>
           <a href="{page:link[@page = $previous]/@uri}">&laquo; prev</a>
         </xsl:if>
        </td>

        <!-- td current -->
        <td align="center">
          [page <xsl:value-of select="@current"/> of <xsl:value-of select="@total"/>]
        </td>

        <!-- td next -->
        <td align="left">
         <xsl:if test="page:link[@type='next']">
          <xsl:variable name="next" select="@current + 1"/>
          <a href="{page:link[@page = $next]/@uri}">next &raquo;</a>
         </xsl:if>
        &nbsp;</td>

       </tr>
      </table>
   </xsl:if>
  </xsl:template>
</xsl:stylesheet>
