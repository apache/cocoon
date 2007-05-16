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
package org.apache.cocoon.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Keeps track of namespaces declarations and resolve namespaces names.
 * <p>
 * This class also provides a very convenient and safe way of handling
 * namespace declarations in SAX pipes. It also allows to filter duplicate namespace
 * declarations that too often clutter up XML documents that went through
 * several transformations, and avoid useless namespace declarations that aren't followed
 * by element events.
 * <p>
 * Usage example in a SAX pipe:
 * <pre>
 *   NamespacesTable namespaces = new NamespacesTable();
 *   ContentHandler nextHandler;
 *
 *   public void startPrefixMapping(String prefix, String uri) throws SAXException {
 *       namespaces.addDeclaration(prefix, uri);
 *   }
 *
 *   public void startElement(...) throws SAXException {
 *       // automatically start mappings for this scope
 *       namespaces.enterScope(nextHandler);
 *       nextHandler.startElement(...);
 *   }
 *
 *   public void endElement(...) throws SAXException {
 *       nextHandler.endElement(...);
 *       // automatically end mappings for this scope
 *       namespaces.leaveScope(nextHandler);
 *   }
 *
 *   public void endPrefixMapping(String prefix) throws SAXException {
 *       // Ignore, it is handled by leaveScope()
 *   }
 * </pre>
 *
 * @version $Id$
 */
public class NamespacesTable {
    /** The last namespace declaration. */
    private Entry lastEntry;
    
    /** The entry that start the prefix mappings for the scope that's about to be entered
     * or was just left.
     */
    private Entry lastDeclaredEntry;

    private boolean usesScopes = false;

    /**
     * Construct a new <code>NamespacesTable</code> instance.
     */
    public NamespacesTable() {
        clear();
    }

    /**
     * Clear and reinitialize this namespace table before reuse.
     *
     * @since 2.1.8
     */
    public void clear() {
        this.lastEntry = Entry.create("","");
        this.addDeclaration("xml", "http://www.w3.org/XML/1998/namespace");
        // Lock this scope
        this.lastEntry.closedScopes = 1;
    }

    /**
     * Declare a new namespace prefix-uri mapping.
     *
     * @return The newly added <code>Declaration</code>.
     */
    public Declaration addDeclaration(String prefix, String uri) {
        // Find a previous declaration of the same prefix
        Entry dup = this.lastEntry;
        while (dup != null && !dup.prefix.equals(prefix)) {
            dup = dup.previous;
        }

        if (dup != null) {
            if (usesScopes && dup.uri.equals(uri)) {
                return dup;
            }
            dup.overriden = true;
        }

        Entry e = Entry.create(prefix, uri);
        e.previous = this.lastEntry;
        e.overrides = dup;
        this.lastEntry = e;
        // this always starts the declared prefix chain
        this.lastDeclaredEntry = e;
        return e;
    }

    /**
     * Undeclare a namespace prefix-uri mapping. If the prefix was previously declared
     * mapping another URI, its value is restored.
     * <p>
     * When using {@link #enterScope()}/{@link #leaveScope()}, this method does nothing and always
     * returns <code>null</code>, as declaration removal is handled in {@link #leaveScope()}.
     *
     * @return the removed <code>Declaration</code> or <b>null</b>.
     */
    public Declaration removeDeclaration(String prefix) {
        if (usesScopes) {
            // Automatically handled in leaveScope
            return null; // or throw and IllegalStateException if enterScope(handler) was used?
        }

        Entry current = this.lastEntry;
        Entry afterCurrent = null;
        while(current != null) {
            if (current.closedScopes > 0) {
                // Don't undeclare mappings not declared in this scope
                return null;
            }

            if (current.prefix.equals(prefix)) {
                // Got it
                // Remove it from the chain
                if (afterCurrent != null) {
                    afterCurrent.previous = current.previous;
                }
                // And report closed scopes on the previous entry
                current.previous.closedScopes += current.closedScopes;
                Entry overrides = current.overrides;
                if (overrides != null) {
                    // No more overriden
                    overrides.overriden = false;
                }

                if (this.lastDeclaredEntry == current) {
                    if (current.previous.closedScopes == 0) {
                        this.lastDeclaredEntry = current.previous;
                    } else {
                        this.lastDeclaredEntry = null;
                    }
                }

                if (this.lastEntry == current) {
                    this.lastEntry = current.previous;
                }

                return current;
            }

            afterCurrent = current;
            current = current.previous;
        }

        // Not found
        return null;
    }

    /**
     * Enter a new scope. This starts a new, empty list of declarations for the new scope.
     * <p>
     * Typically called in a SAX handler <em>before</em> sending a <code>startElement()</code>
     * event.
     *
     * @since 2.1.8
     */
    public void enterScope() {
        this.usesScopes = true;
        this.lastEntry.closedScopes++;
        this.lastDeclaredEntry = null;
    }

