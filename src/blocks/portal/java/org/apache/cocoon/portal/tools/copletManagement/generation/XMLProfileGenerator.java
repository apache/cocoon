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
package org.apache.cocoon.portal.tools.copletManagement.generation;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.persistence.CastorSourceConverter;
import org.apache.cocoon.components.persistence.ConverterException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * 
 * @version CVS $Id$
 */
public class XMLProfileGenerator 
extends ServiceableGenerator {

    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        
        SAXParser parser = null;
        CastorSourceConverter converter = null;
        final Object context = FlowHelper.getContextObject(this.objectModel);
        Map myMap = new HashMap();
        if (context instanceof Map) {
            myMap = (Map) context;
        } else {
            fillContext(context, myMap);
        }
        try {
        	Object layout = myMap.get("layout");
        	converter = (CastorSourceConverter) this.manager.lookup(org.apache.cocoon.components.persistence.CastorSourceConverter.ROLE);
        	ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        	HashMap para = new HashMap();
        	para.put("profiletype", "layout");
        	converter.storeObject(os, para, layout);
        	String xml = new String();
        	xml = os.toString();
            final InputSource inputSource = new InputSource(new StringReader(xml));
            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
            parser.parse(inputSource, super.xmlConsumer);
        } catch (ServiceException e) {
        	throw new ProcessingException(e);
		} catch (ConverterException e) {
			throw new ProcessingException(e);
		}  finally {
            this.manager.release(parser);
            this.manager.release(converter);
        }
    }
    
    // FIXME: Copy from JXTemplateGenerator
    private void fillContext(Object contextObject, Map map) {
        if (contextObject != null) {
            // Hack: I use jxpath to populate the context object's properties
            // in the jexl context
            final JXPathBeanInfo bi = JXPathIntrospector.getBeanInfo(contextObject.getClass());
            if (bi.isDynamic()) {
                Class cl = bi.getDynamicPropertyHandlerClass();
                try {
                    DynamicPropertyHandler h = (DynamicPropertyHandler)cl.newInstance();
                    String[] result = h.getPropertyNames(contextObject);
                    int len = result.length;
                    for (int i = 0; i < len; i++) {
                        try {
                            map.put(result[i], h.getProperty(contextObject, result[i]));
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            } else {
                PropertyDescriptor[] props =  bi.getPropertyDescriptors();
                int len = props.length;
                for (int i = 0; i < len; i++) {
                    try {
                        Method read = props[i].getReadMethod();
                        if (read != null) {
                            map.put(props[i].getName(), read.invoke(contextObject, null));
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }
}

