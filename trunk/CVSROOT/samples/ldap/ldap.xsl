<?xml version="1.0"?>

<xsl:stylesheet xsl:version="1.0"  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:template match="page">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>
    <head>
     <title>
      LDAP Search Results
     </title>
    </head>
    <body bgcolor="#ffffff">
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

 <xsl:template match="searchresult">
	<table cellspacing="0" cellpadding="6">
     <xsl:apply-templates select="cn"/>
     <xsl:apply-templates select="currentorganization"/>
     <xsl:apply-templates select="mail"/>
     <xsl:apply-templates select="telephonenumber"/>
     <xsl:apply-templates select="securephonenumber"/>
	</table>
	<p/>
  </xsl:template>

 <xsl:template match="cn">
	<tr>
	 <th bgcolor="tan" align="right">Common Name</th>
	 <td>
      <xsl:apply-templates/>
	 </td>
	</tr>
  </xsl:template>

 <xsl:template match="currentorganization">
	<tr>
	 <th bgcolor="tan" align="right">Org</th>
	 <td>
      <xsl:apply-templates/>
	 </td>
	</tr>
  </xsl:template>

 <xsl:template match="telephonenumber">
	<tr>
	 <th bgcolor="tan" align="right">Unsecure #</th>
	 <td>
      <xsl:apply-templates/>
	 </td>
	</tr>
  </xsl:template>

 <xsl:template match="securephonenumber">
	<tr>
	 <th bgcolor="tan" align="right">Secure #</th>
	 <td>
      <xsl:apply-templates/>
	 </td>
	</tr>
  </xsl:template>

 <xsl:template match="mail">
	<tr>
	 <th bgcolor="tan" align="right">E-mail</th>
	 <td>
      <a>
	   <xsl:attribute name="href">
        mailto:<xsl:apply-templates/>
	   </xsl:attribute>
       <xsl:apply-templates/>
	  </a>
	 </td>
	</tr>
  </xsl:template>

</xsl:stylesheet>

