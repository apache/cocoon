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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * JXPathModule allows to access properties of any object in generic
 * way.  JXPath provides APIs for the traversal of graphs of
 * JavaBeans, DOM and other types of objects using the XPath
 * syntax. This is based on the AbstractJXPathModule and duplicates
 * the code since multiple inheritance is not possible. Please keep both
 * classes in sync.
 *
 * <p>Configuration example:</p>
 * <table>
 * <tr><td><code>&lt;lenient&gt;false&lt;/lenient&gt;</td>
 * <td>When set to true, non-existing attributes return null, when set to false,
 *     an exception is thrown. Default is true.</td> 
 *</tr>
 * <tr><td><code>&lt;parameter&gt;false&lt;/parameter&gt;</td>
 * <td>Attribute name to be used instead of passed attribute name.</td> 
 *</tr>
 * <tr><td><code>&lt;from-parameter&gt;false&lt;/from-parameter&gt;</td>
 * <td>Attribute name to pass to configured input module</td> 
 *</tr>
 * <tr><td><code>&lt;input-module name="request-attr"/&gt;</td>
 * <td>Uses the "request-attr" input module to obtain a value and 
 *     applies the given JXPath expression to it.</td> 
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
 * <p>In addition, it accepts the attributes "parameter" to override
 * the attribute name and "from-parameter" to pass as attribute name
 * to the configured input module.</p>
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: JXPathMetaModule.java,v 1.7 2004/03/05 13:02:48 bdelacretaz Exp $
 */
public class JXPathMetaModule extends AbstractMetaModule implements Configurable, ThreadSafe {

    /**
     * Contains all globally registered extension classes and
     * packages. Thus the lookup and loading of globally registered
     * extensions is done only once.
     *
     */
    protected JXPathHelperConfiguration configuration = null;

    /** set lenient mode for jxpath (i.e. throw an exception on
     * unsupported attributes) ? 
     */
    private static final boolean lenient = true;

    protected String parameter = "";


    public JXPathMetaModule() {
        // this value has a default in the super class
        this.defaultInput = "request-attr";
    }


    /**
     * Configure component. Preprocess list of packages and functions
     * to add to JXPath context later.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name",this.defaultInput);
        this.parameter = config.getChild("parameter").getValue(this.parameter);

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
     * Looks up object from configured InputModule. 
     *
     * @param modeConf a <code>Configuration</code> value
     * @param objectModel a <code>Map</code> value
     * @return an <code>Object</code> value
     */
    protected  Object getContextObject(Configuration modeConf, Map objectModel) throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }

        Configuration mConf = null;
        String inputName=null;
        String parameter = this.parameter;
        if (modeConf!=null) {
            mConf   = modeConf.getChild("input-module");
            inputName   = mConf.getAttribute("name",null);
            parameter   = modeConf.getChild("from-parameter").getValue(parameter);
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("modeConf is "+modeConf+" this.inputConf is "+this.inputConf
                              +" mConf is "+mConf+" this.input is "+this.input
                              +" this.defaultInput is "+this.defaultInput
                              +" inputName is "+inputName+" parameter is "+parameter);

        Object obj =  this.getValue(parameter, objectModel, 
                                    this.input, this.defaultInput, this.inputConf,
                                    null, inputName, mConf);
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("returning an "+(obj == null ? "null" : obj.getClass().getName())+" as "+obj);

        return obj;
    }

}
