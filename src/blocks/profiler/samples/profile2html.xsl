<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:profile="http://apache.org/cocoon/profiler/1.0">

  <xsl:param name="sort"/>

  <xsl:template match="profile:profilerinfo">
    <html>
      <head>
        <title>Cocoon2 profile information [<xsl:value-of select="@profile:date"/>]</title>
      </head>
      <body>
        Sort results by <a href="?sort=uri">uri</a>,
        <a href="?sort=count">count</a>, <a href="?sort=time">time</a>.

        <table noshade="noshade" border="0" cellspacing="1" cellpadding="0" width="100%">
          <xsl:choose>
            <xsl:when test="$sort = 'uri'">
              <xsl:apply-templates select="profile:pipeline">
                 <xsl:sort select="@profile:uri"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sort = 'time'">
              <xsl:apply-templates select="profile:pipeline">
                 <xsl:sort select="@profile:time" data-type="number"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$sort = 'count'">
              <xsl:apply-templates select="profile:pipeline">
                 <xsl:sort select="@profile:count" data-type="number"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates/>
            </xsl:otherwise>
          </xsl:choose>
        </table>

        <xsl:apply-templates select="profile:pipeline/profile:result/profile:environmentinfo"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="profile:pipeline">
    <xsl:if test="position() mod 5 = 1">
      <tr bgcolor="#FFC0C0">
       <th>NN</th>
       <th>Role (Source)</th>
       <th>Average</th>
       <th colspan="10">Last Results</th>
      </tr>
    </xsl:if>
    <tr bgcolor="#C0C0FF">
     <td colspan="3">
      <a href="?key={@profile:key}">
       <font face="verdana"><strong><xsl:value-of select="@profile:uri"/></strong></font>
       (<xsl:value-of select="@profile:count"/> results,
       total time: <xsl:value-of select="@profile:time"/>,
       average time: <xsl:value-of select="@profile:time div @profile:count"/>)
      </a>
     </td>
     <xsl:for-each select="profile:result">
      <td>
       <a href="?key={../@profile:key}&amp;index={@profile:index}">
        <xsl:value-of select="@profile:index"/>
       </a>
      </td>
     </xsl:for-each>
    </tr>

    <xsl:for-each select="profile:average/profile:component">
      <xsl:variable name="bgcolor">
       <xsl:if test="position() mod 2 = 0">#D0D0D0</xsl:if>
       <xsl:if test="position() mod 2 = 1">#E0E0E0</xsl:if>
      </xsl:variable>
      <tr bgcolor="{$bgcolor}">

       <xsl:variable name="pos" select="position()"/>
       <td width="1%">
        <xsl:value-of select="$pos"/>
       </td>
       <td width="10%">
        <xsl:value-of select="@profile:role"/>
        <xsl:if test="@profile:source">
          (<xsl:value-of select="@profile:source"/>)
        </xsl:if>
       </td>

       <xsl:for-each select="../../profile:average/profile:component[position()=$pos]">
        <th>
         <xsl:value-of select="@profile:time"/>
        </th>
       </xsl:for-each>

       <xsl:for-each select="../../profile:result/profile:component[position()=$pos]">
        <td>
         <a href="?key={../../@profile:key}&amp;index={../@profile:index}&amp;offset={@profile:offset}">
          <xsl:value-of select="@profile:time"/>
         </a>
        </td>
       </xsl:for-each>

      </tr>
    </xsl:for-each>

       <xsl:variable name="pos" select="count(profile:average/profile:component)"/>
      <tr>
       <td width="1%">
        <xsl:value-of select="$pos+1"/>
       </td>
       <td width="10%">
        TOTAL
       </td>

        <th>
         <xsl:value-of select="profile:average/@profile:time"/>
        </th>

       <xsl:for-each select="profile:result">
        <td>
         <xsl:value-of select="@profile:time"/>
        </td>
       </xsl:for-each>

      </tr>
  </xsl:template>

  <xsl:template match="profile:average|profile:result">
   <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="profile:component">
   <table cellspacing="0" cellpadding="0">
    <tr>
     <td>
      <xsl:value-of select="@profile:role"/>
     </td>
    </tr>
   </table>
  </xsl:template>

  <xsl:template match="profile:environmentinfo">
   <xsl:apply-templates select="profile:request-parameters"/>
   <xsl:apply-templates select="profile:session-attributes"/>
  </xsl:template>

  <xsl:template match="profile:request-parameters">
    <table>
      <tr bgcolor="#C0C0FF">
       <th colspan="14">
        Request parameters
       </th>
      </tr>
      <tr bgcolor="#FFC0C0">
        <th>Name</th>
        <th>Value</th>
      </tr>
      <xsl:for-each select="profile:parameter">
        <xsl:variable name="bgcolor">
          <xsl:if test="position() mod 2 = 0">#D0D0D0</xsl:if>
          <xsl:if test="position() mod 2 = 1">#E0E0E0</xsl:if>
        </xsl:variable>

        <tr bgcolor="{$bgcolor}">
          <td><xsl:value-of select="@profile:name"/></td>
          <td><xsl:value-of select="@profile:value"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="profile:session-attributes">
    <table>
      <tr bgcolor="#C0C0FF">
       <th colspan="14">
        Session attributes
       </th>
      </tr>
      <tr bgcolor="#FFC0C0">
        <th>Name</th>
        <th>Value</th>
      </tr>
      <xsl:for-each select="profile:attribute">
        <xsl:variable name="bgcolor">
          <xsl:if test="position() mod 2 = 0">#D0D0D0</xsl:if>
          <xsl:if test="position() mod 2 = 1">#E0E0E0</xsl:if>
        </xsl:variable>

        <tr bgcolor="{$bgcolor}">
          <td><xsl:value-of select="@profile:name"/></td>
          <td><xsl:value-of select="@profile:value"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

</xsl:stylesheet>
