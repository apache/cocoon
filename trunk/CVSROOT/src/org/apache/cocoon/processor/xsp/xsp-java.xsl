<?xml version="1.0"?>
<!--

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

-->

<!-- written by Ricardo Rocha "ricardo@apache.org" -->


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://www.apache.org/1999/XSP/Core"
>

  <xsl:output method="text"/>

  <xsl:template match="/">
    <code xml:space="preserve">
      <xsl:apply-templates select="xsp:page" />
    </code>
  </xsl:template>

  <xsl:template match="xsp:page">
    package <xsl:value-of select="@package"/>;

    import java.io.*;
    import java.net.*;
    import java.util.*;
    import org.w3c.dom.*;
    import org.xml.sax.*;
    import javax.servlet.*;
    import javax.servlet.http.*;

    import org.apache.cocoon.parser.*;
    import org.apache.cocoon.producer.*;
    import org.apache.cocoon.framework.*;

    import org.apache.cocoon.processor.xsp.*;
    import org.apache.cocoon.processor.xsp.library.*;

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
	// Node stack logic variables
        Node xspParentNode = null;
        Node xspCurrentNode = document;
        Stack xspNodeStack = new Stack();

	<xsl:variable name="create-session">
	  <xsl:choose>
	    <xsl:when test="@create-session = 'true'">true</xsl:when>
	    <xsl:otherwise>false</xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>

	// Make session object readily available
        HttpSession session = request.getSession(<xsl:value-of select="$create-session"/>);

        <xsl:for-each select="//xsp:variable">
          <xsl:value-of select="@type"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="@name"/>
          <xsl:text> = </xsl:text>
          <xsl:choose>
            <xsl:when test="@value">
              <xsl:value-of select="@value"/>
            </xsl:when>
            <xsl:otherwise>
              null
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text>;
</xsl:text>
        </xsl:for-each>

        <xsl:for-each select="//processing-instruction()[not(starts-with(name(.),'xml-logicsheet') or (starts-with(name(.),'cocoon-process') and contains(.,'xsp')))]">
          document.appendChild(
            document.createProcessingInstruction(
              "<xsl:value-of select="name()"/>",
              "<xsl:value-of select="."/>"
            )
          );
        </xsl:for-each>

	<!-- Method level declarations should go here... -->

        <xsl:apply-templates select="*[not(starts-with(name(.), 'xsp:'))]"/>
      }
    }
  </xsl:template>

  <xsl:template match="xsp:element">
    xspParentNode = xspCurrentNode;
    xspNodeStack.push(xspParentNode);
    xspCurrentNode =
      document.createElement("<xsl:value-of select="@name"/>");

    <!-- Add namespace declarations -->
    <xsl:for-each select="namespace::*">
      ((Element) xspCurrentNode).setAttribute(
        "xmlns:<xsl:value-of select="local-name(.)"/>",
        "<xsl:value-of select="."/>"
      );
    </xsl:for-each>

    xspParentNode.appendChild(xspCurrentNode);

    <xsl:apply-templates/>

    ((Element) xspCurrentNode).normalize();
    xspCurrentNode = (Node) xspNodeStack.pop();
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
    <xsl:choose>
      <xsl:when test="starts-with(name(..), 'xsp:') and name(..) != 'xsp:content'">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        xspCurrentNode.appendChild(
          xspExpr(<xsl:value-of select="."/>, document)
        );
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp:content">
    <xsl:apply-templates/>
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
    <!-- Appending to xspCurrentNode doesn't work for Cocoon PIs,
         because Cocoon expects its PIs to be at the top level. -->
    document.insertBefore(
       document.createProcessingInstruction(
         "<xsl:value-of select="@target"/>",
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
       ), document.getDocumentElement ()
     );
  </xsl:template>

  <xsl:template match="xsp:comment">
    xspCurrentNode.appendChild(
      document.createTextNode("<xsl:value-of select="."/>")
    );
  </xsl:template>


  <xsl:template match="*">
    xspParentNode = xspCurrentNode;
    xspNodeStack.push(xspParentNode);
    xspCurrentNode =
      document.createElement("<xsl:value-of select="name(.)"/>");
    xspParentNode.appendChild(xspCurrentNode);

    <xsl:apply-templates select="@*"/>

    <!-- Add namespace declarations -->
    <xsl:for-each select="namespace::*">
      ((Element) xspCurrentNode).setAttribute(
        "xmlns:<xsl:value-of select="local-name(.)"/>",
        "<xsl:value-of select="."/>"
      );
    </xsl:for-each>

    <xsl:apply-templates/>

    ((Element) xspCurrentNode).normalize();
    xspCurrentNode = (Node) xspNodeStack.pop();
  </xsl:template>

  <xsl:template match="@*">
    <xsl:variable name="attribute-name">
      <xsl:choose>
        <xsl:when test="starts-with(name(.), 'xsp:')">xmlns:<xsl:value-of select="substring(name(.), 5)"/></xsl:when>
	<xsl:otherwise><xsl:value-of select="name(.)"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    ((Element) xspCurrentNode).setAttribute(
      "<xsl:value-of select="$attribute-name"/>",
      "<xsl:apply-templates select="." mode="Escape"/>"
    );
  </xsl:template>

  <xsl:template match="@*" mode="Escape">
    <xsl:variable name="backslashEscaped">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="."/>
        <xsl:with-param name="replace" select="'\'"/>
        <xsl:with-param name="with" select="'\\'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="backslashAndQuotesEscaped">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="$backslashEscaped"/>
        <xsl:with-param name="replace" select="'&quot;'"/>
        <xsl:with-param name="with" select="'\&quot;'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="$backslashAndQuotesEscaped"/>
  </xsl:template>

  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:choose>
      <xsl:when test="contains($text,$replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$with"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="with" select="$with"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp:variable"/>

  <!-- *** Dynamic Tag Support *** -->

  <!-- Expand dynamic tags to code -->
  <xsl:template name="expr-value" match="xsp:expr-value">
    <xsl:choose>
      <xsl:when test="name(*[1]) = 'xsp:expr'">
        <xsl:value-of select="*[1]"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="."/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Prolog declarations required to mix code and markup -->
  <xsl:template match="xsp:declare-node-stack">
    Node xspParentNode = null;
    Node xspCurrentNode = <xsl:value-of select="@node-argument"/>;
    Stack xspNodeStack = new Stack();
    Document document = <xsl:value-of select="@node-argument"/>.getOwnerDocument();
  </xsl:template>

</xsl:stylesheet>
