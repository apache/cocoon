<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
  
    <xsl:processing-instruction name="cocoon-process">type="xsp"</xsl:processing-instruction>

    <xsl:processing-instruction name="cocoon-process">type="xslt"</xsl:processing-instruction>
    <xsl:processing-instruction name="xml-stylesheet">href="page-html.xsl" type="text/xsl"</xsl:processing-instruction>

    <xsp:page language="java" xmlns:xsp="http://apache.org/DTD/XSP/Layer1">
 
    <xsp:logic>
      static private int counter = 0; 
      
      private synchronized int count() { 
        return counter++; 
      }
      
      private String normalize(String string) {
        if (string == null) return "";
        else return string;
      }
    </xsp:logic>  
  
    <xsl:copy>
     <xsl:apply-templates/>
    </xsl:copy>
   
   </xsp:page>

  </xsl:template>

  <xsl:template match="title|author|link">
   <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="p">
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <xsl:template match="request-data">
   <list title="Request Data">
    <xsl:apply-templates/>
   </list>
  </xsl:template>

  <xsl:template match="count">
   <xsp:expr>count()</xsp:expr>
  </xsl:template>

  <xsl:template match="string">
   <element name="{@name}">
    <xsp:expr>normalize(request.get<xsl:value-of select="@method"/>())</xsp:expr>
   </element>
  </xsl:template>

  <xsl:template match="int">
   <element name="{@name}">
    <xsp:expr>request.get<xsl:value-of select="@method"/>()</xsp:expr>
   </element>
  </xsl:template>

  <xsl:template match="request-headers">
       <xsp:logic><![CDATA[
         Enumeration e = request.getHeaderNames(); 
         if ((e != null) && (e.hasMoreElements())) { ]]>
            <p>Here are the request headers...</p>
            <list title="Request Headers">
                <xsp:logic><![CDATA[
                  while (e.hasMoreElements()) {  
                    String k = (String) e.nextElement(); ]]>
                    <element>
                        <xsp:attribute name="name">
                            <xsp:expr>k</xsp:expr>
                        </xsp:attribute>
                        <xsp:expr>request.getHeader(k)</xsp:expr>
                    </element>
                  }
                </xsp:logic>
            </list>
          }
        </xsp:logic>
  </xsl:template>
 
  <xsl:template match="servlet-parameters">
   <xsp:logic><![CDATA[
          e = request.getParameterNames(); 
          if ((e != null) && (e.hasMoreElements())) { ]]>
            <p>and here the servlet parameters that were passed along
            with the request...</p>
            <list title="Servlet Parameters">
                <xsp:logic><![CDATA[
                  while (e.hasMoreElements()) { 
                    String k = (String) e.nextElement();
                    String val = request.getParameter(k); 
                    String vals[] = request.getParameterValues(k); ]]>
                    <element>
                        <xsp:attribute name="name">
                            <xsp:expr>k</xsp:expr>
                        </xsp:attribute>
                        <xsp:logic><![CDATA[
                            for(int i = 0; i < vals.length; i++) { ]]>
                                <item>
                                    <xsp:expr>vals[i]</xsp:expr>
                                </item>
                            }
                        </xsp:logic>
                    </element>
                  }
                </xsp:logic>
            </list>
          } 
        </xsp:logic>      
  </xsl:template>
 
</xsl:stylesheet>