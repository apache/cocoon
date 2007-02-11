<?xml version="1.0"?>

<!-- CVS $Id: page2xls.xsl,v 1.3 2003/10/22 11:42:05 joerg Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:sql="http://apache.org/cocoon/SQL/2.0"
                             xmlns:gmr="http://www.gnome.org/gnumeric/v7" >

  <xsl:template match="page">
   <gmr:Workbook xmlns:gmr="http://www.gnome.org/gnumeric/v7">
     <gmr:Sheets>
         <gmr:Sheet DisplayFormulas="false" HideZero="false" HideGrid="false" HideColHeader="false" HideRowHeader="false" DisplayOutlines="true" OutlineSymbolsBelow="true" OutlineSymbolsRight="true">
	         <gmr:Name><xsl:value-of select="title"/></gmr:Name>
        	 <gmr:MaxCol>2</gmr:MaxCol>
	         <gmr:Cols DefaultSizePts="48">
                     <gmr:ColInfo No="0" Unit="48" MarginA="2" MarginB="2" Count="7"/>
                 </gmr:Cols>
     		 <gmr:Rows DefaultSizePts="12.8">
       			<gmr:RowInfo No="0" Unit="12.8" MarginA="0" MarginB="0" Count="9"/>
       			<gmr:RowInfo No="10" Unit="12.8" MarginA="1" MarginB="0" Count="24"/>
     		 </gmr:Rows>
 		 <gmr:Cells>
     			<xsl:apply-templates/>
                 </gmr:Cells>
     	</gmr:Sheet>
     </gmr:Sheets>
    </gmr:Workbook>
  </xsl:template>

  <xsl:template match="content">
      <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="para">
     <gmr:Cell Col="0" ValueType="60">
      <xsl:variable name="rownumber"><xsl:number level="any" from="content" count="para"/></xsl:variable>
      <xsl:attribute name="Row">
         <xsl:value-of select="$rownumber"/>
      </xsl:attribute>
       <gmr:Content>
		<xsl:apply-templates/> 
	</gmr:Content>
     </gmr:Cell>
  </xsl:template>

 <xsl:template match="title"></xsl:template>

</xsl:stylesheet>
