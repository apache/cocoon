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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.xpath.PrefixResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates an XML collection listing performing XPath queries on XML sources.
 * It can be used both as a plain TraversableGenerator or, if an XPath is
 * specified, it will perform an XPath query on every XML resource, where "xml
 * resource" is, by default, any resource ending with ".xml", which can be
 * overriden by setting the (regexp) pattern "xmlFiles as a sitemap parameter, 
 * or where the name of the resource has a container-wide mime-type mapping to 
 * 'text/xml' such as specified by mime-mapping elements in a web.xml 
 * descriptor file.
 * 
 * The XPath can be specified in two ways:
 * <ol>
 *    <li>By using an XPointerish syntax in the URL: everything following the
 *         pound                 sign                 (possiby preceding  query
 * string arguments)  will be treated as the XPath;
 *     </li>
 *     <li>Specifying it as a sitemap parameter named "xpath"
 *  </ol>
 *
 * Sample usage:
 *
 * Sitemap:
 * &lt;map:match pattern="documents/**"&gt;
 *   &lt;map:generate type="xpathdirectory"
 *     src="    docs/{1}#/article/title|/article/abstract" &gt;
 *     &lt;          map:parameter name="xmlFiles" value="\.xml$"/&gt;   
 * &lt;/map:generate&gt;
 * &lt;map: serialize type="xml" /&gt; &lt;/map:match&gt;
 *
 * Request:
 *   http://www.some.host/documents/test
 * Result:
 * &lt;collection:collection
 *   name="test" lastModified="1010400942000"
 *   date="1/7/02 11:55 AM" requested="true"
 *   xmlns:collection="http://apache.org/cocoon/collection/1.0"&gt;
 *   &lt;collection:collection name="subdirectory" lastModified="1010400942000" date="1/7/02 11:55 AM" /&gt;
 *   &lt;collection:resource name="test.xml" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt;
 *     &lt;collection:xpath docid="test.xml" query="/article/title"&gt;
 *       &lt;title&gt;This is a test document&lt;/title&gt;
 *       &lt;abstract&gt;
 *         &lt;para&gt;Abstract of my test article&lt;/para&gt;
 *       &lt;/abstract&gt;
 *     &lt;/collection:xpath&gt;
 *   &lt;/collection:resource&gt;
 *   &lt;collection:resource name="test.gif" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt;
 * &lt;/collection:collection&gt;
 * 
 * If you need to use namespaces, you can set them as sitemap parameters in
 * the form:
 * lt;map:parameter name="xmlns:<i>your prefix</i>" value="nsURI"/**"&gt; 
 *
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:d.madama@pro-netics.com">Daniele Madama</a>
 * @version CVS $Id: XPathTraversableGenerator.java,v 1.5 2004/03/05 13:02:22 bdelacretaz Exp $
 */
public class XPathTraversableGenerator extends TraversableGenerator {

	/** Local name for the element that contains the included XML snippet. */
	protected static final String XPATH_NODE_NAME = "xpath";
	/** Attribute for the XPath query. */
	protected static final String QUERY_ATTR_NAME = "query";
	/** The document containing a successful XPath query */
	protected static final String RESULT_DOCID_ATTR = "docid";

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
    /** The prefix resolver for namespaced queries */
	protected XPathPrefixResolver prefixResolver;
    /** The cocoon context used for mime-type mappings */
    protected Context context;
	 	
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        // See if an XPath was specified
        int pointer;
        if ((pointer = this.source.indexOf("#")) != -1) {
          int endpointer = this.source.indexOf('?');
          if (endpointer != -1) {
          	this.xpath = source.substring(pointer + 1, endpointer); 
          } else {
			this.xpath = source.substring(pointer + 1);
          }
          this.source = src.substring(0, pointer);
          if (endpointer != -1) {
          	this.source += src.substring(endpointer);
          }
		  
		  this.cacheKeyParList.add(this.xpath); 	
          if (this.getLogger().isDebugEnabled())
            this.getLogger().debug("Applying XPath: " + xpath
              + " to collection " + source);
        } else {
			this.xpath = par.getParameter("xpath", null);
			this.cacheKeyParList.add(this.xpath);
			this.getLogger().debug("Applying XPath: " + xpath
			   + " to collection " + source);
        }
        
		String xmlFilesPattern = null;
		try {
			xmlFilesPattern = par.getParameter("xmlFiles", "\\.xml$");
			this.cacheKeyParList.add(xmlFilesPattern);
			this.xmlRE = new RE(xmlFilesPattern);
			if (this.getLogger().isDebugEnabled()) {
				this.getLogger().debug("pattern for XML files: " + xmlFilesPattern);
			}
		} catch (RESyntaxException rese) {
			throw new ProcessingException("Syntax error in regexp pattern '"
										  + xmlFilesPattern + "'", rese);
		}
        
