<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:page="http://apache.org/cocoon/paginate/1.0"
  xmlns:mail="http://apache.org/cocoon/mail/1.0"
  exclude-result-prefixes="xsl page mail"
>

<xsl:template match="document">
  <document>
    <body>
      <xsl:apply-templates select="body/*"/>
    </body>
  </document>
</xsl:template>

<xsl:template match="body">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="mail:mail">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="mail:folder">
  <div class="list" style="width: 90%;">
    <div class="row">
      <span class="left">Name</span> <span class="right"> <xsl:value-of select="@name"/> </span>
    </div>
    <div class="row">
      <span class="left">Total Messages</span> <span class="right"> <xsl:value-of select="@total-messages"/> </span>
    </div>
    <div class="row">
      <span class="left">New Messages</span> <span class="right"> <xsl:value-of select="@new-messages"/> </span>
    </div>
    <div class="row">
      <span class="left">Deleted Messages</span> <span class="right"> <xsl:value-of select="@deleted-messages"/> </span>
    </div>
    <div class="row">
      <span class="left">Unread Message</span> <span class="right"> <xsl:value-of select="@unread-messages"/></span>
    </div>
    <div class="spacer"/>
    <ul style="padding: 5px; ">
      <li>Name <xsl:value-of select="@name"/></li>
      <li>Fullname <xsl:value-of select="@full-name"/></li>
      <li>URLname <xsl:value-of select="@url-name"/></li>
    </ul>
    <div class="spacer"/>
  </div>
</xsl:template>

<xsl:template match="@*|*|text()" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*|*|text()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>

