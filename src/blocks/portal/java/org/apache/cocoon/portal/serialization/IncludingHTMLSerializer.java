/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

*/
package org.apache.cocoon.portal.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: IncludingHTMLSerializer.java,v 1.1 2004/01/22 14:01:22 cziegeler Exp $
 */
public class IncludingHTMLSerializer 
    extends HTMLSerializer {
	
    public static final ThreadLocal portlets = new ThreadLocal();
    
    public static final String NAMESPACE = "http://apache.org/cocoon/portal/include";
    
    protected List orderedPortletList = new ArrayList(20);
    
    protected static final char token = '~';
    
    protected static final char[] tokens = new char[] {token, token};
    
    /* (non-Javadoc)
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
     * Add a portlet
     */
    public static void addPortlet(String name, String content) {
        Map map = (Map) portlets.get();
        if ( map == null ) {
            map = new HashMap();
            portlets.set(map);
        }
        map.put(name, content);
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (!NAMESPACE.equals(uri)) {
            super.endElement(uri, loc, raw);            
        }
    }

    /* (non-Javadoc)
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
                this.orderedPortletList.add(value);
                this.characters(tokens, 0, tokens.length);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapOutputComponent#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream stream) throws IOException {
        super.setOutputStream(new ReplacingOutputStream(stream, this.orderedPortletList));
    }

}
class ReplacingOutputStream extends OutputStream {
    
    /** Stream */
    protected OutputStream stream;
    
    protected boolean inKey;
    
    protected Iterator valueIterator;
    
    protected List orderedValues;
    
    /**
     * Constructor
     */
    public ReplacingOutputStream(OutputStream stream, List values) {
        this.stream = stream;    
        this.orderedValues = values;
        this.inKey = false;
    }
    
    /* (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
        this.stream.close();
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        this.stream.flush();
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if ( len == 0 ) {
            return;
        }
        if ( this.inKey ) {
            if ( b[off] == IncludingHTMLSerializer.token ) {
                this.writeNextValue();
                off++;
                len--;
            } else {
                this.write(IncludingHTMLSerializer.token);
            }
            this.inKey = false;
        }
        // search for key
        boolean end = false;
        do {
            int s = off;
            int e = off+len;
            while (s < e && b[s] != IncludingHTMLSerializer.token) {
                s++;
            }
            if ( s == e ) {
                this.stream.write(b, off, len);
                end = true;
            } else if ( s == e-1 ) {
                this.stream.write(b, off, len-1);
                this.inKey = true;
                end = true;                
            } else {
                if ( b[s+1] == IncludingHTMLSerializer.token) {
                    final int l = s-off;
                    this.stream.write(b, off, l);
                    off += (l+2);
                    len -= (l+2);
                    this.writeNextValue();
                    
                } else {
                    final int l = s-off+2;
                    this.stream.write(b, off, l);
                    off += l;
                    len -= l;
                }
                end = (len == 0);
            }
        } while (!end);
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
        if ( b == IncludingHTMLSerializer.token ) {
            if ( this.inKey ) {
                this.writeNextValue();
            } 
            this.inKey = !this.inKey;
        } else {
            if ( this.inKey ) {
                this.inKey = false;
                this.stream.write(IncludingHTMLSerializer.token);
            }
            this.stream.write(b);
        }
    }

    /** 
     * Write next value
     */
    protected void writeNextValue() throws IOException {
        if ( this.valueIterator == null ) {
            this.valueIterator = this.orderedValues.iterator();
        }
        final String value = (String)this.valueIterator.next();
        if ( value != null ) {
            this.stream.write(value.getBytes(), 0, value.length());
        }        
    }
}

