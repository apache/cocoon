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
 * @version CVS $Id: LinkSerializer.java,v 1.7 2004/03/08 14:03:32 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Serializer
 * @x-avalon.lifestyle type=pooled
 */
public class LinkSerializer extends ExtendedXLinkPipe implements Serializer {

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
