/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.NotifyingBuilder;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.CopletInstanceDataFeatures;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the adapter to use pipelines as coplets.
 *
 * @version $Id$
 */
public class URICopletAdapter 
    extends AbstractCopletAdapter {

    /** The source resolver */
    protected SourceResolver resolver;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service( manager );
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, org.xml.sax.ContentHandler)
     */
    public void streamContent(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException {
        final String uri = (String)coplet.getCopletData().getAttribute("uri");
        if ( uri == null ) {
            throw new SAXException("No URI for coplet data "+coplet.getCopletData().getId()+" found.");
        }
        this.streamContent( coplet, uri, contentHandler);
    }

    protected void streamContent(final CopletInstanceData coplet, 
                                 final String uri,
                                 final ContentHandler contentHandler)
    throws SAXException {
		Source copletSource = null;
		try {
			if (uri.startsWith("cocoon:")) {
                Boolean handlePars = (Boolean)this.getConfiguration( coplet, "handleParameters", Boolean.FALSE);

                String sourceUri = uri;

                if ( handlePars.booleanValue() ) {
                    List list = CopletInstanceDataFeatures.getChangedCopletInstanceDataObjects(this.portalService);
                    if ( list.contains( coplet )) {
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
				par.put(Constants.PORTAL_NAME_KEY, this.portalService.getPortalName());
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
		} finally {
			this.resolver.release(copletSource);
		}
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.resolver );
            this.resolver = null;
        }
        super.dispose();
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
                    // ignore
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
            } else {

                this.streamContent( coplet, uri, handler);
            }
   
            return true;
        }
        return false;
    }
}
