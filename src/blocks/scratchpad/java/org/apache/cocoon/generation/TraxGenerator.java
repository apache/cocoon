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
