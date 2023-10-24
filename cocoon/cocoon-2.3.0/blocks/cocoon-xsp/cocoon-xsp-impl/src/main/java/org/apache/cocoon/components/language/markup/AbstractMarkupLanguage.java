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
package org.apache.cocoon.components.language.markup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.store.Store;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.HashMap;
import org.apache.cocoon.xml.AbstractXMLPipe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Base implementation of <code>MarkupLanguage</code>. This class uses
 * logicsheets as the only means of code generation. Code generation
 * should be decoupled from this context!!!
 *
 * @version $Id$
 */
public abstract class AbstractMarkupLanguage
        extends AbstractLogEnabled
        implements MarkupLanguage, Serviceable, Configurable, Recyclable, Disposable
{
    /**
     * Name "attr-interpolation" of boolean attribute to enable
     * expression interpolation in attribute values.
     */
    public static final String ATTR_INTERPOLATION = "attr-interpolation";

    /**
     * Name "text-interpolation" of boolean attribute to enable
     * expression interpolation inside text nodes.
     */
    public static final String TEXT_INTERPOLATION = "text-interpolation";

    /** The 'file' URL protocol. */
    private static final String FILE = "file:";

    /** Prefix for cache keys to avoid name clash with the XSLTProcessor */
    private static final String CACHE_PREFIX = "logicsheet:";

    /** This language name */
    protected String name;

    /** The supported language table */
    protected HashMap languages;

    /** The code-generation logicsheet cache */
    protected Store logicsheetCache;

    /** The markup language's namespace uri */
    private String uri;

    /** The markup language's namespace prefix */
    private String prefix;

    /** Are attribute expressions to be expanded? */
    private boolean attrInterpolation;

    /** Are text expressions to be expanded? */
    private boolean textInterpolation;

    /** The service manager */
    protected ServiceManager manager;

    /** The URL factory source resolver used to resolve URIs */
    private SourceResolver resolver;


    /**
     * Stores the list of logicsheets required by the currently
     * loaded program.
     */
    private final LinkedList logicSheetList = new LinkedList();


    /** The default constructor. */
    public AbstractMarkupLanguage() {
        // Initialize language table
        this.languages = new HashMap();
    }

    /**
     * Process additional configuration. Load supported programming
     * language definitions
     *
     * @param conf The language configuration
     * @exception ConfigurationException If an error occurs loading logichseets
     */
    public void configure(Configuration conf) throws ConfigurationException {
        try {
            this.name = conf.getAttribute("name");

            // Cannot use Parameterizable because parameterize() is called
            // after configure(), and <xsp-language> param's are already
            // needed for processing logicsheet definitions.
            Parameters params = Parameters.fromConfiguration(conf);
            this.uri = params.getParameter("uri");
            this.prefix = params.getParameter("prefix", null);
            this.attrInterpolation =
                params.getParameterAsBoolean(ATTR_INTERPOLATION, false);
            this.textInterpolation =
                params.getParameterAsBoolean(TEXT_INTERPOLATION, false);

            // Set up each target-language
            Configuration[] l = conf.getChildren("target-language");
            for (int i = 0; i < l.length; i++) {
                LanguageDescriptor language = new LanguageDescriptor();
                language.setName(l[i].getAttribute("name"));

                // Create & Store the core logicsheet
                Logicsheet logicsheet = createLogicsheet(l[i], false);
                language.setLogicsheet(logicsheet.getSystemId());

                // Set up each built-in logicsheet
                Configuration[] n = l[i].getChildren("builtin-logicsheet");
                for (int j = 0; j < n.length; j++) {
                    // Create & Store the named logicsheets
                    NamedLogicsheet namedLogicsheet =
                        (NamedLogicsheet) createLogicsheet(n[j], true);

                    language.addNamedLogicsheet(
                            namedLogicsheet.getURI(),
                            namedLogicsheet.getPrefix(),
                            namedLogicsheet.getSystemId());
                }

                this.languages.put(language.getName(), language);
            }
        } catch (Exception e) {
          getLogger().warn("Configuration Error: " + e.getMessage(), e);
          throw new ConfigurationException("AbstractMarkupLanguage: "
                                           + e.getMessage(), e);
        }
    }

    /**
     * Abstract out the Logicsheet creation.  Handles both Named and regular logicsheets.
     */
    private Logicsheet createLogicsheet(Configuration configuration, boolean named)
            throws Exception
    {
        Parameters params = Parameters.fromConfiguration(configuration);

        Logicsheet logicsheet;
        if (named) {
            String location = params.getParameter("href", null);
            String uri = params.getParameter("uri", null);
            String prefix = params.getParameter("prefix", null);

            NamedLogicsheet namedLogicsheet
                = new NamedLogicsheet(location, manager,
                                      resolver, getLogicsheetFilter());
            namedLogicsheet.setURI(uri);
            namedLogicsheet.setPrefix(prefix);
            logicsheet = namedLogicsheet;
        } else {
            String location = params.getParameter("core-logicsheet", null);
            logicsheet = new Logicsheet(location, manager,
                                        resolver, getLogicsheetFilter());
        }

        String logicsheetName = logicsheet.getSystemId();
        logicsheetCache.store(CACHE_PREFIX + logicsheetName, logicsheet);

        return logicsheet;
    }

    /**
     * Set the global service manager.
     * @param manager The sitemap-specified service manager
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

        // Initialize logicsheet cache
        this.logicsheetCache = (Store) manager.lookup(Store.TRANSIENT_STORE);

        // Initialize the source resolver
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Recycle this component: clear logic sheet list and dependencies.
     */
    public void recycle() {
        this.logicSheetList.clear();
    }

    /**
     * Release all resources.
     */
    public void dispose() {
        this.manager.release(this.logicsheetCache);
        this.logicsheetCache = null;

        this.manager.release(this.resolver);
        this.resolver = null;
        this.manager = null;
        this.languages.clear();
    }

    /**
     * Return the markup language name. Two markup languages are
     * well-know at the moment: sitemap and xsp.
     *
     * @return The language name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the namespace URI for this language.
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Returns the namespace prefix for this language.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Returns true if expansion of attribute expressions is enabled
     * for this language.
     */
    public boolean hasAttrInterpolation() {
        return this.attrInterpolation;
    }

    /**
     * Returns true if expansion of expressions inside text nodes is enabled
     * for this language.
     */
    public boolean hasTextInterpolation() {
        return this.textInterpolation;
    }

    /**
     * Return the source document's encoding. This can be <code>null</code> for
     * the platform's default encoding. The default implementation returns
     * <code>null</code>, but derived classes may override it if encoding applies to
     * their concrete languages.
     *
     * FIXME: There should be a way to get the
     * XML document's encoding as seen by the parser; unfortunately, this
     * information is not returned by current DOM or SAX parsers...
     *
     * @return The document-specified encoding
     */
    public String getEncoding() {
        return null;
    }

    /**
     * Returns a filter that chains on the fly the requested
     * transformers for source code generation. This method scans the
     * input SAX events for built-in logicsheet declared as namespace
     * attribute on the root element. Derived class should overide
     * this method and the public inner class in order to add more
     * specif action and to build a more specific transformer chain.
     *
     * @param logicsheetMarkupGenerator the logicsheet markup generator
     * @return XMLFilter the filter that build on the fly the transformer chain
     */
    protected TransformerChainBuilderFilter getTransformerChainBuilder(
        LogicsheetCodeGenerator logicsheetMarkupGenerator)
    {
        return new TransformerChainBuilderFilter(logicsheetMarkupGenerator);
    }

    /**
     * Prepare the input source for logicsheet processing and code
     * generation with a preprocess filter.  The return
     * <code>XMLFilter</code> object is the first filter on the
     * transformer chain.  The default implementation does nothing by
     * returning a identity filter, but derived classes should (at
     * least) use the passed programming language to quote
     * <code>Strings</code>
     *
     * @param filename The source filename
     * @param language The target programming language
     * @return The preprocess filter
     */
    protected AbstractXMLPipe getPreprocessFilter(String filename,
                                                  AbstractXMLPipe filter,
                                                  ProgrammingLanguage language)
    {
        // No-op
        return filter;
    }

    /**
     * Add a dependency on an external file to the document for inclusion in
     * generated code. This is used to populate a list of <code>File</code>'s
     * tested for change on each invocation; this information is used to assert whether regeneration is necessary.
     *
     * @param location The file path of the dependent file
     * @see AbstractMarkupLanguage
     * @see org.apache.cocoon.generation.ServerPagesGenerator
     * @see org.apache.cocoon.generation.AbstractServerPage
     */
    protected abstract void addDependency(String location);

    /**
     * Generate source code from the input document for the target
     * <code>ProgrammingLanguage</code>. After preprocessing the input
     * document, this method applies logicsheets in the following
     * order:
     *
     * <ul>
     * <li>User-defined logicsheets</li>
     * <li>Namespace-mapped logicsheets</li>
     * <li>Language-specific logicsheet</li>
     * </ul>
     *
     * @param source The input source
     * @param filename The input document's original filename
     * @param programmingLanguage The target programming language
     * @return The generated source code
     * @exception Exception If an error occurs during code generation
     */
    public String generateCode(Source source,
                               String filename,
                               ProgrammingLanguage programmingLanguage)
            throws Exception {

        String languageName = programmingLanguage.getLanguageName();
        LanguageDescriptor language = (LanguageDescriptor)this.languages.get(languageName);
        if (language == null) {
            throw new IllegalArgumentException("Unsupported programming language: " + languageName);
        }

        // Create code generator
        LogicsheetCodeGenerator codeGenerator = new LogicsheetCodeGenerator();
        codeGenerator.initialize();
        // Set the transformer chain builder filter
        TransformerChainBuilderFilter tranBuilder =
                getTransformerChainBuilder(codeGenerator);
        tranBuilder.setLanguageDescriptor(language);

        // Get the needed preprocess filter
        AbstractXMLPipe preprocessor = getPreprocessFilter(filename, tranBuilder, programmingLanguage);
        return codeGenerator.generateCode(source, preprocessor);
    }

    /**
     * Add logicsheet list to the code generator.
     * @param codeGenerator The code generator
     */
    protected void addLogicsheetsToGenerator(LogicsheetCodeGenerator codeGenerator)
    throws MalformedURLException, IOException, SAXException, ProcessingException {

        if (codeGenerator == null) {
            getLogger().debug("This should never happen: codeGenerator is null");
            throw new SAXException("codeGenerator must never be null.");
        }

        // Walk backwards and remove duplicates.
        LinkedList newLogicSheetList = new LinkedList();
        for(int i = logicSheetList.size()-1; i>=0; i--) {
            Logicsheet logicsheet = (Logicsheet) logicSheetList.get(i);
            if(newLogicSheetList.indexOf(logicsheet) == -1)
                newLogicSheetList.addFirst(logicsheet);
        }

        // Add the list of logicsheets now.
        Iterator iterator = newLogicSheetList.iterator();
        while(iterator.hasNext()) {
            Logicsheet logicsheet = (Logicsheet) iterator.next();
            codeGenerator.addLogicsheet(logicsheet);
        }
    }

    /**
     * Add a logicsheet to the code generator.
     * @param language Target programming language of the logicsheet
     * @param logicsheetLocation Location of the logicsheet to be added
     * @exception MalformedURLException If location is invalid
     * @exception IOException IO Error
     * @exception SAXException Logicsheet parse error
     */
    protected void addLogicsheetToList(LanguageDescriptor language,
                                       String logicsheetLocation)
        throws IOException, SAXException, ProcessingException
    {
        Source inputSource = null;
        String logicsheetName;
        try {
            // Logicsheet is reusable (across multiple XSPs) object,
            // and it is resolved via urlResolver, and not via per-request
            // temporary resolver.
            // Resolve logicsheet location relative to sitemap from where it is used.
            inputSource = this.resolver.resolveURI(logicsheetLocation);
            logicsheetName = inputSource.getURI();
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } finally {
            this.resolver.release( inputSource );
        }

        // Logicsheets are chained by looking at the namespaces on the xsl:stylesheet
        // root node.  To get at these namespaces, the stylesheet must be parsed.
        // Stylesheets are cached that we have only one chance to fill the namespaces.
        // To avoid a race condition, we have to lock the critical section.
        // For maximum concurrency we lock the cache, store if necessary the new,
        // unparsed logicsheet, and then lock the logicsheet for the long-running
        // parse operation.
        
        Logicsheet logicsheet;
        synchronized (logicsheetCache) {
            String cacheKey = CACHE_PREFIX + logicsheetName;
            logicsheet = (Logicsheet)logicsheetCache.get(cacheKey);
            if (logicsheet == null) {
                // Resolver (local) could not be used as it is temporary
                // (per-request) object, yet Logicsheet is being cached and reused
                // across multiple requests. "Global" url-factory-based resolver
                // passed to the Logicsheet.
                logicsheet = new Logicsheet(logicsheetName, manager,
                                            resolver, getLogicsheetFilter());
                logicsheetCache.store(cacheKey, logicsheet);
            }
        }
        
        synchronized (logicsheet) {
            Map namespaces = logicsheet.getNamespaceURIs();
            if (namespaces == null)
                logicsheet.fillNamespaceURIs();
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("addLogicsheetToList: "
                + "name: " + logicsheetName
                + ", location: " + logicsheetLocation
                + ", instance: " + logicsheet);
        }

        if (logicsheetName.startsWith(FILE)) {
            String filename = logicsheetName.substring(FILE.length());
            addDependency(filename);
            getLogger().debug("addLogicsheetToList: "
                + "adding dependency on file " + filename);
        }

        logicSheetList.add(logicsheet);

        Map namespaces = logicsheet.getNamespaceURIs();
        if(!logicsheetLocation.equals(language.getLogicsheet())) {
            if(namespaces.size() > 0) {
                Iterator iter = namespaces.keySet().iterator();
                while(iter.hasNext()) {
                    String namespace = (String) iter.next();
                    String namedLogicsheetName = language.getNamedLogicsheetByURI(namespace);
                    if(namedLogicsheetName!= null
                        && !logicsheetLocation.equals(namedLogicsheetName)) {
                        getLogger().debug("Adding embedded logic sheet for "
                            + namespace + ": " + namedLogicsheetName);
                        // Add embedded logic sheets too.
                        addLogicsheetToList(language, namedLogicsheetName);
                    }
                }
            }
        }
    }

    /**
     * Return the optional filter to prepocess logicsheets.
     */
    protected LogicsheetFilter getLogicsheetFilter() {
        return new LogicsheetFilter();
    }

    //
    // Inner classes
    //

    /** This class holds transient information about a target programming language. */
    protected class LanguageDescriptor {
        /** The progamming language name */
        protected String name;

        /** The progamming language core logicsheet */
        protected String logicsheet;

        /** The list of built-in logicsheets defined for this target language */
        protected HashMap namedLogicsheets;

        /** The default constructor */
        protected LanguageDescriptor() {
            this.namedLogicsheets = new HashMap();
        }

        /**
         * Set the programming language's name
         * @param name The programming language's name
         */
        protected void setName(String name) {
            this.name = name;
        }

        /**
         * Return the programming language's name
         * @return The programming language's name
         */
        protected String getName() {
            return this.name;
        }

        /**
         * Set the programming language's core logichseet location
         * @param logicsheet The programming language's core logichseet location
         */
        protected void setLogicsheet(String logicsheet) {
            this.logicsheet = logicsheet;
        }

        /**
         * Return the programming language's core logichseet location
         * @return The programming language's core logichseet location
         */
        protected String getLogicsheet() {
            return this.logicsheet;
        }

        /**
         * Add a namespace-mapped logicsheet to this language
         * @param prefix The logichseet's namespace prefix
         * @param uri The logichseet's namespace uri
         * @param namedLogicsheet The logichseet's location
         */
        protected void addNamedLogicsheet(String uri, String prefix, String namedLogicsheet) {
            this.namedLogicsheets.put(uri, namedLogicsheet);
        }

        /**
         * Return a namespace-mapped logicsheet given its uri
         * @return The namespace-mapped logicsheet
         */
        protected String getNamedLogicsheetByURI(String uri) {
            return (String)this.namedLogicsheets.get(uri);
        }
    }


    /**
     * An XMLFilter that build the chain of transformers on the fly.
     * Each time a stylesheet is found, a call to the code generator is done
     * to add the new transformer at the end of the current transformer chain.
     */
    public class TransformerChainBuilderFilter extends AbstractXMLPipe {
        /** The markup generator */
        protected LogicsheetCodeGenerator logicsheetMarkupGenerator;

        /** the language description */
        protected LanguageDescriptor language;

        private boolean isRootElem;
        private List startPrefixes;

        /**
         * the constructor depends on the code generator
         * @param logicsheetMarkupGenerator The code generator
         */
        protected TransformerChainBuilderFilter(LogicsheetCodeGenerator logicsheetMarkupGenerator) {
            this.logicsheetMarkupGenerator = logicsheetMarkupGenerator;
        }

        /**
         * This method should be called prior to receiving any SAX event.
         * Indeed the language information is needed to get the core stylesheet.
         * @param language the language in used
         */
        protected void setLanguageDescriptor(LanguageDescriptor language) {
            this.language = language;
        }

        /** @see org.xml.sax.ContentHandler */
        public void startDocument() throws SAXException {
            isRootElem = true;
            startPrefixes = new ArrayList();
        }

        /** @see org.xml.sax.ContentHandler */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (!isRootElem) {
                super.startPrefixMapping(prefix, uri);
            } else {
                // Cache the prefix mapping
                String[] prefixNamingArray = new String[2];
                prefixNamingArray[0] = prefix;
                prefixNamingArray[1] = uri;
                this.startPrefixes.add(prefixNamingArray);
            }
        }

        /** @see org.xml.sax.ContentHandler */
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (isRootElem) {
                isRootElem = false;
                try {
                    // Add namespace-mapped logicsheets
                    int prefixesCount = this.startPrefixes.size();
                    for (int i = 0; i < prefixesCount; i++) {
                        String[] prefixNamingArray = (String[]) this.startPrefixes.get(i);
                        String namedLogicsheetName = this.language.getNamedLogicsheetByURI(prefixNamingArray[1]);
                        if (namedLogicsheetName != null) {
                            AbstractMarkupLanguage.this.addLogicsheetToList(language, namedLogicsheetName);
                        }
                    }

                    // Add the language stylesheet (Always the last one)
                    AbstractMarkupLanguage.this.addLogicsheetToList(language, this.language.getLogicsheet());
                    AbstractMarkupLanguage.this.addLogicsheetsToGenerator(this.logicsheetMarkupGenerator);
                } catch (ProcessingException pe) {
                    throw new SAXException (pe);
                } catch (IOException ioe) {
                    throw new SAXException(ioe);
                }

                // All stylesheet have been configured and correctly setup.
                // Starts firing SAX events, especially the startDocument event,
                // and the cached prefixNaming.
                super.startDocument();
                int prefixesCount = this.startPrefixes.size();
                for (int i = 0; i < prefixesCount; i++) {
                    String[] prefixNamingArray = (String[]) this.startPrefixes.get(i);
                    super.startPrefixMapping(prefixNamingArray[0], prefixNamingArray[1]);
                }
            }
            // Call super method
            super.startElement(namespaceURI, localName, qName, atts);
        }
    }
}
