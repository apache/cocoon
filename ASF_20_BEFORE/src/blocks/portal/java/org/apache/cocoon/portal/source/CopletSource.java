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
package org.apache.cocoon.portal.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.components.CocoonComponentManager;
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
 * @version CVS $Id: CopletSource.java,v 1.6 2004/02/23 14:52:49 cziegeler Exp $
 */
public class CopletSource 
    implements Source, XMLizable, Serviceable {

    protected ServiceManager manager;
    
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
        ComponentManager sitemapManager = CocoonComponentManager.getSitemapComponentManager();
        ComponentSelector serializerSelector = null;
        Serializer serializer = null;
        try {
            serializerSelector = (ComponentSelector) sitemapManager.lookup(Serializer.ROLE+"Selector");
            serializer = (Serializer) serializerSelector.select("xml");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            serializer.setOutputStream(os);
            this.toSAX(serializer);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (SAXException se) {
            throw new CascadingIOException("Unable to stream content.", se);
        } catch (ComponentException ce) {
            throw new CascadingIOException("Unable to get components for serializing.", ce);
        } finally {
            if ( serializer != null ) {
                serializerSelector.release(serializer);
            }
            sitemapManager.release(serializerSelector);
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
