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
package org.apache.cocoon.portal.profile.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: MapProfileLS.java,v 1.5 2003/12/10 17:02:04 cziegeler Exp $
 */
public class MapProfileLS
    extends AbstractLogEnabled
    implements Component, Serviceable, ProfileLS, ThreadSafe {

    /** The component manager */
    protected ServiceManager manager;


    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
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
            if (!DOMUtil.getValueOf(element, "descendant::sourceResult/execution").trim().equals("success")) {
                throw new IOException("Could not save profile: "+DOMUtil.getValueOf(element, "descendant::sourceResult/message"));
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
