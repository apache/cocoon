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
package org.apache.cocoon.portal.profile.impl;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cocoon.portal.profile.Converter;
import org.apache.cocoon.portal.profile.PersistenceType;
import org.apache.cocoon.portal.profile.ProfileStore;
import org.apache.cocoon.portal.util.AbstractBean;
import org.apache.cocoon.util.NetUtils;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;

/**
 * This implementation uses a {@link Converter} component to load/save
 * the profile.
 *
 * @version $Id$
 */
public class MapBasedProfileStore
    extends AbstractBean
    implements ProfileStore {

    /** The converter component. */
    protected Converter converter;

    /** The source resolver. */
    protected SourceResolver resolver;

    public void setSourceResolver(SourceResolver sr) {
        this.resolver = sr;
    }

    public void setConverter(Converter c) {
        this.converter = c;
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
     * @see org.apache.cocoon.portal.profile.ProfileStore#loadProfile(java.lang.Object, org.apache.cocoon.portal.profile.PersistenceType)
     */
    public Object loadProfile(Object key, PersistenceType type)
    throws Exception {
		final Map keyMap = (Map) key;

        final String uri = this.getURI( keyMap );

		Source source = null;
		try {
			source = this.resolver.resolveURI(uri);

			return this.converter.getObject(source.getInputStream(),
                                       type,
                                       null);
		} finally {
            this.resolver.release(source);
		}
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileStore#saveProfile(java.lang.Object, org.apache.cocoon.portal.profile.PersistenceType, java.lang.Object)
     */
    public void saveProfile(Object key, PersistenceType type, Object profile)
    throws Exception {
        final Map keyMap = (Map) key;

        final String uri = this.getURI( keyMap );

        // first test: modifiable source?
        Source source = null;
        try {
            source = this.resolver.resolveURI(uri);
            if ( source instanceof ModifiableSource ) {
                this.converter.storeObject( ((ModifiableSource)source).getOutputStream(),
                                        profile,
                                        type,
                                        null);
                return;
            }
        } finally {
            resolver.release(source);
            source = null;
        }

        final StringBuffer buffer = new StringBuffer(uri);
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        this.converter.storeObject(writer,
                              profile,
                              type,
                              null);

        buffer.append("&content=");
        try {
            buffer.append(NetUtils.encode(writer.toString(), "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }

        source = this.resolver.resolveURI(buffer.toString());
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileStore#getValidity(java.lang.Object, java.lang.String)
     */
    public SourceValidity getValidity(Object key, String type) {
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
