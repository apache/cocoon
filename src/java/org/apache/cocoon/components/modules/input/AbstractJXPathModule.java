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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.jxpath.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * @version CVS $Id: AbstractJXPathModule.java,v 1.3 2004/03/08 13:58:30 cziegeler Exp $
 */
public abstract class AbstractJXPathModule extends AbstractInputModule {

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

        // JXPathMetaModule starts copying here
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


    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        if (contextObj == null) return null;
        if (modeConf != null) { 
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name); 
        }
        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            setupExtensions(jxContext, modeConf);
            if (this.lenient) jxContext.setLenient(true); // return null insted of exception on non existing property
            Object obj = jxContext.getValue(name);
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
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name); 
        }
        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            List values = new LinkedList();
            setupExtensions(jxContext, modeConf);
            if (this.lenient) jxContext.setLenient(true); // return null insted of exception on non existing property
            Iterator i = jxContext.iterate(name);
            while (i.hasNext()) {
                values.add(i.next());
            }
            Object[] obj = values.toArray();
            if (obj.length == 0) obj = null;
            return obj;
        } catch (Exception e) {
            throw new ConfigurationException(
                "Module does not support <" + name + ">" + "attribute.",
                e
            );
        }
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
