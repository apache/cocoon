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
package org.apache.cocoon.xml;

import org.xml.sax.SAXException;

/**
 * This utility class is used to keep track namespaces declarations and resolve
 * namespaces names.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: NamespacesTable.java,v 1.2 2003/09/24 21:41:11 cziegeler Exp $
 */
public class NamespacesTable {
    /** The initial namespace declaration. */
    private Entry entry=null;

    /**
     * Construct a new <code>NamespacesTable</code> instance.
     */
    public NamespacesTable() {
        super();
        this.entry=Entry.create("","");
        // Set the previous declaration of this namespace as self, so it will
        // not be possible to remove it :)
        this.entry.previousDeclaration=this.entry;
        this.addDeclaration("xml", "http://www.w3.org/XML/1998/namespace");
    }

    /**
     * Declare a new namespace prefix-uri mapping.
     *
     * @return The newly added <code>Declaration</code>.
     */
    public Declaration addDeclaration(String prefix, String uri) {
        Entry e=Entry.create(prefix,uri);
        Entry previous=null;
        Entry current=this.entry;
        while (current!=null) {
            if (current.prefixHash==e.prefixHash) {
                // Set the current entry to be the previous declaration for the
                // specified prefix and remove it from the chain.
                e.previousDeclaration=current;
                e.nextEntry=current.nextEntry;
                current.nextEntry=null;
                // Set the new entry in the chain
                if (previous==null) this.entry=e;
                else previous.nextEntry=e;
                return(e);
            } else {
                previous=current;
                current=current.nextEntry;
            }
        }
        if (previous==null) this.entry=e;
        else previous.nextEntry=e;
        return(e);
    }

    /**
     * Undeclare a namespace prefix-uri mapping.
     * <br>
     * If the prefix was previously declared mapping another URI, its value
     * is restored.
     *
     * @return The removed <code>Declaration</code> or <b>null</b>.
     */
    public Declaration removeDeclaration(String prefix) {
        int hash=prefix.hashCode();
        Entry previous=null;
        Entry current=this.entry;
        while (current!=null) {
            if (current.prefixHash==hash) {
                if (current.previousDeclaration==null) {
                    if (previous==null) this.entry=current.nextEntry;
                    else previous.nextEntry=current.nextEntry;
                } else {
                    current.previousDeclaration.nextEntry=current.nextEntry;
                    if (previous==null) this.entry=current.previousDeclaration;
                    else previous.nextEntry=current.previousDeclaration;
                }
                return(current);
            } else {
                previous=current;
                current=current.nextEntry;
            }
        }
        return(null);
    }

    /**
     * Return the URI associated with the given prefix or <b>null</b> if the
     * prefix was not mapped.
     */
    public String getUri(String prefix) {
        int hash=prefix.hashCode();
        Entry current=this.entry;
        while (current!=null) {
            if(current.prefixHash==hash) return(current.uri);
            else current=current.nextEntry;
        }
        return(null);
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
        int hash=uri.hashCode();
        Entry current=this.entry;
        int count=0;
        while (current!=null) {
            if(current.uriHash==hash) count++;
            current=current.nextEntry;
        }
        if (count==0) return(new String[0]);
        String prefixes[]=new String[count];
        count=0;
        while (current!=null) {
            if(current.uriHash==hash) prefixes[count++]=current.prefix;
            current=current.nextEntry;
        }
        return(prefixes);
    }


    /**
     * Return one of the prefixes currently mapped to the specified URI or
     * <b>null</b>.
     */
    public String getPrefix(String uri) {
        int hash=uri.hashCode();
        Entry current=this.entry;
        while (current!=null) {
            if(current.uriHash==hash) return(current.prefix);
            current=current.nextEntry;
        }
        return(null);
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
        /** The URI hashcode. */
        protected int uriHash=0;
        /** The prefix hashcode. */
        protected int prefixHash=0;
        /** The URI string. */
        protected String uri="";
        /** The prefix string. */
        protected String prefix="";
        /** The previous declaration for the same prefix. */
        protected Entry previousDeclaration;
        /** The declaration following this one in the table. */
        protected Entry nextEntry;

        /** Create a new namespace declaration. */
        protected static Entry create(String prefix, String uri) {
            // Create a new entry
            Entry e=new Entry();
            // Set the prefix string and hash code.
            if (prefix!=null) e.prefix=prefix;
            e.prefixHash=e.prefix.hashCode();
            // Set the uri string and hash code.
            if (uri!=null) e.uri=uri;
            e.uriHash=e.uri.hashCode();
            // Return the entry
            return(e);
        }

        /** Return the namespace URI. */
        public String getUri() { return(this.uri); }
        /** Return the namespace prefix. */
        public String getPrefix() { return(this.prefix); }
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
        public String getUri() { return(this.uri); }
        /** Return the namespace prefix. */
        public String getPrefix() { return(this.prefix); }
        /** Return the namespace local name. */
        public String getLocalName() { return(this.local); }
        /** Return the namespace raw name. */
        public String getQName() { return(this.raw); }
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
