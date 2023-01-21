/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;

import net.sourceforge.chaperon.build.LexicalAutomatonBuilder;
import net.sourceforge.chaperon.common.Decoder;
import net.sourceforge.chaperon.model.lexicon.Lexicon;
import net.sourceforge.chaperon.model.lexicon.LexiconFactory;
import net.sourceforge.chaperon.process.LexicalAutomaton;
import net.sourceforge.chaperon.process.PatternProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This transfomer transforms text pattern of a XML file into lexemes by using a lexicon file.
 *
 * <p>
 * Input:
 * </p>
 * <pre>
 * &lt;section&gt;
 *  Text 123 bla
 * &lt;/section&gt;
 * </pre>
 *
 * <p>
 * can be transformed into the following output:
 * </p>
 * <pre>
 * &lt;section&gt;
 *  Text
 *  &lt;lexeme symbol="number" text="123"/&gt;
 *  bla
 * &lt;/section&gt;
 * </pre>
 *
 * @version $Id$
 */
public class PatternTransformer extends AbstractTransformer
                                implements Serviceable, Recyclable, Disposable,
                                           Parameterizable, CacheableProcessingComponent {

    private final Log logger = LogFactory.getLog(getClass());

  /** Namespace for the SAX events. */
  public static final String NS = "http://chaperon.sourceforge.net/schema/lexemes/2.0";

  private String lexicon = null;
  private Source lexiconSource = null;
  private ServiceManager manager = null;
  private SourceResolver resolver = null;
  private LexicalAutomaton automaton = null;
  private PatternProcessor processor = new PatternProcessor();
  private boolean groups = false;
  private StringBuffer buffer = new StringBuffer();
  private StringBuffer output = new StringBuffer();


  /**
   * Pass the ServiceManager to the Serviceable. The Serviceable implementation should use the
   * specified ServiceManager to acquire the services it needs for execution.
   *
   * @param manager The ServiceManager which this Serviceable uses.
   */
  public void service(ServiceManager manager)
  {
    this.manager = manager;
  }

  /**
   * Provide component with parameters.
   *
   * @param parameters the parameters
   *
   * @throws ParameterException if parameters are invalid
   */
  public void parameterize(Parameters parameters) throws ParameterException
  {
    groups = parameters.getParameterAsBoolean("groups", false);
  }

  /**
   * Set the SourceResolver, objectModel Map, the source and sitemap Parameters used to process the
   * request.
   *
   * @param resolver Source resolver
   * @param objectmodel Object model
   * @param src Source
   * @param parameters Parameters
   *
   * @throws IOException
   * @throws ProcessingException
   * @throws SAXException
   */
  public void setup(SourceResolver resolver, Map objectmodel, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException
  {
    this.resolver = resolver;

    Store store = null;

    try
    {
      this.lexicon = src;

      this.lexiconSource = resolver.resolveURI(this.lexicon);

      // Retrieve the lexical automaton from the transient store
      store = (Store)this.manager.lookup(Store.TRANSIENT_STORE);

      LexicalAutomatonEntry entry = (LexicalAutomatonEntry)store.get(this.lexiconSource.getURI());

      // If the lexicon has changed, rebuild the lexical automaton
      if ((entry==null) || (entry.getValidity()==null) ||
          (entry.getValidity().isValid(this.lexiconSource.getValidity())<=0))
      {
        this.logger.info("(Re)building the automaton from '"+this.lexiconSource.getURI()+"'");

        if (this.lexiconSource.getInputStream()==null)
          throw new ProcessingException("Source '"+this.lexiconSource.getURI()+"' not found");

        LexiconFactory factory = new LexiconFactory();
        SourceUtil.toSAX(this.manager, this.lexiconSource, null, factory);

        Lexicon lexicon = factory.getLexicon();

        LexicalAutomatonBuilder builder =
          new LexicalAutomatonBuilder(lexicon/*, new AvalonLogger(this.logger)*/);

        this.automaton = builder.getLexicalAutomaton();

        this.logger.info("Store automaton into store for '"+this.lexiconSource.getURI()+"'");
        store.store(this.lexiconSource.getURI(),
                    new LexicalAutomatonEntry(this.automaton, this.lexiconSource.getValidity()));
      }
      else
      {
        this.logger.info("Getting automaton from store for '"+this.lexiconSource.getURI()+"'");
        this.automaton = entry.getLexicalAutomaton();
      }
    }
    catch (SourceException se)
    {
      throw new ProcessingException("Error during resolving of '"+src+"'.", se);
    }
    catch (ServiceException se)
    {
      throw new ProcessingException("Could not lookup for component", se);
    }
    finally
    {
      if (store!=null)
        this.manager.release(store);
    }
  }

  /**
   * Generate the unique key. This key must be unique inside the space of this component.
   *
   * @return The generated key hashes the src
   */
  public Serializable getKey()
  {
    return this.lexiconSource.getURI();
  }

  /**
   * Generate the validity object.
   *
   * @return The generated validity object or <code>null</code> if the component is currently not
   *         cacheable.
   */
  public SourceValidity getValidity()
  {
    return this.lexiconSource.getValidity();
  }

  /**
   * Recycle this component. All instance variables are set to <code>null</code>.
   */
  public void recycle()
  {
    if ((this.resolver!=null) && (this.lexiconSource!=null))
    {
      this.resolver.release(this.lexiconSource);
      this.lexiconSource = null;
    }

    this.automaton = null;
    super.recycle();
  }

  /**
   * The dispose operation is called at the end of a components lifecycle.
   */
  public void dispose()
  {
    if ((this.resolver!=null) && (this.lexiconSource!=null))
    {
      this.resolver.release(this.lexiconSource);
      this.lexiconSource = null;
    }

    this.manager = null;
  }

  /**
   * Receive notification of the beginning of an element.
   *
   * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if
   *        Namespace processing is not being performed.
   * @param loc The local name (without prefix), or the empty string if Namespace processing is not
   *        being performed.
   * @param raw The raw XML 1.0 name (with prefix), or the empty string if raw names are not
   *        available.
   * @param a The attributes attached to the element. If there are no attributes, it shall be an
   *        empty Attributes object.
   *
   * @throws SAXException
   */
  public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException
  {
    search();

    if (contentHandler!=null)
      contentHandler.startElement(uri, loc, raw, a);
  }

  /**
   * Receive notification of the end of an element.
   *
   * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if
   *        Namespace processing is not being performed.
   * @param loc The local name (without prefix), or the empty string if Namespace processing is not
   *        being performed.
   * @param raw The raw XML 1.0 name (with prefix), or the empty string if raw names are not
   *        available.
   *
   * @throws SAXException
   */
  public void endElement(String uri, String loc, String raw)
    throws SAXException
  {
    search();

    if (contentHandler!=null)
      contentHandler.endElement(uri, loc, raw);
  }

  /**
   * Receive notification of character data.
   *
   * @param c The characters from the XML document.
   * @param start The start position in the array.
   * @param len The number of characters to read from the array.
   *
   * @throws SAXException
   */
  public void characters(char[] c, int start, int len)
    throws SAXException
  {
    buffer.append(c, start, len);
  }

  /**
   * Receive notification of ignorable whitespace in element content.
   *
   * @param c The characters from the XML document.
   * @param start The start position in the array.
   * @param len The number of characters to read from the array.
   *
   * @throws SAXException
   */
  public void ignorableWhitespace(char[] c, int start, int len)
    throws SAXException
  {
    buffer.append(c, start, len);
  }

  /**
   * Receive notification of a processing instruction.
   *
   * @param target The processing instruction target.
   * @param data The processing instruction data, or null if none was supplied.
   *
   * @throws SAXException
   */
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    search();

    if (contentHandler!=null)
      contentHandler.processingInstruction(target, data);
  }

  /**
   * Report an XML comment anywhere in the document.
   *
   * @param ch An array holding the characters in the comment.
   * @param start The starting position in the array.
   * @param len The number of characters to use from the array.
   *
   * @throws SAXException
   */
  public void comment(char[] ch, int start, int len) throws SAXException
  {
    search();

    if (lexicalHandler!=null)
      lexicalHandler.comment(ch, start, len);
  }

  /**
   * @throws SAXException
   */
  private void search() throws SAXException
  {
    if (buffer.length()<=0)
      return;

    char[] text = buffer.toString().toCharArray();

    String lexemesymbol;
    String lexemetext;
    String[] groups = null;
    int lexemeindex;
    int position = 0;

    output.setLength(0);
    do
    {
      lexemesymbol = null;
      lexemetext = null;

      for (lexemeindex = automaton.getLexemeCount()-1; lexemeindex>=0; lexemeindex--)
      {
        processor.setPatternAutomaton(automaton.getLexemeDefinition(lexemeindex));

        if ((processor.match(text, position)) &&
            ((lexemetext==null) || (processor.getGroup().length()>=lexemetext.length())))
        {
          lexemesymbol = automaton.getLexemeSymbol(lexemeindex);
          lexemetext = processor.getGroup();
          if (this.groups)
          {
            groups = new String[processor.getGroupCount()];
            for (int group = 0; group<processor.getGroupCount(); group++)
              groups[group] = processor.getGroup(group);
          }
        }
      }

      if ((lexemetext!=null) && (lexemetext.length()>0))
      {
        if (lexemesymbol!=null)
        {
          if (logger!=null)
            logger.debug("Recognize token "+lexemesymbol+" with "+Decoder.toString(lexemetext));

          if (output.length()>0)
            contentHandler.characters(output.toString().toCharArray(), 0, output.length());

          output.setLength(0);

          contentHandler.startPrefixMapping("", NS);

          AttributesImpl atts = new AttributesImpl();

          atts.addAttribute("", "symbol", "symbol", "CDATA", lexemesymbol);
          atts.addAttribute("", "text", "text", "CDATA", lexemetext);
          contentHandler.startElement(NS, "lexeme", "lexeme", atts);

            if (this.groups) {
                for (int group = 0; group < groups.length; group++) {
                    contentHandler.startElement(NS, "group", "group", XMLUtils.EMPTY_ATTRIBUTES);
                    contentHandler.characters(groups[group].toCharArray(), 0, groups[group].length());
                    contentHandler.endElement(NS, "group", "group");
                }
            }

          contentHandler.endElement(NS, "lexeme", "lexeme");
          contentHandler.endPrefixMapping("");
        }
        else if (logger!=null)
          logger.debug("Ignore lexeme with "+Decoder.toString(lexemetext));

        position += lexemetext.length();
      }
      else
      {
        output.append(text[position]);
        position++;
      }
    }
    while (position<text.length);

    if (output.length()>0)
      contentHandler.characters(output.toString().toCharArray(), 0, output.length());

    buffer.setLength(0);
  }

  /**
   * This class represent a entry in a store to cache the lexical automaton.
   */
  public class LexicalAutomatonEntry implements Serializable
  {
    private SourceValidity validity = null;
    private LexicalAutomaton automaton = null;

    /**
     * Create a new entry.
     *
     * @param automaton Lexical automaton.
     * @param validity Validity of the lexicon file.
     */
    public LexicalAutomatonEntry(LexicalAutomaton automaton, SourceValidity validity)
    {
      this.automaton = automaton;
      this.validity = validity;
    }

    /**
     * Return the validity of the lexicon file.
     *
     * @return Validity of the lexicon file.
     */
    public SourceValidity getValidity()
    {
      return this.validity;
    }

    /**
     * Return the lexical automaton.
     *
     * @return Lexical automaton.
     */
    public LexicalAutomaton getLexicalAutomaton()
    {
      return this.automaton;
    }

    private void writeObject(java.io.ObjectOutputStream out)
      throws IOException
    {
      out.writeObject(validity);
      out.writeObject(automaton);
    }

    private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
    {
      validity = (SourceValidity)in.readObject();
      automaton = (LexicalAutomaton)in.readObject();
    }
  }
}
