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
package org.apache.cocoon.serialization;

import org.apache.cocoon.Constants;
import org.apache.cocoon.xml.xlink.ExtendedXLinkPipe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: LinkSerializer.java,v 1.5 2003/12/06 21:22:09 cziegeler Exp $
 */

public class LinkSerializer
    extends ExtendedXLinkPipe
    implements Serializer {

    private PrintStream out;

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream out) throws IOException {
        this.out = new PrintStream(out);
    }

    /**
     * Get the mime-type of the output of this <code>Component</code>.
     */
    public String getMimeType() {
        return Constants.LINK_CONTENT_TYPE;
    }

    public void simpleLink(String href, String role, String arcrole, String title, String show, String actuate, String uri, String name, String raw, Attributes attr)
    throws SAXException {
        if (traversable(href)) {
            print(href);
        }
        super.simpleLink(href, role, arcrole, title, show, actuate, uri, name, raw, attr);
    }

    public void startLocator(String href, String role, String title, String label, String uri, String name, String raw, Attributes attr)
    throws SAXException {
        if (traversable(href)) {
            print(href);
        }
        super.startLocator(href, role, title, label, uri, name, raw, attr);
    }

    private boolean traversable(String href) {
        if (href.length() == 0) return false;
        if (href.charAt(0) == '#') return false;
        if (href.indexOf("://") != -1) return false;
        if (href.startsWith("mailto:")) return false;
        if (href.startsWith("news:")) return false;
        if (href.startsWith("javascript:")) return false;
        return true;
    }

    private void print(String href) {
        int ankerPos = href.indexOf('#');
        if (ankerPos == -1) {
            // TODO: Xalan encodes international characters into URL encoding
            out.println(href);
        } else {
            out.println(href.substring(0, ankerPos));
        }
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return false;
    }

    /**
     * Recyclable
     */
    public void recycle() {
        super.recycle();
        this.out = null;
    }
}
