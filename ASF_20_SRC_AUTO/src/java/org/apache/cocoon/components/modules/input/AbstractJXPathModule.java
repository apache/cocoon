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

package org.apache.cocoon.components.modules.input;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * JXPathModule allows to access properties of any object in generic
 * way.  JXPath provides APIs for the traversal of graphs of
 * JavaBeans, DOM and other types of objects using the XPath
 * syntax. JXPathMetaModule is based on this class and duplicates
 * the code since multiple inheritance is not possible. Please keep both
 * classes in sync.
 *
 * <p>Configuration example:</p>
 * <table>
 * <tr><td><code>&lt;lenient&gt;false&lt;/lenient&gt;</td>
 * <td>When set to true, non-existing attributes return null, when set to false,
 *     an exception is thrown. Default is true.</td> 
 *</tr>
 * <tr><td><code>&lt;parameter&gt;foo&lt;/parameter&gt;</td>
 * <td>When set overrides attribute name passed to module.</td> 
 *</tr>
 * <tr><td><code>&lt;function name="java.lang.String" prefix="str"/&gt;</td>
 * <td>Imports the class "String" as extension class to the JXPathContext using 
 * the prefix "str". Thus "str:length(xpath)" would apply the method "length" to 
 * the string object obtained from the xpath expression. Please note that the class
 * needs to be fully qualified.</td> 
 *</tr>
 * <tr><td><code>&lt;package name="java.util" prefix="util"/&gt;</td>
 * <td>Imports all classes in the package "java.util" as extension classes to the 
 * JXPathContext using the prefix "util". Thus "util:Date.new()" would create a 
 * new java.util.Date object.</td> 
 * </tr></table>
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: AbstractJXPathModule.java,v 1.4 2004/03/05 13:02:48 bdelacretaz Exp $
 */
public abstract class AbstractJXPathModule extends AbstractInputModule {

    protected JXPathHelperConfiguration configuration = null;

    private static final boolean lenient = true; 

    /** override attribute name */
    protected String parameter = null;

    /**
     * Configure component. Preprocess list of packages and functions
     * to add to JXPath context later.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {

        this.configuration = JXPathHelper.setup(config, lenient); 
    }


    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        if (modeConf != null) { 
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name); 
        }
        return JXPathHelper.getAttribute(name, modeConf, this.configuration, contextObj);
    }


    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        return JXPathHelper.getAttributeNames(this.configuration, contextObj);
    }


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
     */
    protected abstract Object getContextObject(Configuration modeConf,
                                               Map objectModel) throws ConfigurationException;
}
