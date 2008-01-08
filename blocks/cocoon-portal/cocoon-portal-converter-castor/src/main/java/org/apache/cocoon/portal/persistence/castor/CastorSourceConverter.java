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
package org.apache.cocoon.portal.persistence.castor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.cocoon.portal.profile.Converter;
import org.apache.cocoon.portal.profile.ConverterException;
import org.apache.cocoon.portal.profile.PersistenceType;
import org.apache.cocoon.portal.profile.ProfileStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    implements Converter {

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

    /**
     * @see org.apache.cocoon.portal.profile.Converter#getObject(java.io.InputStream, org.apache.cocoon.portal.profile.PersistenceType, java.util.Map)
     */
    public Object getObject(InputStream stream,
                            PersistenceType type,
                            Map         parameters)
    throws ConverterException {
        try {
            threadLocalMap.set(type);
            final Unmarshaller unmarshaller = (Unmarshaller)((Object[])this.mappings.get(type.getType()))[1];
            final Object result = unmarshaller.unmarshal(new InputSource(stream));
            stream.close();
            return result;
        } catch (IllegalStateException ise) {
            throw new ConverterException("Unable to unmarshal objects for mapping " + type.getType(), ise);
        } catch (Exception e) {
            throw new ConverterException(e.getMessage(), e);
        } finally {
            threadLocalMap.set(null);
        }
    }

	/**
	 * @see org.apache.cocoon.portal.profile.Converter#storeObject(java.io.OutputStream, java.lang.Object, org.apache.cocoon.portal.profile.PersistenceType, java.util.Map)
	 */
	public void storeObject(OutputStream stream,
	                        Object       object,
                            PersistenceType type,
                            Map          parameters)
    throws ConverterException {
        Object references = object;
        if ( object instanceof Collection && !(object instanceof CollectionWrapper) ) {
            references = new CollectionWrapper((Collection)object);
        }
        Writer writer = new OutputStreamWriter(stream);
		try {
            threadLocalMap.set(type);
			Marshaller marshaller = new Marshaller( writer );
			marshaller.setMapping((Mapping)((Object[])this.mappings.get(type.getType()))[0]);
            boolean suppressXSIType = this.defaultSuppressXSIType;
            if ( parameters != null ) {
                Boolean value = (Boolean)parameters.get("suppressXSIType");
                if (value != null) {
                    suppressXSIType = value.booleanValue();
                }
            }
            marshaller.setSuppressXSIType(suppressXSIType);
			marshaller.marshal(references);
			writer.close();
		} catch (MappingException e) {
			throw new ConverterException("Can't create Unmarshaller", e);
		} catch (Exception e) {
			throw new ConverterException(e.getMessage(), e);
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
        final String prefix = "resource://org/apache/cocoon/portal/persistence/castor/";
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
			final InputSource inputSource = new InputSource();
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
}
