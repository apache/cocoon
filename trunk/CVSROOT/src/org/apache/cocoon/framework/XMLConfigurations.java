/*-- $Id: XMLConfigurations.java,v 1.1 2000-02-03 08:05:25 balld Exp $ --

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class makes a Configurations object from an XML source.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1 $ $Date: 2000-02-03 08:05:25 $
 */

public class XMLConfigurations extends Configurations {

	Hashtable values = new Hashtable();
	Hashtable children = new Hashtable();
	String my_prefix = null;

	public XMLConfigurations() {}

    public XMLConfigurations(String file) throws Exception {
        InputStream input = new FileInputStream(file);
		org.apache.xerces.parsers.DOMParser parser = 
			new org.apache.xerces.parsers.DOMParser();
		parser.parse(new InputSource(input));
		buildConfigurations(this,parser.getDocument().getDocumentElement());
        input.close();
    }

	protected static void buildConfigurations(XMLConfigurations confs, Node source_node) {
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
						switch(type) {
							case Node.TEXT_NODE:
								buffer.append(child_node.getNodeValue());
								break;
							case Node.ELEMENT_NODE:
								has_grandchildren = true;
								break;
						}
								
					}
					confs.values.put(name,buffer.toString());
					if (has_grandchildren) {
						XMLConfigurations child_confs = new XMLConfigurations();
						child_confs.setBasename(confs.my_prefix+'.'+name);
						buildConfigurations(child_confs,node);
						confs.children.put(name,child_confs);
					}
					break;
			}
		}
	}

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

    public Object get(String key, Object def) {
        Object o = this.get(key);
        return (o == null) ? def : o;
    }

    public Object getNotNull(String key) {
        Object o = this.get(key);
        if (o == null) {
			throw new RuntimeException("Cocoon configuration item '" + key + "' is not set");
        } else {
            return o;
        }
    }

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
     * Create a subconfiguration starting from the base node.
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

    public void setBasename(String my_prefix) {
        this.my_prefix = my_prefix;
    }
}
