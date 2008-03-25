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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.MultiSourceValidity;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.thread.RunnableManager;
import org.apache.cocoon.transformation.helpers.NOPRecorder;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * <p>A simple transformer including resolvable sources (accessed through
 * Cocoon's {@link SourceResolver}) from its input.</p>
 *
 * <p>Inclusion is triggered by the <code>&lt;include ... /&gt;</code> element
 * defined in the <code>http://apache.org/cocoon/include/1.0</code> namespace.</p>
 *
 * <p>Example:</p>
 * <pre>
 * &lt;i:include xmlns:i="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include"/&gt;
 * </pre>
 *
 * <p>An interesting feature of this {@link Transformer} is that it implements the
 * {@link CacheableProcessingComponent} interface and provides full support for
 * caching. In other words, if the input given to this transformer has not changed,
 * and all of the included sources are (cacheable) and still valid, this transformer
 * will not force a pipeline re-generation like the {@link CIncludeTransformer}.</p>
 *
 *
 * <h3>Relative Source Resolution</h3>
 * <p>Include sources which are specified using relative URI will be resolved
 * relative to the source document location. This is consistent with
 * {@link XIncludeTransformer} behavior, but differs from {@link CIncludeTransformer}.
 *
 * <h3>Root Element Stripping</h3>
 * <p>The root element of included content may be automatically stripped by specifying
 * <code>strip-root="true"</code> on the <code>include</code> element. This is the same
 * functionality as provided by {@link org.apache.cocoon.transformation.CIncludeTransformer}.
 * 
 * <p>Example:</p>
 * <pre>
 * &lt;i:include xmlns:i="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include" strip-root="true"/&gt;
 * </pre>
 *
 * <h3>Parameters Passing</h3>
 * <p>Parameters to be passed to the included sources can be specified in two ways:
 * the first one is to encode them onto the source itelf, for example:</p>
 *
 * <pre>
 * &lt;i:include xmlns:i="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include?paramA=valueA&amp;paramB=valueB"/&gt;
 * </pre>
 *
 * <p>Another approach allows the encoding of parameters to be done automatically by
 * the transformer, so that one can easily pass parameter name or values containing
 * the <code>&amp;</code> (amperstand) or <code>=</code> (equals) character, which are
 * reserved characters in URIs. An example:</p>
 *
 * <pre>
 * &lt;i:include xmlns:i="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include"&gt;
 *   &lt;i:parameter name="firstParameterName" value="firstParameterValue"/&gt;
 *   &lt;i:parameter name="other&amp;Para=Name" value="other=Para&amp;Value"/&gt;
 * &lt;/i:include&gt;
 * </pre>
 *
 *
 * <h3>Fallback Element</h3>
 * <p>IncludeTransformer allows fallback element to be specified within
 * include element. XML content of the fallback element will be included instead
 * of source content if source inclusion caused an exception. Fallback element
 * can have nested include elements. An example:</p>
 *
 * <pre>
 * &lt;i:include xmlns:i="http://apache.org/cocoon/include/1.0"
 *               src="cocoon://path/to/include"&gt;
 *   &lt;i:fallback&gt;
 *     <strong>The data is temporarily unavailable.</strong>
 *     We are sorry for the trouble; please try again later.
 *   &lt;/i:fallback&gt;
 * &lt;/i:include&gt;
 * </pre>
 *
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
 *
 * <h3>Recursive Processing</h3>
 * <p>This {@link Transformer} allows recursive processing of includes.
 * By setting the optional parameter <code>recursive</code> to true,
 * the various included contents are scanned for include elements, and processed
 * in the same manner as incoming XML events. This parameter can be set in either
 * the transformer definition (to affect all IncludeTransformer instances):</p>
 * <pre>
 *   &lt;recursive&gt;true&lt;/recursive&gt;
 * </pre>
 *
 * <p>or in a pipeline itself (to only affect that instance of the IncludeTransformer):</p>
 * <pre>
 *   &lt;map:parameter name="recursive" value="true"/&gt;
 * </pre>
 * <p>This feature is similar to the XInclude processing. By default,
 * recursive processing is turned off.</p>
 *
 *
 * @cocoon.sitemap.component.documentation
 * A simple transformer including resolvable sources (accessed through
 * Cocoon's SourceResolver) from its input.
 * @cocoon.sitemap.component.name include
 * @cocoon.sitemap.component.documentation.caching Yes
 * @cocoon.sitemap.component.pooling.max 16
 * 
 * @version $Id$
 */
public class IncludeTransformer extends AbstractTransformer
                                implements Serviceable, Configurable,
                                           CacheableProcessingComponent {

    /** <p>The namespace URI of the elements recognized by this transformer.</p> */
    private static final String NS_URI = "http://apache.org/cocoon/include/1.0";

    /** <p>The name of the element triggering inclusion of sources.</p> */
    private static final String INCLUDE_ELEMENT = "include";

    /** <p>The name of the element defining a fallback content.</p> */
    private static final String FALLBACK_ELEMENT = "fallback";

    /** <p>The name of the element defining an included subrequest parameter.</p> */
    private static final String PARAMETER_ELEMENT = "parameter";

    /** <p>The name of the attribute indicating the included source URI.</p> */
    private static final String SRC_ATTRIBUTE = "src";

    /** <p>The name of the mime type attribute containing the hint for the {@link org.apache.excalibur.xmlizer.XMLizer}.</p> */
    private static final String MIME_ATTRIBUTE = "mime-type";

    /** <p>The name of the parse attribute indicating type of included source processing: xml or text.</p> */
    private static final String PARSE_ATTRIBUTE = "parse";

    /** <p>The name of the strip-root attribute indicating that the root element of included xml source should be stripped.</p> */
    private static final String STRIP_ROOT_ATTRIBUTE = "strip-root";

    /** <p>The name of the attribute indicating the parameter name.</p> */
    private static final String NAME_ATTRIBUTE = "name";

    /** <p>The name of the attribute indicating the parameter name.</p> */
    private static final String VALUE_ATTRIBUTE = "value";

    /** <p>The encoding to use for parameter names and values.</p> */
    private static final String ENCODING = "US-ASCII";

    //
    // Global configuration
    //

    /** The {@link ServiceManager} instance associated with this instance. */
    protected ServiceManager manager;

    /** Configuration option controlling recursive includes processing */
    private boolean defaultRecursive;

    /** Configuration option controlling parallel (in multiple threads) includes processing */
    private boolean defaultParallel;

    /** Configuration option controlling parallel (in multiple threads) includes processing in the recursive includes */
    private boolean defaultRecursiveParallel;

    /** The name of the thread pool to use (for parallel processing). */
    protected String threadPool;

    /** The default value to be appended to the caching key. */
    private String defaultKey;

    //
    // Current configuration
    //

    /** The {@link SourceResolver} used to resolve included URIs. */
    protected SourceResolver resolver;

    /** The {@link RequestAttributes} used within parallel threads */
    protected RequestAttributes attributes;

    /** The {@link Environment} used within parallel threads */
    protected Environment environment;

    /** The {@link Processor} used within parallel threads */
    private Processor processor;

    /** The value to be appended to the caching key. */
    private String key;

    //
    // Current state
    //

    /** The {@link SourceValidity} instance associated with this request. */
    protected MultiSourceValidity validity;

    /** A {@link NamespacesTable} used to filter namespace declarations. */
    private NamespacesTable namespaces;

    /** The {@link IncludeXMLPipe} which is doing all the work */
    private final IncludeXMLPipe pipe;


    /**
     * <p>Create a new {@link IncludeTransformer} instance.</p>
     */
    public IncludeTransformer() {
        pipe = new IncludeXMLPipe();
    }

    /**
     * <p>Setup the {@link ServiceManager} available for this instance.</p>
     *
     * @see Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see Configurable#configure(Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        /* Read configuration nodes for recursive, parallel, recursive-parallel */
        this.defaultRecursive = configuration.getChild("recursive").getValueAsBoolean(false);
        this.defaultParallel = configuration.getChild("parallel").getValueAsBoolean(false);
        this.defaultRecursiveParallel = configuration.getChild("recursive-parallel").getValueAsBoolean(false);
        /* Read configuration node for thread pool name */
        this.threadPool = configuration.getChild("thread-pool").getValue("default");
        this.defaultKey = configuration.getChild("key").getValue(null);
    }

    /**
     * <p>Setup this component instance in the context of its pipeline and
     * current request.</p>
     *
     * @see Serviceable#service(ServiceManager)
     */
    public void setup(SourceResolver resolver, Map om, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        /* Read sitemap parameters */
        this.pipe.recursive = parameters.getParameterAsBoolean("recursive", this.defaultRecursive);
        this.pipe.parallel = parameters.getParameterAsBoolean("parallel", this.defaultParallel);
        this.pipe.recursiveParallel = parameters.getParameterAsBoolean("recursive-parallel", this.defaultRecursiveParallel);
        this.key = parameters.getParameter("key", this.defaultKey);

        /* Init transformer state */
        if (this.pipe.parallel) {
            this.attributes = RequestContextHolder.getRequestAttributes();
            this.environment = EnvironmentHelper.getCurrentEnvironment();
            this.processor = EnvironmentHelper.getCurrentProcessor();
        }
        this.namespaces = new NamespacesTable();
        this.resolver = resolver;
        this.validity = null;

        // Set root include pipe as consumer.
        // Won't use setter methods here - they are overridden
        super.xmlConsumer = pipe;
        super.contentHandler = pipe;
        super.lexicalHandler = pipe;
    }

    public void setConsumer(XMLConsumer consumer) {
        pipe.setConsumer(consumer);
    }

    public void setContentHandler(ContentHandler handler) {
        pipe.setContentHandler(handler);
    }

    public void setLexicalHandler(LexicalHandler handler) {
        pipe.setLexicalHandler(handler);
    }

    /**
     * <p>Recycle this component instance.</p>
     *
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.namespaces = null;
        this.validity = null;

        /* Make sure all threads completed their work */
        this.pipe.recycle();

        // Resolver can be nulled out when all threads completed processing
        // and released their Sources.
        this.resolver = null;

        this.processor = null;
        this.environment = null;
        this.attributes = null;

        super.recycle();
    }


    /**
     * <p>Receive notification of the beginning of an XML document.</p>
     *
     * @see ContentHandler#startDocument
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
     * @see ContentHandler#startDocument()
     */
    public void endDocument()
    throws SAXException {
        /* Make sure that the validity is "closed" at the end */
        this.validity.close();

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
            this.namespaces.addDeclaration(prefix, nsuri);
        } else {
            /* Map the current prefix, as we don't know it */
            super.startPrefixMapping(prefix, nsuri);
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
        if (NS_URI.equals(this.namespaces.getUri(prefix))) {
            /* Skipping unmapping for the current prefix as it's ours */
            this.namespaces.removeDeclaration(prefix);
        } else {
            /* Unmap the current prefix, as we don't know it */
            super.endPrefixMapping(prefix);
        }
    }

    /**
     * <p>Return the caching key associated with this transformation.</p>
     *
     * <p>When including <code>cocoon://</code> sources with dynamic
     * content depending on environment (request parameters, session attributes,
     * etc), it makes sense to provide such environment values to the transformer
     * to be included into the key using <code>key</code> sitemap parameter.</p>
     *
     * @see CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        /*
         * In case of including "cocoon://" or other dynamic sources key
         * ideally has to include ProcessingPipelineKey of the included
         * "cocoon://" sources, but it's not possible as at this time
         * we don't know yet which sources will get included into the
         * response.
         *
         * Hence, javadoc recommends providing key using sitemap parameter.
         */
        return key == null? "I": "I" + key;
    }

    /**
     * <p>Generate (or return) the {@link SourceValidity} instance used to
     * possibly validate cached generations.</p>
     *
     * @return a <b>non null</b> {@link SourceValidity}.
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        if (validity == null) {
            validity = new MultiSourceValidity(resolver, -1);
        }
        return validity;
    }

    /**
     * Description of the include element
     */
    private class IncludeElement {
        /** Parameter controlling recursive includes processing */
        private boolean recursive;

        /** Parameter controlling parallel (in multiple threads) includes processing */
        private boolean parallel;

        /** Parameter controlling parallel (in multiple threads) includes processing in recursive includes */
        private boolean recursiveParallel;

        /** The source base URI. */
        private String base;

        /** The source URI to be included declared in an src attribute of the include element. */
        protected String source;

        /** The flag indicating whether source content has to be parsed into XML or included as text. */
        protected boolean parse;

        /** The mime type hint to the {@link org.apache.excalibur.xmlizer.XMLizer} when parsing the source content. */
        protected String mimeType;

        /** The flag indicating whether to strip the root element. */
        protected boolean stripRoot;

        /** The buffer collecting fallback content. */
        protected SaxBuffer fallback;

        /** A {@link Map} of the parameters to supply to the included source. */
        protected Map parameters;

        /** The current parameter name captured. */
        protected String parameter;

        /** The current parameter value (as a {@link StringBuffer}). */
        protected StringBuffer value;

        /** Create include element */
        private IncludeElement(String base, boolean parallel, boolean recursive, boolean recursiveParallel) {
            this.base = base;
            this.parallel = parallel;
            this.recursive = recursive;
            this.recursiveParallel = recursiveParallel;
        }

        /**
         * Process element into the buffer.
         * This can not be shared buffer, as it must be cleaned if fallback is invoked.
         */
        public void process(SaxBuffer buffer)
        throws SAXException {
            try {
                process0(buffer, buffer);
            } catch (SAXException e) {
                buffer.recycle();
                if (this.fallback == null) {
                    throw e;
                }

                if (getLogger().isInfoEnabled()) {
                    getLogger().info("Failed to load <" + this.source + ">, using fallback.", e);
                }
                // Stream fallback through IncludeXMLPipe
                this.fallback.toSAX(new IncludeXMLPipe(buffer, buffer,
                                                       recursive, recursiveParallel && parallel, recursiveParallel));
            }
        }

        /** Load URI into the provided handlers, process fallback */
        public void process(ContentHandler contentHandler, LexicalHandler lexicalHandler)
        throws SAXException {
            try {
                if (this.fallback != null) {
                    SaxBuffer buffer = new SaxBuffer();
                    process(buffer);
                    buffer.toSAX(contentHandler);
                } else {
                    process0(contentHandler, lexicalHandler);
                }
            } catch (SAXException e) {
                // source must not be cached if an error occurs
                validity = null;
                throw e;
            }
        }

        /** Load URI into the provided handlers. */
        private void process0(ContentHandler contentHandler, LexicalHandler lexicalHandler)
        throws SAXException {
            Source source = null;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Loading <" + this.source + ">");
            }

            // Setup this thread's environment
            try {
                if (base != null) {
                    source = resolver.resolveURI(this.source, base, null);
                } else {
                    source = resolver.resolveURI(this.source);
                }
                if (validity != null) {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized (validity) {
                        validity.addSource(source);
                    }
                }

                // Include source
                if (this.parse && recursive) {
                    SourceUtil.toSAX(manager, source, this.mimeType,
                                     new IncludeXMLPipe(contentHandler, lexicalHandler,
                                                        recursive, recursiveParallel && parallel, recursiveParallel));
                } else if (this.parse) {
                    IncludeXMLConsumer includeXMLConsumer = new IncludeXMLConsumer(contentHandler, lexicalHandler);
                    includeXMLConsumer.setIgnoreRootElement(stripRoot);
                    SourceUtil.toSAX(manager, source, this.mimeType, includeXMLConsumer);
                } else {
                    SourceUtil.toCharacters(source, "utf-8", contentHandler);
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Loaded <" + this.source + ">");
                }
            } catch (SAXException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Failed to load <" + this.source + ">", e);
                }

                throw e;

            } catch (ProcessingException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Failed to load <" + this.source + ">", e);
                }

                throw new SAXException(e);

            } catch (IOException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Failed to load <" + this.source + ">", e);
                }

                throw new SAXException(e);

            } finally {
                if (source != null) {
                    resolver.release(source);
                }
            }
        }
    }

    /**
     * XML pipe reacting on the elements in the include namespace.
     */
    private class IncludeXMLPipe extends AbstractXMLPipe {

        //
        // Configuration
        //

        /** Indicates whether this is root include pipe (owned by transformer) or a nested one */
        private final boolean root;

        /** Parameter controlling recursive includes processing */
        private boolean recursive;

        /** Parameter controlling parallel (in multiple threads) includes processing */
        private boolean parallel;

        /** Parameter controlling parallel (in multiple threads) includes processing in recursive includes */
        private boolean recursiveParallel;

        //
        // Current state
        //

        /** Stack of {@link XMLConsumer}s */
        private final Stack consumers = new Stack();

        /** Current depth of nested elements in the include namespace */
        private int depth;

        /** Base URI used for the resolving included sources */
        private String base;

        /** The source to be included declared in an include element. */
        private IncludeElement element;

        /** If parallel processing is enabled, then this boolean tells us whether buffering has started yet. */
        private boolean buffering;

        /**
         * <p>The IncludeBuffer that is used to buffering events if parallel
         * processing is turned on.</p>
         * <p>This object is also used as a lock for the thread counter <code>threads</code>.</p>
         */
        private SaxBuffer buffer;

        /** Inclusion threads/tasks counter (if executing in parallel) */
        private int threads;

        /**
         * <p>Create a new {@link IncludeXMLPipe} instance.</p>
         */
        public IncludeXMLPipe() {
            root = true;
        }

        /**
         * <p>Create a new {@link IncludeXMLPipe} instance.</p>
         */
        public IncludeXMLPipe(ContentHandler contentHandler, LexicalHandler lexicalHandler,
                              boolean recursive, boolean parallel, boolean recursiveParallel) {
            root = false;
            setContentHandler(contentHandler);
            setLexicalHandler(lexicalHandler);
            this.recursive = recursive;
            this.parallel = parallel;
            this.recursiveParallel = recursiveParallel;
        }

        /**
         * Finish processing.
         */
        public void recycle() {
            if (this.buffering) {
                // Wait for threads to complete and release Sources
                waitForThreads();
                this.buffering = false;
                this.buffer = null;
            }
            this.threads = 0;

            this.consumers.clear();
            this.base = null;
            this.element = null;

            super.recycle();
        }

        /** Push current consumer into the stack, replace with new one */
        private void push(XMLConsumer consumer) {
            this.consumers.push(new Object[]{ super.xmlConsumer, super.contentHandler, super.lexicalHandler });
            setConsumer(consumer);
        }

        /** Pop consumer from the stack, replace current one */
        private void pop() {
            Object[] consumer = (Object[]) this.consumers.pop();
            if (consumer[0] != null) {
                setConsumer((XMLConsumer) consumer[0]);
            } else {
                setContentHandler((ContentHandler) consumer[1]);
                setLexicalHandler((LexicalHandler) consumer[2]);
            }
        }

        //
        // ContentHandler interface
        //

        public void setDocumentLocator(Locator locator) {
            try {
                if (locator != null && locator.getSystemId() != null) {
                    Source source = resolver.resolveURI(locator.getSystemId());
                    try {
                        base = source.getURI();
                    } finally {
                        resolver.release(source);
                    }
                }
            } catch (IOException e) {
                getLogger().warn("Unable to resolve document base URI: <" + locator.getSystemId() + ">");
            }

            super.setDocumentLocator(locator);
        }

        /**
         * <p>Receive notification of the beginning of an XML document.</p>
         * @see ContentHandler#startDocument
         */
        public void startDocument() throws SAXException {
            if (root) {
                super.startDocument();
            }
        }

        /**
         * <p>Receive notification of the end of an XML document.</p>
         * @see ContentHandler#startDocument
         */
        public void endDocument() throws SAXException {
            /* This is the end of the line - process the buffered events */
            if (this.buffering) {
                pop();
                this.buffer.toSAX(super.contentHandler);
            }

            if (root) {
                super.endDocument();
            }
        }

        /**
         * <p>Receive notification of the start of an element.</p>
         * @see ContentHandler#startElement
         */
        public void startElement(String uri, String localName, String qName, Attributes atts)
        throws SAXException {

            /* Check the namespace declaration */
            if (NS_URI.equals(uri)) {

                /*
                 * Depth 0: Outside of any include tag
                 * Depth 1: Must be Inside <include> tag
                 * Depth 2: Inside <fallback> tag
                 */
                depth++;

                /* Inclusion will not happen here but when we close this tag */
                if (INCLUDE_ELEMENT.equals(localName) && depth == 1) {
                    /* Check before we include (we don't want nested stuff) */
                    if (element != null) {
                        throw new SAXException("Element " + INCLUDE_ELEMENT + " nested in another one.");
                    }
                    element = new IncludeElement(this.base, this.parallel, this.recursive, this.recursiveParallel);

                    /* Remember the source we are trying to include */
                    element.source = atts.getValue(SRC_ATTRIBUTE);
                    if (element.source == null || element.source.length() == 0) {
                        throw new SAXException("Attribute '" + SRC_ATTRIBUTE + "' empty or missing.");
                    }

                    /* Defaults to 'xml' */
                    String value = atts.getValue(PARSE_ATTRIBUTE);
                    if (value == null || value.equals("xml")) {
                        element.parse = true;
                    } else if (value.equals("text")) {
                        element.parse = false;
                    } else {
                        throw new SAXException("Attribute '" + PARSE_ATTRIBUTE + "' has invalid value.");
                    }

                    /* Defaults to 'text/xml' */
                    element.mimeType = atts.getValue(MIME_ATTRIBUTE);
                    if (!element.parse && element.mimeType != null) {
                        throw new SAXException("Attribute '" + MIME_ATTRIBUTE + "' can't be specified for text inclusions.");
                    } else if (element.mimeType == null) {
                        element.mimeType = "text/xml";
                    }

                    /* Defaults to false */
                    String stripRoot = atts.getValue(STRIP_ROOT_ATTRIBUTE);
                    element.stripRoot = Boolean.valueOf(stripRoot).booleanValue();

                    /* Ignore nested content */
                    push(new NOPRecorder(){});

                    /* Done with this element */
                    return;
                }

                /* If this is a fallback parameter, capture its content. */
                if (FALLBACK_ELEMENT.equals(localName) && depth == 2) {
                    /* Check if we are in the right context */
                    if (element == null) {
                        throw new SAXException("Element " + FALLBACK_ELEMENT + " specified outside of " + INCLUDE_ELEMENT + ".");
                    }
                    if (element.fallback != null) {
                        throw new SAXException("Duplicate element " + FALLBACK_ELEMENT + ".");
                    }

                    /* Buffer fallback content */
                    push(element.fallback = new SaxBuffer());

                    /* Done with this element */
                    return;
                }

                /* If this is a parameter, then make sure we prepare. */
                if (PARAMETER_ELEMENT.equals(localName) && depth == 2) {
                    /* Check if we are in the right context */
                    if (element == null) {
                        throw new SAXException("Element " + PARAMETER_ELEMENT + " specified outside of " + INCLUDE_ELEMENT + ".");
                    }
                    if (element.parameter != null) {
                        throw new SAXException("Element " + PARAMETER_ELEMENT + " nested in another one.");
                    }

                    /* Get and process the parameter name */
                    element.parameter = atts.getValue(NAME_ATTRIBUTE);
                    if (element.parameter == null || element.parameter.length() == 0) {
                        throw new SAXException("Attribute '" + NAME_ATTRIBUTE + "' empty or missing.");
                    }

                    /* Make some room for the parameter value */
                    String value = atts.getValue(VALUE_ATTRIBUTE);
                    if (value != null) {
                        element.value = new StringBuffer(value);
                    }

                    /* Done with this element */
                    return;
                }

                /* We don't have a clue of why we got here (wrong element?) */
                if (depth < 2) {
                    throw new SAXException("Element '" + localName + "' was not expected here.");
                }
            }

            super.startElement(uri, localName, qName, atts);
        }

        /**
         * <p>Receive notification of the end of an element.</p>
         * @see ContentHandler#endElement
         */
        public void endElement(String uri, String localName, String qName)
        throws SAXException {
            /* Check the namespace declaration */
            if (NS_URI.equals(uri)) {

                /*
                 * Depth 0: Outside of any include tag
                 * Depth 1: Inside <include> tag
                 * Depth 2: Inside <fallback> tag
                 */
                depth--;

                /* Inclusion will happen here, when we close the include element */
                if (INCLUDE_ELEMENT.equals(localName) && depth == 0) {
                    /* End ignoring nested content */
                    pop();

                    /* Get the source discovered opening the element and include */
                    if (element.parameters != null) {
                        element.source = NetUtils.parameterize(element.source,
                                                               element.parameters);
                        element.parameters = null;
                    }

                    /* Check for parallel processing */
                    if (this.parallel) {
                        if (!this.buffering) {
                            this.buffering = true;
                            buffer = new SaxBuffer();
                            push(buffer);
                        }

                        /* Process include element in separate thread */
                        buffer.xmlizable(new IncludeBuffer(element));

                    } else {
                        /* Process include element inline */
                        element.process(super.contentHandler, super.lexicalHandler);
                    }

                    /* We are done with this include element */
                    this.element = null;
                    return;
                }

                if (FALLBACK_ELEMENT.equals(localName) && depth == 1) {
                    /* End buffering fallback content */
                    pop();

                    /* Done with this element */
                    return;
                }

                /* Addition of parameters happens here (so that we can capture chars) */
                if (PARAMETER_ELEMENT.equals(localName) && depth == 1) {
                    String value = (element.value != null? element.value.toString(): "");

                    /* Store the parameter name and value */
                    try {
                        /*
                         * Note: the parameter name and value are URL encoded, so that
                         * weird characters such as "&" or "=" (have special meaning)
                         * are passed through flawlessly.
                         */
                        if (element.parameters == null) {
                            element.parameters = new HashMap(5);
                        }
                        element.parameters.put(NetUtils.encode(element.parameter, ENCODING),
                                               NetUtils.encode(value, ENCODING));
                    } catch (UnsupportedEncodingException e) {
                        throw new SAXException("Your platform does not support the " +
                                               ENCODING + " encoding", e);
                    }

                    /* We are done with this parameter element */
                    element.value = null;
                    element.parameter = null;
                    return;
                }
            }

            /* This is not our namespace, pass the event on! */
            super.endElement(uri, localName, qName);
        }

        /**
         * <p>Receive notification of characters.</p>
         * @see ContentHandler#characters
         */
        public void characters(char[] data, int offset, int length)
        throws SAXException {
            if (element != null && element.parameter != null) {
                /* If we have a parameter value to add to, let's add this chunk */
                if (element.value == null) {
                    element.value = new StringBuffer();
                }
                element.value.append(data, offset, length);
                return;
            }

            /* Forward */
            super.characters(data, offset, length);
        }

        //
        // Thread management
        //

        /**
         * Increment active threads counter
         */
        int incrementThreads() {
            //noinspection SynchronizeOnNonFinalField
            synchronized (buffer) {
                return ++threads;
            }
        }

        /**
         * Decrement active threads counter
         */
        void decrementThreads() {
            //noinspection SynchronizeOnNonFinalField
            synchronized (buffer) {
                if (--threads <= 0) {
                    buffer.notify();
                }
            }
        }

        /**
         * Wait till there is no active threads
         */
        private void waitForThreads() {
            //noinspection SynchronizeOnNonFinalField
            synchronized (buffer) {
                if (threads > 0) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(threads + " threads in progress, waiting");
                    }

                    try {
                        buffer.wait();
                    } catch (InterruptedException e) { /* ignored */ }
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
        private class IncludeBuffer extends SaxBuffer
                                    implements Runnable {

            private IncludeElement element;
            private int thread;
            private boolean finished;
            private SAXException e;


            public IncludeBuffer(IncludeElement element) {
                this.element = element;

                RunnableManager runnable = null;
                try {
                    runnable = (RunnableManager) IncludeTransformer.this.manager.lookup(RunnableManager.ROLE);
                    runnable.execute(IncludeTransformer.this.threadPool, this);
                } catch (final ServiceException e) {
                    // In case we failed to spawn a thread
                    throw new CascadingRuntimeException(e.getMessage(), e);
                } finally {
                    IncludeTransformer.this.manager.release(runnable);
                }

                // Increment active threads counter
                this.thread = incrementThreads();
            }

            /**
             * Load content of the source into this buffer.
             */
            public void run() {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Thread #" + thread + " loading <" + element.source + ">");
                    }

                    // Setup this thread's environment
                    RequestContextHolder.setRequestAttributes(attributes);
                    EnvironmentHelper.enterProcessor(processor, environment);
                    try {
                        element.process(this);

                    } catch (SAXException e) {
                        this.e = e;

                    } finally {
                        EnvironmentHelper.leaveProcessor();
                        RequestContextHolder.resetRequestAttributes();
                    }
                } catch (ProcessingException e) {
                    /* Unable to set thread's environment */
                    this.e = new SAXException(e);

                } finally {
                    synchronized (this) {
                        this.finished = true;
                        notify();
                    }

                    // Make sure that active threads counter is decremented
                    decrementThreads();
                }

                if (getLogger().isDebugEnabled()) {
                    if (this.e == null) {
                        getLogger().debug("Thread #" + thread + " loaded <" + element.source + ">");
                    } else {
                        getLogger().debug("Thread #" + thread + " failed to load <" + element.source + ">", this.e);
                    }
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
                        } catch (InterruptedException e) { /* ignored */ }
                        // Don't continue waiting if interrupted.
                    }
                }

                if (this.e != null) {
                    throw this.e;
                }

                super.toSAX(contentHandler);
            }
        }
    }
}
