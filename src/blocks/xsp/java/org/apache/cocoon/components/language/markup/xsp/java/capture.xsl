<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- $Id: capture.xsl,v 1.1 2004/03/10 12:58:06 stephan Exp $-->
<!--

 ============================================================================
                   The Apache Software License, Version 1.2
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

<!--
 * Logicsheet for capturing parts of the generated XML as SAX XML fragments or
 * DOM nodes.
 *
 * This logicsheet allows to use XSP-generated XML for other purposes than
 * content production.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/10 12:58:06 $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:capture="http://apache.org/cocoon/capture/1.0"
>
<!-- Namespace URI for this logicsheet -->
<xsl:param name="namespace-uri">http://apache.org/cocoon/capture/1.0</xsl:param>

<!-- Include logicsheet common stuff -->
<xsl:include href="logicsheet-util.xsl"/>

<!--
   Class-level declarations
-->
<xsl:template match="xsp:page">
  <xsp:page>
    <xsl:apply-templates select="@*"/>
    <xsp:structure>
      <xsp:include>org.apache.cocoon.components.sax.XMLByteStreamCompiler</xsp:include>
      <xsp:include>org.apache.cocoon.components.sax.XMLByteStreamFragment</xsp:include>
      <xsp:include>org.apache.cocoon.xml.XMLFragment</xsp:include>
      <xsp:include>org.apache.cocoon.xml.dom.DOMBuilder</xsp:include>
      <xsp:include>org.apache.excalibur.xml.dom.DOMParser</xsp:include>
      <xsp:include>org.w3c.dom.DocumentFragment</xsp:include>
      <xsp:include>org.w3c.dom.Node</xsp:include>
      <xsp:include>org.xml.sax.ContentHandler</xsp:include>
      <xsp:include>org.xml.sax.ext.LexicalHandler</xsp:include>
    </xsp:structure>
    <xsl:if test="//capture:dom-variable or //capture:dom-request-attr">
      <xsp:logic>
        private DOMParser captureParser;
      </xsp:logic>
    </xsl:if>
   <xsl:apply-templates/>
  </xsp:page>
</xsl:template>

<!--
   Before generation begins, setup a parser if DOM captures are performed.
-->
<xsl:template match="xsp:page/*[not(self::xsp:*)]">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:choose>
      <xsl:when test="//capture:dom-variable or //capture:dom-request-attr">
        <xsp:logic>
        try {
          this.captureParser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
        } catch(Exception e) {
          throw new ProcessingException("Cannot get parser" , e);
        }
        try {
        </xsp:logic>
        <xsl:apply-templates/>
        <xsp:logic>
        } finally {
          this.manager.release((Component)this.captureParser);
        }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:copy>
</xsl:template>

<!--
   Captures its content and store it as an XMLFragment variable.

   @param name name of the generated variable holding the fragment
-->
<xsl:template match="capture:fragment-variable">
  <xsl:variable name="name">
    <xsl:call-template name="get-parameter">
      <xsl:with-param name="name">name</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="id" select="generate-id(.)"/>
  <xsp:logic>
    // Save the current XML consumer
    ContentHandler contentHandler_<xsl:value-of select="$id"/> = this.contentHandler;
    LexicalHandler lexicalHandler_<xsl:value-of select="$id"/> = this.lexicalHandler;
    // Create a new one that will capture all SAX events
    XMLByteStreamCompiler consumer_<xsl:value-of select="$id"/> = new XMLByteStreamCompiler();
    try {
      this.contentHandler = consumer_<xsl:value-of select="$id"/>;
      this.lexicalHandler = consumer_<xsl:value-of select="$id"/>;
      this.contentHandler.startDocument(); // XMLByteStream wants documents
      <xsl:apply-templates/>
      this.contentHandler.endDocument();
    } finally {
      // Always restore previous consumer
      this.contentHandler = contentHandler_<xsl:value-of select="$id"/>;
      this.lexicalHandler = lexicalHandler_<xsl:value-of select="$id"/>;
    }
    XMLFragment <xsl:value-of select="$name"/> =
      new XMLByteStreamFragment(consumer_<xsl:value-of select="$id"/>.getSAXFragment());
  </xsp:logic>
</xsl:template>

<!--
   Captures its content and store it as an XMLFragment in a request attribute.

   @param name the request attribute name (String)
