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

import net.sourceforge.chaperon.model.extended.ExtendedGrammar;
import net.sourceforge.chaperon.process.extended.ExtendedDirectParserProcessor;

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

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.UnmarshalHandler;
import org.exolab.castor.xml.Unmarshaller;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;

import java.util.Map;

/**
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: ExtendedParserTransformer.java,v 1.6 2004/03/05 13:01:48 bdelacretaz Exp $
 */
public class ExtendedParserTransformer extends ExtendedDirectParserProcessor
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
    //setLog(new AvalonLogger(logger));
  }

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
      this.grammar = src;

      this.grammarSource = resolver.resolveURI(this.grammar);

      // Retrieve the parser automaton from the transient store
      store = (Store)this.manager.lookup(Store.TRANSIENT_STORE);

      GrammarEntry entry = (GrammarEntry)store.get(this.grammarSource.getURI());

      // If the parser automaton has changed, rebuild the parser automaton
      if ((entry==null) || (entry.getValidity()==null) ||
          ((entry.getValidity().isValid(this.grammarSource.getValidity()))<=0))
      {
        this.logger.info("(Re)building the grammar from '"+this.grammarSource.getURI()+"'");

        if (this.grammarSource.getInputStream()==null)
          throw new ProcessingException("Source '"+this.grammarSource.getURI()+"' not found");

        Mapping mapping = new Mapping();

        mapping.loadMapping(new InputSource(ExtendedGrammar.class.getResource("mapping.xml")
                                                                 .openStream()));

        Unmarshaller unmarshaller = new Unmarshaller(ExtendedGrammar.class);
        unmarshaller.setMapping(mapping);

        UnmarshalHandler unmarshalHandler = unmarshaller.createHandler();
        SourceUtil.toSAX(this.manager, this.grammarSource, null,
                         Unmarshaller.getContentHandler(unmarshalHandler));

        ExtendedGrammar grammar = (ExtendedGrammar)unmarshalHandler.getObject();

        if (grammar==null)
          throw new ProcessingException("Error while reading the grammar from "+src);

        setExtendedGrammar(grammar);

        this.logger.info("Store grammar into store for '"+this.grammarSource.getURI()+"'");
        store.store(this.grammarSource.getURI(),
                    new GrammarEntry(grammar, this.grammarSource.getValidity()));
      }
      else
      {
        this.logger.info("Getting grammar from store for '"+this.grammarSource.getURI()+"'");
        setExtendedGrammar(entry.getExtendedGrammar());
      }
    }
    catch (MappingException me)
    {
      throw new ProcessingException("Error while reading the grammar", me);
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
  public class GrammarEntry implements Serializable
  {
    private SourceValidity validity = null;
    private ExtendedGrammar grammar = null;

    /**
     * Create a new entry.
     *
     * @param grammar Extended grammar
     * @param validity Validity for the grammar file.
     */
    public GrammarEntry(ExtendedGrammar grammar, SourceValidity validity)
    {
      this.grammar = grammar;
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
    public ExtendedGrammar getExtendedGrammar()
    {
      return this.grammar;
    }

    private void writeObject(java.io.ObjectOutputStream out)
      throws IOException
    {
      out.writeObject(validity);
      out.writeObject(grammar);
    }

    private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
    {
      validity = (SourceValidity)in.readObject();
      grammar = (ExtendedGrammar)in.readObject();
    }
  }
}
