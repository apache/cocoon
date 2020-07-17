/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.thread.RunnableManager;
import org.apache.cocoon.environment.CocoonRunnable;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import EDU.oswego.cs.dl.util.concurrent.CountDown;

/**
 * This is the abstract base adapter to use pipelines as coplets
 * 
 * <h2>Configuration</h2>
 * <table><tbody>
 * <tr>
 *   <th>buffer</th>
 *   <td>Shall the content of the coplet be buffered? If a coplet is
 *       buffered, errors local to the coplet are caught and a not 
 *       availability notice is delivered instead. Buffering does not
 *       cache responses for subsequent requests.</td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>false</code></td>
 *  </tr>
 * <tr>
 *   <th>timeout</th>
 *   <td>Max time in seconds content delivery may take. After a timeout,
 *       a not availability notice is delivered. Setting a timeout automatically
 *       turns on buffering.</td>
 *   <td></td>
 *   <td>int</td>
 *   <td><code>null</code></td>
 *  </tr>
 * </tbody></table>
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
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
     * Get a configuration value
     * First the coplet data is queried and if it doesn't provide an
     * attribute with the given name, the coplet base data is used.
     * If no value is found the default value is returned.
     */
    protected Object getConfiguration(CopletInstanceData coplet,
                                      String key,
                                      Object defaultValue) {
        Object data = this.getConfiguration(coplet, key);
        if ( data == null ) {
            data = defaultValue;
        }
        return data;
    }

    /**
     * Implement this and not toSAX()
     */
    public abstract void streamContent(CopletInstanceData coplet, 
                                         ContentHandler contentHandler)
    throws SAXException; 
    
    /**
     * This method streams the content of a coplet instance data.
     * It handles buffering and timeout setting and calls
     * {@link #streamContent(CopletInstanceData, ContentHandler)}
     * for creating the content.
     *
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#toSAX(org.apache.cocoon.portal.coplet.CopletInstanceData, org.xml.sax.ContentHandler)
     */
    public void toSAX(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException {
        final long startTime = System.currentTimeMillis();
        Boolean bool = (Boolean) this.getConfiguration( coplet, "buffer" );
        Integer timeout = (Integer) this.getConfiguration( coplet, "timeout");
        if ( timeout != null ) {
            // if timeout is set we have to buffer!
            bool = Boolean.TRUE;
        }
        
        if ( bool != null && bool.booleanValue() ) {
            boolean read = false;
            SaxBuffer buffer = new SaxBuffer();
            Exception error = null;
            try {
                
                if ( timeout != null ) {
                    final int milli = timeout.intValue() * 1000;
                    LoaderThread loader = new LoaderThread(this, coplet, buffer);
                    final RunnableManager runnableManager = (RunnableManager)this.manager.lookup( RunnableManager.ROLE );
                    try {
                        runnableManager.execute( new CocoonRunnable(loader) );
                    } finally {
                        this.manager.release( runnableManager );
                    }
                    try {
                        read = loader.join( milli );
                    } catch (InterruptedException ignore) {
                        // ignored
                    }
                    error = loader.exception;
                    if ( error != null && this.getLogger().isWarnEnabled() ) {
                        this.getLogger().warn("Unable to get content of coplet: " + coplet.getId(), error);                        
                    }
                } else {
                    this.streamContent( coplet, buffer );
                    read = true;
                }
            } catch (Exception exception ) {
                error = exception;
                this.getLogger().warn("Unable to get content of coplet: " + coplet.getId(), exception);
            } catch (Throwable t ) {
                error = new ProcessingException("Unable to get content of coplet: " + coplet.getId(), t);
                this.getLogger().warn("Unable to get content of coplet: " + coplet.getId(), t);                
            }
            
            if ( read ) {
                buffer.toSAX( contentHandler );
            } else {
                if ( !this.renderErrorContent(coplet, contentHandler, error)) {
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
        if ( this.getLogger().isInfoEnabled() ) {
            final long msecs = System.currentTimeMillis() - startTime;
            this.getLogger().info("Streamed coplet " + coplet.getCopletData().getId() +
                                  " (instance " + coplet.getId() + ") in " + msecs + "ms.");
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#init(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void init(CopletInstanceData coplet) {
        // nothing to do here, can be overwritten in subclasses
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#destroy(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void destroy(CopletInstanceData coplet) {
        // nothing to do here, can be overwritten in subclasses
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#login(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void login(CopletInstanceData coplet) {
        // copy temporary attributes from the coplet data
        Iterator iter = coplet.getCopletData().getAttributes().entrySet().iterator();
        while ( iter.hasNext() ) {
            Map.Entry entry = (Map.Entry)iter.next();
            if ( entry.getKey().toString().startsWith("temporary:") ) {
                coplet.setTemporaryAttribute(entry.getKey().toString().substring(10),
                        entry.getValue());
            }
        }
    }
        
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#logout(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void logout(CopletInstanceData coplet) {
        // nothing to do here, can be overwritten in subclasses
    }
    
    /**
     * Render the error content for a coplet
     * @param coplet  The coplet instance data
     * @param handler The content handler
     * @param error   The exception that occured
     * @return True if the error content has been rendered, otherwise false
     * @throws SAXException
     */
    protected boolean renderErrorContent(CopletInstanceData coplet, 
                                         ContentHandler     handler,
                                         Exception          error)
    throws SAXException {
        return false;
    }
}

final class LoaderThread implements Runnable {
    
    private final AbstractCopletAdapter adapter;
    private final ContentHandler        handler;
    private final CopletInstanceData    coplet;
    private final CountDown             finished;
    Exception exception;

    public LoaderThread(AbstractCopletAdapter adapter, 
                         CopletInstanceData coplet,
                         ContentHandler handler) {
        this.adapter = adapter;
        this.coplet  = coplet;
        this.handler = handler;
        this.finished = new CountDown( 1 );
    }
    
    public void run() {
        try {
            adapter.streamContent( this.coplet, this.handler );
        } catch (Exception local) {
            this.exception = local;
        } finally {
            this.finished.release();
        }
    }
    
    boolean join( final long milis )
    throws InterruptedException {
        return this.finished.attempt( milis );
    }
    
}
