/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.serialization.HTMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * This is a special serializer that allows to include content that is not XML at the 
 * last possible point.
 * This is very useful for the portlets as they don't deliver valid XML but HTML.
 *
 * The trick is to insert a special token in the characters of the SAX stream: '~~'.
 * This token is filtered later on and replaced with the complete content of the portlet.
 *
 * @version $Id$
 */
public class IncludingHTMLSerializer 
    extends HTMLSerializer {
	
    public static final ThreadLocal portlets = new ThreadLocal();

    public static final String NAMESPACE = "http://apache.org/cocoon/portal/include";

    protected LinkedList orderedPortletList = new LinkedList();

    protected static final char[] tokens = new char[] {ReplacingOutputStream.token, ReplacingOutputStream.token};

    /** The encoding. */
    protected String encoding = "utf-8";
 
    /**
     * @see org.apache.cocoon.serialization.HTMLSerializer#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        super.configure(conf);
        this.encoding = conf.getChild("encoding").getValue(this.encoding);
    }

    /**
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();
        Map map = (Map)portlets.get();
        if ( map != null ) {
            map.clear();
        }
        this.orderedPortletList.clear();
    }

    /**
     * Add a portlet.
     */
    public static void addPortlet(String name, String content) {
        Map map = (Map) portlets.get();
        if ( map == null ) {
            map = new HashMap();
            portlets.set(map);
        }
        map.put(name, content);
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (!NAMESPACE.equals(uri)) {
            super.endElement(uri, loc, raw);            
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        if (!NAMESPACE.equals(uri)) {
            super.startElement(uri, loc, raw, a);
        } else {
            final String portletId = a.getValue("portlet");

            String value = null;
            Map map = (Map)portlets.get();
            if ( map != null ) {
                value = (String)map.get(portletId);
            }
            if ( value != null ) {
                this.orderedPortletList.addFirst(value);
                this.characters(tokens, 0, tokens.length);
            }
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapOutputComponent#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream stream) throws IOException {
        super.setOutputStream(new ReplacingOutputStream(stream,
                                                        this.orderedPortletList,
                                                        this.encoding));
    }

}
