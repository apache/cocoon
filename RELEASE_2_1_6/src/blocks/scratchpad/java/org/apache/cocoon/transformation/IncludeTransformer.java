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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.MultiSourceValidity;
import org.apache.cocoon.components.thread.RunnableManager;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.EmbeddedXMLPipe;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.NamespacesTable;
import org.apache.cocoon.xml.SaxBuffer;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>A simple transformer including resolvable sources (accessed through
 * Cocoon's {@link SourceResolver} from its input.</p>
 *
 * <p>Inclusion is triggered by the <code>&lt;include ... /&gt;</code> element
 * defined in the <code>http://apache.org/cocoon/include/1.0</code> namespace.</p>
 *
 * <p>Example:</p>
 * <pre>
 * &lt;incl:include xmlns:incl="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include"/&gt;
 * </pre>
 *
 * <p>Parameters to be passed to the included sources can be specified in two ways:
 * the first one is to encode them onto the source itelf, for example:</p>
 *
 * <pre>
 * &lt;incl:include xmlns:incl="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include?paramA=valueA&amp;paramB=valueB"/&gt;
 * </pre>
 *
 * <p>Another approach allows the encoding of parameters to be done automatically by
 * the transformer, so that one can easily pass parameter name or values containing
 * the <code>&</code> (amperstand) or <code>=</code> (equals) character, which are
 * reserved characters in URIs. An example:</p>
 *
 * <pre>
 * &lt;incl:include xmlns:incl="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include"&gt;
 *   &lt;incl:parameter name="firstParameterName" value="firstParameterValue"/&gt;
 *   &lt;incl:parameter name="other&amp;Para=Name" value="other=Para&amp;Value"/&gt;
 * &lt;/incl:include&gt;
 * </pre>
 *
 * <p>An interesting feature of this {@link Transformer} is that it implements the
 * {@link CacheableProcessingComponent} interface and provides full support for
 * caching. In other words, if the input given to this transformer has not changed,
 * and all of the included sources are (cacheable) and still valid, this transformer
 * will not force a pipeline re-generation like the {@link CIncludeTransformer}.</p>
 *
 * <h3>Parallel Processing</h3>
 * <p>Another feature of this {@link Transformer} is that it allows parallel processing
 * of includes. By setting the optional parameter <code>parallel</code> to true,
 * the various included contents are processed (included) in parallel threads rather
 * than in series, in one thread. This parameter can be set in either the transformer
 * definition (to affect all IncludeTransformer instances):</p>
 * <pre>
 *   &lt;parallel&gt;true&lt;/parallel&gt;
 * </pre>
 *
 * <p>or in a pipeline itself (to only affect that instance of the IncludeTransformer):</p>
 * <pre>
 *   &lt;map:parameter name="parallel" value="true"/&gt;
 * </pre>
 * <p>By default, parallel processing is turned off.</p>
 *
 * @cocoon.sitemap.component.name   include
 * @cocoon.sitemap.component.logger sitemap.transformer.include
 *
 * @cocoon.sitemap.component.pooling.min   2
 * @cocoon.sitemap.component.pooling.max  16
 * @cocoon.sitemap.component.pooling.grow  2
 */
public class IncludeTransformer extends AbstractTransformer
                                implements Serviceable, Configurable,
                                           Transformer, CacheableProcessingComponent {

    /** <p>The namespace URI of the elements recognized by this transformer.</p> */
    private static final String NS_URI = "http://apache.org/cocoon/include/1.0";

    /** <p>The name of the element triggering inclusion of sources.</p> */
    private static final String INCLUDE_ELEMENT = "include";

    /** <p>The name of the element defining an included subrequest parameter.</p> */
    private static final String PARAMETER_ELEMENT = "parameter";

    /** <p>The name of the attribute indicating the included source URI.</p> */
    private static final String SRC_ATTRIBUTE = "src";

    /** <p>The name of the attribute indicating the parameter name.</p> */
    private static final String NAME_ATTRIBUTE = "name";

    /** <p>The name of the attribute indicating the parameter name.</p> */
    private static final String VALUE_ATTRIBUTE = "value";

    /** <p>The encoding to use for parameter names and values.</p> */
    private static final String ENCODING = "US-ASCII";

    //
    // Global configuration
    //

    /** <p>The {@link ServiceManager} instance associated with this instance.</p> */
    private ServiceManager m_manager;

    /**
     * <p>Configuration option controlling parallel (in multiple threads)
     * includes processing</p>
     */
    private boolean defaultParallel;

    //
    // Current configuration
    //

    /** <p>The {@link SourceResolver} used to resolve included URIs.</p> */
    private SourceResolver m_resolver;

    /**
     * <p>Pipeline parameter controlling parallel (in multiple threads)
     * includes processing</p>
     */
    private boolean m_parallel;

    //
    // Current state
    //

    /** <p>The {@link SourceValidity} instance associated with this request.</p> */
    private MultiSourceValidity m_validity;

    /** <p>A {@link NamespacesTable} used to filter namespace declarations.</p> */
    private NamespacesTable m_namespaces;

    /** <p>A {@link Map} of the parameters to supply to the included source.</p> */
    private Map x_parameters;

    /** <p>The source to be included declared in an include element.</p> */
    private String x_source;

    /** <p>The current parameter name captured.</p> */
    private String x_parameter;

    /** <p>The current parameter value (as a {@link StringBuffer}).</p> */
    private StringBuffer x_value;

    /**
     * <p>If parallel processing is enabled, then this boolean tells us
     * whether buffering has started yet.</p>
     */
    private boolean m_buffering;

    /**
     * <p>The IncludeBuffer that is used to buffering events if parallel
     * processing is turned on</p>
     * <p>This object is also used as a lock for thread counter m_threads</p>
     */
    private SaxBuffer m_buffer;

    /**
     * <p>Inclusion threads/tasks counter (if executing in parallel)</p>
     */
    private int m_threads;


    /**
     * <p>Create a new {@link IncludeTransformer} instance.</p>
     */
    public IncludeTransformer() {
    }

    /**
     * <p>Setup the {@link ServiceManager} available for this instance.</p>
     *
     * @see Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.m_manager = manager;
    }

    /* (non-Javadoc)
     * @see Configurable#configure(Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        // Get value of parallel node from the configuration - defaults to false
        this.defaultParallel = configuration.getChild("parallel").getValueAsBoolean(false);
    }

    /**
     * <p>Setup this component instance in the context of its pipeline and
     * current request.</p>
     *
     * @see Serviceable#service(ServiceManager)
     */
    public void setup(SourceResolver resolver, Map om, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        // Read parameters
        this.m_parallel = parameters.getParameterAsBoolean("parallel", this.defaultParallel);

        // Init transformer state
        this.m_namespaces = new NamespacesTable();
        this.m_resolver = resolver;
        this.m_validity = null;
        this.x_parameters = null;
        this.x_value = null;
        this.m_buffering = false;
    }

    /**
     * <p>Recycle this component instance.</p>
     *
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.m_namespaces = null;
        this.m_validity = null;
        this.x_parameters = null;
        this.x_value = null;
        this.x_source = null;

        if (this.m_buffering) {
            // Wait for threads to complete and release Sources
            waitForThreads();
            this.m_buffering = false;
            this.m_buffer = null;
        }

        // Resolver can be nulled out when all threads completed processing
        // and released their Sources.
        this.m_resolver = null;

        super.recycle();
    }

    /**
     * <p>Receive notification of the beginning of an XML document.</p>
     *
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument()
    throws SAXException {
        /* Make sure that we have a validity while processing */
        getValidity();

        super.startDocument();
    }

    /**
     * <p>Receive notification of the end of an XML document.</p>
     *
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void endDocument()
    throws SAXException {
        /* Make sure that the validity is "closed" at the end */
        this.m_validity.close();

        /* This is the end of the line - process the buffered events */
        if (this.m_buffering) {
            this.m_buffer.toSAX(super.contentHandler);
        }

        super.endDocument();
    }

    /**
     * <p>Receive notification of the start of a prefix mapping.</p>
     *
     * <p>This transformer will remove all prefix mapping declarations for those
     * prefixes associated with the <code>http://apache.org/cocoon/include/1.0</code>
     * namespace.</p>
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String nsuri)
    throws SAXException {
        if (NS_URI.equals(nsuri)) {
            /* Skipping mapping for the current prefix as it's ours */
            this.m_namespaces.addDeclaration(prefix, nsuri);
        } else {
            /* Map the current prefix, as we don't know it */
            if (this.m_buffering) {
                m_buffer.startPrefixMapping(prefix, nsuri);
            } else {
                super.startPrefixMapping(prefix, nsuri);
            }
        }
    }

    /**
     * <p>Receive notification of the end of a prefix mapping.</p>
     *
     * <p>This transformer will remove all prefix mapping declarations for those
     * prefixes associated with the <code>http://apache.org/cocoon/include/1.0</code>
     * namespace.</p>
     *
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (NS_URI.equals(this.m_namespaces.getUri(prefix))) {
            /* Skipping unmapping for the current prefix as it's ours */
            this.m_namespaces.removeDeclaration(prefix);
        } else {
            /* Unmap the current prefix, as we don't know it */
            if (this.m_buffering) {
                m_buffer.endPrefixMapping(prefix);
            } else {
                super.endPrefixMapping(prefix);
            }
        }
    }

    /**
     * <p>Receive notification of characters.</p>
     *
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char data[], int offset, int length)
    throws SAXException {
        /* If we have a parameter value to add to, let's add this chunk */
        if (this.x_parameter != null) {
            if (this.x_value == null) {
                this.x_value = new StringBuffer();
            }
            this.x_value.append(data, offset, length);

            /* Forward this only if we are not inside an include tag */
        } else if (this.x_source == null) {
            if (this.m_buffering) {
                m_buffer.characters(data, offset, length);
            } else {
                super.characters(data, offset, length);
            }
        }
    }

    /**
     * <p>Receive notification of the start of an element.</p>
     *
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException {
        /* Check the namespace declaration */
        if (NS_URI.equals(uri)) {
            /* Inclusion will not happen here but when we close this tag */
            if (INCLUDE_ELEMENT.equals(localName)) {
                /* Check before we include (we don't want nested stuff) */
                if (this.x_source != null) {
                    throw new SAXException("Invalid include nested in another");
                }

                /* Remember the source we are trying to include */
                this.x_source = atts.getValue(SRC_ATTRIBUTE);
                if ((this.x_source == null) || (this.x_source.length() == 0)) {
                    throw new SAXException("Attribute \"" + SRC_ATTRIBUTE
                                           + "\" not specified");
                }

                /* Whatever list of parameters we got before, we wipe it! */
                this.x_parameters = null;
                this.x_value = null;
                this.x_parameter = null;

                /* Done with this element */
                return;
            }

            /* If this is a parameter, then make sure we prepare. */
            if (PARAMETER_ELEMENT.equals(localName)) {
                /* Check if we are in the right context */
                if (this.x_source == null) {
                    throw new SAXException("Parameter specified outside of include");
                }
                if (this.x_parameter != null) {
                    throw new SAXException("Invalid parameter nested in another");
                }

                /* Get and process the parameter name */
                this.x_parameter = atts.getValue(NAME_ATTRIBUTE);
                if ((this.x_parameter == null) || (this.x_parameter.length() == 0)) {
                    throw new SAXException("Attribute \"" + NAME_ATTRIBUTE
                                           + "\" not specified");
                }

                /* Make some room for the parameter value */
                String value = atts.getValue(VALUE_ATTRIBUTE);
                if (value != null) this.x_value = new StringBuffer(value);

                /* Done with this element */
                return;
            }

            /* We don't have a clue of why we got here (wrong element?) */
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Unknown element \"" + localName + "\"");
            }
            return;
        }

        /* Not our namespace, simply check and pass this element on! */
        if (this.x_source == null) {
            if (this.m_buffering) {
                m_buffer.startElement(uri, localName, qName, atts);
            } else {
                super.startElement(uri, localName, qName, atts);
            }
            return;
        }
        throw new SAXException("Element <" + qName + "/> invalid inside include");
    }

    /**
     * <p>Receive notification of the end of an element.</p>
     *
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName)
    throws SAXException {
        /* Check the namespace declaration */
        if (NS_URI.equals(uri)) {

            /* Inclusion will happen here, when we close the include element */
            if (INCLUDE_ELEMENT.equals(localName)) {

                /* Get the source discovered opening the element and include */
                Source source = null;
                try {
                    if (this.x_parameters != null) {
                        this.x_source = NetUtils.parameterize(this.x_source,
                                                              this.x_parameters);
                    }
                    source = this.m_resolver.resolveURI(this.x_source);
                    if (this.m_validity != null) this.m_validity.addSource(source);

                    /* Check for parallel processing */
                    if (this.m_parallel) {
                        this.m_buffering = true;
                        if (m_buffer == null) {
                            m_buffer = new SaxBuffer();
                        }
                        m_buffer.xmlizable(new IncludeBuffer(source));
                    } else {
                        SourceUtil.toSAX(this.m_manager, source, "text/xml",
                                         new IncludeXMLConsumer(super.contentHandler));
                    }
                } catch (IOException e) {
                    /* Something bad happenend processing a stream */
                    throw new SAXException(e);
                } catch (ProcessingException e) {
                    /* Something bad happened processing a pipeline */
                    throw new SAXException(e);
                } finally {
                    /* Make sure we release the source if we aren't in parellel mode.
                       In parallel mode, the spawned thread releases the source afer processing */
                    if (!this.m_buffering && source != null) {
                        this.m_resolver.release(source);
                    }
                }

                /* We are done with the include element */
                this.x_parameters = null;
                this.x_value = null;
                this.x_parameter = null;
                this.x_source = null;
                return;
            }

            /* Addition of parameters happens here (so that we can capture chars) */
            if (PARAMETER_ELEMENT.equals(localName)) {
                String value = (this.x_value != null? this.x_value.toString(): "");

                /* Store the parameter name and value */
                try {
                    /*
                     * Note: the parameter name and value are URL encoded, so that
                     * weird characters such as "&" or "=" (have special meaning)
                     * are passed through flawlessly.
                     */
                    if (this.x_parameters == null) this.x_parameters = new HashMap(5);
                    this.x_parameters.put(NetUtils.encode(this.x_parameter, ENCODING),
                                          NetUtils.encode(value, ENCODING));
                } catch (UnsupportedEncodingException e) {
                    throw new SAXException("Your platform does not support the " +
                                           ENCODING + " encoding", e);
                }

                /* We are done with this parameter element */
                this.x_value = null;
                this.x_parameter = null;
                return;
            }

        } else {
            /* This is not our namespace, pass the event on! */
            if (this.m_buffering) {
                m_buffer.endElement(uri, localName, qName);
            } else {
                super.endElement(uri, localName, qName);
            }
        }
    }

    /**
     * <p>Return the validity key associated with this transformation.</p>
     *
     * @see CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        /*
         * FIXME: In case of including "cocoon://" or other dynamic sources
         * key has to be dynamic.
         */
        return "I";
    }

    /**
     * <p>Generate (or return) the {@link SourceValidity} instance used to
     * possibly validate cached generations.</p>
     *
     * @return a <b>non null</b> {@link SourceValidity}.
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        if (m_validity == null) {
            m_validity = new MultiSourceValidity(m_resolver, -1);
        }
        return m_validity;
    }

    /**
     * Increment active threads counter
     */
    int incrementThreads() {
        synchronized (m_buffer) {
            return ++m_threads;
        }
    }

    /**
     * Decrement active threads counter
     */
    void decrementThreads() {
        synchronized (m_buffer) {
            if (--m_threads <= 0) {
                m_buffer.notify();
            }
        }
    }

    /**
     * Wait till there is no active threads
     */
    private void waitForThreads() {
        synchronized (m_buffer) {
            if (m_threads > 0) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(m_threads + " threads in progress, waiting");
                }

                try {
                    m_buffer.wait();
                } catch (InterruptedException ignored) { }
                // Don't continue waiting if interrupted.
            }
        }
    }

    /**
     * Buffer for loading included source in separate thread.
     * Streaming of the loaded buffer possible only when source is
     * loaded completely. If loading is not complete, toSAX method
     * will block.
     */
    private class IncludeBuffer extends SaxBuffer implements Runnable {
        private Source source;
        private boolean finished;
        private SAXException e;

        public IncludeBuffer(Source source) {
            this.source = source;

            try {
                final RunnableManager runnableManager = (RunnableManager)m_manager.lookup( RunnableManager.ROLE );
                runnableManager.execute( "daemon", this ); // XXX: GP: Do we really need daemon threads here ?
                m_manager.release( runnableManager );
            } catch (final ServiceException e) {
                // In case we failed to spawn a thread
                this.e = new SAXException(e);
                m_resolver.release(source);
                throw new CascadingRuntimeException( e.getMessage(), e );
            } catch (RuntimeException e) {
                // In case we failed to spawn a thread
                this.e = new SAXException(e);
                m_resolver.release(source);
                throw e;
            }
        }

        /**
         * Stream content of this buffer when it is loaded completely.
         * This method blocks if loading is not complete.
         */
        public void toSAX(ContentHandler contentHandler)
        throws SAXException {
            synchronized (this) {
                if (!this.finished) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) { }
                    // Don't continue waiting if interrupted.
                }
            }

            if (this.e != null) {
                throw this.e;
            }

            super.toSAX(contentHandler);
        }

        /**
         * Load content of the source into this buffer.
         */
        public void run() {
            // Increment active threads counter
            int t = incrementThreads();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Thread #" + t + " loading <" + source.getURI() + ">");
            }

            try {
                SourceUtil.toSAX(m_manager, this.source, "text/xml", new EmbeddedXMLPipe(this));
            } catch (Exception e) {
                if (!(e instanceof SAXException)) {
                    this.e = new SAXException(e);
                } else {
                    this.e = (SAXException) e;
                }
            } finally {
                synchronized (this) {
                    this.finished = true;
                    notify();
                }

                // Release source and decrement active threads counter
                m_resolver.release(this.source);
                decrementThreads();
            }

            if (getLogger().isDebugEnabled()) {
                if (this.e == null) {
                    getLogger().debug("Thread #" + t + " loaded <" + source.getURI() + ">");
                } else {
                    getLogger().debug("Thread #" + t + " failed to load <" + source.getURI() + ">", this.e);
                }
            }
        }
    }
}
