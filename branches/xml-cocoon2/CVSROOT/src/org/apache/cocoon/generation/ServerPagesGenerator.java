/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.io.File;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;

import java.util.LinkedList;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import org.apache.avalon.component.Composable;
import org.apache.avalon.component.Component;
import org.apache.avalon.component.ComponentManager;
import org.apache.excalibur.pool.Poolable;
import org.apache.excalibur.pool.Recyclable;
import org.apache.avalon.Disposable;

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.generator.ProgramGenerator;
import org.apache.cocoon.components.language.markup.xsp.XSPGenerator;
import org.apache.cocoon.components.url.URLFactory;

import java.io.IOException;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;

/**
 * This class acts as a proxy to a dynamically loaded<code>Generator</code>
 * delegating actual SAX event generation.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.25 $ $Date: 2001-04-23 17:52:44 $
 */
public class ServerPagesGenerator
  extends ServletGenerator
  implements ContentHandler, LexicalHandler, Recyclable, Disposable
{
  /**
   * The sitemap-defined server pages program generator
   */
  protected static ProgramGenerator programGenerator = null;

  protected static URLFactory factory = null;

  /**
   * Set the global component manager. This method sets the sitemap-defined
   * program generator
   *
   * @param manager The global component manager
   */
  public void compose(ComponentManager manager) {
    super.compose(manager);

    if (programGenerator == null) {
      getLogger().debug("Looking up " + Roles.PROGRAM_GENERATOR);
      try {
          this.programGenerator = (ProgramGenerator)
              manager.lookup(Roles.PROGRAM_GENERATOR);
          this.factory = (URLFactory) manager.lookup(Roles.URL_FACTORY);
      } catch (Exception e) {
          getLogger().error("Could not find ProgramGenerator", e);
      }
    }
  }

  /**
   * The loaded generator's <code>MarkupLanguage</code>
   */
  protected String markupLanguage;

  /**
   * The loaded generator's <code>ProgrammingLanguage</code>
   */
  protected String programmingLanguage;

  /**
   * The default <code>MarkupLanguage</code>
   */
  public final static String DEFAULT_MARKUP_LANGUAGE = "xsp";

  /**
   * The default <code>ProgrammingLanguage</code>
   */
  public final static String DEFAULT_PROGRAMMING_LANGUAGE = "java";


  /**
   * Generate XML data. This method loads a server pages generator associated
   * with its (file) input source and delegates SAX event generator to it
   * taking care of "closing" any event left open by the loaded generator as a
   * result of its possible "premature" return (a common situation in server
   * pages)
   *
   * @exception IOException IO Error
   * @exception SAXException SAX event generation error
   * @exception ProcessingException Error during load/execution
   */
  public void generate() throws IOException, SAXException, ProcessingException {
    InputSource inputSource = this.resolver.resolveEntity(null, this.source);

    String systemId = inputSource.getSystemId();

    URL url = factory.getURL(systemId);

    if (!url.getProtocol().equals("file")) {
      throw new ResourceNotFoundException("Not a file: " + url.toString());
    }

    File file = new File(url.getFile());

    if (!file.canRead()) {
      throw new ResourceNotFoundException("Can't read file: " + url.toString());
    }

    if (this.markupLanguage == null) {
        this.markupLanguage = this.parameters.getParameter(
          "markup-language", DEFAULT_MARKUP_LANGUAGE
        );
        this.programmingLanguage = this.parameters.getParameter(
          "programming-language", DEFAULT_PROGRAMMING_LANGUAGE
        );
    }

    XSPGenerator generator = null;

    try {
      generator = (XSPGenerator)
        programGenerator.load(file, this.markupLanguage, this.programmingLanguage, this.resolver);
    } catch (Exception e) {
      getLogger().warn("ServerPagesGenerator.generate()", e);
      throw new ResourceNotFoundException(e.getMessage(), e);
    }

    generator.setContentHandler(this);
    generator.setLexicalHandler(this);
    generator.setup(this.resolver, this.objectModel, this.source, this.parameters);

    // log exception and ensure that generator is released.
    try {
        generator.generate();
    } catch (IOException e){
        getLogger().error("IOException in ServerPagesGenerator.generate()", e);
        throw e;
    } catch (SAXException e){
        getLogger().error("SAXException in ServerPagesGenerator.generate()", e);
        throw e;
    } catch (ProcessingException e){
        getLogger().error("ProcessingException in ServerPagesGenerator.generate()", e);
        throw e;
    } catch (Exception e){
        getLogger().error("Exception in ServerPagesGenerator.generate()", e);
    } finally {
        if(generator != null)
            programGenerator.release(generator);
    }

    // End any started events in case of premature return
    while (this.eventStack.size()!=0) {
      EventData eventData = (EventData) this.eventStack.removeFirst();

      switch (eventData.eventType) {
        case DOCUMENT:
          this.contentHandler.endDocument();
          break;
        case ELEMENT:
          this.contentHandler.endElement(
            eventData.getNamespaceURI(),
            eventData.getLocalName(),
            eventData.getRawName()
          );
          break;
        case PREFIX_MAPPING:
          this.contentHandler.endPrefixMapping(eventData.getPrefix());
          break;
        case CDATA:
          this.lexicalHandler.endCDATA();
          break;
        case DTD:
          this.lexicalHandler.endDTD();
          break;
        case ENTITY:
          this.lexicalHandler.endEntity(eventData.getName());
          break;
      }
    }
  }

  /* Handlers */

  /**
   * The SAX event stack. Used for "completing" pendind SAX events left "open"
   * by prematurely returning server pages generators
   */
  protected LinkedList eventStack = new LinkedList();

    /**
     * Receive notification of character data.
     */
  public void characters(char[] ch, int start, int length) throws SAXException {
    this.contentHandler.characters(ch, start, length);
  }

    /**
     * Receive notification of the end of a document.
     */
  public void endDocument() throws SAXException {
    this.eventStack.removeFirst();
    this.contentHandler.endDocument();
  }

    /**
     * Receive notification of the end of an element.
     */
  public void endElement(String namespaceURI, String localName, String rawName)
    throws SAXException
  {
    this.eventStack.removeFirst();
    this.contentHandler.endElement(namespaceURI, localName, rawName);
  }

    /**
     * End the scope of a prefix-URI mapping.
     */
  public void endPrefixMapping(String prefix) throws SAXException {
    this.eventStack.removeFirst();
    this.contentHandler.endPrefixMapping(prefix);
  }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
  public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException
  {
    this.contentHandler.ignorableWhitespace(ch, start, length);
  }

    /**
     * Receive notification of a processing instruction.
     */
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    this.contentHandler.processingInstruction(target, data);
  }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
  public void setDocumentLocator(Locator locator) {
    this.contentHandler.setDocumentLocator(locator);
  }

    /**
     * Receive notification of a skipped entity.
     */
  public void skippedEntity(String name)
    throws SAXException
  {
    this.contentHandler.skippedEntity(name);
  }

    /**
     * Receive notification of the beginning of a document.
     */
  public void startDocument() throws SAXException {
    this.contentHandler.startDocument();
    this.eventStack.addFirst(new EventData(DOCUMENT));
  }

    /**
     * Receive notification of the beginning of an element.
     */
  public void startElement(
    String namespaceURI, String localName, String rawName, Attributes atts
  )
    throws SAXException
  {
    this.contentHandler.startElement(namespaceURI, localName, rawName, atts);
    this.eventStack.addFirst(new EventData(ELEMENT, namespaceURI, localName, rawName));
  }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
  public void startPrefixMapping(String prefix, String uri) throws SAXException
  {
    this.contentHandler.startPrefixMapping(prefix, uri);
    this.eventStack.addFirst(new EventData(PREFIX_MAPPING, prefix, uri));
  }

  public void comment(char[] ch, int start, int length) throws SAXException {
    this.lexicalHandler.comment(ch, start, length);
  }

  public void endCDATA() throws SAXException {
    this.lexicalHandler.endCDATA();
    this.eventStack.removeFirst();
  }

  public void endDTD() throws SAXException {
    this.lexicalHandler.endDTD();
    this.eventStack.removeFirst();
  }

  public void endEntity(String name) throws SAXException {
    this.lexicalHandler.endEntity(name);
    this.eventStack.removeFirst();
  }

  public void startCDATA() throws SAXException {
    this.lexicalHandler.startCDATA();
    this.eventStack.addFirst(new EventData(CDATA));
  }

  public void startDTD(String name, String publicId, String systemId)
    throws SAXException
  {
    this.lexicalHandler.startDTD(name, publicId, systemId);
    this.eventStack.addFirst(new EventData(DTD, name, publicId, systemId));
  }

  public void startEntity(String name) throws SAXException {
    this.lexicalHandler.startEntity(name);
    this.eventStack.addFirst(new EventData(ENTITY, name));
  }

  protected final static int DOCUMENT = 0;
  protected final static int ELEMENT = 1;
  protected final static int PREFIX_MAPPING = 2;
  protected final static int CDATA = 3;
  protected final static int DTD = 4;
  protected final static int ENTITY = 5;

  protected class EventData {
    protected int eventType;
    protected String namespaceURI;
    protected String localName;
    protected String rawName;
    protected String prefix;
    protected String publicId;
    protected String systemId;
    protected String name;

    protected EventData(int eventType) {
      this.eventType = eventType; // DOCUMENT | CDATA
    }

    protected EventData(
      int eventType, String data1, String data2, String data3
    )
    {
      this.eventType = eventType;
      switch (this.eventType) {
        case ELEMENT:
          this.namespaceURI = data1;
          this.localName = data2;
          this.rawName = data3;
      break;
        case DTD:
          this.name = data1;
          this.publicId = data2;
          this.systemId = data3;
      break;
      }
    }

    protected EventData(
      int eventType, String data1, String data2
    )
    {
      this.eventType = eventType;
      switch (this.eventType) {
        case PREFIX_MAPPING:
          this.prefix = data1;
          this.namespaceURI = data2;
          break;
      }
    }

    protected EventData(int eventType, String data) {
      this.eventType = eventType;
      switch (this.eventType) {
        case ENTITY:
          this.name = data;
          break;
      }
    }

    protected String getNamespaceURI() { return this.namespaceURI; }
    protected String getLocalName() { return this.localName; }
    protected String getRawName() { return this.rawName; }
    protected String getPrefix() { return this.prefix; }
    protected String getPublicId() { return this.publicId; }
    protected String getSystemId() { return this.systemId; }
    protected String getName() { return this.name; }
  }

    /**
     * dispose
     */
    public void dispose() {
        if(this.programGenerator != null) manager.release((Component)this.programGenerator);
        if(this.factory != null) manager.release((Component)this.factory);
    }
}
