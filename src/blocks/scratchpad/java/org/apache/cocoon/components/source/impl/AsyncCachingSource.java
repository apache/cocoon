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
package org.apache.cocoon.components.source.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.ExtendedCachedResponse;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This class implements a proxy like source that uses another source
 * to get the content. This implementation can cache the content for
 * a given period of time
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AsyncCachingSource.java,v 1.1 2003/09/04 12:42:34 cziegeler Exp $
 * @since 2.1.1
 */
public class AsyncCachingSource
extends CachingSource
implements Source, Composable, Initializable, Disposable, XMLizable {

    protected SourceValidity validity;
    
    /**
     * Construct a new object
     */
    public AsyncCachingSource( String location,
                          Map    parameters) 
    throws MalformedURLException {
        super(location, parameters );
    }

    public void setResponse(CachedResponse r) {
        this.cachedResponse = (ExtendedCachedResponse) r;
    }
    
    /**
     * Get the last modification date.
     * @return The last modification in milliseconds since January 1, 1970 GMT
     *         or 0 if it is unknown
     */
    public long getLastModified() {
        return 0;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
    throws IOException, SourceException {
        byte[] response = this.cachedResponse.getResponse();
        if ( response == null ) {
            
            // the stream was not cached, so we *have* to get it here
            
            this.initSource();
            
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[2048];
            final InputStream inputStream = this.source.getInputStream();
            int length;
        
            while ((length = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, length);
            }
            baos.flush();
            inputStream.close();
            
            // update cache
            final byte[] xmlResponse = this.cachedResponse.getAlternativeResponse();
            this.cachedResponse = new ExtendedCachedResponse(this.cachedResponse.getValidityObjects(),
                                                             baos.toByteArray());
            this.cachedResponse.setAlternativeResponse(xmlResponse);
            try {
                this.cache.store(this.streamKey, this.cachedResponse);
            } catch (ProcessingException ignore) {
            }
            
            response = this.cachedResponse.getResponse();
        }
        return new ByteArrayInputStream(response);            
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        return this.validity;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.validity = this.cachedResponse.getValidityObjects()[0];
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.xml.sax.XMLizable#toSAX(org.xml.sax.ContentHandler)
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
       XMLDeserializer deserializer = null;
       try {
           deserializer = (XMLDeserializer) this.manager.lookup(XMLDeserializer.ROLE);
           if ( contentHandler instanceof XMLConsumer) {
               deserializer.setConsumer((XMLConsumer)contentHandler);
           } else {
               deserializer.setConsumer(new ContentHandlerWrapper(contentHandler));
           }
           deserializer.deserialize( this.cachedResponse.getAlternativeResponse() );
       } catch (ComponentException ce ) {
           throw new SAXException("Unable to lookup xml deserializer.", ce);
       } finally {
           this.manager.release(deserializer);
       }
    }

}
