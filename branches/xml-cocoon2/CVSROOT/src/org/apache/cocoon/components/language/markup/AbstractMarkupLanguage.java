/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.avalon.Composer;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Parameters;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.components.store.MemoryStore;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;

import org.apache.log.LogKit;
import org.apache.log.Logger;

/**
 * Base implementation of <code>MarkupLanguage</code>. This class uses
 * logicsheets as the only means of code generation. Code generation should
 * be decoupled from this context!!!
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2000-12-11 15:05:55 $
 */
public abstract class AbstractMarkupLanguage
     implements MarkupLanguage, Composer, Configurable
{
    /**
     * The logger for AbstractMarkupLanguage
     */
    protected Logger log = LogKit.getLoggerFor("cocoon");
    /**
    * The supported language table
    */
    protected Hashtable languages;

    /**
    * The in-memory code-generation logicsheet cache
    */
    protected MemoryStore logicsheetCache;

    /**
    * The markup language's namespace uri
    */
    protected String uri;

    /**
    * The markup language's namespace prefix
    */
    protected String prefix;

    /** The component manager */
    protected ComponentManager manager;

    /**
    * Set the global component manager.
    *
    * @param manager The sitemap-specified component manager
    */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
    * The default constructor.
    */
    public AbstractMarkupLanguage() throws SAXException, IOException {
        // Initialize language table
        this.languages = new Hashtable();

        // Initialize logicsheet cache
        this.logicsheetCache = new MemoryStore();
    }

    /**
    * Initialize the (required) markup language namespace definition.
    *
    * @param params The sitemap-supplied parameters
    * @exception Exception Not actually thrown
    */
    protected void setParameters(Parameters params) throws Exception
    {
        this.uri = params.getParameter("uri",null);
        this.prefix = params.getParameter("prefix", null);
    }

    /**
    * Process additional configuration. Load supported programming language
    * definitions
    *
    * @param conf The language configuration
    * @exception ConfigurationException If an error occurs loading logichseets
    */
    public void configure(Configuration conf)
        throws ConfigurationException
    {
        try {
            Iterator l = conf.getChildren("target-language");
            while (l.hasNext()) {
                Configuration lc = (Configuration) l.next();

                LanguageDescriptor language = new LanguageDescriptor();
                language.setName(lc.getAttribute("name"));

                Parameters lcp = Parameters.fromConfiguration(lc);
                String logicsheetLocation =
                            lcp.getParameter("core-logicsheet",null);

                URL logicsheetURL = NetUtils.getURL(logicsheetLocation);
                String logicsheetName = logicsheetURL.toExternalForm();
                // FIXME (SSA) should be abstracted
                Logicsheet logicsheet = new Logicsheet();
                logicsheet.setInputSource(new InputSource(logicsheetURL.toString()));
                CachedURL entry = new CachedURL(logicsheetURL, logicsheet);
                this.logicsheetCache.store(logicsheetName, entry);
                language.setLogicsheet(logicsheetName);

                Iterator n = lc.getChildren("builtin-logicsheet");
                while (n.hasNext()) {
                    Configuration nc = (Configuration) n.next();
                    Parameters ncp = Parameters.fromConfiguration(nc);

                    String namedLogicsheetPrefix = ncp.getParameter("prefix", null);
                    String namedLogicsheetUri = ncp.getParameter("uri", null);
                    String namedLogicsheetLocation = ncp.getParameter("href", null);

                    // FIXME: This is repetitive; add method for both cases
                    URL namedLogicsheetURL = NetUtils.getURL(namedLogicsheetLocation);
                    String namedLogicsheetName = namedLogicsheetURL.toExternalForm();
                    NamedLogicsheet namedLogicsheet = new NamedLogicsheet();
                    namedLogicsheet.setInputSource(
                        new InputSource(namedLogicsheetURL.toString())
                    );
                    namedLogicsheet.setPrefix(namedLogicsheetPrefix);
                    namedLogicsheet.setUri(namedLogicsheetUri);
                    CachedURL namedEntry =
                    new CachedURL(namedLogicsheetURL, namedLogicsheet);
                    this.logicsheetCache.store(namedLogicsheetName, namedEntry);
                    language.addNamedLogicsheet(
                        namedLogicsheetPrefix, namedLogicsheetName
                    );
                }

            this.languages.put(language.getName(), language);
            }
        } catch (Exception e) {
            log.warn("Configuration Error: " + e.getMessage(), e);
            throw new ConfigurationException("AbstractMarkupLanguage: " + e.getMessage(), e);
        }
    }

    /**
    * Return the source document's encoding. This can be <code>null</code> for
    * the platform's default encoding. The default implementation returns
    * <code>null, but derived classes may override it if encoding applies to
    * their concrete languages. FIXME: There should be a way to get the
    * XML document's encoding as seen by the parser; unfortunately, this
    * information is not returned by current DOM or SAX parsers...
    *
    * @return The document-specified encoding
    */
    public String getEncoding() {
        return null;
    }

    /**
    * Returns a filter that chains on the fly the requested transformers for source
    * code generation. This method scans the input SAX events for built-in logicsheet
    * declared as namespace attribute on the root element.
    *
    * Derived class should overide this method and the public inner class in
    * order to add more specif action and to build a more specific transformer
    * chain.
    *
    * @param logicsheetMarkupGenerator the logicsheet markup generator
    * @param resolver the entity resolver
    * @return XMLFilter the filter that build on the fly the transformer chain
    */
    protected TransformerChainBuilderFilter getTranformerChainBuilder (
                                LogicsheetCodeGenerator logicsheetMarkupGenerator,
                                EntityResolver resolver) {
        return new TransformerChainBuilderFilter(logicsheetMarkupGenerator, resolver);
    }

    /**
    * Prepare the input source for logicsheet processing and code generation
    * with a preprocess filter.
    * The return <code>XMLFilter</code> object is the first filter on the
    * transformer chain.
    *
    * The default implementation does nothing by returning a identity filter, but
    * derived classes should (at least) use the passed programming language to
    * quote <code>Strings</code>
    *
    * @param filename The source filename
    * @param language The target programming language
    * @return The preprocess filter
    */
    protected XMLFilter getPreprocessFilter(
        String filename, ProgrammingLanguage language
    )
    {
        return new XMLFilterImpl();
    }

    /**
    * Add a dependency on an external file to the document for inclusion in
    * generated code. This is used by <code>AbstractServerPagesGenerator</code>
    * to populate a list of <code>File</code>'s tested for change on each
    * invocation; this information, in turn, is used by
    * <code>ServerPagesLoaderImpl</code> to assert whether regeneration is
    * necessary.
    *
    * @param PARAM_NAME Param description
    * @return the value
    * @exception EXCEPTION_NAME If an error occurs
    * @see ServerPages <code>AbstractServerPagesGenerator</code>
    *      and <code>ServerPagesLoaderImpl</code>
    */
    protected abstract void addDependency(String location);

    /**
    * Generate source code from the input document for the target
    * <code>ProgrammingLanguage</code>. After preprocessing the input document,
    * this method applies logicsheets in the following order:
    * <ul>
    *   <li>User-defined logicsheets</li>
    *   <li>Namespace-mapped logicsheets</li>
    *   <li>Language-specific logicsheet</li>
    * </ul>
    *
    * @param input The input source
    * @param filename The input document's original filename
    * @param programmingLanguage The target programming language
    * @return The generated source code
    * @exception Exception If an error occurs during code generation
    */
    public String generateCode(
        InputSource input, String filename, ProgrammingLanguage programmingLanguage,
        EntityResolver resolver
    ) throws Exception {
        String languageName = programmingLanguage.getLanguageName();
        LanguageDescriptor language =
            (LanguageDescriptor) this.languages.get(languageName);

        if (language == null) {
            throw new IllegalArgumentException(
                "Unsupported programming language: " + languageName
            );
        }

        // Create a XMLReader
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        // Get the needed preprocess filter
        XMLFilter preprocessFilter = this.getPreprocessFilter(filename, programmingLanguage);
        preprocessFilter.setParent(reader);

        // Create code generator
        LogicsheetCodeGenerator codeGenerator = new LogicsheetCodeGenerator();

        // set the transformer chain builder filter
        TransformerChainBuilderFilter tranBuilder = getTranformerChainBuilder(codeGenerator, resolver);
        tranBuilder.setLanguageDescriptor(language);
        tranBuilder.setParent(preprocessFilter);

        return codeGenerator.generateCode(tranBuilder, input, filename);
    }

    /**
    * Add a logicsheet to the code generator.
    *
    * @param codeGenerator The code generator
    * @param logicsheetLocation Location of the logicsheet to be added
    * @param document The input document
    * @exception MalformedURLException If location is invalid
    * @exception IOException IO Error
    * @exception SAXException Logicsheet parse error
    */
    protected void addLogicsheet(
        LogicsheetCodeGenerator codeGenerator,
        String logicsheetLocation,
        EntityResolver entityResolver
    ) throws MalformedURLException, IOException, SAXException
    {
        String systemId = null;
        InputSource inputSource = null;

        if (codeGenerator == null) {
            log.debug("This should never happen: codeGenerator is null");
            throw new SAXException("codeGenerator must never be null.");
        }

        if (logicsheetLocation.indexOf(":/") < 0) { // Relative to Cocoon root
            inputSource = entityResolver.resolveEntity(null, logicsheetLocation);
            systemId = inputSource.getSystemId();
        } else { // Fully resolved URL
            systemId = logicsheetLocation;
            inputSource = new InputSource(systemId);
        }

        URL url = new URL(systemId);
        String logicsheetName = url.toExternalForm();

        CachedURL entry = (CachedURL) this.logicsheetCache.get(logicsheetName);

        Logicsheet logicsheet = null;

        if (entry == null) {
            logicsheet = new Logicsheet();
            logicsheet.setInputSource(inputSource);
            entry = new CachedURL(url, logicsheet);
            this.logicsheetCache.store(logicsheetName, entry);
        }

        logicsheet = entry.getLogicsheet();

        if (entry.hasChanged()) {
            logicsheet.setInputSource(inputSource);
        }

        if (entry.isFile()) {
            this.addDependency(IOUtils.getFullFilename(entry.getFile()));
        }

        codeGenerator.addLogicsheet(logicsheet);
    }

//
// Inner classes
//

    /**
    * This class holds transient information about a target programming
    * language.
    *
    */
    protected class LanguageDescriptor {
        /**
         * The progamming language name
         */
        protected String name;

        /**
         * The progamming language core logicsheet
         */
        protected String logicsheet;

        /**
         * The list of built-in logicsheets defined for this target language
         */
        protected Hashtable namedLogicsheets;

        /**
         * The default constructor
         */
        protected LanguageDescriptor() {
            this.namedLogicsheets = new Hashtable();
        }

        /**
         * Set the programming language's name
         *
         * @param name The programming language's name
         */
        protected void setName(String name) {
            this.name = name;
        }

        /**
        * Return the programming language's name
        *
        * @return The programming language's name
        */
        protected String getName() {
            return this.name;
        }

        /**
         * Set the programming language's core logichseet location
         *
         * @param logicsheet The programming language's core logichseet location
         */
        protected void setLogicsheet(String logicsheet) {
            this.logicsheet = logicsheet;
        }

        /**
         * Return the programming language's core logichseet location
         *
         * @return The programming language's core logichseet location
         */
        protected String getLogicsheet() {
            return this.logicsheet;
        }

        /**
         * Add a namespace-mapped logicsheet to this language
         *
         * @param prefix The logichseet's namespace prefix
         * @param uri The logichseet's namespace uri
         * @param namedLogicsheet The logichseet's location
         */
        protected void addNamedLogicsheet(String prefix, String namedLogicsheet) {
            this.namedLogicsheets.put(
                prefix,
                namedLogicsheet
            );
        }

        /**
         * Return a namespace-mapped logicsheet given its name
         *
         * @return The namespace-mapped logicsheet
         */
        protected String getNamedLogicsheet(String prefix) {
            return (String) this.namedLogicsheets.get(prefix);
        }
    }

    /**
    * This class holds a cached URL entry associated with a logicsheet
    *
    */
    protected class CachedURL {
        /**
         * The logicsheet URL
         */
        protected URL url;
        /**
         * The logicsheet's <code>File</code> if it's actually a file.
         * This is used to provide last modification information not
         * otherwise available for URL's in Java :-(
         */
        protected File file;
        /**
         * The cached logicsheet
         */
        protected Logicsheet logicsheet;
        /**
         * The las time this logicsheet was changed/loaded
         */
        protected long lastModified;

        /**
         * The constructor.
         */
        protected CachedURL(URL url, Logicsheet logicsheet) throws IOException {
            this.url = url;
            this.logicsheet = logicsheet;

            if (this.isFile()) {
                this.file = new File(url.getFile());
            }

            this.lastModified = (new Date()).getTime();
        }

        /**
         * Return this entry's URL
         *
         * @return The cached logicsheet's URL
         */
        protected URL getURL() {
            return this.url;
        }

        protected boolean isFile() {
            return this.url.getProtocol().equals("file");
        }

        /**
         * Return this entry's <code>File</code>
         *
         * @return The cached logicsheet's <code>File</code>
         */
        protected File getFile() {
            return this.file;
        }

        /**
         * Return this entry's cached logicsheet
         *
         * @return The cached logicsheet
         */
        protected Logicsheet getLogicsheet() {
            return this.logicsheet;
        }

        /**
         * Assert whether this entry's logicsheet should be reloaded
         *
         * @return Whether the cached logicsheet has changed
         */
        protected boolean hasChanged() {
            if (this.file == null) {
                return false;
            }

            return this.lastModified < this.file.lastModified();
        }
    }

    /**
    * a XMLFilter that build the chain of transformers on the fly.
    * Each time a stylesheet is found, a call to the code generator is done
    * to add the new transformer at the end of the current transformer chain.
    *
    */
    public class TransformerChainBuilderFilter extends XMLFilterImpl {

        /**
         * The markup generator
         */
        protected LogicsheetCodeGenerator logicsheetMarkupGenerator;

        /**
         * the language description
         */
        protected LanguageDescriptor language;

        /**
         * the entity resolver
         */
        protected EntityResolver resolver;

        private boolean isRootElem;

        private List startPrefixes;

        /**
        * the constructor depends on the code generator, and the entity resolver
        *
        * @param logicsheetMarkupGenerator The code generator
        * @param resolver
        */
        protected TransformerChainBuilderFilter (
            LogicsheetCodeGenerator logicsheetMarkupGenerator,
                EntityResolver resolver
        ) {
            this.logicsheetMarkupGenerator = logicsheetMarkupGenerator;
            this.resolver = resolver;
        }

        /**
        * This method should be called prior to receiving any SAX event.
        * Indeed the language information is needed to get the core stylesheet.
        *
        * @language the language in used
        */
        protected void setLanguageDescriptor(LanguageDescriptor language) {
            this.language = language;
        }


        /**
         * @see ContentHandler
         */
        public void startDocument () throws SAXException {
            isRootElem=true;
            startPrefixes = new ArrayList();
        }

        /**
         * @see ContentHandler
         */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if(!isRootElem) {
                super.startPrefixMapping(prefix, uri);
            } else {
                // cache the prefix mapping
                String[] prefixArray = new String [2];
                prefixArray[0]= prefix;
                prefixArray[1]= uri;
                this.startPrefixes.add(prefixArray);
            }
        }

        /**
         * @see ContentHandler
         */
        public void startElement (
            String namespaceURI, String localName,
            String qName, Attributes atts
         ) throws SAXException {
            if(isRootElem) {
                isRootElem=false;
                try {
                    // Add namespace-mapped logicsheets
                    int prefixesCount = this.startPrefixes.size();
                    for (int i = 0; i < prefixesCount; i++) {
                        String[] prefixNamingArray = (String[]) this.startPrefixes.get(i);
                        String namedLogicsheetName =
                            this.language.getNamedLogicsheet(prefixNamingArray[0]);
                        if (namedLogicsheetName != null) {
                            AbstractMarkupLanguage.this.addLogicsheet(
                                this.logicsheetMarkupGenerator, namedLogicsheetName, resolver
                            );
                        }
                    }
                    // Add the language stylesheet (Always the last one)
                    AbstractMarkupLanguage.this.addLogicsheet(
                        this.logicsheetMarkupGenerator, this.language.getLogicsheet(), resolver
                    );
                } catch (IOException ioe) {
                    log.warn("AbstractMarkupLanguage.startElement", ioe);
                    throw new SAXException (ioe);
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
