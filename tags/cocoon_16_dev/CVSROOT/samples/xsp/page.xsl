<?xml version="1.0"?>

<!-- Author: Stefano Mazzocchi "stefano@apache.org" -->
<!-- Version: $Id: page.xsl,v 1.1.1.1 1999-11-09 01:51:35 stefano Exp $ -->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://www.apache.org/DTD/XSP/Layer1">

  <!-- This template process the root element -->
  <xsl:template match="page">
   <xsp:page
     xmlns:xsp="http://www.apache.org/DTD/XSP/Layer1"
     language="java">

    <xsl:pi name="cocoon-format">type="text/xsp"</xsl:pi>
   
    <xsp:logic xml:space="preserve"><![CDATA[
     static private int counter = 0;

     private synchronized int count() {
      return counter++;
     }
    ]]></xsp:logic>

    <xsp:content>
     <xsl:copy>
      <xsl:apply-templates/>
     </xsl:copy>
    </xsp:content>
   </xsp:page>
  </xsl:template>

  <!-- This template copies the elements without processing -->
  <xsl:template match="p|title|author|name|address|version">
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <!-- This template process the request data expanding the list -->
  <!-- of variables into XSP logic indication                    -->
  <xsl:template match="request-data">
   <list>
    <title><xsl:value-of select="@title"/></title>
    <xsl:for-each select="data">
     <element name="{@name}">
      <xsp:expr>request.get<xsl:value-of select="@value"/>()</xsp:expr>
     </element>
    </xsl:for-each>
   </list>
  </xsl:template>

  <!-- This template process the request headers -->
  <xsl:template match="request-headers">
   <xsp:logic xml:space="preserve"><![CDATA[
    Enumeration e = request.getHeaderNames();
    if ((e != null) && (e.hasMoreElements())) ]]>
     <xsp:content>
      <list>
       <title><xsl:value-of select="@title"/></title>
       <xsp:logic xml:space="preserve"><![CDATA[
        while (e.hasMoreElements()) {
         String k = (String) e.nextElement(); ]]>
         <xsp:element name="element">
          <xsp:attribute name="name">
           <xsp:expr>k</xsp:expr>
          </xsp:attribute>
          <xsp:expr>request.getHeader(k)</xsp:expr>
         </xsp:element>
        }
       </xsp:logic>
      </list>
     </xsp:content>
    }
   </xsp:logic>
  </xsl:template>

  <!-- This template process the servlet parameters -->
  <xsl:template match="servlet-parameters">
   <xsp:logic xml:space="preserve"><![CDATA[
    e = request.getParameterNames();
    if ((e != null) && (e.hasMoreElements())) { ]]>
     <xsl:content>
      <list>
       <title><xsl:value-of select="@title"/></title>
       <xsp:logic xml:space="preserve">
        while (e.hasMoreElements()) {
         String k = (String) e.nextElement();
         String val = request.getParameter(k);
         String vals[] = request.getParameterValues(k);
         <xsp:element name="element">
          <xsp:attribute name="name">
           <xsp:expr>k</xsp:expr>
          </xsp:attribute>
          <xsp:logic xml:space="preserve"><![CDATA[
           for(int i = 0; i < vals.length; i++) { ]]>
            <item><xsp:expr>vals[i]</xsp:expr></item>
           }
          </xsp:logic>
         </xsp:element>
        }
       </xsp:logic>
      </list>
     </xsl:content>
    }
   </xsp:logic>
  </xsl:template>

</xsl:stylesheet>