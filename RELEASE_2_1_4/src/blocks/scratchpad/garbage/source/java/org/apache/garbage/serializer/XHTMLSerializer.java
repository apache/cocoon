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
 * @version CVS $Id: XHTMLSerializer.java,v 1.1 2003/09/04 12:42:45 cziegeler Exp $
 */
public class XHTMLSerializer extends XMLSerializer {

    /** The namespace URI for XHTML 1.0. */
    public static final String XHTML1_NAMESPACE =
            "http://www.w3.org/1999/xhtml";

    /** A representation of the XHTML 1.0 strict document type. */
    public static final DocType XHTML1_DOCTYPE_STRICT = new DocType(
            "html", "-//W3C//DTD XHTML 1.0 Strict//EN",
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");

    /** A representation of the XHTML 1.0 transitional document type. */
    public static final DocType XHTML1_DOCTYPE_TRANSITIONAL = new DocType(
            "html", "-//W3C//DTD XHTML 1.0 Transitional//EN",
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");

    /** A representation of the XHTML 1.0 frameset document type. */
    public static final DocType XHTML1_DOCTYPE_FRAMESET = new DocType(
            "html", "-//W3C//DTD XHTML 1.0 Frameset//EN",
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd");

    /* ====================================================================== */

    /**
     * Create a new instance of this <code>XHTMLSerializer</code>
     */
    public XHTMLSerializer() {
        super();
    }

    /**
     * Return the MIME Content-Type produced by this serializer.
     */
    public String getContentType() {
        if (encoding == null) return("text/html");
        return("text/html; charset=" + encoding);
    }

    /* ====================================================================== */

    /**
     * Receive notification of the beginning of the document body.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public void body(String uri, String local, String qual)
    throws SAXException {
        if (this.doctype == null) this.doctype = XHTML1_DOCTYPE_TRANSITIONAL;
        if (this.namespaces.getUri("").length() == 0) {
            this.namespaces.push("", XHTML1_NAMESPACE);
        }
        super.body(uri, local, qual);
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     * @param namespaces An array of <code>String</code> objects containing
     *                   the namespaces to be declared by this element.
     * @param namespaces An array of <code>String</code> objects containing
     *                   all attributes of this element.
     */
    public void startElementImpl(String uri, String local, String qual,
                                 String namespaces[][], String attributes[][])
    throws SAXException {
        if (uri.length() == 0) uri = XHTML1_NAMESPACE;
        super.startElementImpl(uri, local, qual, namespaces, attributes);
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public void endElementImpl(String uri, String local, String qual)
    throws SAXException {
        if (uri.length() == 0) uri = XHTML1_NAMESPACE;

        if (XHTML1_NAMESPACE.equals(uri)) {
            if ((local.equalsIgnoreCase("script")) ||
                (local.equalsIgnoreCase("style"))) {
                this.closeElement(false);
            } else if (local.equalsIgnoreCase("head")) {
                String loc = "meta";
                String pre = namespaces.getPrefix(XHTML1_NAMESPACE);
                String qua = namespaces.qualify(XHTML1_NAMESPACE, loc, "meta");
                String nsp[][] = new String[0][0];
                String att[][] = new String[2][ATTRIBUTE_LENGTH];

                att[0][ATTRIBUTE_NSURI] = att[1][ATTRIBUTE_NSURI] = "";
                att[0][ATTRIBUTE_LOCAL] = att[0][ATTRIBUTE_QNAME] = "http-equiv";
                att[1][ATTRIBUTE_LOCAL] = att[1][ATTRIBUTE_QNAME] = "content";
                att[0][ATTRIBUTE_VALUE] = "Content-Type";
                att[1][ATTRIBUTE_VALUE] = this.getContentType();

                this.closeElement(false);
                this.startElementImpl(XHTML1_NAMESPACE, loc, qua, nsp, att);
                this.endElementImpl(XHTML1_NAMESPACE, loc, qua);
            }
        }
        super.endElementImpl(uri, local, qual);
    }
}
