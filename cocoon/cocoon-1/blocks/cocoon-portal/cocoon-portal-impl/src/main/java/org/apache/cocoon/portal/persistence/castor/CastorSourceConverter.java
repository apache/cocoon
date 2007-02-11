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
import java.util.Map.Entry;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.portal.persistence.Converter;
import org.apache.cocoon.portal.persistence.ConverterException;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.util.ClassUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
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
    extends AbstractLogEnabled
    implements Serviceable, Configurable, Initializable, ThreadSafe, Converter {

    /**
     * Used to pass resolvable objects to the field handler.
     */
    public static final ThreadLocal threadLocalMap = new ThreadLocal();

    protected Map mappingSources = new HashMap();
    protected ServiceManager manager;
    protected Map mappings = new HashMap();
    protected boolean defaultSuppressXSIType;
    protected boolean defaultValidateUnmarshalling;

    /** This object resolves the references between the different profile files. */
    protected ReferenceResolver idResolver = new ReferenceResolver();

    /**
     * @see org.apache.cocoon.portal.persistence.Converter#getObject(java.io.InputStream, java.lang.String, java.util.Map, java.util.Map)
     */
    public Object getObject(InputStream stream,
                            String      mappingName,
                            Map         references,
                            Map         parameters)
    throws ConverterException {
        try {
            threadLocalMap.set(references);
            final Unmarshaller unmarshaller = (Unmarshaller)((Object[])this.mappings.get(mappingName))[1];
            final Object result = unmarshaller.unmarshal(new InputSource(stream));
            stream.close();
            return result;
        } catch (IllegalStateException ise) {
            throw new ConverterException("Unable to unmarshal objects for mapping " + mappingName, ise);
        } catch (Exception e) {
            throw new ConverterException(e.getMessage(), e);
        } finally {
            threadLocalMap.set(null);
        }
    }

	/**
	 * @see org.apache.cocoon.portal.persistence.Converter#storeObject(java.io.OutputStream, java.lang.String, java.lang.Object, java.util.Map)
	 */
	public void storeObject(OutputStream stream,
                            String       mappingName,
                            Object       referenceObject,
                            Map          parameters)
    throws ConverterException {
        Object references = referenceObject; 
        if ( referenceObject instanceof Collection && !(referenceObject instanceof CollectionWrapper) ) {
            references = new CollectionWrapper((Collection)referenceObject);
        }
        Writer writer = new OutputStreamWriter(stream);
		try {
			Marshaller marshaller = new Marshaller( writer );
			marshaller.setMapping((Mapping)((Object[])this.mappings.get(mappingName))[0]);
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
		}
	}

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        // configure castor - so we don't need a properties file
        final Properties castorProperties = LocalConfiguration.getInstance().getProperties();
        castorProperties.setProperty("org.exolab.castor.parser.namespaces", "true");
        final Configuration[] properties = config.getChild("castor-properties").getChildren("property");
        for(int i=0; i<properties.length; i++) {
            final String propName = properties[i].getAttribute("name");
            final String propValue = properties[i].getAttribute("value");
            castorProperties.setProperty(propName, propValue);
        }

        // default configuration
        final String prefix = "resource://org/apache/cocoon/portal/persistence/castor/";
        this.mappingSources.put(ProfileLS.PROFILETYPE_LAYOUT, prefix + ProfileLS.PROFILETYPE_LAYOUT +".xml");
        this.mappingSources.put(ProfileLS.PROFILETYPE_COPLETTYPE, prefix + ProfileLS.PROFILETYPE_COPLETTYPE + ".xml");
        this.mappingSources.put(ProfileLS.PROFILETYPE_COPLETDEFINITION, prefix + ProfileLS.PROFILETYPE_COPLETDEFINITION + ".xml");
        this.mappingSources.put(ProfileLS.PROFILETYPE_COPLETINSTANCE, prefix + ProfileLS.PROFILETYPE_COPLETINSTANCE + ".xml");
        boolean plutoAvailable = false;
        try {
            ClassUtils.loadClass("org.apache.cocoon.portal.pluto.adapter.PortletAdapter");
            plutoAvailable = true;
        } catch (Exception ignore) {
            this.getLogger().info("Pluto is not available - no default mapping for castor loaded.");
        }
        if ( plutoAvailable ) {
            this.mappingSources.put("portletpreferences", prefix + "pluto.xml");
        }

        // the custom configuration might overwrite the default config
        Configuration[] children = config.getChildren("mapping-source");
    	for (int i=0; i<children.length; i++) {
    	    Configuration mappingSource = children[i];
	        this.mappingSources.put(mappingSource.getAttribute("source"), mappingSource.getValue());
	    }
        this.defaultSuppressXSIType = config.getChild("suppressXSIType").getValueAsBoolean(false);
        this.defaultValidateUnmarshalling = config.getChild("validate-on-unmarshalling").getValueAsBoolean(false);
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        SourceResolver resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
        Source source = null;
        try {
			Entry entry;
			String name;
			String mappingSource;
			Mapping mapping;
			Iterator iterator = this.mappingSources.entrySet().iterator();
        	while (iterator.hasNext()) {
        		entry = (Map.Entry)iterator.next(); 
        		name = (String)entry.getKey();
        		mappingSource = (String)entry.getValue();
        		
				source = resolver.resolveURI(mappingSource);
				mapping = new Mapping();
				mapping.loadMapping(SourceUtil.getInputSource(source));

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
        } finally {
            if (source != null) {
                resolver.release(source);
            }
            manager.release(resolver);
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
