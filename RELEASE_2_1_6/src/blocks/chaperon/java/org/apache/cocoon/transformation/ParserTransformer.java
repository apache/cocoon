/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import net.sourceforge.chaperon.build.ParserAutomatonBuilder;
import net.sourceforge.chaperon.model.grammar.Grammar;
import net.sourceforge.chaperon.model.grammar.GrammarFactory;
import net.sourceforge.chaperon.process.ParserAutomaton;
import net.sourceforge.chaperon.process.ParserProcessor;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;

import java.util.Map;

/**
 * This transfomer transforms lexical tokens in a XML file into a XML hirachy by using a grammar
 * file.
 * 
 * <p>
 * Input:
 * </p>
 * <pre>
 * &lt;lexemes xmlns="http://chaperon.sourceforge.net/schema/lexemes/1.0"&gt;
 *  &lt;lexeme symbol="word" text="Text"/&gt;
 *  &lt;lexeme symbol="number" text="123"/&gt;
 *  &lt;lexeme symbol="word" text="bla"/&gt;
 * &lt;/lexemes&gt;
 * </pre>
 * 
 * <p>
 * were transform into the following output:
 * </p>
 * <pre>
 * &lt;sentence xmlns="http://chaperon.sourceforge.net/schema/syntaxtree/1.0"&gt;
 *  &lt;word&gt;Text&lt;/word&gt;
 *  &lt;number&gt;123&lt;/number&gt;
 *  &lt;word&gt;bla&lt;/word&gt;
 * &lt;/sentence&gt;
 * </pre>
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: ParserTransformer.java,v 1.12 2004/03/05 13:01:48 bdelacretaz Exp $
 */
public class ParserTransformer extends ParserProcessor
        implements Transformer, LogEnabled, Serviceable, Parameterizable,
                   Recyclable, Disposable, CacheableProcessingComponent
{
  private String grammar = null;
  private Source grammarSource = null;
  private Logger logger = null;
  private ServiceManager manager = null;
  private SourceResolver resolver = null;

  /**
   * Provide component with a logger.
   *
   * @param logger the logger
   */
  public void enableLogging(Logger logger)
  {
    this.logger = logger;

    // TODO: check if the loglevel is correct LogKitLogger -> Logger
    // setLog(new AvalonLogger(logger));
    //setLog(new ConsoleLog());
  }

  /**
   * Pass the ServiceManager to the object. The Serviceable implementation should use the
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
    setFlatten(parameters.getParameterAsBoolean("flatten", false));

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

    setFailSafe(parameters.getParameterAsBoolean("failsafe", false));

    Store store = null;
    try
    {
      this.grammar = src;

      this.grammarSource = resolver.resolveURI(this.grammar);

      // Retrieve the parser automaton from the transient store
      store = (Store)this.manager.lookup(Store.TRANSIENT_STORE);

      ParserAutomatonEntry entry = (ParserAutomatonEntry)store.get(this.grammarSource.getURI());

      // If the parser automaton has changed, rebuild the parser automaton
      if ((entry==null) || (entry.getValidity()==null) ||
          ((entry.getValidity().isValid(this.grammarSource.getValidity()))<=0))
      {
        this.logger.info("(Re)building the automaton from '"+this.grammarSource.getURI()+"'");

        //SAXConfigurationHandler confighandler = new SAXConfigurationHandler();
        if (this.grammarSource.getInputStream()==null)
          throw new ProcessingException("Source '"+this.grammarSource.getURI()+"' not found");

        GrammarFactory factory = new GrammarFactory();
        SourceUtil.toSAX(this.manager, this.grammarSource, null, factory);

        //Configuration config = confighandler.getConfiguration();
        //Grammar grammar = GrammarFactory.createGrammar(config);
        Grammar grammar = factory.getGrammar();

        if (grammar==null)
          throw new ProcessingException("Error while reading the grammar from "+src);

        ParserAutomatonBuilder builder =
          new ParserAutomatonBuilder(grammar  /*, new AvalonLogger(logger)*/);

        ParserAutomaton automaton = builder.getParserAutomaton();
        setParserAutomaton(builder.getParserAutomaton());

        this.logger.info("Store automaton into store for '"+this.grammarSource.getURI()+"'");
        store.store(this.grammarSource.getURI(),
                    new ParserAutomatonEntry(automaton, this.grammarSource.getValidity()));
      }
      else
      {
        this.logger.info("Getting automaton from store for '"+this.grammarSource.getURI()+"'");
        setParserAutomaton(entry.getParserAutomaton());
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
    return this.grammarSource.getURI();
  }

  /**
   * Generate the validity object.
   *
   * @return The generated validity object or <code>null</code> if the component is currently not
   *         cacheable.
   */
  public SourceValidity getValidity()
  {
    return this.grammarSource.getValidity();
  }

  /**
   * Recycle this component. All instance variables are set to <code>null</code>.
   */
  public void recycle()
  {
    if ((this.resolver!=null) && (this.grammarSource!=null))
    {
      this.resolver.release(this.grammarSource);
      this.grammarSource = null;
    }
  }

  /**
   * The dispose operation is called at the end of a components lifecycle.
   */
  public void dispose()
  {
    if ((this.resolver!=null) && (this.grammarSource!=null))
    {
      this.resolver.release(this.grammarSource);
      this.grammarSource = null;
    }

    this.manager = null;
  }

  /**
   * This class represent a entry in a store to cache the parser automaton.
   */
  public class ParserAutomatonEntry implements Serializable
  {
    private SourceValidity validity = null;
    private ParserAutomaton automaton = null;

    /**
     * Create a new entry.
     *
     * @param automaton Parser automaton.
     * @param validity Validity for the grammar file.
     */
    public ParserAutomatonEntry(ParserAutomaton automaton, SourceValidity validity)
    {
      this.automaton = automaton;
      this.validity = validity;
    }

    /**
     * Return the validity of the grammar file.
     *
     * @return Validity of the grammar file.
     */
    public SourceValidity getValidity()
    {
      return this.validity;
    }

    /**
     * Return the parser automaton.
     *
     * @return Parser automaton.
     */
    public ParserAutomaton getParserAutomaton()
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
      automaton = (ParserAutomaton)in.readObject();
    }
  }
}
