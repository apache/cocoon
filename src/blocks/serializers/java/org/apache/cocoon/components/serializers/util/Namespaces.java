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
package org.apache.cocoon.components.serializers.util;

import org.xml.sax.SAXException;

/**
 * The <code>Namespaces</code> class is an utility class implementing a
 * stack for XML namespaces declarations.
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Namespaces.java,v 1.2 2004/06/02 07:42:27 tcurdt Exp $
 */
public class Namespaces {
    /** The array of all URIs in this stack. */
    private String uri[] = new String[512];
    /** The array of all prefixes in this stack. */
    private String pre[] = new String[512];
    /** The number of URI/prefix mappings in this stack. */
    private int depth = 0;
    /** The last "committed" namespace. */
    private int last = 0;

    /** The index of the namespace prefix in for <code>commit()</code>. */
    public static final int NAMESPACE_PREFIX = 0;
    /** The index of the namespace uri in for <code>commit()</code>. */
    public static final int NAMESPACE_URI = 1;

    /**
     * Create a new <code>Namespaces</code> instance.
     */
    public Namespaces() {
        super();

        this.push("", "");
        this.push("xml", "http://www.w3.org/XML/1998/namespace");
        this.last = this.depth;
    }

    /**
     * Push a new namespace declaration into this stack.
     *
     * @param prefix The prefix to associate with the specified URI.
     * @param uri The URI associated with the namespace.
     */
    public synchronized void push(String prefix, String uri) {
        if (this.depth == this.uri.length) {
            int newDepth = this.uri.length + (this.uri.length >> 1);
            String newUri[] = new String[newDepth];
            String newPre[] = new String[newDepth];
            System.arraycopy(this.uri, 0, newUri, 0, this.depth);
            System.arraycopy(this.pre, 0, newPre, 0, this.depth);
            this.uri = newUri;
            this.pre = newPre;
        }

        this.uri[this.depth] = uri;
        this.pre[this.depth] = prefix;
        this.depth ++;
    }

    /**
     * Pop a new namespace declaration out of this stack.
     * <br />
     * If more than one namespace is associated with the specified namespace,
     * only the last pushed namespace will be popped out.
     *
     * @param prefix The prefix to associate with the specified URI.
     * @throws SAXException If the prefix was not mapped in this stack.
     */
    public synchronized void pop(String prefix)
    throws SAXException {
        for (int x = this.position(prefix, pre); x < this.depth; x++) {
            int k = (x + 1);
            this.pre[x] = this.pre[k];
            this.uri[x] = this.uri[k];
        }
        //this.pre[this.depth] = null;
        //this.uri[this.depth] = null;
        this.last--;
        this.depth--;
    }

    /**
     * Qualify an XML name.
     * <br />
     * Given a URI, local name and qualified name as passed to the SAX
     * <code>ContentHandler</code> interface in the <code>startElement()</code>
     * method, this method will always return a valid XML name token usable
     * for serialization (checking namespaces URIs and prefixes).
     *
     * @param nsuri The Namespace URI, or the empty string if the element has
     *              no namespace URI or if namespace processing is not being
     *              performed.
     * @param local The local name (without prefix), or the empty string if
     *              namespace processing is not being performed.
     * @param qualified The qualified name (with prefix), or the empty string
     *                  if qualified names are not available.
     * @throws SAXException If the specified URI is not mapped with a prefix.
     */
    public String qualify(String nsuri, String local, String qualified)
    throws SAXException {
        if (nsuri == null) nsuri = "";
        if (local == null) local = "";
        if (qualified == null) qualified = "";

        /** No namespaces processing. */
        if ((nsuri.length() == 0 ) && (local.length() == 0)) return(qualified);

        /*
         * Get the prefix for the given namespace and return the qualified
         * name: "prefix:local" if prefix is not empty, "local" otherwise.
         */
        int position = position(nsuri, this.uri);
        if (this.pre[position].length() > 0) {
            return(this.pre[position] + ':' + local);
        }
        return(local);
    }

    /**
     * Checkpoint this stack, returning the list of all namespaces added since
     * the last <code>commit()</code> or <code>pop(...)</code> call.
     */
    public String[][] commit() {
        int size = this.depth - this.last;
        String result[][] = new String[size][2];
        int k = 0;
        for (int x = this.last; x < this.depth; x++) {
            result[k][NAMESPACE_PREFIX] = this.pre[x];
            result[k][NAMESPACE_URI]    = this.uri[x];
            k++;
        }
        this.last = this.depth;
        return(result);
    }

    /**
     * Return the namespace URI associated with the specified prefix.
     *
     * @throws SAXException If the prefix cannot be mapped.
     */
    public String getUri(String prefix)
    throws SAXException {
        return(this.uri[this.position(prefix, this.pre)]);
    }

    /**
     * Return the namespace prefix associated with the specified URI.
     *
     * @throws SAXException If the URI cannot be mapped.
     */
    public String getPrefix(String nsuri)
    throws SAXException {
        return(this.pre[this.position(nsuri, this.uri)]);
    }

    /**
     * Return the position of the given check <code>String</code> in the
     * specified <code>String</code> array.
     */
    private int position(String check, String array[])
    throws SAXException {
        int x = this.depth;
        while (true) {
            if (check.equals(array[--x])) return(x);
            if (x == 0) break;
        }
        throw new SAXException("Unable to map \"" + check + "\"");
    }

}
