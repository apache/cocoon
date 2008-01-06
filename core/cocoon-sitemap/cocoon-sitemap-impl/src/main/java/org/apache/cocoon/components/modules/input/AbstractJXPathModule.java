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

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * JXPathModule allows to access properties of any object in generic
 * way.  JXPath provides APIs for the traversal of graphs of
 * JavaBeans, DOM and other types of objects using the XPath
 * syntax.
 *
 * <p><strong>Note:</strong> JXPathMetaModule is based on this class
 * and duplicates the code since multiple inheritance is not possible.
 * Please keep both classes in sync.</p>
 *
 * <h3>Configuration</h3>
 * <table>
 * <tr>
 *   <td><code>&lt;lenient&gt;false&lt;/lenient&gt;</code></td>
 *   <td>When set to true, non-existing attributes return null; when set to false,
 *       an exception is thrown. Default is true.</td>
 * </tr>
 * <tr>
 *   <td><code>&lt;parameter&gt;foo&lt;/parameter&gt;</td>
 *   <td>When set overrides attribute name passed to module.</td>
 * </tr>
 * <tr>
 *   <td><code>&lt;function name="java.lang.String" prefix="str"/&gt;</td>
 *   <td>Imports the class "String" as extension class to the JXPathContext using
 *   the prefix "str". Thus "str:length(xpath)" would apply the method "length" to
 *   the string object obtained from the xpath expression. Please note that the class
 *   needs to be fully qualified.</td>
 * </tr>
 * <tr>
 *   <td><code>&lt;package name="java.util" prefix="util"/&gt;</td>
 *   <td>Imports all classes in the package "java.util" as extension classes to the
 *   JXPathContext using the prefix "util". Thus "util:Date.new()" would create a
 *   new java.util.Date object.</td>
 * </tr>
 * <tr>
 *   <td><code>&lt;namespace uri="uri:foo" prefix="bar"/&gt;</td>
 *   <td>Registers the namespace identified by URI <code>uri:foo</code>
 *   with the JXPathContext using the prefix <code>bar</code>. Thus
 *   expressions can query XML with nodes in this namespace using
 *   registered prefix.</td>
 * </tr>
 * </table>
 *
 * @version $Id$
 */
public abstract class AbstractJXPathModule extends AbstractInputModule {

    /**
     * Contains all globally registered extension classes and
     * packages. Thus the lookup and loading of globally registered
     * extensions is done only once.
     */
    protected JXPathHelperConfiguration configuration;

    /**
     * Overrides attribute name
     */
    protected String parameter;

    /**
     * Configure component. Preprocess list of packages and functions
     * to add to JXPath context later.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.configuration = JXPathHelper.setup(config);
    }


    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object contextObj = getContextObject(modeConf, objectModel);
        if (modeConf != null) {
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name);
        }
        return JXPathHelper.getAttributeValue(name, modeConf, this.configuration, contextObj);
    }


    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object contextObj = getContextObject(modeConf, objectModel);
        return JXPathHelper.getAttributeNames(this.configuration, contextObj);
    }


    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object contextObj = getContextObject(modeConf, objectModel);
        if (modeConf != null) {
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name);
        }
        return JXPathHelper.getAttributeValues(name, modeConf, this.configuration, contextObj);
    }


    /**
     * Returns the object which should be used as JXPath context.
     * Descendants should override this method to return a specific object
     * that is requried by the implementing class.
     * Examples are: request, session and application context objects.
     * @param modeConf The Configuration.
     * @param objectModel Cocoon's object model Map.
     * @return The context object.
     * @throws ConfigurationException when an error occurs.
     */
    protected abstract Object getContextObject(Configuration modeConf,
                                               Map objectModel)
    throws ConfigurationException;
}
