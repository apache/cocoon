/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.transformation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.excalibur.xmlizer.XMLizer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer records the content of all description elements
 * and tries to interpret them as valid XML.
 * It's actually a quick hack...
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RSSTransformer.java,v 1.5 2003/12/23 14:38:07 cziegeler Exp $
 */
public final class RSSTransformer
extends AbstractSAXTransformer
implements CacheableProcessingComponent {

    /** The xmlizer for converting html to xml */
    protected XMLizer xmlizer;
    
    /** The xml deserializer */
    protected XMLDeserializer deserializer;
    
    /** The filter */
    protected XMLConsumer filter;
    
    /**
     *  receive notification of start element event.
     **/
    public void startElement(String uri, String name, String raw, Attributes attributes)
    throws SAXException {
        super.startElement(uri,name,raw,attributes);
        if ("description".equals(name)) {
            this.startTextRecording();
        }
    }

    /**
     * receive notification of end element event.
     */
    public void endElement(String uri,String name,String raw)
    throws SAXException  {
        if ("description".equals(name)) {
            final String text = this.endTextRecording();
            final String html = "<html><body>"+text+"</body></html>";
            
            Object parsed = null;            
            XMLSerializer serializer = null; 
            try {
                serializer = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);
                InputStream inputStream = new ByteArrayInputStream(html.getBytes());
                this.xmlizer.toSAX(inputStream,
                                    "text/html",
                                    null,
                                    serializer);
                // if no exception occurs, everything is fine!
                parsed = serializer.getSAXFragment();
            } catch (Exception ignore) {
            } finally {
                this.manager.release( serializer );
            }
            if ( parsed != null ) {
                this.deserializer.setConsumer( this.filter );
                this.deserializer.deserialize( parsed );
            } else {
                this.sendTextEvent(text);
            }
        }
        super.endElement(uri,name,raw);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.manager.release( this.xmlizer );
        this.manager.release( this.deserializer );
        this.xmlizer = null;
        this.deserializer = null;
        this.filter = null;
        super.recycle();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                       Map objectModel,
                       String src,
                       Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        try {
            this.xmlizer = (XMLizer)this.manager.lookup(XMLizer.ROLE);
            this.deserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup component.", ce);
        }
    }

   class HTMLFilter extends IncludeXMLConsumer {
       
       int bodyCount = 0;
       
       public HTMLFilter(XMLConsumer consumer) {
           super(consumer);
       }

       public void startElement(String uri, String local, String qName, Attributes attr) throws SAXException {
           if (bodyCount > 0 ) {
               super.startElement(uri, local, qName, attr);
           } 
           if ("body".equalsIgnoreCase(local)) {
               bodyCount++;
           }
       }

       public void endElement(String uri, String local, String qName) throws SAXException {
           if ("body".equalsIgnoreCase(local)) {
               bodyCount--;
           }
           if (bodyCount > 0 ) {
               super.endElement(uri, local, qName );
           } 
       }

   }

    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#setupTransforming()
     */
    public void setupTransforming()
        throws IOException, ProcessingException, SAXException {
        super.setupTransforming();
        this.filter = new HTMLFilter( this.xmlConsumer );
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        return "1";
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

}
