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
package org.apache.cocoon.environment;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;


/**
 * This is an utility class to create an object model which is similar to the one
 * used in flow, that can be used from every component.
 * 
 * Work-in-progress, derived from JXTemplateGenerator
 * 
 * @version $Id: TemplateObjectModelHelper.java 359757 2005-12-29 08:53:30Z cziegeler $
 */
public class TemplateObjectModelHelper {

    /** Avoid instantiation */
    private TemplateObjectModelHelper() {}

    public static void fillContext(Object contextObject, Map map) {
        // Hack: I use jxpath to populate the context object's properties
        // in the jexl context
        final JXPathBeanInfo bi =
            JXPathIntrospector.getBeanInfo(contextObject.getClass());
        if (bi.isDynamic()) {
            Class cl = bi.getDynamicPropertyHandlerClass();
            try {
                DynamicPropertyHandler h =
                    (DynamicPropertyHandler) cl.newInstance();
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
                        map.put(props[i].getName(),
                                read.invoke(contextObject, null));
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    /**
     * Create the object model.
     * Currently the object model is a map with one single entry:
     *  cocoon + request         The Request Object
     *         + session         The Session (if available)
     *         + context         The Context
     *         + continuation    The Continuation (if available)
     *         + parameters      The parameters (if provided)
     */
    public static Map getTemplateObjectModel(final Map objectModel,
                                             final Parameters parameters) {

        // first create the "cocoon object":
        final Map cocoon = new HashMap();

        // cocoon.request
        final Request request = ObjectModelHelper.getRequest( objectModel );
        cocoon.put("request", request);

        // cocoon.session
        final Session session = request.getSession(false);
        if (session != null) {
            cocoon.put("session", session);
        }

        // cocoon.context
        final org.apache.cocoon.environment.Context context =
            ObjectModelHelper.getContext( objectModel );
        cocoon.put("context", context);

        // cocoon.continuation
        final Object cont = FlowHelper.getWebContinuation(objectModel);
        if ( cont != null ) {
            cocoon.put("continuation", cont);
        }

        // cocoon.parameters
        if ( parameters != null ) {
            cocoon.put("parameters", new ParametersMap(parameters));
        }

        final Map map = new HashMap();
        map.put("cocoon", cocoon);

        // Now add objects from flow context (if any)
        final Object contextObject = FlowHelper.getContextObject(objectModel);
        if (contextObject instanceof Map) {
            map.putAll((Map)contextObject);
        } else if ( contextObject != null ) {
            fillContext(contextObject, map);
        }

        return map;
    }

    protected static final class ParametersMap extends Parameters implements Map {

        protected final Parameters wrappedParameters;
        protected Map map;

        public ParametersMap(Parameters wrapped) {
            wrappedParameters = wrapped;
        }

        public boolean equals(Object arg0) {
            return wrappedParameters.equals(arg0);
        }

        public String[] getNames() {
            return wrappedParameters.getNames();
        }

        public String getParameter(String arg0, String arg1) {
            return wrappedParameters.getParameter(arg0, arg1);
        }

        public String getParameter(String arg0) throws ParameterException {
            return wrappedParameters.getParameter(arg0);
        }

        public boolean getParameterAsBoolean(String arg0, boolean arg1) {
            return wrappedParameters.getParameterAsBoolean(arg0, arg1);
        }

        public boolean getParameterAsBoolean(String arg0) throws ParameterException {
            return wrappedParameters.getParameterAsBoolean(arg0);
        }

        public float getParameterAsFloat(String arg0, float arg1) {
            return wrappedParameters.getParameterAsFloat(arg0, arg1);
        }

        public float getParameterAsFloat(String arg0) throws ParameterException {
            return wrappedParameters.getParameterAsFloat(arg0);
        }

        public int getParameterAsInteger(String arg0, int arg1) {
            return wrappedParameters.getParameterAsInteger(arg0, arg1);
        }

        public int getParameterAsInteger(String arg0) throws ParameterException {
            return wrappedParameters.getParameterAsInteger(arg0);
        }

        public long getParameterAsLong(String arg0, long arg1) {
            return wrappedParameters.getParameterAsLong(arg0, arg1);
        }

        public long getParameterAsLong(String arg0) throws ParameterException {
            return wrappedParameters.getParameterAsLong(arg0);
        }

        public Iterator getParameterNames() {
            return wrappedParameters.getParameterNames();
        }

        public int hashCode() {
            return wrappedParameters.hashCode();
        }

        public boolean isParameter(String arg0) {
            return wrappedParameters.isParameter(arg0);
        }

        public void makeReadOnly() {
            wrappedParameters.makeReadOnly();
        }

        public Parameters merge(Parameters arg0) {
            return wrappedParameters.merge(arg0);
        }

        public void removeParameter(String arg0) {
            wrappedParameters.removeParameter(arg0);
        }

        public String setParameter(String arg0, String arg1) throws IllegalStateException {
            return wrappedParameters.setParameter(arg0, arg1);
        }

        public void clear() {
            this.checkWriteable();
        }

        protected Map getMap() {
            if ( this.map == null ) {
                this.map = new HashMap();
                String[] names = this.getNames();
                for(int i=0; i<names.length;i++) {
                    map.put(names[i], this.getParameter(names[i], null));
                }
            }
            return this.map;
        }

        public boolean containsKey(Object arg0) {
            if ( arg0 == null ) {
                return false;
            }
            return this.getParameter(arg0.toString(), null) != null;
        }

        public boolean containsValue(Object arg0) {
            return this.getMap().containsValue(arg0);
        }

        public Set entrySet() {
            return this.getMap().entrySet();
        }

        public Object get(Object arg0) {
            if ( arg0 == null ) {
                return null;
            }
            return this.getParameter(arg0.toString(), null);
        }

        public boolean isEmpty() {
            return this.getNames().length == 0;
        }

        public Set keySet() {
            return this.getMap().keySet();
        }

        public Object put(Object arg0, Object arg1) {
            this.checkWriteable();
            return null;
        }

        public void putAll(Map arg0) {
            this.checkWriteable();
        }

        public Object remove(Object arg0) {
            this.checkWriteable();
            return null;
        }

        public int size() {
            return this.getNames().length;
        }

        public Collection values() {
            return this.getMap().values();
        }

        /**
         * @see org.apache.avalon.framework.parameters.Parameters#toString()
         */
        public String toString() {
            return this.wrappedParameters.toString();
        }
    }
}
