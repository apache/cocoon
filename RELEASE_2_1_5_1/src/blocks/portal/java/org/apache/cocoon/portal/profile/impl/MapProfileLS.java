/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.persistence.CastorSourceConverter;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: MapProfileLS.java,v 1.7 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class MapProfileLS
    extends AbstractLogEnabled
    implements Component, Serviceable, ProfileLS, ThreadSafe, Disposable {

    /** The component manager */
    protected ServiceManager manager;

    /** The XPath Processor */
    protected XPathProcessor xpathProcessor;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.xpathProcessor );
            this.xpathProcessor = null;
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    protected String getURI(Map keyMap) 
    throws Exception {
        final StringBuffer buffer = new StringBuffer();
        Iterator iter = keyMap.entrySet().iterator();
        boolean pars = false;
        boolean first = true;
        while ( iter.hasNext() ) {
            final Map.Entry entry = (Entry) iter.next();
            if ( pars ) {
                if ( first ) {
                    first = false;
                    if ( buffer.toString().indexOf('?') == -1 ) {
                        buffer.append('?');
                    } else {
                        buffer.append('&');
                    }
                } else {
                    buffer.append('&');
                }
                buffer.append(entry.getKey().toString());
                buffer.append('=');
            } else {
                if ( !first) {
                    buffer.append('/');
                }
                first = false;
            }
            String append = entry.getValue().toString();
            if ( "?".equals(append) ) {
                first = true;
                pars = true;
            } else {
                buffer.append(append);
            }
        }
        
        return buffer.toString();
    }
    
    protected StringBuffer getSaveURI(Map keyMap)
    throws Exception {
        final StringBuffer buffer = new StringBuffer(this.getURI(keyMap));
        return buffer;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileLS#loadProfile(java.lang.Object)
     */
    public Object loadProfile(Object key, Map parameters) 
    throws Exception {
		final Map keyMap = (Map) key;
        
        final String uri = this.getURI( keyMap );
        
		Source source = null;
		CastorSourceConverter converter = null;
        SourceResolver resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
		try {
			source = resolver.resolveURI(uri);
            converter = (CastorSourceConverter) this.manager.lookup(CastorSourceConverter.ROLE);

			return converter.getObject(source.getInputStream(), parameters);
		} finally {
            if ( resolver != null ) {
                resolver.release(source);
            }
			manager.release(converter);
			manager.release(resolver);
		}
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileLS#saveProfile(java.lang.Object, java.lang.Object)
     */
    public void saveProfile(Object key, Map parameters, Object profile) throws Exception {
        final Map keyMap = (Map) key;
        
        final String uri = this.getURI( keyMap );

        // first test: modifiable source?
        SourceResolver resolver = null;
        CastorSourceConverter converter = null;
        Source source = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(uri);
            if ( source instanceof ModifiableSource ) {
                converter = (CastorSourceConverter) this.manager.lookup(CastorSourceConverter.ROLE);
                converter.storeObject( ((ModifiableSource)source).getOutputStream(), parameters, profile);
                return;
            }

        } finally {
            if ( resolver != null ) {
                resolver.release(source);
            }
            manager.release(converter);
            manager.release(resolver);
            source = null;
            converter = null;
            resolver = null;
        }
        
        final StringBuffer buffer = this.getSaveURI( keyMap );

		SAXParser parser = null;
		try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            converter = (CastorSourceConverter) this.manager.lookup(CastorSourceConverter.ROLE);

            ByteArrayOutputStream writer = new ByteArrayOutputStream();
        
            converter.storeObject(writer, parameters, profile);

            buffer.append("&content=");
            buffer.append(SourceUtil.encode(writer.toString()));

            source = resolver.resolveURI(buffer.toString());

            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
            Element element = DOMUtil.getDocumentFragment(parser, new InputStreamReader(source.getInputStream())).getOwnerDocument().getDocumentElement();
            if (!DOMUtil.getValueOf(element, "descendant::sourceResult/execution", this.xpathProcessor).trim().equals("success")) {
                throw new IOException("Could not save profile: "+DOMUtil.getValueOf(element, "descendant::sourceResult/message", this.xpathProcessor));
            }

		} finally {
            if ( resolver != null ) {
                resolver.release(source);
            }
			manager.release(parser);
			manager.release(converter);
			manager.release(resolver);
		}
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileLS#getValidity(java.lang.Object)
     */
    public SourceValidity getValidity(Object key, Map parameters) {
		SourceResolver resolver = null;
		Source source = null;
		try {
            final Map keyMap = (Map) key;
        
            final String uri = this.getURI( keyMap );

			resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
			source = resolver.resolveURI(uri);
			return source.getValidity();
		} catch (Exception e) {
			getLogger().warn(e.getMessage(), e);
			return null;
		} finally {
			if (resolver != null) {
                resolver.release(source);
			}
			manager.release(resolver);
		}
    }

}
