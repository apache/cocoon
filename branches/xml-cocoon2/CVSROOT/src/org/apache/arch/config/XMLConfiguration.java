/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.util;

import java.util.*;
import org.w3c.dom.*;

/**
 * This class is a Configuration implementation that wraps around
 * a DOM tree which is supposed to be the memory representation of an XML
 * configuration file.
 *
 * It is the caller responsibility to come up with a way to create
 * the DOM tree since this class is only an utility wrapper and doesn't
 * have any parsing logic built-in (nor it should, for a clean design).
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:48 $
 */
 
public class XMLConfiguration extends AbstractConfiguration {

    private Node node;

    public XMLConfiguration(Node node, Node defaults) {
        this.node = this.merge(node, defaults);
    }

	public XMLConfiguration(Node node) {
        this.node = node;
    }
    
    public String getName() {
        return this.node.getNodeName();
    }
	
	public String getValue() {
        String value = this.node.getNodeValue();
        if (value != null) {
            return value;
        } else {
            throw new IllegalStateException("Current configuration has no associated value: " + getName());
        }
    }

	public String getAttribute(String name) {
        Node attribute = this.node.getAttributes().getNamedItem(name);
        if (attribute != null) {
            return attribute.getNodeValue();
        } else {
            throw new IllegalStateException("Requested attribute not found: " + name);
        }
    }
    
    public Configuration getConfiguration(String name) {
        NodeList list = this.getNodes(name);
        if (list.getLength() > 1) {
            throw new IllegalStateException("Requested configuration has multiple instances. Use getConfigurations() instead.");
        } else {
            return new XMLConfiguration(list.item(0));
        }
    }

	public Enumeration getConfigurations(String name) {
        return new NodeListEnumerator(this.getNodes(name));
    }
    
    private Node merge(Node conf, Node defs) {
        // FIXME: Yet to be written!
        // this method should merge the two trees allowing conf nodes to replace
        // nodes in the defs tree if they represent the same instance.
        
        // for now
        return conf;
    }

    private NodeList getNodes(String name) {
        if ((name.startsWith("../")) || (name.startsWith("./"))) throw new IllegalArgumentException("Only absolute paths are allowed.");
        if (name.endsWith("/")) throw new IllegalArgumentException("Path terminating with separator are not allowed.");
        if (name.startsWith("/")) name = name.substring(1);
        StringTokenizer nameTokens = new StringTokenizer(name, "/");
        Node current = this.node;
        
        try {
            while (nameTokens.hasMoreTokens()) {
                String token = nameTokens.nextToken();
                current = getNode(current.getChildNodes(), token);
            }
        } catch (DOMException e) {
            throw new IllegalArgumentException("Error found processing node \"" + name + "\": " + e.getMessage());
        } catch (NodeNotFoundException e) {
            throw new IllegalArgumentException("No configuration found associated to given name: " + name);
        } catch (TooManyNodesFoundException e) {
            throw new IllegalStateException("Element \"" + e.getMessage() + "\" found more than once. This name cannot lead to a unique configuration.");
        }
        
        return current.getChildNodes();
    }
    
    private Node getNode(NodeList list, String name) {
        Node node = null;
        int length = list.getLength();
        int counter = 0;
        
        for (int i = 0; i < length; i++) {
            Node n = list.item(i);
            if (n.getNodeName().equals(name)) {
                node = n;
                counter++;
            }
        }
        
        if (counter == 0) throw new NodeNotFoundException();
        if (counter > 1) throw new TooManyNodesFoundException(name);
        
        return node;
    }
    
    class NodeNotFoundException extends RuntimeException {}

    class TooManyNodesFoundException extends RuntimeException {
        public TooManyNodesFoundException(String message) {
            super(message);
        }
    }
    
    class NodeListEnumerator implements Enumeration {
        NodeList list;
        int counter;
        int length;
        
        public NodeListEnumerator(NodeList list) {
            this.list = list;
            this.counter = 0;
            this.length = list.getLength();
        }
        
        public boolean hasMoreElements() {
            return counter < length;
        }
        
        public Object nextElement() {
            return new XMLConfiguration(list.item(counter++));
        }
    }
}