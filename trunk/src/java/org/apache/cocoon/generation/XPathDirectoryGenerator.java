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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates an XML directory listing performing XPath queries on XML files. It
 * can be used both as a plain DirectoryGenerator or, by specifying a parameter
 * <code>xpath</code>, it will perform an XPath query on every XML resource.
 * Therefore an additional parameter <code>xmlFiles</code> can be set in the
 * sitemap setting the regular expression pattern for determining if a file
 * should be handled as XML file or not. The default value for this param is
 * <code>\.xml$</code>, so that it matches all files ending <code>.xml</code>.
 * <br>
 * Sample usage:<br>
 * <br>
 * Sitemap:
 * <pre>
 * &lt;map:match pattern="documents/**"&gt;
 *   &lt;map:generate type="xpathdirectory" src="docs/{1}"&gt;
 *     &lt;map:parameter name="xpath" value="/article/title|/article/abstract"/&gt;
 *     &lt;map:parameter name="xmlFiles" value="\.xml$"/&gt;
 *   &lt;/map:generate&gt;
 *   &lt;map:serialize type="xml" /&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * <p>Request:<br>
 *   http://www.some.host/documents/test</p>
 *
 * Result:
 * <pre>
 * &lt;dir:directory
 *   name="test" lastModified="1010400942000"
 *   date="1/7/02 11:55 AM" requested="true"
 *   xmlns:dir="http://apache.org/cocoon/directory/2.0"&gt;
 *   &lt;dir:directory name="subdirectory" lastModified="1010400942000" date="1/7/02 11:55 AM"/&gt;
 *   &lt;dir:file name="test.xml" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt;
 *     &lt;dir:xpath query="/article/title"&gt;
 *       &lt;title&gt;This is a test document&lt;/title&gt;
 *       &lt;abstract&gt;
 *         &lt;para&gt;Abstract of my test article&lt;/para&gt;
 *       &lt;/abstract&gt;
 *     &lt;/dir:xpath&gt;
 *   &lt;/dir:file&gt;
 *   &lt;dir:file name="test.gif" lastModified="1011011579000" date="1/14/02 1:32 PM"/&gt;
 * &lt;/dir:directory&gt;
 * </pre>
 *
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:joerg@apache.org">Jörg Heinicke</a>
 * @version CVS $Id: XPathDirectoryGenerator.java,v 1.7 2004/03/08 14:02:44 cziegeler Exp $
 */
public class XPathDirectoryGenerator extends DirectoryGenerator {

    /** Local name for the element that contains the included XML snippet. */
    protected static final String XPATH_NODE_NAME = "xpath";
    /** Attribute for the XPath query. */
    protected static final String QUERY_ATTR_NAME = "query";

    /** The regular expression for the XML files pattern. */
    protected RE xmlRE = null;
    /** The document that should be parsed and (partly) included. */
    protected Document doc = null;
    /** The XPath. */
    protected String xpath = null;
    /** The XPath processor. */
    protected XPathProcessor processor = null;
    /** The parser for the XML snippets to be included. */
    protected DOMParser parser = null;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        // See if an XPath was specified
        this.xpath = par.getParameter("xpath", null);
        this.cacheKeyParList.add(this.xpath);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Applying XPath: " + this.xpath +
                              " to directory " + this.source);
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
            throw new ProcessingException("Syntax error in regexp pattern '"
                                          + xmlFilesPattern + "'", rese);
        }
    }

    /**
     * Serviceable
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
        this.parser = (DOMParser)manager.lookup(DOMParser.ROLE);
    }

    /**
     * Disposable
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.processor );
            this.manager.release( this.parser );
            this.processor = null;
            this.parser = null;
        }
        super.dispose();
    }
    /**
     * Extends the startNode() method of the DirectoryGenerator by starting
     * a possible XPath query on a file.
     */
    protected void startNode(String nodeName, File path) throws SAXException {
        super.startNode(nodeName, path);
        if (this.xpath != null && path.isFile() && this.isXML(path)) {
            performXPathQuery(path);
        }
    }

    /**
     * Determines if a given File shall be handled as XML.
     *
     * @param path  the File to check
     * @return true  if the given File shall handled as XML, false otherwise.
     */
    protected boolean isXML(File path) {
        return this.xmlRE.match(path.getName());
    }

    /**
     * Performs an XPath query on the file.
     * @param xmlFile  the File the XPath is performed on.
     * @throws SAXException  if something goes wrong while adding the XML snippet.
     */
    protected void performXPathQuery(File xmlFile) throws SAXException {
        this.doc = null;
        Source source = null;
        try {
            source = resolver.resolveURI(xmlFile.toURL().toExternalForm());
            this.doc = this.parser.parseDocument(SourceUtil.getInputSource(source));
        } catch (SAXException e) {
            getLogger().error("Warning:" + xmlFile.getName() +
                              " is not a valid XML file. Ignoring.", e);
        } catch (ProcessingException e) {
            getLogger().error("Warning: Problem while reading the file " +
                              xmlFile.getName() + ". Ignoring.", e);
        } catch (IOException e) {
            getLogger().error("Warning: Problem while reading the file " +
                              xmlFile.getName() + ". Ignoring.", e);
        } finally {
            resolver.release(source);
        }

        if (doc != null) {
            NodeList nl = this.processor.selectNodeList(this.doc.getDocumentElement(), this.xpath);
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
     * Recycle resources
     */
    public void recycle() {
        this.xpath = null;
        this.doc = null;
        //this.parser = null;
        //this.processor = null;
        super.recycle();
    }
}
