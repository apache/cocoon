<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:page="http://apache.org/cocoon/paginate/1.0"
  xmlns:mail="http://apache.org/cocoon/mail/1.0"
  exclude-result-prefixes="xsl page mail"
>

<xsl:template match="/">
  <!--xsl:copy-of select="."/-->
  
  <document>
    <header>
      <title>Folder</title>
    </header>
    <body>
      <s1 title="Folder">
        <table style="margin-left: auto; margin-right: auto;
          width: 95%;
          border-collapse: collapse;
          border-top: 1px dotted;
          border-bottom: 1px dotted;
          " 
          class="list">
          <tr>
            <th align="left">#</th>
            <th align="left">From</th>
            <th align="left">Subject</th>
            <th align="left">Sent</th>
            <th align="left">Size</th>
          </tr>
          <xsl:apply-templates/>
        </table>
      </s1>
    </body>
  </document>
</xsl:template>

<xsl:template match="mail:message-envelope">
    <tr >
      <td  style="border-top: 1px dotted; " >
        <link>
          <xsl:attribute name="href">mail.html?cmd=cat-message-by-id&amp;id=<xsl:value-of select="mail:message-number"/></xsl:attribute>
          <xsl:value-of select="mail:message-number"/>
        </link>
      </td>
      <td style="border-top: 1px dotted; " ><xsl:value-of select="mail:from/@personal"/> &lt;<xsl:value-of select="mail:from/@email-address"/>&gt; </td>
      <td style="border-top: 1px dotted; " ><xsl:value-of select="mail:subject"/> </td>
      <td style="border-top: 1px dotted; " ><xsl:value-of select="mail:sent-date"/> </td>
      <td style="border-top: 1px dotted; " ><xsl:value-of select="mail:size"/> </td>
    </tr>  
</xsl:template>

</xsl:stylesheet>

