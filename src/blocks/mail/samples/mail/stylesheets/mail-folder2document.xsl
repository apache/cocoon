<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:page="http://apache.org/cocoon/paginate/1.0"
  xmlns:mail="http://apache.org/cocoon/mail/1.0"
  exclude-result-prefixes="xsl page"
>

<xsl:template match="/">
<!--xsl:copy-of select="."/-->
  <document>
    <header>
      <title>Folder</title>
    </header>
    <body>
      <xsl:apply-templates/>
    </body>
  </document>
</xsl:template>

<xsl:template match="mail:folder">
    <xsl:attribute name="title">Folder - <xsl:value-of select="@name"/></xsl:attribute>
    
    <xsl:variable name="is-directory" select="@is-directory"/>
    <xsl:variable name="holds-messages" select="@holds-messages"/>
    <div class="list" style="width: 90%;">
      <div class="row">
        <span style="color: #887788; font-size:150%" class="left"> 
        Name <xsl:value-of select="@full-name"/>
        <xsl:text> </xsl:text>
        <xsl:if test="$is-directory = 'yes'">
            <a>
              <xsl:attribute name="href">mail.html?cmd=list-folder&amp;folder=<xsl:value-of select="@full-name"/></xsl:attribute>
              folders
            </a>
        </xsl:if>
        <xsl:text> </xsl:text>
        <xsl:if test="$holds-messages = 'yes'">
            <xsl:if test="@has-new-messages = 'yes'">
              has new messages
              <xsl:text> </xsl:text>
            </xsl:if>
            <a>
              <xsl:attribute name="href">mail.html?cmd=list-folder-messages&amp;folder=<xsl:value-of select="@full-name"/></xsl:attribute>
              messages
            </a>
        </xsl:if>

        </span>
      </div>
      <div class="row">
        <span class="left">Total/New/Deleted/Unread Messages </span> 
        <span class="right">
          <xsl:value-of select="@total-messages"/> /
          <xsl:value-of select="@new-messages"/> / 
          <xsl:value-of select="@deleted-messages"/> /
          <xsl:value-of select="@unread-messages"/> 
        </span>
      </div>
      <div class="spacer"/>
    </div>
</xsl:template>

<xsl:template match="@*|*|text()" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*|*|text()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>