        String[] params = par.getNames();
        this.prefixResolver = new XPathPrefixResolver(this.getLogger());
        for (int i = 0; i < params.length; i++) {
            if (params[i].startsWith("xmlns:")) {
            	String paramValue = par.getParameter(params[i], "");
            	String paramName = params[i].substring(6);
            	if (getLogger().isDebugEnabled()) {
            		getLogger().debug("add param to prefixResolver: " + paramName);
            	}
            	this.prefixResolver.addPrefix(paramName, paramValue);
            }
        }
        
        this.context = ObjectModelHelper.getContext(objectModel);
    }
    
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
        parser = (DOMParser)manager.lookup(DOMParser.ROLE);
    }

    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( processor );
            this.manager.release( parser );
            this.processor = null;
            this.parser = null;
        }
        super.dispose();
    }
    
    protected void addContent(TraversableSource source) throws SAXException, ProcessingException {
        super.addContent(source);
        if (!source.isCollection() && isXML(source) && xpath != null) {
            performXPathQuery(source);
        }
    }
    
	/**
	 * Determines if a given TraversableSource shall be handled as XML.
	 *
	 * @param path  the TraversableSource to check
	 * @return true  if the given TraversableSource shall handled as XML, false
	 * otherwise.
	 */
	protected boolean isXML(TraversableSource path) {
        String mimeType = this.context.getMimeType(path.getName());
		return this.xmlRE.match(path.getName()) || "text/xml".equalsIgnoreCase(mimeType);
	}
	
	/**
	 * Performs an XPath query on the source.
	 * @param in  the Source the XPath is performed on.
	 * @throws SAXException  if something goes wrong while adding the XML snippet.
	 */
    protected void performXPathQuery(TraversableSource in)
      throws SAXException {
      doc = null;
      try {
        doc = parser.parseDocument(new InputSource(in.getInputStream()));
      } catch (SAXException se) {
         this.getLogger().error("Warning:" + in.getName()
          + " is not a valid XML document. Ignoring");
      } catch (Exception e) {
         this.getLogger().error("Unable to resolve and parse document" + e);
       }
       if (doc != null) {
         NodeList nl = processor.selectNodeList(doc.getDocumentElement(), xpath, this.prefixResolver);
         final String id = in.getName();
         AttributesImpl attributes = new AttributesImpl();
         attributes.addAttribute("", RESULT_DOCID_ATTR, RESULT_DOCID_ATTR,
          " CDATA", id);
         attributes.addAttribute("", QUERY_ATTR_NAME, QUERY_ATTR_NAME, "CDATA",
           xpath);
         super.contentHandler.startElement(URI, XPATH_NODE_NAME, PREFIX + ":" + XPATH_NODE_NAME, attributes);
         DOMStreamer ds = new DOMStreamer(super.xmlConsumer);
         for (int i = 0; i < nl.getLength(); i++)
           ds.stream(nl.item(i));
         super.contentHandler.endElement(URI, XPATH_NODE_NAME, PREFIX + ":" + XPATH_NODE_NAME);
      }
    }

    /**
     * Recycle resources
     *
     */
   public void recycle() {
      super.recycle();
      this.xpath = null;
      this.attributes = null;
      this.doc = null;
      this.xmlRE = null;
      this.prefixResolver = null;
      this.context = null;
    }

    /**
     * A brain-dead PrefixResolver implementation
     * 
     */
    
    class XPathPrefixResolver implements PrefixResolver {
    	
    	private Map params;

        private Logger logger;
        
        public XPathPrefixResolver(Logger logger) {
        	this.params = new HashMap();
            this.logger = logger;
        }

        /**
         * Get a namespace URI given a prefix.
         * 
         * @see org.apache.excalibur.xml.xpath.PrefixResolver#prefixToNamespace(java.lang.String)
         */
        public String prefixToNamespace(String prefix) {
        	if (this.logger.isDebugEnabled()) {
                this.logger.debug("prefix: " + prefix);
        	}
        	if (this.params.containsKey(prefix)) {
        		if(this.logger.isDebugEnabled()) {
                    this.logger.debug("prefix; " + prefix + " - namespace: " + this.params.get(prefix));
        		}
        		return (String) this.params.get(prefix);
        	}
            return null;
        }
    	
    	public void addPrefix(String prefix, String uri) {    		
    		this.params.put(prefix, uri);
    	}

    }
    
}
