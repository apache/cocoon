/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.SourceException;
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
 * overriden by setting the (regexp) pattern "xmlFiles as a sitemap parameter.
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
 * &lt;dir:directory
 *   name="test" lastModified="1010400942000"
 *   date="1/7/02 11:55 AM" requested="true"
 *   xmlns:dir="http://apache.org/cocoon/directory/2.0"&gt;
 *   &lt;dir:directory name="subdirectory" lastModified="1010400942000" date="1/7/02 11:55 AM" /&gt;
 *   &lt;dir:file name="test.xml" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt;
 *     &lt;dir:xpath docid="test.xml" query="/article/title"&gt;
 *       &lt;title&gt;This is a test document&lt;/title&gt;
 *       &lt;abstract&gt;
 *         &lt;para&gt;Abstract of my test article&lt;/para&gt;
 *       &lt;/abstract&gt;
 *     &lt;/dir:xpath&gt;
 *   &lt;/dir:file&gt;
 *   &lt;dir:file name="test.gif" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt;
 * &lt;/dir:directory&gt;
 * 
 * If you need to use namespaces, you can set them as sitemap parameters in
 * the form:
 * lt;map:parameter name="xmlns:<i>your prefix</i>" value="nsURI"/**"&gt; 
 *
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:d.madama@pro-netics.com">Daniele Madama</a>
 * @version CVS $Id: XPathTraversableGenerator.java,v 1.2 2003/07/13 12:33:53 gianugo Exp $
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
        this.prefixResolver = new XPathPrefixResolver();
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
    }

    public void compose(ComponentManager manager) throws ComponentException {
        super.compose(manager);
        processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
        parser = (DOMParser)manager.lookup(DOMParser.ROLE);
    }

    /**
     * Adds a single node to the generated document. If the path is a
     * directory, and depth is greater than zero, then recursive calls
     * are made to add nodes for the directory's children. Moreover,
     * if the file is an XML file (ends with .xml), the XPath query
     * is performed and results returned.
     *
     * @param   path
     *      the (Traversable) source to process
     * @param   depth
     *      how deep to scan the collection
     *
     * @throws  SAXException
     *      if an error occurs while constructing nodes
     */
    protected void addPath(TraversableSource path, int depth)
    throws SAXException {
		if (path.isCollection()) {
			startNode(COL_NODE_NAME, path);
			if (depth > 0) {
                
				Collection contents;
				try {
					contents = path.getChildren();
				} catch (SourceException e) {
					throw new SAXException("Error adding paths", e);
				}

				if (sort.equals("name")) {
					Arrays.sort(contents.toArray(), new Comparator() {
						public int compare(Object o1, Object o2) {
							if (reverse) {
								return ((TraversableSource)o2).getName().compareTo(((TraversableSource)o1).getName());
							}
							return ((TraversableSource)o1).getName().compareTo(((TraversableSource)o2).getName());
						}
					});
				} else if (sort.equals("size")) {
					Arrays.sort(contents.toArray(), new Comparator() {
						public int compare(Object o1, Object o2) {
							if (reverse) {
								return new Long(((TraversableSource)o2).getContentLength()).compareTo(
									new Long(((TraversableSource)o1).getContentLength()));
							}
							return new Long(((TraversableSource)o1).getContentLength()).compareTo(
								new Long(((TraversableSource)o2).getContentLength()));
						}
					});
				} else if (sort.equals("lastmodified")) {
					Arrays.sort(contents.toArray(), new Comparator() {
						public int compare(Object o1, Object o2) {
							if (reverse) {
								return new Long(((TraversableSource)o2).getLastModified()).compareTo(
									new Long(((TraversableSource)o1).getLastModified()));
							}
							return new Long(((TraversableSource)o1).getLastModified()).compareTo(
								new Long(((TraversableSource)o2).getLastModified()));
						}
					});
				} else if (sort.equals("collection")) {
					Arrays.sort(contents.toArray(), new Comparator() {
						public int compare(Object o1, Object o2) {
							TraversableSource ts1 = (TraversableSource)o1;
							TraversableSource ts2 = (TraversableSource)o2;

							if (reverse) {
								if (ts2.isCollection() && !ts1.isCollection())
									return -1;
								if (!ts2.isCollection() && ts1.isCollection())
									return 1;
								return ts2.getName().compareTo(ts1.getName());
							}
							if (ts2.isCollection() && !ts1.isCollection())
								return 1;
							if (!ts2.isCollection() && ts1.isCollection())
								return -1;
							return ts1.getName().compareTo(ts2.getName());
						}
					});
				}

				for (int i = 0; i < contents.size(); i++) {
					if (isIncluded((TraversableSource) contents.toArray()[i]) && !isExcluded((TraversableSource) contents.toArray()[i])) {
						addPath((TraversableSource) contents.toArray()[i], depth - 1);
					}
				}
			}
			endNode(COL_NODE_NAME);
		} else {
			if (isIncluded(path) && !isExcluded(path)) {
				startNode(RESOURCE_NODE_NAME, path);
				if (isXML(path)  && xpath != null)
				  performXPathQuery(path);
				endNode(RESOURCE_NODE_NAME);
			}
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
		return this.xmlRE.match(path.getName());
	}
	
	/**
	 * Performs an XPath query on the source.
	 * @param xmlFile  the Source the XPath is performed on.
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
    }

    /**
     * A brain-dead PrefixResolver implementation
     * 
     */
    
    class XPathPrefixResolver implements PrefixResolver {
    	
    	private Map params;

        public XPathPrefixResolver() {
        	this.params = new HashMap();
        }

        /**
         * Get a namespace URI given a prefix.
         * 
         * @see org.apache.excalibur.xml.xpath.PrefixResolver#prefixToNamespace(java.lang.String)
         */
        public String prefixToNamespace(String prefix) {
        	if (getLogger().isDebugEnabled()) {
        		getLogger().debug("prefix: " + prefix);
        	}
        	if (this.params.containsKey(prefix)) {
        		if(getLogger().isDebugEnabled()) {
        			getLogger().debug("prefix; " + prefix + " - namespace: " + this.params.get(prefix));
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
