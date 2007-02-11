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
package org.apache.cocoon.portal.coplet.adapter.impl;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This is the adapter to use pipelines as coplets
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: AbstractCopletAdapter.java,v 1.6 2003/10/20 13:36:41 cziegeler Exp $
 */
public abstract class AbstractCopletAdapter 
    extends AbstractLogEnabled
    implements CopletAdapter, ThreadSafe, Serviceable {
	
    /** The service manager */
    protected ServiceManager manager;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get a configuration value
     * First the coplet data is queried and if it doesn't provide an
     * attribute with the given name, the coplet base data is used.
     */
    protected Object getConfiguration(CopletInstanceData coplet, String key) {
        CopletData copletData = coplet.getCopletData();
        Object data = copletData.getAttribute( key );
        if ( data == null) {
            data = copletData.getCopletBaseData().getCopletConfig().get( key );
        }
        return data;
    }
    
    /**
     * Implement this and not toSAX()
     */
    public abstract void streamContent(CopletInstanceData coplet, 
                                         ContentHandler contentHandler)
    throws SAXException; 
    
    public void toSAX(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException {
        Boolean bool = (Boolean) this.getConfiguration( coplet, "buffer" );
        Integer timeout = (Integer) this.getConfiguration( coplet, "timeout");
        if ( timeout != null ) {
            // if timeout is set we have to buffer!
            bool = Boolean.TRUE;
        }
        
        if ( bool != null && bool.booleanValue() ) {
            boolean read = false;
            XMLSerializer serializer = null;
            Object data = null;
            try {
                serializer = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);
                
                if ( timeout != null ) {
                    final int milli = timeout.intValue() * 1000;
                    LoaderThread loader = new LoaderThread(this, coplet, serializer);
                    Thread thread = new Thread(loader);
                    thread.start();
                    try {
                        thread.join(milli);
                    } catch (InterruptedException ignore) {
                    }
                    if ( loader.finished ) {
                        data = serializer.getSAXFragment();
                        read = true;
                    }
                } else {
                    this.streamContent( coplet, serializer );
                    data = serializer.getSAXFragment();
                    read = true;
                }
            } catch (ServiceException ce) {
                throw new SAXException("Unable to lookup xml serializer.", ce);
            } catch (Exception exception ) {
                this.getLogger().warn("Unable to get content of coplet: " + coplet.getId(), exception);
            } finally {
                this.manager.release( serializer );
            }
            
            if ( read ) {
                XMLDeserializer deserializer = null;
                try {
                    deserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                    if ( contentHandler instanceof XMLConsumer ) {
                        deserializer.setConsumer( (XMLConsumer)contentHandler );
                    } else {
                        LexicalHandler lh = (contentHandler instanceof LexicalHandler ? (LexicalHandler)contentHandler : null);
                        deserializer.setConsumer(  new ContentHandlerWrapper(contentHandler, lh));
                    }
                    deserializer.deserialize( data );
                } catch (ServiceException ce) {
                    throw new SAXException("Unable to lookup xml deserializer.", ce);
                } finally {
                    this.manager.release( deserializer );
                }
            } else {
                if ( !this.renderErrorContent(coplet, contentHandler)) {
                    // FIXME - get correct error message
                    contentHandler.startDocument();
                    XMLUtils.startElement( contentHandler, "p");
                    XMLUtils.data( contentHandler, "The coplet " + coplet.getId() + " is currently not available.");
                    XMLUtils.endElement(contentHandler, "p");
                    contentHandler.endDocument();                
                }
            }
        } else {
            this.streamContent( coplet, contentHandler );
        }
        
    }
    
    public void init(CopletInstanceData coplet) {
    }
    
    public void destroy(CopletInstanceData coplet) {
    }

    public void login(CopletInstanceData coplet) {
    }
        
    public void logout(CopletInstanceData coplet) {
    }
    
    /**
     * Render the error content for a coplet
     * @param coplet
     * @param handler
     * @return True if the error content has been rendered, otherwise false
     * @throws SAXException
     */
    protected boolean renderErrorContent(CopletInstanceData coplet, ContentHandler handler)
    throws SAXException {
        return false;
    }
}

final class LoaderThread implements Runnable {
    
    private  AbstractCopletAdapter adapter;
    private  ContentHandler        handler;
    private  CopletInstanceData    coplet;
    boolean  finished;
    Exception exception;

    public LoaderThread(AbstractCopletAdapter adapter, 
                         CopletInstanceData coplet,
                         ContentHandler handler) {
        this.adapter = adapter;
        this.coplet  = coplet;
        this.handler = handler;
    }
    
    public void run() {
        try {
            adapter.streamContent( this.coplet, this.handler );
        } catch (Exception local) {
            this.exception = local;
        } finally {
            this.finished = true;
        }
    }
    
}
