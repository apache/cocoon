<?xml version="1.0"?>

<!--+
    | Covert samples file to the HTML page. Uses styles/main.css stylesheet.
    |
    | Author: Nicola Ken Barozzi "nicolaken@apache.org"
    | Author: Vadim Gritsenko "vgritsenko@apache.org"
    | Author: Christian Haul "haul@apache.org"
    | CVS $Id: simple-samples2html.xsl,v 1.9 2003/12/12 14:59:02 vgritsenko Exp $
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:param name="contextPath"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Apache Cocoon @version@</title>
        <link rel="SHORTCUT ICON" href="favicon.ico"/>
        <link href="{$contextPath}/styles/main.css" type="text/css" rel="stylesheet"/>
      </head>
      <body>
       <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
         <tr>
           <td width="*">The Apache Software Foundation is proud to present...</td>
           <td width="40%" align="center"><img border="0" src="{$contextPath}/images/cocoon.gif"/></td>
           <td width="30%" align="center">Version: <b>@version@</b></td>
         </tr>
       </table>

       <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
         <tr>
           <td width="75%">
             <h2><xsl:value-of select="samples/@name"/></h2>
           </td>
           <td nowrap="nowrap" align="right">
             Orthogonal views:
             <a href="?cocoon-view=content">Content</a>
             &#160;
             <a href="?cocoon-view=pretty-content">Pretty content</a>
             &#160;
             <a href="?cocoon-view=links">Links</a>
           </td>
         </tr>
       </table>

       <xsl:apply-templates select="samples"/>

       <p class="copyright">
         Copyright &#169; @year@ <a href="http://www.apache.org/">The Apache Software Foundation</a>.
         All rights reserved.
       </p>
      </body>
    </html>
  </xsl:template>


  <xsl:template match="samples">
    <xsl:variable name="gc" select="4"/><!-- group correction -->
    <xsl:variable name="all-groups" select="$gc * count(group)"/>
    <xsl:variable name="all-samples" select="count(group/sample)+count(group/note)+$all-groups"/>
    <xsl:variable name="half-samples" select="round($all-samples div 2)"/>
    <xsl:variable name="half-possibilities">
      <xsl:choose>
        <xsl:when test="count(group) = 1">1 </xsl:when><!-- single group sample.xml -->
        <xsl:otherwise>
          <xsl:for-each select="group">
            <xsl:if test="position() &lt; last() and position() &gt;= 1">
              <xsl:variable name="group-position" select="position()"/>
              <xsl:variable name="prev-sample" select="count(../group[position() &lt;= $group-position - 1]/sample) + count(../group[position() &lt;= $group-position - 1]/note) + position() * $gc - $gc"/>
              <xsl:variable name="curr-sample" select="count(../group[position() &lt;= $group-position]/sample) + count(../group[position() &lt;= $group-position]/note) + position() * $gc"/>
              <xsl:variable name="next-sample" select="count(../group[position() &lt;= $group-position + 1]/sample) + count(../group[position() &lt;= $group-position + 1]/note) + position() * $gc + $gc"/>
              <xsl:variable name="prev-deviation">
                <xsl:choose>
                  <xsl:when test="$prev-sample &gt; $half-samples">
                    <xsl:value-of select="$prev-sample - $half-samples"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$half-samples - $prev-sample"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="curr-deviation">
                <xsl:choose>
                  <xsl:when test="$curr-sample &gt; $half-samples">
                    <xsl:value-of select="$curr-sample - $half-samples"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$half-samples - $curr-sample"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="next-deviation">
                <xsl:choose>
                  <xsl:when test="$next-sample &gt; $half-samples">
                    <xsl:value-of select="$next-sample - $half-samples"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$half-samples - $next-sample"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:if test="$prev-deviation &gt;= $curr-deviation and $curr-deviation &lt;= $next-deviation">
                <xsl:value-of select="$group-position"/><xsl:text> </xsl:text>
              </xsl:if>
            </xsl:if>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="half">
      <xsl:value-of select="substring-before($half-possibilities, ' ')"/>
    </xsl:variable>

    <table width="100%" cellspacing="5">
      <tr>
        <td width="50%" valign="top">
          <xsl:for-each select="group">
            <xsl:variable name="group-position" select="position()"/>
            <xsl:choose>
              <xsl:when test="$group-position &lt;= $half">
                <h4 class="samplesGroup"><xsl:value-of select="@name"/></h4>
                <p class="samplesText"><xsl:apply-templates/></p>
              </xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </td>
        <td valign="top">
          <xsl:for-each select="group">  <!-- [position()<=$half] -->
            <xsl:variable name="group-position" select="position()"/>
            <xsl:choose>
              <xsl:when test="$group-position &gt; $half">
                <h4 class="samplesGroup"><xsl:value-of select="@name"/></h4>
                <p class="samplesText"><xsl:apply-templates/></p>
              </xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </td>
      </tr>
    </table>
  </xsl:template>


  <xsl:template match="sample">
    <xsl:variable name="link">
      <xsl:choose>
        <xsl:when test="starts-with(@href,'/')">
          <xsl:value-of select="concat($contextPath, @href)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@href"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <a href="{$link}"><xsl:value-of select="@name"/></a><xsl:text> - </xsl:text>
    <xsl:value-of select="."/>
    <br/>
  </xsl:template>


  <xsl:template match="note">
    <p class="samplesNote">
      <xsl:apply-templates/>
    </p>
  </xsl:template>


  <xsl:template match="@*|node()" priority="-2">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()" priority="-1">
    <xsl:value-of select="."/>
  </xsl:template>
</xsl:stylesheet>
