/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.serializer;

import org.apache.garbage.serializer.util.DocType;
import org.xml.sax.SAXException;


/**
 *
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: HTMLSerializer.java,v 1.1 2003/09/04 12:42:45 cziegeler Exp $
 */
public class HTMLSerializer extends XHTMLSerializer {

    /** A representation of the HTML 4.01 strict document type. */
    public static final DocType HTML401_DOCTYPE_STRICT = new DocType(
            "HTML", "-//W3C//DTD HTML 4.01//EN",
            "http://www.w3.org/TR/html4/strict.dtd");

    /** A representation of the HTML 4.01 transitional document type. */
    public static final DocType HTML401_DOCTYPE_TRANSITIONAL = new DocType(
            "HTML", "-//W3C//DTD HTML 4.01 Transitional//EN",
            "http://www.w3.org/TR/html4/loose.dtd");

    /** A representation of the HTML 4.01 frameset document type. */
    public static final DocType HTML401_DOCTYPE_FRAMESET = new DocType(
            "HTML", "-//W3C//DTD HTML 4.01 Frameset//EN",
            "http://www.w3.org/TR/html4/frameset.dtd");

    /* ====================================================================== */

    /**
     * Create a new instance of this <code>HTMLSerializer</code>
     */
    public HTMLSerializer() {
        super();
    }

    /* ====================================================================== */

    /** Empty namespaces declaration. */
    private static final String NAMESPACES[][] = new String[0][0];

    /** Check if the URI is allowed by this serializer. */
    private boolean checkNamespace(String nsuri) {
        if (nsuri.length() == 0) return(true);
        if (XHTML1_NAMESPACE.equals(nsuri)) return(true);
        return(false);
    }

    /* ====================================================================== */

    /**
     * Write the XML document header.
     * <p>
     * This method overrides the default <code>XMLSerializer</code>.behaviour.
     * </p>
     */
    public void head()
    throws SAXException {
        // NO NOTHING!
    }

    /**
     * Receive notification of the beginning of the document body.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public void body(String nsuri, String local, String qual)
    throws SAXException {
        String name = local.toUpperCase();
        if (! this.checkNamespace(nsuri)) {
            throw new SAXException("Unsupported namespace \"" + nsuri + "\" "
                                   + "for HTML root element \"" + qual + "\""
                                   + this.getLocation());
        }

        if (this.doctype == null) {
            this.doctype = HTML401_DOCTYPE_TRANSITIONAL;
        } else if (XHTML1_DOCTYPE_STRICT.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_STRICT;
        } else if (XHTML1_DOCTYPE_TRANSITIONAL.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_TRANSITIONAL;
        } else if (XHTML1_DOCTYPE_FRAMESET.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_FRAMESET;
        } else {
            this.doctype = new DocType(this.doctype.getName().toUpperCase(),
                                       this.doctype.getPublicId(),
                                       this.doctype.getSystemId());
        }
        super.body(XHTML1_NAMESPACE, name, name);
    }


    /**
     * Receive notification of the beginning of an element.
     */
    public void startElementImpl(String nsuri, String local, String qual,
                             String namespaces[][], String attributes[][])
    throws SAXException {
        String name = local.toUpperCase();
        if (! this.checkNamespace(nsuri)) {
            throw new SAXException("Unsupported namespace \"" + nsuri + "\" "
                                   + "for HTML element \"" + qual + "\""
                                   + this.getLocation());
        }

        int length = 0;
        for (int x = 0; x < attributes.length; x ++) {
            if (checkNamespace(attributes[x][ATTRIBUTE_NSURI])) length ++;
        }

        String at[][] = new String[length][ATTRIBUTE_LENGTH];
        length = 0;
        for (int x = 0; x < attributes.length; x ++) {
            if (!checkNamespace(attributes[x][ATTRIBUTE_NSURI])) continue;

            String at_name = attributes[x][ATTRIBUTE_LOCAL].toLowerCase();
            at[length][ATTRIBUTE_NSURI] = XHTML1_NAMESPACE;
            at[length][ATTRIBUTE_LOCAL] = at_name;
            at[length][ATTRIBUTE_QNAME] = at_name;
            at[length][ATTRIBUTE_VALUE] = attributes[x][ATTRIBUTE_VALUE];
            length++;
        }

        super.startElementImpl(XHTML1_NAMESPACE, name, name, NAMESPACES, at);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElementImpl(String nsuri, String local, String qual)
    throws SAXException {
        this.closeElement(false);

        String name = local.toUpperCase();
        if (! this.checkNamespace(nsuri)) {
            throw new SAXException("Unsupported namespace \"" + nsuri + "\" "
                                   + "for HTML element \"" + qual + "\""
                                   + this.getLocation());
        }

        if (name.equals("AREA")) return;
        if (name.equals("BASE")) return;
        if (name.equals("BASEFONT")) return;
        if (name.equals("BR")) return;
        if (name.equals("COL")) return;
        if (name.equals("FRAME")) return;
        if (name.equals("HR")) return;
        if (name.equals("IMG")) return;
        if (name.equals("INPUT")) return;
        if (name.equals("ISINDEX")) return;
        if (name.equals("LINK")) return;
        if (name.equals("META")) return;
        if (name.equals("PARAM")) return;

        super.endElementImpl(XHTML1_NAMESPACE, name, name);
    }
}
