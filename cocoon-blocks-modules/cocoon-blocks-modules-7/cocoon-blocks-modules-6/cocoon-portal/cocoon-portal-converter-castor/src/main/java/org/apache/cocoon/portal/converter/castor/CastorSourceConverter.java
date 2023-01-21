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
package org.apache.cocoon.portal.converter.castor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.cocoon.portal.profile.PersistenceType;
import org.apache.cocoon.portal.profile.ProfileException;
import org.apache.cocoon.portal.profile.ProfileKey;
import org.apache.cocoon.portal.profile.ProfileStore;
import org.apache.cocoon.portal.util.AbstractBean;
import org.apache.cocoon.portal.util.PropertyHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.IDResolver;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * This is a component that converts a profile (= object tree) to XML and vice-versa
 * using Castor. It could be used to persist objects as a XML representation.
 *
 * In order to work properly the methods provided by this interface require some
 * parameters:
 * objectmap : containing a map of objects for resolving references during load
 * profiletype: specifying the mapping (e.g. in the portal this is one of layout, copletinstancedata, copletdata or copletbasedate)
 * suppressXSIType: Sets whether or not the xsi:type attributes should appear on the marshalled document.
 *
 * @version $Id$
 */
public class CastorSourceConverter
    extends AbstractBean
    implements ProfileStore {

    /**
     * Used to pass resolvable objects to the field handler.
     */
    public static final ThreadLocal threadLocalMap = new ThreadLocal();

    protected final Map mappings = new HashMap();
    protected boolean defaultSuppressXSIType;
    protected boolean defaultValidateUnmarshalling;
    protected Properties castorProperties;

    /** This object resolves the references between the different profile files. */
    protected ReferenceResolver idResolver = new ReferenceResolver();

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    /** The source resolver. */
    protected SourceResolver resolver;

    /** The configuration for loading/saving the profile. */
    protected Properties configuration;

    public void setSourceResolver(SourceResolver sr) {
        this.resolver = sr;
    }

    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    public void setSuppressXSIType(boolean flag) {
        this.defaultSuppressXSIType = flag;
    }

    public void setValidateUnmarshalling(boolean flag) {
        this.defaultValidateUnmarshalling = flag;
    }

    public void setCastorProperties(Properties props) {
        this.castorProperties = props;
    }

    protected Object getObject(InputStream stream,
                            PersistenceType type)
    throws ProfileException {
        try {
            threadLocalMap.set(type);
            final Unmarshaller unmarshaller = (Unmarshaller)((Object[])this.mappings.get(type.getType()))[1];
            final Object result = unmarshaller.unmarshal(new InputSource(stream));
            stream.close();
            return result;
        } catch (IllegalStateException ise) {
            throw new ProfileException("Unable to unmarshal objects for mapping " + type.getType(), ise);
        } catch (Exception e) {
            throw new ProfileException(e.getMessage(), e);
        } finally {
            threadLocalMap.set(null);
        }
    }

	protected void storeObject(OutputStream stream,
	                        Object       object,
                            PersistenceType type)
    throws ProfileException {
        Object references = object;
        if ( object instanceof Collection && !(object instanceof CollectionWrapper) ) {
            references = new CollectionWrapper((Collection)object);
        }
        Writer writer = new OutputStreamWriter(stream);
		try {
            threadLocalMap.set(type);
			Marshaller marshaller = new Marshaller( writer );
			marshaller.setMapping((Mapping)((Object[])this.mappings.get(type.getType()))[0]);
            marshaller.setSuppressXSIType(this.defaultSuppressXSIType);
			marshaller.marshal(references);
			writer.close();
		} catch (MappingException e) {
			throw new ProfileException("Can't create Unmarshaller", e);
		} catch (Exception e) {
			throw new ProfileException(e.getMessage(), e);
        } finally {
            threadLocalMap.set(null);
		}
	}

    /**
     * Initialize this service.
     * Load the mappings and configure castor
     */
    public void init() throws Exception {
        // configure castor - so we don't need a properties file
        final Properties castorProperties = LocalConfiguration.getInstance().getProperties();
        castorProperties.setProperty("org.exolab.castor.parser.namespaces", "true");
        if ( this.castorProperties != null ) {
            castorProperties.putAll(this.castorProperties);
        }

        // default configuration
        final String prefix = "resource://org/apache/cocoon/portal/converter/castor/";
        final Map mappingSources = new HashMap();
        mappingSources.put(ProfileStore.PROFILETYPE_LAYOUT, prefix + ProfileStore.PROFILETYPE_LAYOUT +".xml");
        mappingSources.put(ProfileStore.PROFILETYPE_COPLETDEFINITION, prefix + ProfileStore.PROFILETYPE_COPLETDEFINITION + ".xml");
        mappingSources.put(ProfileStore.PROFILETYPE_COPLETINSTANCE, prefix + ProfileStore.PROFILETYPE_COPLETINSTANCE + ".xml");

        boolean plutoAvailable = false;
        try {
            this.getClass().getClassLoader().loadClass("org.apache.cocoon.portal.pluto.adapter.PortletAdapter");
            plutoAvailable = true;
        } catch (Exception ignore) {
            this.getLogger().info("Pluto is not available - no default mapping for castor loaded.");
        }
        if ( plutoAvailable ) {
            mappingSources.put("portletpreferences", prefix + "pluto.xml");
        }
		final Iterator iterator = mappingSources.entrySet().iterator();
    	while (iterator.hasNext()) {
    		final Map.Entry entry = (Map.Entry)iterator.next();
    		final String name = (String)entry.getKey();
    		final String mappingSource = (String)entry.getValue();

    		final InputStream is = this.getClass().getResourceAsStream(mappingSource.substring(10));
			final InputSource inputSource = new InputSource(is);
			inputSource.setSystemId(mappingSource);

			final Mapping mapping = new Mapping();
			mapping.loadMapping(inputSource);

            // create unmarshaller
            try {
                final Unmarshaller unmarshaller = new Unmarshaller(mapping);
                unmarshaller.setValidation(this.defaultValidateUnmarshalling);
                unmarshaller.setIDResolver(this.idResolver);
                this.mappings.put(name, new Object[] {mapping, unmarshaller});
            } catch (Exception e) {
                this.getLogger().debug("Unable to create unmarshaller for mapping: " + name, e);
                throw e;
            }
    	}
    }

    public final static class ReferenceResolver implements IDResolver {

        public ReferenceResolver() {
            // nothing to do
        }

        /**
         * @see org.exolab.castor.xml.IDResolver#resolve(java.lang.String)
         */
        public Object resolve(String refId) {
            final Object o = ((Map)CastorSourceConverter.threadLocalMap.get()).get(refId);
            return o;
        }
    }

    protected String getURI(ProfileKey key, String type, boolean load)
    throws Exception {
        // find uri in configuration
        final StringBuffer config = new StringBuffer(type);
        config.append('-');
        config.append(key.getProfileCategory());
        config.append('-');
        if ( load ) {
            config.append("load");
        } else {
            config.append("save");
        }
        final String uri = this.configuration.getProperty(config.toString());
        if ( uri == null ) {
            throw new ProfileException("Configuration for key '" + config.toString() + "' is missing.");
        }

        return PropertyHelper.replace(uri, key);
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileStore#loadProfile(org.apache.cocoon.portal.profile.ProfileKey, org.apache.cocoon.portal.profile.PersistenceType)
     */
    public Object loadProfile(ProfileKey key, PersistenceType type)
    throws Exception {
        final String uri = this.getURI( key, type.getType(), true );
        Source source = null;
        try {
            source = this.resolver.resolveURI(uri);

            return this.getObject(source.getInputStream(),
                                       type);
        } finally {
            this.resolver.release(source);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileStore#saveProfile(org.apache.cocoon.portal.profile.ProfileKey, org.apache.cocoon.portal.profile.PersistenceType, java.lang.Object)
     */
    public void saveProfile(ProfileKey key, PersistenceType type, Object profile)
    throws Exception {
        final String uri = this.getURI( key, type.getType(), false );

        // first test: modifiable source?
        Source source = null;
        try {
            source = this.resolver.resolveURI(uri);
            if ( source instanceof ModifiableSource ) {
                this.storeObject( ((ModifiableSource)source).getOutputStream(),
                                        profile,
                                        type);
                return;
            }
        } finally {
            resolver.release(source);
            source = null;
        }

        final StringBuffer buffer = new StringBuffer(uri);
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        this.storeObject(writer,
                          profile,
                          type);

        buffer.append("&content=");
        try {
            buffer.append(URLEncoder.encode(writer.toString(), "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }

        source = this.resolver.resolveURI(buffer.toString());
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileStore#getValidity(org.apache.cocoon.portal.profile.ProfileKey, java.lang.String)
     */
    public SourceValidity getValidity(ProfileKey key, String type) {
        Source source = null;
        try {
            final String uri = this.getURI( key, type, true );

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
