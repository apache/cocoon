<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>
    <head>
     <title><xsl:value-of select="title"/></title>
    </head>
    <body>
    <p><br/></p>
    <center>
     <table border="0" width="60%" bgcolor="#000000" cellspacing="0" cellpadding="0">
      <tr>
       <td width="100%">
        <table border="0" width="100%" cellpadding="4">
         <tr>
          <td width="100%" bgcolor="#c0c0c0" align="right" valign="middle">
           <big><big><xsl:value-of select="title"/></big></big>
          </td>
         </tr>
         <tr>
          <td width="100%" bgcolor="#ffffff">
           <xsl:apply-templates/>
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
    </center>

    <p align="center">
     <font size="-1">
      Copyright &#169; @year@ <a href="http://xml.apache.org">The Apache XML Project</a>.<br/>
      All rights reserved.
     </font>
    </p>

    </body>
   </html>
  </xsl:template>

  <xsl:template match="title|author">
   <!-- ignore -->
  </xsl:template>

  <xsl:template match="p">
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <xsl:template match="em">
    <strong><xsl:apply-templates/></strong>
  </xsl:template>

  <xsl:template match="link">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="list">
   <center>
    <table border="0" width="90%" bgcolor="#000000" cellspacing="0" cellpadding="0">
     <tr>
      <td width="100%">
       <table border="0" width="100%" cellpadding="4">
        <tr>
         <th bgcolor="#e0e0e0" colspan="2" align="right"><xsl:value-of select="@title"/></th>
        </tr>
        <xsl:apply-templates/>
       </table>
      </td>
     </tr>
    </table>
   </center>
  </xsl:template>

  <xsl:template match="element">
   <tr>
    <td>
     <xsl:choose>
      <xsl:when test="position() mod 2 = 0">
       <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
       <xsl:attribute name="bgcolor">#ffffff</xsl:attribute>
      </xsl:otherwise>
     </xsl:choose>
     <xsl:value-of select="@name"/>
    </td>
    <td width="100%">
     <xsl:choose>
      <xsl:when test="position() mod 2 = 0">
       <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
       <xsl:attribute name="bgcolor">#ffffff</xsl:attribute>
      </xsl:otherwise>
     </xsl:choose>
     <xsl:choose>
      <xsl:when test=".//item">
       <ul>
        <xsl:for-each select=".//item">
         <li>
          <xsl:apply-templates/>
         </li>
        </xsl:for-each>
       </ul>
      </xsl:when>
      <xsl:otherwise>
       <xsl:apply-templates/><xsl:text>&#160;</xsl:text>
      </xsl:otherwise>
     </xsl:choose>
    </td>
   </tr>
  </xsl:template>

</xsl:stylesheet>