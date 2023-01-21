/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.persistence.Converter;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Element;

/**
 * This implementation uses a {@link Converter} component to load/save
 * the profile.
 *
 * @version $Id$
 */
public class MapProfileLS
    extends AbstractLogEnabled
    implements Serviceable, ProfileLS, ThreadSafe, Disposable {

    /** The service manager. */
    protected ServiceManager manager;

    /** The XPath Processor. */
    protected XPathProcessor xpathProcessor;

    /** The converter component. */
    protected Converter converter;

    /** The source resolver. */
    protected SourceResolver resolver;

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.xpathProcessor );
            this.xpathProcessor = null;
            this.manager.release( this.converter );
            this.converter = null;
            this.manager.release( this.resolver );
            this.resolver = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
        this.converter = (Converter)this.manager.lookup(Converter.ROLE);
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    protected String getURI(Map keyMap) 
    throws Exception {
        final StringBuffer buffer = new StringBuffer();
        Iterator iter = keyMap.entrySet().iterator();
        boolean pars = false;
        boolean first = true;
        while ( iter.hasNext() ) {
            final Map.Entry entry = (Entry) iter.next();
            final String append = entry.getValue().toString();
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
                if ( !first && !"?".equals(append) ) {
                    buffer.append('/');
                }
                first = false;
            }
            if ( "?".equals(append) ) {
                first = true;
                pars = true;
            } else {
                buffer.append(append);
            }
        }
        
        return buffer.toString();
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileLS#loadProfile(java.lang.Object, java.util.Map)
     */
    public Object loadProfile(Object key, Map parameters) 
    throws Exception {
		final Map keyMap = (Map) key;

        final String uri = this.getURI( keyMap );

		Source source = null;
		try {
			source = this.resolver.resolveURI(uri);

			return this.converter.getObject(source.getInputStream(),
                                       (String)parameters.get(PARAMETER_PROFILETYPE),
                                       (Map)parameters.get(PARAMETER_OBJECTMAP),
                                       null);
		} finally {
            this.resolver.release(source);
		}
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileLS#saveProfile(java.lang.Object, java.util.Map, java.lang.Object)
     */
    public void saveProfile(Object key, Map parameters, Object profile) throws Exception {
        final Map keyMap = (Map) key;

        final String uri = this.getURI( keyMap );

        // first test: modifiable source?
        Source source = null;
        try {
            source = this.resolver.resolveURI(uri);
            if ( source instanceof ModifiableSource ) {
                this.converter.storeObject( ((ModifiableSource)source).getOutputStream(),
                                        (String)parameters.get(PARAMETER_PROFILETYPE),
                                        profile,
                                        parameters);
                return;
            }
        } finally {
            resolver.release(source);
            source = null;
        }

        final StringBuffer buffer = new StringBuffer(uri);
		SAXParser parser = null;
		try {
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            this.converter.storeObject(writer,
                                  (String)parameters.get(PARAMETER_PROFILETYPE),
                                  profile,
                                  parameters);

            buffer.append("&content=");
            try {
                buffer.append(NetUtils.encode(writer.toString(), "utf-8"));
            } catch (UnsupportedEncodingException uee) {
                // ignore this as utf-8 is always supported
            }

            source = this.resolver.resolveURI(buffer.toString());

            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
            Element element = DOMUtil.getDocumentFragment(parser, new InputStreamReader(source.getInputStream())).getOwnerDocument().getDocumentElement();
            if (!DOMUtil.getValueOf(element, "descendant::sourceResult/execution", this.xpathProcessor).trim().equals("success")) {
                throw new IOException("Could not save profile: "+DOMUtil.getValueOf(element, "descendant::sourceResult/message", this.xpathProcessor));
            }

		} finally {
            this.resolver.release(source);
			this.manager.release(parser);
		}
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileLS#getValidity(java.lang.Object, java.util.Map)
     */
    public SourceValidity getValidity(Object key, Map parameters) {
		Source source = null;
		try {
            final Map keyMap = (Map) key;

            final String uri = this.getURI( keyMap );

			source = this.resolver.resolveURI(uri);
			return source.getValidity();
		} catch (Exception e) {
			getLogger().warn(e.getMessage(), e);
			return null;
		} finally {
            this.resolver.release(source);
		}
    }
}
