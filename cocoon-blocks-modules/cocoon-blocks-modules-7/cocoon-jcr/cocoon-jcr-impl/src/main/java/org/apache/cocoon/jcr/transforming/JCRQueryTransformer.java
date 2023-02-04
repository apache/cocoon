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
package org.apache.cocoon.jcr.transforming;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.jcr.Item;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.namespace.QName;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This transformer performs a query on a JCR server.
 * <p>
 * It expects a "query" element without namespace containing the 
 * query to execute, either in XPath or SQL, and eventually the properties to
 * fetch (since JSR-170 does not define a standard for specifying them in xpath
 * queries).
 * </p>
 * <p> 
 * It will then replace it with a "result" element containing the query results
 * in elements named "node".
 * </p>
 * <h2>Sample invocation:</h2>
 * <pre>
 * &lt;query language="xpath" 
 *       xmlns:jcr="http://www.jcp.org/jcr/1.0"
 *       xmlns:cms="http://www.mycms.org"&gt;
 *   &lt;jcr:encoding/&gt;
 *   &lt;jcr:isCheckedOut/&gt;
 *   &lt;cms:data&gt;
 *     &lt;cms:author/&gt;
 *     &lt;cms:workflow&gt;
 *       &lt;cms:lastStep/&gt;
 *     &lt;/cms:workflow&gt;
 *   &lt;/cms:data&gt;
 *   jcr:root/site/articles/*
 * &lt;/query&gt;
 * </pre>
 * <h2>Sample output:</h2>
 * <pre>
 * &lt;result xmlns:cms="http://www.mycms.org" 
 *         xmlns:jcr="http://www.jcp.org/jcr/1.0"&gt;
 *   &lt;node href="/site/articles/home.xml" name="home.xml" type="nt:folder" uuid="2359e49d-e84b-47aa-b27c-fb46b86abb19"&gt;
 *     &lt;jcr:encoding type="String"&gt;UTF-8&lt;/jcr:encoding&gt;
 *     &lt;jcr:isCheckedOut type="Boolean"&gt;false&lt;/jcr:isCheckedOut&gt;
 *     &lt;cms:data href="/site/articles/home.xml/cms:data" name="cms:data" type="cms:dataRecord" uuid="72e36378-f26f-4548-993f-21b5c97306f0"&gt;
 *       &lt;cms:author type="String"&gt;Simone&lt;/cms:author&gt;
 *       &lt;cms:workflow href="/site/articles/home.xml/cms:data/cms:workflow" name="cms:workflow" type="cms:workflowRecord" uuid="72e36378-f26f-4548-993f-31a6d9931e4"&gt;
 *       	&lt;cms:lastStep type="Integer"&gt;1&lt;/cms:lastStep&gt;
 *       &lt;/cms:workflow&gt;
 *     &lt;/cms:data&gt;
 *     &lt;jcr:primaryType type="Name"&gt;cms:article&lt;/jcr:primaryType&gt;
 *   &lt;/node&gt;
 * &lt;/result&gt;
 * </pre>
 *
 * TODO: render bynary properties 
 * 
 * @version $Id$
 */
public class JCRQueryTransformer extends AbstractSAXTransformer {

	/**
	 * Wether we are inside the query element or not
	 */
	protected boolean recording = false;
	
	/**
	 * Language of the query, as taken from query/@language 
	 */
	protected String language;
	
	/**
	 * Query statement
	 */
	protected String query;
	
	/**
	 * Basic selection, this will hold all properties and subnodes fetching hierarchy.
	 */
	protected JCRSelection selection;
	
	/**
	 * Latest JCRSelection created, so that children can be properly assigned.
	 */
	protected Stack currentSelection;
	
	protected Repository repo;
	
    public void startElement(String uri, String name, String raw,
			Attributes attr) throws SAXException {
    	if (name.equals("query")) {
    		recording = true;
    		language = attr.getValue("language");
    		selection = new JCRSelection();
    		currentSelection = new Stack();
    	} else if (recording) {
    		JCRSelection eleselection = new JCRSelection(uri, name);
    		if (currentSelection.empty()) {
    			selection.addSubnode(eleselection);
    		} else {
    			JCRSelection parent = (JCRSelection) currentSelection.peek();
    			parent.addSubnode(eleselection);
    		}
    		currentSelection.push(eleselection);
    	} else {
    		super.startElement(uri, name, raw, attr);
    	}
	}
    
