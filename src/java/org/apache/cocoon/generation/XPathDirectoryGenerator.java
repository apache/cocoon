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
package org.apache.cocoon.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;

import org.apache.cocoon.components.source.SourceUtil;

import org.apache.cocoon.environment.SourceResolver;

import org.apache.cocoon.xml.dom.DOMStreamer;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;

import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.xpath.PrefixResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.AttributesImpl;


/**
 * <p>
 * Generates an XML directory listing performing XPath queries on XML files. It can be used both as a plain
 * DirectoryGenerator or, by specifying a parameter <code>xpath</code>, it will perform an XPath query on every XML
 * resource. A <code>nsmapping</code> parameter can be specified to point to a file containing lines to map prefixes
 * to namespaces like this:
 * </p>
 * 
 * <p>
 * prefix=namespace-uri<br/> prefix2=namespace-uri-2
 * </p>
 * 
 * <p>
 * A parameter <code>nsmapping-reload</code> specifies if the prefix-2-namespace mapping file should be checked to be
 * reloaded on each request to this generator if it was modified since the last time it was read.
 * </p>
 * 
 * <p>
 * An additional parameter <code>xmlFiles</code> can be set in the sitemap setting the regular expression pattern for
 * determining if a file should be handled as XML file or not. The default value for this param is
 * <code>\.xml$</code>, so that it  matches all files ending <code>.xml</code>.
 * </p>
 * 
 * <p></p>
 * <br>Sample usage: <br><br>Sitemap:
 * <pre>
 *  &lt;map:match pattern="documents/**"&gt; 
 *   &lt;map:generate type="xpathdirectory" src="docs/{1}"&gt; 
 *    &lt;map:parameter name="xpath" value="/article/title|/article/abstract"/&gt; 
 *    &lt;map:parameter name="nsmapping" value="mapping.proeprties"/&gt; 
 *    &lt;map:parameter name="nsmapping-reload" value="false"/&gt; 
 *    &lt;map:parameter name="xmlFiles" value="\.xml$"/&gt; 
 *   &lt;/map:generate&gt; 
 *   &lt;map:serialize type="xml" /&gt; 
 *  &lt;/map:match&gt;
 * </pre>
 * 
 * <p>
 * Request: <br>http://www.some.host/documents/test
 * </p>
 * Result:
 * <pre>
 *  &lt;dir:directory name="test" lastModified="1010400942000" date="1/7/02 11:55 AM" requested="true" xmlns:dir="http://apache.org/cocoon/directory/2.0"&gt; 
 *   &lt;dir:directory name="subdirectory" lastModified="1010400942000" date="1/7/02 11:55 AM"/&gt; 
 *   &lt;dir:file name="test.xml" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt; 
 *    &lt;dir:xpath query="/article/title"&gt; 
 *     &lt;title&gt;This is a test document&lt;/title&gt; 
 *      &lt;abstract&gt; 
 *       &lt;para&gt;Abstract of my test article&lt;/para&gt; 
 *      &lt;/abstract&gt; 
 *     &lt;/dir:xpath&gt; 
 *    &lt;/dir:file&gt; 
 *   &lt;dir:file name="test.gif" lastModified="1011011579000" date="1/14/02 1:32 PM"/&gt; 
 *  &lt;/dir:directory&gt;
 * </pre>
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:joerg@apache.org">J\u00F6rg Heinicke</a>
 * @version CVS $Id: XPathDirectoryGenerator.java,v 1.9 2004/05/07 21:13:50 joerg Exp $
 */
