/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * @version CVS $Id: RSSTransformer.java,v 1.6 2004/03/05 13:02:16 bdelacretaz Exp $
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
