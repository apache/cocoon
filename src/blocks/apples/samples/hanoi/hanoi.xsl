<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:hn="http://apache.org/cocoon/apples/samples/hanoi">




  <xsl:template match="hn:hanoi">

    <xsl:variable name="stacks" select="hn:stacks"/> 
    <xsl:variable name="height" select="@height"/>
    <xsl:variable name="cols-per-stack" select="($height)*2 + 1"/>
    <xsl:variable name="cols-total" select="($cols-per-stack)*3"/>
    <xsl:variable name="on-the-move" select="boolean(hn:float/@size != '')"/>
    
    <center>
      <table border="0">
        <!-- fixing col-widths row -->
        <tr>
      <xsl:call-template name="fix-cells" >
        <xsl:with-param name="remaining" select="$cols-total" />
      </xsl:call-template>
        </tr>
        <!-- /fixing col-widths row -->
        
        <!-- title-row -->
        <tr>
          <td colspan="{$cols-total}">
            <center>-- Towers of Hanoi Puzzle --</center>            
            <hr width="70%" />
          </td>
        </tr>
        <!-- /title-row -->
        
        <!-- moving-disc-row -->
        <tr>
          <td colspan="{$cols-total}"><center>Disc on the move:</center></td>
        </tr>
        <tr bgcolor="#ffffff" height="5px">
          <td colspan="{$cols-per-stack}"/>          
          <xsl:call-template name="disc">
            <xsl:with-param name="disc" select="hn:float" />
            <xsl:with-param name="max_size" select="$height"/>
            <xsl:with-param name="disc_color">#996633</xsl:with-param>
          </xsl:call-template>
          <td colspan="{$cols-per-stack}"/>
        </tr>

        <tr>
          <td colspan="{$cols-total}"><hr width="70%"/></td>
        </tr>        
    <!-- /moving-disc-row -->
    
    <!-- stack-header-row -->
      <tr>
        <xsl:choose>
          <xsl:when test="$on-the-move" >
          <td colspan="{$cols-per-stack}"><center><a href="?stack=0">Drop It!</a></center></td>
          <td colspan="{$cols-per-stack}"><center><a href="?stack=1">Drop It!</a></center></td>
          <td colspan="{$cols-per-stack}"><center><a href="?stack=2">Drop It!</a></center></td>
          </xsl:when>
          <xsl:otherwise>
          <td colspan="{$cols-per-stack}"><center><a href="?stack=0">Lift It!</a></center></td>
          <td colspan="{$cols-per-stack}"><center><a href="?stack=1">Lift It!</a></center></td>
          <td colspan="{$cols-per-stack}"><center><a href="?stack=2">Lift It!</a></center></td>
          </xsl:otherwise>
        </xsl:choose>
      </tr>
    <!-- /stack-header-row -->
  
    
        <!-- stack-rows -->
        <xsl:call-template name="remaining-stack-rows">
          <xsl:with-param name="remaining" select="$height"/>
          <xsl:with-param name="max_size" select="$height"/>
          <xsl:with-param name="cols-per-stack" select="$cols-per-stack"/>
          <xsl:with-param name="stacks" select="$stacks"/>
        </xsl:call-template>        
        <!-- /stack-rows -->
                        
      </table>
    </center>
  </xsl:template>


  <xsl:template name="remaining-stack-rows">
    <xsl:param name="remaining"/>
    <xsl:param name="max_size"/>
    <xsl:param name="cols-per-stack"/>
    <xsl:param name="stacks"/>

    <xsl:call-template name="stack-row">
      <xsl:with-param name="row-number" select="$remaining"/>
      <xsl:with-param name="max_size" select="$max_size"/>
      <xsl:with-param name="cols-per-stack" select="$cols-per-stack"/>
      <xsl:with-param name="stacks" select="$stacks"/>
    </xsl:call-template>

    <xsl:if test="$remaining &gt; 1">
      <xsl:call-template name="remaining-stack-rows">
        <xsl:with-param name="remaining" select="$remaining - 1"/>
        <xsl:with-param name="max_size" select="$max_size"/>
        <xsl:with-param name="cols-per-stack" select="$cols-per-stack"/>
        <xsl:with-param name="stacks" select="$stacks"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="stack-row">
    <xsl:param name="row-number"/>
    <xsl:param name="max_size"/>
    <xsl:param name="cols-per-stack"/>
    <xsl:param name="stacks"/>

  <tr height="5px" bgcolor="#ffffff">  
      <xsl:for-each select="$stacks/hn:stack">
        <xsl:variable name="stack-ndx" select="position()"/>

        <xsl:call-template name="disc">
          <xsl:with-param name="disc" select="hn:disc[number($row-number)]" />
          <xsl:with-param name="max_size" select="$max_size"/>
          <xsl:with-param name="disc_color">#2C6D91</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </tr>
  </xsl:template>



  <xsl:template name="disc">
    <xsl:param name="disc" />
    <xsl:param name="max_size" />
    <xsl:param name="disc_color" />
    
    <xsl:variable name="size" select="$disc/@size" />

  <xsl:choose>
      <xsl:when test="$size &gt; 0" >
        <xsl:choose>
          <xsl:when test="($max_size - $size) &gt; 0" >
            <td colspan="{$max_size - $size}" bgcolor="#ffffff"/>
            <td colspan="{$size}"             bgcolor="{$disc_color}"/>
            <td                               bgcolor="#000000"/>
            <td colspan="{$size}"             bgcolor="{$disc_color}"/>
            <td colspan="{$max_size - $size}" bgcolor="#ffffff"/>
          </xsl:when>
          <xsl:otherwise>
            <td colspan="{$size}"             bgcolor="{$disc_color}"/>
            <td                               bgcolor="#000000"/>
            <td colspan="{$size}"             bgcolor="{$disc_color}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    <xsl:otherwise>
      <td colspan="{$max_size}" bgcolor="#ffffff"/>
      <td                       bgcolor="#000000"/>
      <td colspan="{$max_size}" bgcolor="#ffffff"/>
    </xsl:otherwise>
  </xsl:choose>    

  </xsl:template>


  <xsl:template name="fix-cells">
    <xsl:param name="remaining"/>

  <td width="5px" />

    <xsl:if test="$remaining &gt; 1">
      <xsl:call-template name="fix-cells">
        <xsl:with-param name="remaining" select="$remaining - 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>
