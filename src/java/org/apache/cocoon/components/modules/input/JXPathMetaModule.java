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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.jxpath.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * @version CVS $Id: JXPathMetaModule.java,v 1.5 2004/03/08 13:58:30 cziegeler Exp $
 */
public class JXPathMetaModule extends AbstractMetaModule implements Configurable, ThreadSafe {

    /**
     * Contains all globally registered extension classes and
     * packages. Thus the lookup and loading of globally registered
     * extensions is done only once.
     *
     */
    protected FunctionLibrary library = null;

    /** set lenient mode for jxpath (i.e. throw an exception on
     * unsupported attributes) ? 
     */
    protected boolean lenient = true;

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

        // start verbatim copy of AbstractJXPathModule
        // please keep both in sync.

        this.lenient = config.getChild("lenient").getValueAsBoolean(this.lenient);
        this.library = new FunctionLibrary();
        getFunctions(this.library, config);
        getPackages(this.library, config);
    }



    /**
     * Register all extension functions listed in the configuration
     * through <code>&lt;function name="fully.qualified.Class"
     * prefix="prefix"/&gt;</code> in the given FunctionLibrary.
     *
     * @param lib a <code>FunctionLibrary</code> value
     * @param conf a <code>Configuration</code> value
     */
    protected void getFunctions(FunctionLibrary lib, Configuration conf) {

        Configuration[] children = conf.getChildren("function");
        int i = children.length;
        while (i-- >0) {
            String clazzName = children[i].getAttribute("name",null);
            String prefix = children[i].getAttribute("prefix",null);
            if (clazzName != null && prefix != null) {
                try {
                    Class clazz = Class.forName(clazzName);
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("adding Class "+clazzName+" to functions");
                    lib.addFunctions(new ClassFunctions(clazz, prefix));
                } catch (ClassNotFoundException cnf) {
                    if (getLogger().isWarnEnabled())
                        getLogger().warn("Class not found: "+clazzName);
                }
            } else {
                if (getLogger().isWarnEnabled())
                    getLogger().warn("Class name or prefix null: "+clazzName+" / "+prefix);
            }
        }
    }


    /**
     * Register all extension packages listed in the configuration
     * through <code>&lt;package name="fully.qualified.package"
     * prefix="prefix"/&gt;</code> in the given FunctionLibrary.
     *
     * @param lib a <code>FunctionLibrary</code> value
     * @param conf a <code>Configuration</code> value
     */
    protected void getPackages(FunctionLibrary lib, Configuration conf)  {

        Configuration[] children = conf.getChildren("package");
        int i = children.length;
        while (i-- >0) {
            String packageName = children[i].getAttribute("name",null);
            String prefix = children[i].getAttribute("prefix",null);
            if (packageName != null && prefix != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("adding Package "+packageName+" to functions");
                lib.addFunctions(new PackageFunctions(packageName, prefix));
            } else {
                if (getLogger().isWarnEnabled())
                    getLogger().warn("Package name or prefix null: "+packageName+" / "+prefix);
            }
        }
    }


    /**
     * Actually add global functions and packages as well as those
     * listed in the configuration object.
     *
     * @param context a <code>JXPathContext</code> value
     * @param conf a <code>Configuration</code> value holding local
     * packages and functions.
     */
    protected void setupExtensions(JXPathContext context, Configuration conf) {
        
        FunctionLibrary localLib = null;

        if (conf != null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("adding local Classes and Packages to functions");
            localLib = new FunctionLibrary();
            localLib.addFunctions(this.library);
            getPackages(localLib, conf);
            getFunctions(localLib, conf);
        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("no local Classes or Packages");
            localLib = this.library;
        }
        
        context.setFunctions(localLib);
    }


    public Object getAttribute(String name, Configuration modeConf,
                               Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        if (contextObj == null) return null;
        if (modeConf != null) {
            name = modeConf.getChild("parameter").getValue(name);
        }
        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            setupExtensions(jxContext, modeConf);
            if (this.lenient) jxContext.setLenient(true); // return null insted of exception on non existing property
            Object obj = jxContext.getValue(name);
            if (getLogger().isDebugEnabled())
                getLogger().debug("for "+name+" returning an "+(obj == null ? "null" : obj.getClass().getName())+" as "+obj);
            return obj;
        } catch (Exception e) {
            throw new ConfigurationException(
                "Module does not support <" + name + ">" + "attribute.",
                e
            );
        }
    }

    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        if (contextObj == null) return null;
        try {
            JXPathBeanInfo info = JXPathIntrospector.getBeanInfo(
                contextObj.getClass());
            java.beans.PropertyDescriptor[] properties = info.getPropertyDescriptors();
            java.util.List names = new java.util.LinkedList();
            for (int i = 0; i < properties.length; i++) {
                names.add(properties[i].getName());
            }
            return names.listIterator();
        } catch (Exception e) {
            throw new ConfigurationException(
                "Error retrieving attribute names for class: "
                + contextObj.getClass(),
                e
            );
        }

    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        if (contextObj == null) return null;
        if (modeConf != null) {
            name = modeConf.getChild("parameter").getValue(name);
        }
        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            List values = null;
            setupExtensions(jxContext, modeConf);
            if (this.lenient) jxContext.setLenient(true); // return null insted of exception on non existing property
            Iterator i = jxContext.iterate(name);
            if (i.hasNext()) { values = new LinkedList(); } 
            while (i.hasNext()) {
                values.add(i.next());
            }
            Object[] obj = values.toArray();
            if (obj.length == 0) obj = null;
            if (getLogger().isDebugEnabled())
                getLogger().debug("for "+name+" returning an "+(obj == null ? "null" : obj.getClass().getName())+" as "+obj);
            return obj;
        } catch (Exception e) {
            throw new ConfigurationException(
                "Module does not support <" + name + ">" + "attribute.",
                e
            );
        }
    }

    // end verbatim copy of AbstractJXPathModule

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