    /**
     * Start all declared mappings of the current scope and enter a new scope.  This starts a new,
     * empty list of declarations for the new scope.
     * <p>
     * Typically called in a SAX handler <em>before</em> sending a <code>startElement()</code>
     * event.
     *
     * @param handler the handler that will receive startPrefixMapping events.
     * @throws SAXException
     * @since 2.1.8
     */
    public void enterScope(ContentHandler handler) throws SAXException {
        this.usesScopes = true;
        Entry current = this.lastEntry;
        while (current != null && current.closedScopes == 0) {
            handler.startPrefixMapping(current.prefix, current.uri);
            current = current.previous;
        }
        this.lastEntry.closedScopes++;
        this.lastDeclaredEntry = null;
    }

    /**
     * Leave a scope. The namespace declarations that occured before the corresponding
     * <code>enterScope()</code> are no more visible using the resolution methods, but
     * still available using {@link #getCurrentScopeDeclarations()} until the next call
     * to {@link #addDeclaration(String, String)} or {@link #enterScope()}.
     * <p>
     * Typically called in a SAX handler <em>after</em> sending a <code>endElement()</code>
     * event.
     *
     * @since 2.1.8
     */
    public void leaveScope() {
        Entry current = this.lastEntry;

        // Purge declarations that were added but not included in a scope
        while (current.closedScopes == 0) {
            current = current.previous;
        }

        current.closedScopes--;

        if (current.closedScopes == 0) {
            this.lastDeclaredEntry = current;
        } else {
            // More than one scope closed here: no local declarations
            this.lastDeclaredEntry = null;
        }

        while (current != null && current.closedScopes == 0) {
            Entry overrides = current.overrides;
            if (overrides != null) {
                // No more overriden
                overrides.overriden = false;
            }
            current = current.previous;
        }
        this.lastEntry = current;
    }

    /**
     * Leave a scope. The namespace declarations that occured before the corresponding
     * <code>enterScope()</code> are no more visible using the resolution methods, but
     * still available using {@link #getCurrentScopeDeclarations()} until the next call
     * to {@link #addDeclaration(String, String)} or {@link #enterScope()}.
     * <p>
     * Typically called in a SAX handler <em>after</em> sending a <code>endElement()</code>
     * event.
     *
     * @param handler the handler that will receive endPrefixMapping events.
     * @throws SAXException
     * @since 2.1.8
     */
    public void leaveScope(ContentHandler handler) throws SAXException {
        Entry current = this.lastEntry;
        
        // Purge declarations that were added but not included in a scope
        while (current.closedScopes == 0) {
            current = current.previous;
        }

        current.closedScopes--;

        if (current.closedScopes == 0) {
            this.lastDeclaredEntry = current;
        } else {
            // More than one scope closed here: no local declarations
            this.lastDeclaredEntry = null;
        }

        while (current != null && current.closedScopes == 0) {
            handler.endPrefixMapping(current.prefix);
            Entry overrides = current.overrides;
            if (overrides != null) {
                // No more overriden
                overrides.overriden = false;
            }
            current = current.previous;
        }

        this.lastEntry = current;
    }

    private static final Declaration[] NO_DECLS = new Declaration[0];

    /**
     * Get the declarations that were declared within the current scope.
     *
     * @return the declarations (possibly empty, but never null)
     * @since 2.1.8
     */
    public Declaration[] getCurrentScopeDeclarations() {
        int count = 0;
        Entry current = this.lastDeclaredEntry;
        while (current != null && current.closedScopes == 0) {
            count++;
            current = current.previous;
        }

        if (count == 0) return NO_DECLS;

        Declaration[] decls = new Declaration[count];
        count = 0;
        current = this.lastDeclaredEntry;
        while (current != null && current.closedScopes == 0) {
            decls[count++] = current;
            current = current.previous;
        }
        return decls;
    }

    /**
     * Return the URI associated with the given prefix or <b>null</b> if the
     * prefix was not mapped.
     */
    public String getUri(String prefix) {
        Entry current = this.lastEntry;
        while (current != null) {
            if (current.prefix.equals(prefix)) {
                return current.uri;
            }
            current = current.previous;
        }

        // Not found
        return null;
    }

    /**
     * Return an array with all prefixes currently mapped to the specified URI.
     * <br>
     * The array length might be <b>zero</b> if no prefixes are associated with
     * the specified uri.
     *
     * @return A <b>non-null</b> <code>String</code> array.
     */
    public String[] getPrefixes(String uri) {

        Entry current=this.lastEntry;
        int count=0;
        while (current!=null) {
            if(!current.overriden && current.uri.equals(uri))
                count++;
            current=current.previous;
        }
        if (count==0) return(new String[0]);

        String prefixes[]=new String[count];
        count=0;
        current = this.lastEntry;
        while (current!=null) {
            if(!current.overriden && current.uri.equals(uri))
                prefixes[count++] = current.prefix;
            current = current.previous;
        }
        return prefixes;
    }


    /**
     * Return one of the prefixes currently mapped to the specified URI or
     * <b>null</b>.
     */
    public String getPrefix(String uri) {
        Entry current = this.lastEntry;
        while (current != null) {
            if(!current.overriden && current.uri.equals(uri))
                return current.prefix;
            current = current.previous;
        }
        return null;
    }

