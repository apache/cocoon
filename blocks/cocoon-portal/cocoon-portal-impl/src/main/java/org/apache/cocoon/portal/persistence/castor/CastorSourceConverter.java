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
            this.idResolver.setObjectMap(references);
            Unmarshaller unmarshaller = (Unmarshaller)((Object[])this.mappings.get(mappingName))[1];
            Object result = unmarshaller.unmarshal(new InputSource(stream));
            stream.close();
            return result;
        } catch (Exception e) {
            throw new ConverterException(e.getMessage(), e);
        } finally {
            this.idResolver.clearObjectMap();
        }
    }

	/**
	 * @see org.apache.cocoon.portal.persistence.Converter#storeObject(java.io.OutputStream, java.lang.String, java.lang.Object, java.util.Map)
	 */
	public void storeObject(OutputStream stream,
                            String       mappingName,
                            Object       object,
                            Map          parameters)
    throws ConverterException {
        if ( object instanceof Collection && !(object instanceof CollectionWrapper) ) {
            object = new CollectionWrapper((Collection)object);
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
			marshaller.marshal(object);
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
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        // configure castor - so we don't need a properties file
        LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.parser.namespaces", "true");

        // default configuration
        final String prefix = "resource://org/apache/cocoon/portal/persistence/castor/";
        this.mappingSources.put("layout", prefix + "layout.xml");
        this.mappingSources.put("copletbasedata", prefix + "copletbasedata.xml");
        this.mappingSources.put("copletdata", prefix + "copletdata.xml");
        this.mappingSources.put("copletinstancedata", prefix + "copletinstancedata.xml");
        this.mappingSources.put("portletpreferences", prefix + "pluto.xml");

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

        /**
         * Used to pass resolvable objects to the field handler.
         */
        private ThreadLocal threadLocalMap = new ThreadLocal();

        public ReferenceResolver() {
            // nothing to do
        }

        /**
         * Sets the map used to pass resolvable objects to the field handler.
         */
        public void setObjectMap(Map objectMap) {
            this.threadLocalMap.set(objectMap);
        }

        public void clearObjectMap() {
            this.threadLocalMap.set(null);
        }

        /**
         * @see org.exolab.castor.xml.IDResolver#resolve(java.lang.String)
         */
        public Object resolve(String refId) {
            // TODO - Should we throw an exception if the reference is not available?
            return ((Map)this.threadLocalMap.get()).get(refId);
        }
    }
}
