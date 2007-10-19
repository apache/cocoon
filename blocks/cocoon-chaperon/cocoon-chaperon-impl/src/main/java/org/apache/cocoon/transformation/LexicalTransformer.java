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
import org.apache.cocoon.xml.XMLConsumer;

import net.sourceforge.chaperon.build.LexicalAutomatonBuilder;
import net.sourceforge.chaperon.model.lexicon.Lexicon;
import net.sourceforge.chaperon.model.lexicon.LexiconFactory;
import net.sourceforge.chaperon.process.LexicalAutomaton;
import net.sourceforge.chaperon.process.LexicalProcessor;
import org.xml.sax.SAXException;

/**
 * This transfomer transforms special mark text part of a XML file into lexemes by using a lexicon
 * file.
 * 
 * <p>
 * Input:
 * </p>
 * <pre>
 * &lt;text xmlns="http://chaperon.sourceforge.net/schema/text/1.0"&gt;
 *  Text 123 bla
 * &lt;/text&gt;
 * </pre>
 * 
 * <p>
 * were transform into the following output:
 * </p>
 * <pre>
 * &lt;lexemes xmlns="http://chaperon.sourceforge.net/schema/lexemes/1.0"&gt;
 *  &lt;lexeme symbol="word" text="Text"/&gt;
 *  &lt;lexeme symbol="number" text="123"/&gt;
 *  &lt;lexeme symbol="word" text="bla"/&gt;
 * &lt;/lexemes&gt;
 * </pre>
 *
 * @version $Id$
 */
public class LexicalTransformer extends LexicalProcessor
                                implements Transformer, Serviceable, Recyclable, Disposable,
                                           Parameterizable, CacheableProcessingComponent {

    private final Log logger = LogFactory.getLog(getClass());

  private String lexicon = null;
  private Source lexiconSource = null;
  private ServiceManager manager = null;
  private SourceResolver resolver = null;


  /**
   * Pass the ServiceManager to the object. The Serviceable implementation
   * should use the specified ServiceManager to acquire the services it needs
   * for execution.
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
    //setRecovery(parameters.getParameterAsBoolean("recovery", false));
    setLocalizable(parameters.getParameterAsBoolean("localizable", false));
  }

  /**
   * Set the <code>XMLConsumer</code> that will receive XML data.
   *
   * @param consumer
   */
  public void setConsumer(XMLConsumer consumer)
  {
    setContentHandler(consumer);
    setLexicalHandler(consumer);
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

      // Retrieve the parser table from the transient store
      store = (Store)this.manager.lookup(Store.TRANSIENT_STORE);

      LexicalAutomatonEntry entry = (LexicalAutomatonEntry)store.get(this.lexiconSource.getURI());

      // If the parser table has changed, rebuild the parser table
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

        LexicalAutomaton automaton = builder.getLexicalAutomaton();
        setLexicalAutomaton(automaton);

        this.logger.info("Store automaton into store for '"+this.lexiconSource.getURI()+"'");

        store.store(this.lexiconSource.getURI(),
                    new LexicalAutomatonEntry(automaton, this.lexiconSource.getValidity()));
      }
      else
      {
        this.logger.info("Getting automaton from store for '"+this.lexiconSource.getURI()+"'");
        setLexicalAutomaton(entry.getLexicalAutomaton());
      }
    }
    catch (SourceException se)
    {
      throw new ProcessingException("Error during resolving of '"+src+"'.", se);
    }
    catch (ServiceException se)
    {
      throw new ProcessingException("Could not lookup for service", se);
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
