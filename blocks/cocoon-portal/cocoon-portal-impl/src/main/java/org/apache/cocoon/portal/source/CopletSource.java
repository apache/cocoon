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
package org.apache.cocoon.portal.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.serialization.Serializer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the source implementation of the coplet source.
 *
 * @version $Id$
 */
public class CopletSource 
    implements Source, XMLizable, Serviceable {

    protected ServiceManager manager;

    protected String uri;
    protected String copletControllerName;
    protected CopletInstance copletInstanceData;

    /** The used protocol */
    protected String scheme;

    /** The portal service. */
    protected final PortalService portalService;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
    }

    public CopletSource(String         location,
                        String         protocol,
                        CopletInstance coplet,
                        PortalService  service) {
        this.portalService = service;
        this.uri = location;
        this.scheme = (protocol == null ? "coplet" : protocol);
        this.copletInstanceData = coplet;
        this.copletControllerName = this.copletInstanceData.getCopletDefinition().getCopletType().getCopletAdapterName();
    }

	/**
	 * @see org.apache.excalibur.source.Source#getInputStream()
	 */
	public InputStream getInputStream() throws IOException, SourceNotFoundException {
        Serializer serializer = null;
        try {
            serializer = (Serializer) this.manager.lookup(Serializer.ROLE + "/xml");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            serializer.setOutputStream(os);
            this.toSAX(serializer);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (SAXException se) {
            throw new CascadingIOException("Unable to stream content.", se);
        } catch (ServiceException ce) {
            throw new CascadingIOException("Unable to get components for serializing.", ce);
        } finally {
            this.manager.release(serializer);
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
        // nothing to do 
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
        final CopletAdapter copletAdapter = this.portalService.getCopletAdapter(this.copletControllerName);

        copletAdapter.toSAX(this.copletInstanceData, handler);
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
