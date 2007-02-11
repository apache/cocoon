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
package org.apache.cocoon.xml.xlink;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class extends the XLink semantic capabilities to understand those
 * elements that are have default linking semantics associated.
 *
 * This class reacts on 'href' and 'src' attributes and is able to understand
 * the semantics of XHTML/WML/SMIL/SVG and all the rest of the languages that
 * use either XLink of the above attributes.
 *
 * NOTE: this class is clearly a hack and is not future compatible, but
 * since many XML formats to date are not compatible with the XLink semantics
 * this is what we have to do to live in the bleeding edge. Once there will
 * be a way to remove this, that will be a happy day for XML and for Cocoon too.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: ExtendedXLinkPipe.java,v 1.1 2003/03/09 00:09:48 pier Exp $
 */
public abstract class ExtendedXLinkPipe extends XLinkPipe {

    public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
        if (uri != null) {
            // Get namespaced attributes

            String href = attr.getValue(uri, "href");
            if (href != null) {
                simpleLink(href, null, null, null, null, null, uri, name, raw, attr);
                return;
            }

            String src = attr.getValue(uri, "src");
            if (src != null) {
                simpleLink(src, null, null, null, null, null, uri, name, raw, attr);
                return;
            }

            String background = attr.getValue(uri, "background");
            if (background != null) {
                simpleLink(background, null, null, null, null, null, uri, name, raw, attr);
                return;
            }
        } else {
            uri = "";
        }

        // Get attributes without namespace too

        String href = attr.getValue("", "href");
        if (href != null) {
            simpleLink(href, null, null, null, null, null, uri, name, raw, attr);
            return;
        }

        String src = attr.getValue("", "src");
        if (src != null) {
            simpleLink(src, null, null, null, null, null, uri, name, raw, attr);
            return;
        }

        String background = attr.getValue("", "background");
        if (background != null) {
            simpleLink(background, null, null, null, null, null, uri, name, raw, attr);
            return;
        }

        super.startElement(uri, name, raw, attr);
    }

    public void simpleLink(String href, String role, String arcrole, String title, String show, String actuate, String uri, String name, String raw, Attributes attr) throws SAXException {
        AttributesImpl newattr = new AttributesImpl(attr);
        int hrefIndex = attr.getIndex(uri, "href");
        if (hrefIndex > -1) newattr.setValue(hrefIndex, href);
        int srcIndex = attr.getIndex(uri, "src");
        if (srcIndex > -1) newattr.setValue(srcIndex, href);
        int backgroundIndex = attr.getIndex(uri, "background");
        if (backgroundIndex > -1) newattr.setValue(backgroundIndex, href);
        super.startElement(uri, name, raw, newattr);
    }
}