    /**
     * Resolve a namespace-aware name against the current namespaces
     * declarations.
     *
     * @param uri The namespace URI or <b>null</b> if not known.
     * @param raw The raw (complete) name or <b>null</b> if not known.
     * @param prefix The namespace prefix or <b>null</b> if not known.
     * @param local The local name or <b>null</b> if not known.
     * @return A <b>non-null</b> <code>Name</code>.
     * @exception SAXException If the name cannot be resolved.
     */
    public Name resolve(String uri, String raw, String prefix, String local)
    throws SAXException {
        if (uri==null) uri="";
        if (raw==null) raw="";
        if (prefix==null) prefix="";
        if (local==null) local="";
        // Start examining the URI
        if (raw.length()>0) {
            // The raw name was specified
            int pos=raw.indexOf(':');
            if (pos>0) {
                // We have a namespace prefix:local separator
                String pre=raw.substring(0,pos);
                String loc=raw.substring(pos+1);
                if (prefix.length()==0) prefix=pre;
                else if (!prefix.equals(pre))
                    throw new SAXException("Raw/Prefix mismatch");
                if (local.length()==0) local=loc;
                else if (!local.equals(loc))
                    throw new SAXException("Raw/Local Name mismatch");
            } else {
                // We don't have a prefix:local separator
                if (prefix.length()>0)
                    throw new SAXException("Raw Name/Prefix mismatch");
                if (local.length()==0) local=raw;
                else if (!local.equals(raw))
                    throw new SAXException("Raw Name/Local Name mismatch");
            }
        } else {
            // The raw name was not specified
            if (local.length()==0) throw new SAXException("No Raw/Local Name");
            if (prefix.length()==0) raw=local;
            else raw=prefix+':'+local;
        }
        // We have resolved and checked data between the raw, local, and
        // prefix... We have to doublecheck the namespaces.
        if (uri.length()>0) {
            // We have a URI and a prefix, check them
            if ((prefix.length()>0) &&  (!uri.equals(this.getUri(prefix)))) {
                throw new SAXException("URI/Prefix mismatch [" + prefix + "," + uri + "]");
            } else {
                String temp=this.getPrefix(uri);
                if (temp==null) throw new SAXException("URI not declared");
                else if (temp.length()>0) {
                    prefix=temp;
                    raw=prefix+':'+local;
                }
            }
        } else {
            // We don't have a URI, check if we can find one from the prefix.
            String temp=this.getUri(prefix);
            if (temp==null) throw new SAXException("Prefix not declared");
            else uri=temp;
        }
        NameImpl name=new NameImpl();
        if (uri.length() > 0) name.uri=uri;
        else name.uri=null;
        name.raw=raw;
        name.prefix=prefix;
        name.local=local;
        return(name);
    }

    /** The internal entry structure for this table. */
    private static class Entry implements Declaration {
        /** The URI string. */
        protected String uri="";
        /** The prefix string. */
        protected String prefix="";
        /** The previous declaration. */
        protected Entry previous;
        protected Entry overrides;
        protected int closedScopes = 0;
        protected boolean overriden = false;

        /** Create a new namespace declaration. */
        protected static Entry create(String prefix, String uri) {
            // Create a new entry
            Entry e = new Entry();
            // Set the prefix string.
            if (prefix != null) e.prefix=prefix;
            // Set the uri string.
            if (uri != null) e.uri=uri;
            // Return the entry
            return e;
        }

        /** Return the namespace URI. */
        public String getUri() { return this.uri; }
        /** Return the namespace prefix. */
        public String getPrefix() { return this.prefix; }
    }

    /** The default namespace-aware name declaration implementation */
    private static class NameImpl implements Name {
        /** The namespace URI. */
        protected String uri;
        /** The namespace prefix. */
        protected String prefix;
        /** The namespace local name. */
        protected String local;
        /** The namespace raw name. */
        protected String raw;

        /** Return the namespace URI. */
        public String getUri() { return this.uri; }
        /** Return the namespace prefix. */
        public String getPrefix() { return this.prefix; }
        /** Return the namespace local name. */
        public String getLocalName() { return this.local; }
        /** Return the namespace raw name. */
        public String getQName() { return this.raw; }
    }

    /**
     * A namespace-aware name. (This interface is used in conjunction
     * with <code>NamespacesTable</code>).
     */
    public interface Name {
        /** Return the namespace URI. */
        String getUri();
        /** Return the namespace prefix. */
        String getPrefix();
        /** Return the namespace local name. */
        String getLocalName();
        /** Return the namespace raw name. */
        String getQName();
    }

    /**
     * A namespace declaration. (This interface is used in conjunction
     * with <code>NamespacesTable</code>).
     */
    public interface Declaration {
        /** Return the namespace URI. */
        String getUri();
        /** Return the namespace prefix. */
        String getPrefix();
    }
}
