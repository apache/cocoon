/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:   "This product includes software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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

*/

package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import net.sourceforge.chaperon.adapter.avalon.LexiconFactory;
import net.sourceforge.chaperon.adapter.sax.LexicalHandlerAdapter;
import net.sourceforge.chaperon.adapter.sax.LexicalProcessorAdapter;
import net.sourceforge.chaperon.build.LexicalAutomatonBuilder;
import net.sourceforge.chaperon.model.lexicon.Lexicon;
import net.sourceforge.chaperon.process.LexicalAutomaton;
import net.sourceforge.chaperon.process.LexicalProcessor;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
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

/**
 * This transfomer transforms special mark text part of a XML
 * file into lexemes by using a lexicon file.
 *
 * <p>Input:</p>
 * <pre>
 * &lt;text xmlns="http://chaperon.sourceforge.net/schema/text/1.0"&gt;
 *  Text 123 bla
 * &lt;/text&gt;
 * </pre>
 *
 * <p>were transform into the following output:</p>
 * <pre>
 * &lt;lexemes xmlns="http://chaperon.sourceforge.net/schema/lexemes/1.0"&gt;
 *  &lt;lexeme symbol="word" text="Text"/&gt;
 *  &lt;lexeme symbol="number" text="123"/&gt;
 *  &lt;lexeme symbol="word" text="bla"/&gt;
 * &lt;/lexemes&gt;
 * </pre>
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: LexicalTransformer.java,v 1.3 2003/04/01 16:33:49 stephan Exp $
 */
public class LexicalTransformer extends LexicalProcessorAdapter
  implements Transformer, LogEnabled, Composable, Recyclable, Disposable,
             Parameterizable, CacheableProcessingComponent {

    private XMLConsumer consumer = null;

    private String lexicon = null;
    private Source lexiconSource = null;

    private Logger logger = null;
    private ComponentManager manager = null;
    private SourceResolver resolver = null;

    private LexicalAutomaton automaton = null;
    private LexicalHandlerAdapter adapter = new LexicalHandlerAdapter(true);
    private boolean recovery = false;

    /**
     * Provide component with a logger.
     *
     * @param logger the logger
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Pass the ComponentManager to the composer. The Composable
     * implementation should use the specified ComponentManager
     * to acquire the components it needs for execution.
     *
     * @param manager The ComponentManager which this Composable uses.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Provide component with parameters.
     *
     * @param parameters the parameters
     * @throws ParameterException if parameters are invalid
     */
    public void parameterize(Parameters parameters) throws ParameterException {

        recovery = parameters.getParameterAsBoolean("recovery", false);
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     *
     * @param consumer
     */
    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;

        setContentHandler(consumer);
        setLexicalHandler(consumer);

        adapter.setContentHandler(consumer);
    }

    /**
     * Set the SourceResolver, objectModel Map, the source and sitemap
     * Parameters used to process the request.
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
    public void setup(SourceResolver resolver, Map objectmodel, String src,
                      Parameters parameters)
                        throws ProcessingException, SAXException,
                               IOException {

        this.resolver = resolver;
        Store store = null;

        try {
            this.lexicon = src;

            this.lexiconSource = resolver.resolveURI(this.lexicon);

            // Retrieve the parser table from the transient store
            store = (Store) this.manager.lookup(Store.TRANSIENT_STORE);
            LexicalAutomatonEntry entry = (LexicalAutomatonEntry) store.get(this.lexiconSource.getURI());

            // If the parser table has changed, rebuild the parser table
            if ((entry==null) || (entry.getValidity()==null) ||
                (entry.getValidity().isValid(this.lexiconSource.getValidity())<=
                 0)) {

                this.logger.info("(Re)building the automaton from '"+
                                 this.lexiconSource.getURI()+"'");

                SAXConfigurationHandler confighandler = new SAXConfigurationHandler();

                if (this.lexiconSource.getInputStream()==null) {
                    throw new ProcessingException("Source '"+
                                                  this.lexiconSource.getURI()+
                                                  "' not found");
                }
                SourceUtil.toSAX(this.lexiconSource, confighandler);

                Configuration config = confighandler.getConfiguration();
                Lexicon lexicon = LexiconFactory.createLexicon(config);

                LexicalAutomatonBuilder builder = new LexicalAutomatonBuilder(lexicon,
                                                      this.logger);

                this.automaton = builder.getLexicalAutomaton();

                this.logger.info("Store automaton into store for '"+
                                 this.lexiconSource.getURI()+"'");

                store.store(this.lexiconSource.getURI(),
                            new LexicalAutomatonEntry(this.automaton,
                                                      this.lexiconSource.getValidity()));
            } else {
                this.logger.info("Getting automaton from store for '"+
                                 this.lexiconSource.getURI()+"'");
                this.automaton = entry.getLexicalAutomaton();
            }

            if (getLexicalProcessor()==null) {
                LexicalProcessor processor = new LexicalProcessor();

                processor.setLexicalHandler(this.adapter);
                processor.setLogger(this.logger);
                processor.setRecovery(recovery);
                setLexicalProcessor(processor);
            }

            getLexicalProcessor().setLexicalAutomaton(this.automaton);

        } catch (ConfigurationException confige) {
            throw new ProcessingException("Error during retrieving the lexicon",
                                          confige);
        } catch (SourceException se) {
            throw new ProcessingException("Error during resolving of '"+src+
                                          "'.", se);
        } catch (ComponentException ce) {
            throw new ProcessingException("Could not lookup for component",
                                          ce);
        } finally {
            if (store!=null) {
                this.manager.release(store);
            }
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {
        return this.lexiconSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return this.lexiconSource.getValidity();
    }

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        if ((this.resolver!=null) && (this.lexiconSource!=null)) {
            this.resolver.release(this.lexiconSource);
            this.lexiconSource = null;
        }

        this.automaton = null;
    }

    /**
     * The dispose operation is called at the end of a
     * components lifecycle.
     */
    public void dispose() {
        if ((this.resolver!=null) && (this.lexiconSource!=null)) {
            this.resolver.release(this.lexiconSource);
            this.lexiconSource = null;
        }

        this.manager = null;
    }

    /**
     * This class represent a entry in a store to cache the lexical automaton.
     */
    public class LexicalAutomatonEntry implements Serializable {
        private SourceValidity validity = null;
        private LexicalAutomaton automaton = null;

        /**
         * Create a new entry.
         *
         * @param automaton Lexical automaton.
         * @param validity Validity of the lexicon file.
         */
        public LexicalAutomatonEntry(LexicalAutomaton automaton,
                                     SourceValidity validity) {
            this.automaton = automaton;
            this.validity = validity;
        }

        /**
         * Return the validity of the lexicon file.
         *
         * @return Validity of the lexicon file.
         */
        public SourceValidity getValidity() {
            return this.validity;
        }

        /**
         * Return the lexical automaton.
         *
         * @return Lexical automaton.
         */
        public LexicalAutomaton getLexicalAutomaton() {
            return this.automaton;
        }

        private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
            out.writeObject(validity);
            out.writeObject(automaton);
        }

        private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
            validity = (SourceValidity)in.readObject();
            automaton = (LexicalAutomaton)in.readObject();
        }
    }
}