-->
<xsl:template match="capture:fragment-request-attr">
  <xsl:variable name="name">
    <xsl:call-template name="get-string-parameter">
      <xsl:with-param name="name">name</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="id" select="generate-id(.)"/>
  <xsp:logic>
    ContentHandler contentHandler_<xsl:value-of select="$id"/> = this.contentHandler;
    LexicalHandler lexicalHandler_<xsl:value-of select="$id"/> = this.lexicalHandler;
    XMLByteStreamCompiler consumer_<xsl:value-of select="$id"/> = new XMLByteStreamCompiler();
    try {
      this.contentHandler = consumer_<xsl:value-of select="$id"/>;
      this.lexicalHandler = consumer_<xsl:value-of select="$id"/>;
      this.contentHandler.startDocument(); // XMLByteStream wants documents
      <xsl:apply-templates/>
      this.contentHandler.endDocument();
    } finally {
      this.contentHandler = contentHandler_<xsl:value-of select="$id"/>;
      this.lexicalHandler = lexicalHandler_<xsl:value-of select="$id"/>;
    }
    this.request.setAttribute(<xsl:value-of select="$name"/>,
      new XMLByteStreamFragment(consumer_<xsl:value-of select="$id"/>.getSAXFragment()));
  </xsp:logic>
</xsl:template>

<!--
   Captures its content and store it as a org.w3c.dom.Node variable.
   Note : the node is actually a DocumentFragment.

   @param name name of the generated variable holding the DOM node
-->
<xsl:template match="capture:dom-variable">
  <xsl:variable name="name">
    <xsl:call-template name="get-parameter">
      <xsl:with-param name="name">name</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="id" select="generate-id(.)"/>
  <xsp:logic>
    ContentHandler contentHandler_<xsl:value-of select="$id"/> = this.contentHandler;
    LexicalHandler lexicalHandler_<xsl:value-of select="$id"/> = this.lexicalHandler;
    // Create a DOMBuilder that will feed a DocumentFragment
    DocumentFragment fragment_<xsl:value-of select="$id"/> =
      this.captureParser.createDocument().createDocumentFragment();
    DOMBuilder builder_<xsl:value-of select="$id"/> = new DOMBuilder(fragment_<xsl:value-of select="$id"/>);
    try {
      this.contentHandler = builder_<xsl:value-of select="$id"/>;
      this.lexicalHandler = builder_<xsl:value-of select="$id"/>;
      <xsl:apply-templates/>
    } finally {
      this.contentHandler = contentHandler_<xsl:value-of select="$id"/>;
      this.lexicalHandler = lexicalHandler_<xsl:value-of select="$id"/>;
    }
    Node <xsl:value-of select="$name"/> = fragment_<xsl:value-of select="$id"/>;
  </xsp:logic>
</xsl:template>

<!--
   Captures its content and store it as a org.w3c.dom.Node in a request attribute.
   Note : the node is actually a DocumentFragment.

   @param name the request attribute name (String)
-->
<xsl:template match="capture:dom-request-attr">
  <xsl:variable name="name">
    <xsl:call-template name="get-string-parameter">
      <xsl:with-param name="name">name</xsl:with-param>
      <xsl:with-param name="required">true</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="id" select="generate-id(.)"/>
  <xsp:logic>
    ContentHandler contentHandler_<xsl:value-of select="$id"/> = this.contentHandler;
    LexicalHandler lexicalHandler_<xsl:value-of select="$id"/> = this.lexicalHandler;
    // Create a DOMBuilder that will feed a DocumentFragment
    DocumentFragment fragment_<xsl:value-of select="$id"/> =
      this.captureParser.createDocument().createDocumentFragment();
    DOMBuilder builder_<xsl:value-of select="$id"/> = new DOMBuilder(fragment_<xsl:value-of select="$id"/>);
    try {
      this.contentHandler = builder_<xsl:value-of select="$id"/>;
      this.lexicalHandler = builder_<xsl:value-of select="$id"/>;
      <xsl:apply-templates/>
    } finally {
      this.contentHandler = contentHandler_<xsl:value-of select="$id"/>;
      this.lexicalHandler = lexicalHandler_<xsl:value-of select="$id"/>;
    }
    this.request.setAttribute(<xsl:value-of select="$name"/>, fragment_<xsl:value-of select="$id"/>);
  </xsp:logic>
</xsl:template>

</xsl:stylesheet>
