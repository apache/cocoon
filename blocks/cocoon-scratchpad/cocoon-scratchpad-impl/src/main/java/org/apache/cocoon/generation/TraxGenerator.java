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
package org.apache.cocoon.generation;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.jxdom.DocumentAdapter;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.TraxTransformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.xml.sax.SAXException;

/**
 * <p>XSLT Generator: works by taking a Java Bean as an input "document"</p>
 */
public class TraxGenerator extends TraxTransformer implements Generator {

    //
    // VG: Caching aspect: DocumentAdapter will have to dynamically generate
    // cache validity depending on what resources had been used during
    // generation phase. Another option is to leave it uncacheable.
    //

    DocumentAdapter doc;

    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);
        Object bean = FlowHelper.getContextObject(objectModel);
        WebContinuation kont = FlowHelper.getWebContinuation(objectModel);
        Map map = new HashMap();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Response response = ObjectModelHelper.getResponse(objectModel);
        Context context = ObjectModelHelper.getContext(objectModel);
        if (bean != null) {
            fillContext(bean, map);
            map.put("flowContext", bean);
            map.put("continuation", kont);
        }
        map.put("request", request);
        map.put("response", response);
        map.put("context", context);
        Object session = request.getSession(false);
        if (session != null) {
            map.put("session", session);
        }
        doc = new DocumentAdapter(map, "document");
    }

    private void fillContext(Object contextObject, Map map) {
        if (contextObject == null) return;
        // Hack: I use jxpath to populate the context object's properties
        // in the jexl context
        final JXPathBeanInfo bi = 
            JXPathIntrospector.getBeanInfo(contextObject.getClass());
        if (bi.isDynamic()) {
            Class cl = bi.getDynamicPropertyHandlerClass();
            try {
                DynamicPropertyHandler h = (DynamicPropertyHandler) cl.newInstance();
                String[] result = h.getPropertyNames(contextObject);
                for (int i = 0; i < result.length; i++) {
                    try {
                        map.put(result[i], 
                                (h.getProperty(contextObject, result[i])));
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        } else {
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (int i = 0; i < props.length; i++) {
                try {
                    Method read = props[i].getReadMethod();
                    if (read != null) {
                        map.put(props[i].getName(), 
                                (read.invoke(contextObject, null)));
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    public void generate()
        throws IOException, SAXException, ProcessingException {
        javax.xml.transform.Transformer transformer = 
            transformerHandler.getTransformer();
        DOMSource src = new DOMSource(doc);
        SAXResult result = new SAXResult(xmlConsumer);
        try {
            transformer.transform(src, result);
        } catch (TransformerException exc) {
            throw new SAXException(exc.getMessage(), exc);
        }
    }

    public void setConsumer(XMLConsumer consumer) {
        this.xmlConsumer = consumer;
    }

    public void recycle() {
        super.recycle();
        this.doc = null;
    }
}
