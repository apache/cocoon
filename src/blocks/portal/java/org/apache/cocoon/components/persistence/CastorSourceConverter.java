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
 * @version CVS $Id: CastorSourceConverter.java,v 1.3 2003/12/10 17:02:04 cziegeler Exp $
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
