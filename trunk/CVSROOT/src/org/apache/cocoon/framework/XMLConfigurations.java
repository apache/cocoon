/*-- $Id: XMLConfigurations.java,v 1.2 2000-02-07 08:35:18 balld Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.framework;

import java.util.*;
import java.io.*;
import java.text.StringCharacterIterator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class makes a Configurations object from an XML source.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.2 $ $Date: 2000-02-07 08:35:18 $
 */

public class XMLConfigurations extends Configurations {

	Hashtable values = new Hashtable();
	Hashtable children = new Hashtable();
	String my_prefix = null;

	/**
	 * Strictly for testing
	 */
	public static void main(String argv[]) throws Exception {
		XMLConfigurations confs = new XMLConfigurations(argv[0]);
		System.out.println(confs.toString());
	}

	/**
	 * Creates a new empty Configurations
	 */
	public XMLConfigurations() {}

	/**
	 * Creates a new Configurations object from the specified file
	 *
	 * @param file the name of the file
	 * @exception IOException if the file does not exist or is unreadable
	 * @exception SAXException if the XML is not well formed
	 */
    public XMLConfigurations(String file) throws IOException,SAXException {
        InputStream input = new FileInputStream(file);
		org.apache.xerces.parsers.DOMParser parser = 
			new org.apache.xerces.parsers.DOMParser();
		parser.parse(new InputSource(input));
		buildConfigurations(this,parser.getDocument().getDocumentElement());
        input.close();
    }

	/**
	 * Builds a new Configurations object from the source node
	 * 
	 * @param confs the Configurations object to manipulate
	 * @param source_node the node from which to read
	 */
	protected static void buildConfigurations(XMLConfigurations confs, Node source_node) {
		buildConfigurations(confs, source_node, source_node.getNodeName());
	}

	/**
	 * Builds a new Configurations object from the source node with the given
	 * prefix
	 *
	 * @param confs the Configurations object to manipulate
	 * @param source_node the node from which to read
	 * @param prefix the prefix to use
	 */
	protected static void buildConfigurations(XMLConfigurations confs, Node source_node, String prefix) {
		confs.my_prefix = prefix;
		NodeList nodes = source_node.getChildNodes();
		int length = nodes.getLength();
		for (int i=0; i<length; i++) {
			Node node = nodes.item(i);
			int type = node.getNodeType();
			switch(type) {
				case Node.ELEMENT_NODE:
					String name = node.getNodeName();
					NodeList child_nodes = node.getChildNodes();
					int child_nodes_length = child_nodes.getLength();
					StringBuffer buffer = new StringBuffer();
					boolean has_grandchildren = false;
					for (int j=0; j<child_nodes_length; j++) {
						Node child_node = child_nodes.item(j);
						switch(child_node.getNodeType()) {
							case Node.TEXT_NODE:
								buffer.append(child_node.getNodeValue());
								break;
							case Node.ELEMENT_NODE:
								has_grandchildren = true;
								break;
						}
								
					}
					confs.values.put(name,stripBoundingWhitespace(buffer.toString()));
					if (has_grandchildren) {
						XMLConfigurations child_confs = new XMLConfigurations();
						buildConfigurations(child_confs,node,confs.my_prefix+'.'+name);
						confs.children.put(name,child_confs);
					}
					break;
			}
		}
	}

	/**
	 * Returns the value of the key, or null if the key has no value
	 * 
	 * @param key the key
	 * @return the value
	 */
	public Object get(String key) {
		int index = key.indexOf('.');
		if (index < 0) {
			return values.get(key);
		} else {
			String prefix = key.substring(0,index);
			XMLConfigurations confs = (XMLConfigurations)children.get(prefix);
			if (confs == null) {
				return null;
			}
			return confs.get(key.substring(index+1));
		}
	}

	/**
	 * Associates the value with the key
	 *
	 * @param key the key
	 * @param value the value
	 */
    public void set(String key, Object value) {
		int index = key.indexOf('.');
		if (index < 0) {
			values.put(key,value);
		} else {
			String prefix = key.substring(0,index);
			XMLConfigurations confs = (XMLConfigurations)children.get(prefix);
			if (confs == null) {
				confs = new XMLConfigurations();
				children.put(prefix,confs);
			}
			confs.set(key.substring(index+1),value);
		}
    }

	/**
	 * Returns the value of the key, or the default if the key has no value
	 *
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
    public Object get(String key, Object def) {
        Object o = this.get(key);
        return (o == null) ? def : o;
    }

	/**
	 * Returns the value of the key, or tosses a RuntimeException if the
	 * key has no value
	 *
	 * @param key the key
	 * @return the value
	 * @exception RuntimeException if the key has no value
	 */
    public Object getNotNull(String key) {
        Object o = this.get(key);
        if (o == null) {
			throw new RuntimeException("Cocoon configuration item '" + key + "' is not set");
        } else {
            return o;
        }
    }

	/**
	 * Returns a Vector of values. FIXME better description
	 *
	 * @param key the key
	 * @return the vector
	 */
    public Vector getVector(String key) {
        Vector v = new Vector();
        for (int i = 0; ; i++) {
            Object n = get(key + "." + i);
            if (n == null) {
				break;
			}
            v.addElement(n);
        }
        return v;
    }

    /**
     * Create a Configurations child consisting of all values that begin
	 * with the given key
	 * @param key the key
	 * @return the child Configurations object
	 */
    public Configurations getConfigurations(String key) {
		int index = key.indexOf('.');
        Configurations confs = null;
		String prefix = null;
		if (index < 0) {
			confs = (Configurations)children.get(key);
			if (confs == null) {
				confs = new XMLConfigurations();
				confs.setBasename(my_prefix+'.'+key);
				children.put(key,confs);
			}
			return confs;
		} else {
			prefix = key.substring(0,index);
			confs = (Configurations)children.get(prefix);
			if (confs == null) {
				XMLConfigurations child_confs = new XMLConfigurations();
				confs.setBasename(my_prefix+'.'+prefix);
				children.put(prefix,confs);
			}
			return confs.getConfigurations(key.substring(index+1));
		}
	}

	/**
	 * Sets the prefix
	 *
	 * @param my_prefix the prefix
	 */
    public void setBasename(String my_prefix) {
        this.my_prefix = my_prefix;
    }

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Enumeration enum = values.keys();
		String name;
		while (enum.hasMoreElements()) {
			name = (String)enum.nextElement();
			sb.append(my_prefix+'.'+name+" = "+values.get(name)+'\n');
		}
		enum = children.elements();
		while (enum.hasMoreElements()) {
			XMLConfigurations confs = (XMLConfigurations)enum.nextElement();
			sb.append(confs.toString());
		}
		return sb.toString();
	}

	/**
	 * A utility routine that strips the starting and ending whitespace
	 * from the given string
	 *
	 * @param input the input string
	 * @return the input string with no bounding whitespace
	 */
	protected static String stripBoundingWhitespace(String input) {
		StringBuffer sb = new StringBuffer();
		StringCharacterIterator iter = new StringCharacterIterator(input);
		char c = iter.first();
		while (c != iter.DONE && Character.isWhitespace(c)) {
			c = iter.next();
		}
		int start_offset = iter.getIndex();
		c = iter.last();
		while (c != iter.DONE && Character.isWhitespace(c)) {
			c = iter.previous();
		}
		int end_offset = iter.getIndex();
		if (end_offset <= start_offset) {
			return "";
		}
		return input.substring(start_offset,end_offset+1);
	}

}
