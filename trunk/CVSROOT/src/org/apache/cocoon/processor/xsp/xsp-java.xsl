<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://www.apache.org/1999/XSP/Layer1"
>

  <xsl:template match="xsp:page">
    <code>
      package <xsl:value-of select="@package"/>;
  
      import java.io.*;
      import java.util.*;
      import org.w3c.dom.*;
      import javax.servlet.*;
      import javax.servlet.http.*;
  
      import org.apache.cocoon.parser.*;
      import org.apache.cocoon.producer.*;
      import org.apache.cocoon.framework.*;
      import org.apache.cocoon.processor.xsp.*;
  
      /* User Imports */
      <xsl:for-each select="xsp:structure/xsp:include">
        import <xsl:value-of select="."/>;
      </xsl:for-each>
  
      public class <xsl:value-of select="@name"/> extends XSPPage {
        /* User Class Declarations */
        <xsl:apply-templates select="xsp:logic" />
  
        public void populateDocument(
          HttpServletRequest request,
          HttpServletResponse response,
          Document document
        )
          throws Exception
        {
          Node xspParentNode = null;
          Node xspCurrentNode = document;
          Stack xspNodeStack = new Stack();
          HttpSession session = request.getSession(true);
  
          <xsl:for-each select="/processing-instruction()[not(contains(.,'xsp'))]">
            document.appendChild(
              document.createProcessingInstruction(
                "<xsl:value-of select="name()"/>",
                "<xsl:value-of select="."/>"
              )
            );
          </xsl:for-each>
  
          <xsl:apply-templates select="*[not(starts-with(name(.), 'xsp:'))]"/>
        }
      }
    </code>
  </xsl:template>

  <xsl:template match="*">
    xspParentNode = xspCurrentNode;
    xspNodeStack.push(xspParentNode);
    xspCurrentNode =
      document.createElement("<xsl:value-of select="name(.)"/>");
    xspParentNode.appendChild(xspCurrentNode);
    
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
    
    ((Element) xspCurrentNode).normalize();
    xspCurrentNode = (Node) xspNodeStack.pop();
  </xsl:template>

  <xsl:template match="xsp:element">
    xspParentNode = xspCurrentNode;
    xspNodeStack.push(xspParentNode);
    xspCurrentNode =
      document.createElement("<xsl:value-of select="@name"/>");
    xspParentNode.appendChild(xspCurrentNode);
    
    <xsl:apply-templates/>
    
    ((Element) xspCurrentNode).normalize();
    xspCurrentNode = (Node) xspNodeStack.pop();
  </xsl:template>

  <xsl:template match="@*">
    ((Element) xspCurrentNode).setAttribute(
      "<xsl:value-of select="name(.)"/>",
      "<xsl:value-of select="."/>"
    );
  </xsl:template>

  <xsl:template match="xsp:attribute">
    ((Element) xspCurrentNode).setAttribute(
      "<xsl:value-of select="@name"/>",
      <xsl:for-each select="xsp:text|xsp:expr">
        <xsl:choose>
	  <xsl:when test="name(.) = 'xsp:text'">
	    "<xsl:value-of select="."/>"
	  </xsl:when>
	  <xsl:when test="name(.) = 'xsp:expr'">
	    String.valueOf(<xsl:value-of select="."/>)
	  </xsl:when>
	</xsl:choose>
	+
      </xsl:for-each>
      ""
    );
  </xsl:template>

  <xsl:template match="xsp:expr">
    xspCurrentNode.appendChild(
      xspExpr(<xsl:value-of select="."/>, document)
    );
  </xsl:template>


  <xsl:template match="xsp:logic">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xsp:text">
    xspCurrentNode.appendChild(
      document.createTextNode("<xsl:value-of select="."/>")
    );
  </xsl:template>

  <xsl:template match="xsp:pi">
    xspCurrentNode.appendChild(
      document.createProcessingInstruction(
        "<xsl:value-of select="@target"/>",
        "<xsl:value-of select="."/>"
      )
    );
  </xsl:template>

  <xsl:template match="xsp:comment">
    xspCurrentNode.appendChild(
      document.createTextNode("<xsl:value-of select="."/>")
    );
  </xsl:template>

</xsl:stylesheet>
