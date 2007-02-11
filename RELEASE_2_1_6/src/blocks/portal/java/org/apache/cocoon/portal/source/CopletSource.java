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
package org.apache.cocoon.portal.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.serialization.Serializer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the source implementation of the coplet source
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: CopletSource.java,v 1.8 2004/04/19 14:47:31 cziegeler Exp $
 */
public class CopletSource 
    implements Source, XMLizable, Serviceable, Contextualizable {

    protected ServiceManager manager;
    
    protected Context context;
    
    protected String uri;
    protected String copletControllerName;
    protected CopletInstanceData copletInstanceData;
    
    /** The used protocol */
    protected String scheme;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }
    
    public CopletSource(String location, String protocol,
                         CopletInstanceData coplet) {
        this.uri = location;
        this.scheme = (protocol == null ? "coplet" : protocol);
        this.copletInstanceData = coplet;
        this.copletControllerName = this.copletInstanceData.getCopletData().getCopletBaseData().getCopletAdapterName();
    }
    
	/**
	 * @see org.apache.excalibur.source.Source#getInputStream()
	 */
	public InputStream getInputStream() throws IOException, SourceNotFoundException {
        try {
            ServiceManager sitemapManager = (ServiceManager) this.context.get(ContextHelper.CONTEXT_SITEMAP_SERVICE_MANAGER);
            ServiceSelector serializerSelector = null;
            Serializer serializer = null;
            try {
                serializerSelector = (ServiceSelector) sitemapManager.lookup(Serializer.ROLE+"Selector");
                serializer = (Serializer) serializerSelector.select("xml");
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                serializer.setOutputStream(os);
                this.toSAX(serializer);
                return new ByteArrayInputStream(os.toByteArray());
            } catch (SAXException se) {
                throw new CascadingIOException("Unable to stream content.", se);
            } catch (ServiceException ce) {
                throw new CascadingIOException("Unable to get components for serializing.", ce);
            } finally {
                if ( serializer != null ) {
                    serializerSelector.release(serializer);
                }
                sitemapManager.release(serializerSelector);
            }
        } catch (ContextException ce) {
            throw new CascadingIOException("Unable to get service manager.", ce);
        }
	}

	/**
	 * @see org.apache.excalibur.source.Source#getURI()
	 */
	public String getURI() {
		return this.uri;
	}

	/**
	 * @see org.apache.excalibur.source.Source#getValidity()
	 */
	public SourceValidity getValidity() {
		return null;
	}

	/**
	 * @see org.apache.excalibur.source.Source#refresh()
	 */
	public void refresh() {
	}

	/**
	 * @see org.apache.excalibur.source.Source#getMimeType()
	 */
	public String getMimeType() {
		return null;
	}

	/**
	 * @see org.apache.excalibur.source.Source#getContentLength()
	 */
	public long getContentLength() {
		return -1;
	}

	/**
	 * @see org.apache.excalibur.source.Source#getLastModified()
	 */
	public long getLastModified() {
		return 0;
	}

	/**
	 * @see org.apache.excalibur.xml.sax.XMLizable#toSAX(ContentHandler)
	 */
	public void toSAX(ContentHandler handler) 
    throws SAXException {
        ServiceSelector copletAdapterSelector = null;
        CopletAdapter copletAdapter = null;
        try {
            copletAdapterSelector = (ServiceSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");
            copletAdapter = (CopletAdapter)copletAdapterSelector.select(this.copletControllerName);
            
            copletAdapter.toSAX(this.copletInstanceData, handler);
        } catch (ServiceException ce) {
            throw new SAXException("Unable to lookup coplet adaptor or adaptor selector.", ce);
        } finally {
            if ( null != copletAdapter ) {
                 copletAdapterSelector.release( copletAdapter );
            }
            this.manager.release(copletAdapterSelector);
        }
            
	}

    /**
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }

    /**
     * @see org.apache.excalibur.source.Source#getScheme()
     */
    public String getScheme() {
        return this.scheme;
    }

}
