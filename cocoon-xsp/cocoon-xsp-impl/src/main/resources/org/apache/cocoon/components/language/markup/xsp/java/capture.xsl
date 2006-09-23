<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- $Id$-->

<!--
 * Logicsheet for capturing parts of the generated XML as SAX XML fragments or
 * DOM nodes.
 *
 * This logicsheet allows to use XSP-generated XML for other purposes than
 * content production.
 *
 * @version $Revision: 1.3 $ $Date: 2004/04/05 12:25:31 $
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
          this.manager.release(this.captureParser);
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