public class XPathDirectoryGenerator
extends DirectoryGenerator {
    /** Local name for the element that contains the included XML snippet. */
    protected static final String XPATH_NODE_NAME = "xpath";

    /** Attribute for the XPath query. */
    protected static final String QUERY_ATTR_NAME = "query";

    /** All the mapping files lastmodified dates */
    protected static Map mappingFiles = new HashMap();

    /** The parser for the XML snippets to be included. */
    protected DOMParser parser = null;

    /** The document that should be parsed and (partly) included. */
    protected Document doc = null;

    /** The PrefixResolver responsable for processing current request (if any). */
    protected PrefixResolver prefixResolver = null;

    /** The regular expression for the XML files pattern. */
    protected RE xmlRE = null;

    /** The XPath. */
    protected String xpath = null;

    /** The XPath processor. */
    protected XPathProcessor processor = null;

    /**
     * Disposable
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.processor);
            this.manager.release(this.parser);
            this.processor = null;
            this.parser = null;
        }

        super.dispose();
    }

    /**
     * Recycle resources
     */
    public void recycle() {
        this.xpath = null;
        this.doc = null;

        //this.parser = null;
        //this.processor = null;
        super.recycle();
    }

    /**
     * Serviceable
     *
     * @param manager the ComponentManager
     *
     * @throws ServiceException in case a component could not be found
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        super.service(manager);
        this.processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
        this.parser = (DOMParser)manager.lookup(DOMParser.ROLE);
    }

    /**
     * Setup this sitemap component
     *
     * @param resolver the SourceResolver
     * @param objectModel The environmental object model
     * @param src the source attribute
     * @param par the parameters
     *
     * @throws ProcessingException if processing failes
     * @throws SAXException in case of XML related errors
     * @throws IOException in case of file related errors
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        // See if an XPath was specified
        this.xpath = par.getParameter("xpath", null);
        this.cacheKeyParList.add(this.xpath);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Applying XPath: " + this.xpath + " to directory " + this.source);
        }

        final String mappings = par.getParameter("nsmapping", null);

        if (null != mappings) {
            final boolean mapping_reload = par.getParameterAsBoolean("nsmapping-reload", false);
            final Source mappingSource = resolver.resolveURI(mappings);
            final String mappingKey = mappingSource.getURI();
            final MappingInfo mappingInfo = (MappingInfo)XPathDirectoryGenerator.mappingFiles.get(mappingKey);

            if ((null == mappingInfo) || (mappingInfo.reload == false) ||
                (mappingInfo.mappingSource.getLastModified() < mappingSource.getLastModified())) {
                this.prefixResolver =
                    new MappingInfo(getLogger().getChildLogger("prefix-resolver"), mappingSource, mapping_reload);
                XPathDirectoryGenerator.mappingFiles.put(mappingKey, this.prefixResolver);
            } else {
                this.prefixResolver = mappingInfo;
            }
        }

        String xmlFilesPattern = null;

        try {
            xmlFilesPattern = par.getParameter("xmlFiles", "\\.xml$");
            this.cacheKeyParList.add(xmlFilesPattern);
            this.xmlRE = new RE(xmlFilesPattern);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("pattern for XML files: " + xmlFilesPattern);
            }
        } catch (RESyntaxException rese) {
            throw new ProcessingException("Syntax error in regexp pattern '" + xmlFilesPattern + "'", rese);
        }
    }

    /**
     * Determines if a given File shall be handled as XML.
     *
     * @param path the File to check
     *
     * @return true if the given File shall handled as XML, false otherwise.
     */
    protected boolean isXML(File path) {
        return this.xmlRE.match(path.getName());
    }

    /**
     * Performs an XPath query on the file.
     *
     * @param xmlFile the File the XPath is performed on.
     *
     * @throws SAXException if something goes wrong while adding the XML snippet.
     */
    protected void performXPathQuery(File xmlFile)
    throws SAXException {
        this.doc = null;

        Source source = null;

        try {
            source = resolver.resolveURI(xmlFile.toURL().toExternalForm());
            this.doc = this.parser.parseDocument(SourceUtil.getInputSource(source));
        } catch (SAXException e) {
            getLogger().error("Warning:" + xmlFile.getName() + " is not a valid XML file. Ignoring.", e);
        } catch (ProcessingException e) {
            getLogger().error("Warning: Problem while reading the file " + xmlFile.getName() + ". Ignoring.", e);
        } catch (IOException e) {
            getLogger().error("Warning: Problem while reading the file " + xmlFile.getName() + ". Ignoring.", e);
        } finally {
            resolver.release(source);
        }

        if (doc != null) {
            NodeList nl =
                (null == this.prefixResolver)
                ? this.processor.selectNodeList(this.doc.getDocumentElement(), this.xpath)
                : this.processor.selectNodeList(this.doc.getDocumentElement(), this.xpath, this.prefixResolver);
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute("", QUERY_ATTR_NAME, QUERY_ATTR_NAME, "CDATA", xpath);
            super.contentHandler.startElement(URI, XPATH_NODE_NAME, PREFIX + ":" + XPATH_NODE_NAME, attributes);

            DOMStreamer ds = new DOMStreamer(super.xmlConsumer);

            for (int i = 0; i < nl.getLength(); i++) {
                ds.stream(nl.item(i));
            }

            super.contentHandler.endElement(URI, XPATH_NODE_NAME, PREFIX + ":" + XPATH_NODE_NAME);
        }
    }

    /**
     * Extends the startNode() method of the DirectoryGenerator by starting a possible XPath query on a file.
     *
     * @param nodeName the node currently processing
     * @param path the file path
     *
     * @throws SAXException in case of errors
     */
    protected void startNode(String nodeName, File path)
    throws SAXException {
        super.startNode(nodeName, path);

        if ((this.xpath != null) && path.isFile() && this.isXML(path)) {
            performXPathQuery(path);
        }
    }

    /**
     * The MappingInfo class to reolve namespace prefixes to their namespace URI
     *
     * @author <a href="mailto:giacomo(at)apache.org">Giacomo Pati</a>
     * @version CVS $Id: XPathDirectoryGenerator.java,v 1.9 2004/05/07 21:13:50 joerg Exp $
     */
    private static class MappingInfo
    implements PrefixResolver {
        /** The Source of the mapping file */
        public final Source mappingSource;

        /** Whether to reload if mapping file has changed */
        public final boolean reload;

        /** Our Logger */
        private final Logger logger;

        /** Map of prefixes to namespaces */
        private final Map prefixMap;

        /**
         * Creates a new MappingInfo object.
         *
         * @param logger DOCUMENT ME!
         * @param mappingSource The Source of the mapping file
         * @param reload Whether to reload if mapping file has changed
         *
         * @throws SourceNotFoundException In case the mentioned source is not there
         * @throws IOException in case the source could not be read
         */
        public MappingInfo(final Logger logger, final Source mappingSource, final boolean reload)
        throws SourceNotFoundException, IOException {
            this.logger = logger;
            this.mappingSource = mappingSource;
            this.reload = reload;
            prefixMap = new HashMap();

            final BufferedReader br = new BufferedReader(new InputStreamReader(mappingSource.getInputStream()));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                final int i = line.indexOf('=');

                if (i > 0) {
                    final String prefix = line.substring(0, i);
                    final String namespace = line.substring(i + 1);
                    prefixMap.put(prefix, namespace);
                    logger.debug("added mapping: '" + prefix + "'='" + namespace + "'");
                }
            }
        }

        /* (non-Javadoc)
         * @see org.apache.excalibur.xml.xpath.PrefixResolver#prefixToNamespace(java.lang.String)
         */
        public String prefixToNamespace(String prefix) {
            final String namespace = (String)this.prefixMap.get(prefix);

            if (logger.isDebugEnabled()) {
                logger.debug("have to resolve prefix='" + prefix + ", found namespace='" + namespace + "'");
            }

            return namespace;
        }
    }
}
