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
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.NotifyingBuilder;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the adapter to use pipelines as coplets
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: URICopletAdapter.java,v 1.14 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class URICopletAdapter 
    extends AbstractCopletAdapter
    implements Disposable, Subscriber, Initializable, Contextualizable {
	
    /** The source resolver */
    protected SourceResolver resolver;
    
    /** The application context */
    protected Context context;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service( manager );
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }
    
    public void streamContent(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException {
        final String uri = (String)coplet.getCopletData().getAttribute("uri");
        if ( uri == null ) {
            throw new SAXException("No URI for coplet data "+coplet.getCopletData().getId()+" found.");
        }
        this.streamContent( coplet, uri, contentHandler);
    }

    public void streamContent(final CopletInstanceData coplet, 
                               final String uri,
                               final ContentHandler contentHandler)
    throws SAXException {
		Source copletSource = null;
		PortalService portalService = null;
		try {
			if (uri.startsWith("cocoon:")) {
                portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

                Boolean handlePars = (Boolean)this.getConfiguration( coplet, "handleParameters");
                
                String sourceUri = uri;
                
                if ( handlePars != null && handlePars.booleanValue() ) {
                    List list = (List) portalService.getTemporaryAttribute(URICopletAdapter.class.getName());
                    if ( list != null && list.contains( coplet )) {
                        // add parameters
                        if ( uri.startsWith("cocoon:raw:") ) {
                            sourceUri = "cocoon:" + uri.substring(11); 
                        }
                    } else {
                        // remove parameters
                        if (!uri.startsWith("cocoon:raw:") ) {
                            sourceUri = "cocoon:raw:" + uri.substring(7);
                        }
                    }
                }
                
				HashMap par = new HashMap();
				par.put(Constants.PORTAL_NAME_KEY, portalService.getPortalName());
				par.put(Constants.COPLET_ID_KEY, coplet.getId());
            
				copletSource = this.resolver.resolveURI(sourceUri, null, par);
			} else {
				copletSource = this.resolver.resolveURI(uri);
			}
			SourceUtil.toSAX(copletSource, contentHandler);
		} catch (IOException ioe) {
			throw new SAXException("IOException", ioe);
		} catch (ProcessingException pe) {
			throw new SAXException("ProcessingException", pe);
		} catch (ServiceException ce) {
			throw new SAXException("ComponentException", ce);
		} finally {
			this.resolver.release(copletSource);
			this.manager.release(portalService);
		}
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            EventManager eventManager = null;
            try { 
                eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
                eventManager.getRegister().unsubscribe( this );
            } catch (Exception ignore) {
            } finally {
                this.manager.release( eventManager ); 
            }
            
            this.manager.release( this.resolver );
            this.resolver = null;
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return CopletInstanceEvent.class;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getFilter()
     */
    public Filter getFilter() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#inform(org.apache.cocoon.portal.event.Event)
     */
    public void inform(Event e) {
        CopletInstanceEvent event = (CopletInstanceEvent)e;
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            List list = (List)service.getTemporaryAttribute(URICopletAdapter.class.getName());
            if ( list == null ) {
                list = new ArrayList();
            }
            list.add(event.getTarget());
            service.setTemporaryAttribute(URICopletAdapter.class.getName(), list);
        } catch (ServiceException ignore ) {            
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        EventManager eventManager = null;
        try { 
            eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
            eventManager.getRegister().subscribe( this );
        } finally {
            this.manager.release( eventManager );
        }
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
        final String uri = (String) this.getConfiguration(coplet, "error-uri");
        if ( uri != null ) {
            // TODO - if an error occured for this coplet, remember this
            //         and use directly the error-uri from now on

            if ( uri.startsWith("cocoon:") && error != null) {
                // Create a Notifying
                NotifyingBuilder notifyingBuilder = null;
                Notifying currentNotifying = null;
                try {
                    notifyingBuilder= (NotifyingBuilder)this.manager.lookup(NotifyingBuilder.ROLE);
                    currentNotifying = notifyingBuilder.build(this, error);
                } catch (Exception ignore) {
                } finally {
                    this.manager.release(notifyingBuilder);
                }

                final Map objectModel = ContextHelper.getObjectModel(this.context);
                // Add it to the object model
                if ( currentNotifying != null ) {
                    objectModel.put(org.apache.cocoon.Constants.NOTIFYING_OBJECT, currentNotifying);                    
                    objectModel.put(ObjectModelHelper.THROWABLE_OBJECT, error);
                }
            
                try {
                    this.streamContent( coplet, uri, handler);
                } finally {
                    objectModel.remove(org.apache.cocoon.Constants.NOTIFYING_OBJECT);
                    objectModel.remove(ObjectModelHelper.THROWABLE_OBJECT);
                }
            }
            
            this.streamContent( coplet, uri, handler);
                        
            return true;
        }
        return false;
    }

}
