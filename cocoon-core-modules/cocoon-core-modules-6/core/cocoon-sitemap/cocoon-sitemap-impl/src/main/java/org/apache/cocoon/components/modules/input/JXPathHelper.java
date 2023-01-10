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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class JXPathHelper {

    private JXPathHelper() {
        // no instances allowed
    }

    /**
     * Configure component. Preprocess list of packages, functions
     * and namespaces to add to the JXPath context later.
     *
     * This method used in both AbstractJXPathModule and JXPathMetaModule
     * to configure JXPath.
     *
     * @param config a <code>Configuration</code> value.
     * @return The JXPathHelperConfiguration.
     * @exception ConfigurationException if an error occurs
     */
    public static JXPathHelperConfiguration setup(Configuration config)
    throws ConfigurationException {

        return new JXPathHelperConfiguration(config);
    }


    /**
     * Actually add global functions and packages as well as those
     * listed in the configuration object.
     *
     * @param setup The JXPathHelperConfiguration.
     * @param context a <code>JXPathContext</code> value.
     * @param conf a <code>Configuration</code> value holding local.
     * packages and functions.
     */
    private static void setup(JXPathHelperConfiguration setup, JXPathContext context, Configuration conf)
    throws ConfigurationException {

        // Create local config (if necessary)
        JXPathHelperConfiguration local = conf == null ? setup : new JXPathHelperConfiguration(setup, conf);

        // Setup context with local config
        context.setLenient(setup.isLenient());
        context.setFunctions(local.getLibrary());
        if (local.getNamespaces() != null) {
            for (Iterator i = local.getNamespaces().entrySet().iterator(); i.hasNext();) {
                final Map.Entry entry = (Map.Entry) i.next();
                context.registerNamespace((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * Return the String value of the attribute or element identified in the XPath expression.
     * @param name The XPath expression
     * @param modeConf The Configuration.
     * @param setup The JXPathHelperConfiguration.
     * @param contextObj The root Element to search.
     * @return The String value of the attribute or element identified.
     * @throws ConfigurationException if an Exception occurs.
     */
    public static String getAttributeValue(String name,
                                           Configuration modeConf,
                                           JXPathHelperConfiguration setup,
                                           Object contextObj)
    throws ConfigurationException {

        if (contextObj == null) {
            return null;
        }

        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            setup(setup, jxContext, modeConf);
            Object obj = jxContext.getValue(name);
            if (obj != null) {
                return obj.toString();
            }
            return null;
        } catch (Exception e) {
            throw new ConfigurationException("Module does not support <" + name + ">" + "attribute.", e);
        }
    }

    /**
     * Return the String value of the attribute or the Node found using the XPath expression.
     * @param name The XPath expression
     * @param modeConf The Configuration.
     * @param setup The JXPathHelperConfiguration.
     * @param contextObj The root Element to search.
     * @return The String value of the attribute or the Element located.
     * @throws ConfigurationException if an Exception occurs.
     */
    public static Object getAttribute(String name,
                                      Configuration modeConf,
                                      JXPathHelperConfiguration setup,
                                      Object contextObj)
    throws ConfigurationException {

        if (contextObj == null) {
            return null;
        }

        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            setup(setup, jxContext, modeConf);
            return jxContext.selectSingleNode(name);
        } catch (Exception e) {
            throw new ConfigurationException("Module does not support <" + name + ">" + "attribute.", e);
        }
    }

    public static Object[] getAttributeValues(String name,
                                              Configuration modeConf,
                                              JXPathHelperConfiguration setup,
                                              Object contextObj)
    throws ConfigurationException {

        if (contextObj == null) {
            return null;
        }

        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            setup(setup, jxContext, modeConf);

            List values = null;
            Iterator i = jxContext.iterate(name);
            if (i.hasNext()) {
                values = new LinkedList();
            }
            while (i.hasNext()) {
                values.add(i.next());
            }
            Object[] obj = null;
            if (values != null) {
                obj = values.toArray();
                if (obj.length == 0) {
                    obj = null;
                }
            }
            return obj;
        } catch (Exception e) {
            throw new ConfigurationException("Module does not support <" + name + ">" + "attribute.", e);
        }
    }


    public static Iterator getAttributeNames(JXPathHelperConfiguration setup, Object contextObj)
    throws ConfigurationException {

        if (contextObj == null) {
            return null;
        }

        try {
            JXPathBeanInfo info = JXPathIntrospector.getBeanInfo(contextObj.getClass());
            java.beans.PropertyDescriptor[] properties = info.getPropertyDescriptors();

            List names = new LinkedList();
            for (int i = 0; i < properties.length; i++) {
                names.add(properties[i].getName());
            }

            return names.listIterator();
        } catch (Exception e) {
            throw new ConfigurationException("Error retrieving attribute names for class: " + contextObj.getClass(), e);
        }
    }
}
