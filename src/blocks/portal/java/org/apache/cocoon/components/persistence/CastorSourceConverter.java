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
package org.apache.cocoon.components.persistence;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.portal.util.ReferenceFieldHandler;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * This is a component that converts the profiles (= object tree) to XML and vice-versa
 * using Castor.
 * 
 * In order to work properly the methods provided by this interface require some 
 * parameters:
 * objectmap : containing a map of objects for resolving references during load
 * profiletype: specifying the mapping (this is one of layout, copletinstancedata, copletdata or copletbasedate
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CastorSourceConverter.java,v 1.4 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class CastorSourceConverter
    extends AbstractLogEnabled
    implements Component, Serviceable, Configurable, Initializable, ThreadSafe {
        
    public static final String ROLE = CastorSourceConverter.class.getName();

    private Map mappingSources = new HashMap();
    private ServiceManager manager;
    private Map mappings = new HashMap();

    public Object getObject(InputStream stream, Map parameters) throws ConverterException {
        try {
            ReferenceFieldHandler.setObjectMap((Map)parameters.get("objectmap"));
            Unmarshaller unmarshaller = new Unmarshaller((Mapping)this.mappings.get(parameters.get("profiletype")));
            Object result = unmarshaller.unmarshal(new InputSource(stream));
            stream.close();
            return result;
        } catch (MappingException e) {
            throw new ConverterException("can't create Unmarshaller", e);
        } catch (Exception e) {
            throw new ConverterException(e.getMessage(), e);
        }
    }

	public void storeObject(OutputStream stream, Map parameters, Object object) throws ConverterException {
        Writer writer = new OutputStreamWriter(stream);
		try {
			Marshaller marshaller = new Marshaller( writer );
			marshaller.setMapping((Mapping)this.mappings.get(parameters.get("profiletype")));
			marshaller.marshal(object);
			writer.close();
		} catch (MappingException e) {
			throw new ConverterException("can't create Unmarshaller", e);
		} catch (Exception e) {
			throw new ConverterException(e.getMessage(), e);
		}
	}

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
    	Configuration[] children = config.getChildren("mapping-source");
    	for (int i=0; i<children.length; i++) {
    		Configuration mappingSource = children[i];
    		this.mappingSources.put(mappingSource.getAttribute("source"), mappingSource.getValue());
    	}
    }

    /* (non-Javadoc)
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
				this.mappings.put(name, mapping);
        	}
        } finally {
            if (source != null) {
                resolver.release(source);
            }
            manager.release(resolver);
        }
    }
}