    public void endElement(String uri, String name, String raw) throws SAXException {
    	if (name.equals("query")) {
    		recording = false;
    		lazyInit();
    		
            Session session;
            try {
                // TODO: accept a different workspace?
                session = repo.login();
            } catch (LoginException e) {
                throw new SAXException("Login to repository failed", e);
            } catch (RepositoryException e) {
                throw new SAXException("Cannot access repository", e);
            }
    		
    		try {
    			AttributesImpl emptyAttrs = new AttributesImpl();
    			this.contentHandler.startElement("", "result", "result", emptyAttrs);
    			
				QueryManager queryManager = session.getWorkspace().getQueryManager();
				Query query2 = queryManager.createQuery(this.query, this.language);
				QueryResult result = query2.execute();
				String[] columnNames = result.getColumnNames();
				for (int i = 0; i < columnNames.length; i++) {
					int pos = -1;
					if ((pos = columnNames[i].indexOf(':')) != -1) {
						String prefix = columnNames[i].substring(0, pos);
						String local = columnNames[i].substring(pos + 1);
						String namespaceURI = session.getNamespaceURI(prefix);						
						selection.addSubnode(new JCRSelection(namespaceURI, local));
					}
				}

				Set selns = selection.getNamespaces();
				Map namespaces = new HashMap();
				char spareprefix = 'a';
				for (Iterator iter = selns.iterator(); iter.hasNext();) {
					String selnamespace = (String) iter.next();
					if (!namespaces.containsKey(selnamespace)) {
						String prefix = session.getNamespacePrefix(selnamespace);
						if (prefix == null || prefix.length() == 0) {
							prefix = "jcr" + spareprefix++;
						}
						this.contentHandler.startPrefixMapping(prefix, selnamespace);
						namespaces.put(selnamespace, prefix);
					}
				}
				
				NodeIterator nodes = result.getNodes();
				
				while (nodes.hasNext()) {
					Node node = nodes.nextNode();
					sendItem(node, this.selection, namespaces);
				}
				
				this.contentHandler.endElement("", "result", "result");
			} catch (InvalidQueryException e) {
				throw new SAXException("Invalid query " + this.query + " (" + this.language + ")", e);
			} catch (Exception e) {
				throw new SAXException(e);
			}
    	} else if (!recording) {
    		super.endElement(uri, name, raw);
    	} else {
    		currentSelection.pop();
    	}
    }

	private void sendItem(Item item, JCRSelection acsel, Map namespaces) throws Exception {
		QName name = acsel.getName();
		if (name == null) {
			name = new QName("", "node");
		}
		String raw = name.getLocalPart();
		if (namespaces.containsKey(name.getNamespaceURI())) {
			raw = namespaces.get(name.getNamespaceURI()) + ":" + raw;
		}
		
		if (item.isNode()) {
			Node node = (Node) item;
			
			AttributesImpl nodeAttrs = new AttributesImpl();
			nodeAttrs.addAttribute("", "href", "href", "xsd:string", node.getPath());
			nodeAttrs.addAttribute("", "name", "name", "xsd:string", node.getName());
			nodeAttrs.addAttribute("", "type", "type", "xsd:string", node.getDefinition().getDeclaringNodeType().getName());
			nodeAttrs.addAttribute("", "uuid", "uuid", "xsd:string", node.getUUID());
			this.contentHandler.startElement(name.getNamespaceURI(), name.getLocalPart(), raw, nodeAttrs);
			for (Iterator iter = acsel.getSubnodes().iterator(); iter.hasNext();) {
				JCRSelection subnode = (JCRSelection) iter.next();
				String prefix = (String) namespaces.get(subnode.getName().getNamespaceURI());
				String fullname = prefix + ":" + subnode.getName().getLocalPart();
				if (node.hasProperty(fullname)) {
					Item subitem = node.getProperty(fullname);
					sendItem(subitem, subnode, namespaces);
				} else if (node.hasNode(fullname)) {
					Item subitem = node.getNode(fullname);
					sendItem(subitem, subnode, namespaces);					
				}
			}
			this.contentHandler.endElement(name.getNamespaceURI(), name.getLocalPart(), raw);
		} else {
			Property property = (Property) item;
			
			Value[] vals = null;
			try {
				vals = property.getValues();
			} catch (ValueFormatException e) {
				vals = new Value[] { property.getValue() };
			}
			
			for (int i = 0; i < vals.length; i++) {
				AttributesImpl propAttrs = new AttributesImpl();
				int type = property.getType();
				propAttrs.addAttribute("", "type", "type", "xsd:string", PropertyType.nameFromValue(type));
				this.contentHandler.startElement(name.getNamespaceURI(), name.getLocalPart(), raw, propAttrs);
				
				if (type == PropertyType.BINARY) {
					// TODO how to send a binary value? BASE64 or simple CDATA?
				} else {
					String val = vals[i].getString();
					this.contentHandler.characters(val.toCharArray(), 0, val.length());
				}
				
				this.contentHandler.endElement(name.getNamespaceURI(), name.getLocalPart(), raw);				
			}
		}
	}

	public void characters(char[] buff, int offset, int len) throws SAXException {
		if (!recording) {
			super.characters(buff, offset, len);
		} else {
			this.query = new String(buff, offset, len).trim();
		}
	}
    
    protected void lazyInit() {
        if (this.repo == null) {
            try {
                this.repo = (Repository)manager.lookup(Repository.class.getName());
            } catch (Exception e) {
                throw new CascadingRuntimeException("Cannot lookup repository", e);
            }
        }
    }

}
