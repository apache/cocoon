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
package org.apache.cocoon.components.modules.input;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.PackageFunctions;

/**
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: JXPathHelper.java,v 1.1 2004/02/15 19:12:44 haul Exp $
 */
public class JXPathHelper {

    private JXPathHelper() {
        // no instances allowed
    }

    /**
     * Configure component. Preprocess list of packages and functions
     * to add to JXPath context later.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public static JXPathHelperConfiguration setup(Configuration config, boolean lenient) throws ConfigurationException {

        // JXPathMetaModule starts copying here
        // please keep both in sync.

        lenient = config.getChild("lenient").getValueAsBoolean(lenient);
        FunctionLibrary library = new FunctionLibrary();
        getFunctions(library, config);
        getPackages(library, config);
        return new JXPathHelperConfiguration(library, lenient);
    }

    /**
     * Register all extension functions listed in the configuration
     * through <code>&lt;function name="fully.qualified.Class"
     * prefix="prefix"/&gt;</code> in the given FunctionLibrary.
     *
     * @param lib a <code>FunctionLibrary</code> value
     * @param conf a <code>Configuration</code> value
     */
    private static void getFunctions(FunctionLibrary lib, Configuration conf) {

        Configuration[] children = conf.getChildren("function");
        int i = children.length;
        while (i-- > 0) {
            String clazzName = children[i].getAttribute("name", null);
            String prefix = children[i].getAttribute("prefix", null);
            if (clazzName != null && prefix != null) {
                try {
                    Class clazz = Class.forName(clazzName);
                    lib.addFunctions(new ClassFunctions(clazz, prefix));
                } catch (ClassNotFoundException cnf) {
                    // ignore
                }
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
    private static void getPackages(FunctionLibrary lib, Configuration conf) {

        Configuration[] children = conf.getChildren("package");
        int i = children.length;
        while (i-- > 0) {
            String packageName = children[i].getAttribute("name", null);
            String prefix = children[i].getAttribute("prefix", null);
            if (packageName != null && prefix != null) {
                lib.addFunctions(new PackageFunctions(packageName, prefix));
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
    private static void setupExtensions(JXPathHelperConfiguration setup, JXPathContext context, Configuration conf) {

        FunctionLibrary localLib = null;

        if (conf != null) {
            localLib = new FunctionLibrary();
            localLib.addFunctions(setup.getLibrary());
            getPackages(localLib, conf);
            getFunctions(localLib, conf);
        } else {
            localLib = setup.getLibrary();
        }

        context.setFunctions(localLib);
    }

    public static Object getAttribute(
        String name,
        Configuration modeConf,
        JXPathHelperConfiguration setup,
        Object contextObj)
        throws ConfigurationException {

        if (contextObj == null)
            return null;
        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            setupExtensions(setup, jxContext, modeConf);
            if (setup.isLenient())
                jxContext.setLenient(true); // return null insted of exception on non existing property
            Object obj = jxContext.getValue(name);
            return obj;
        } catch (Exception e) {
            throw new ConfigurationException("Module does not support <" + name + ">" + "attribute.", e);
        }
    }

    public static Object[] getAttributeValues(
        String name,
        Configuration modeConf,
        JXPathHelperConfiguration setup,
        Object contextObj)
        throws ConfigurationException {
            
        if (contextObj == null)
            return null;
        try {
            JXPathContext jxContext = JXPathContext.newContext(contextObj);
            List values = null;
            setupExtensions(setup, jxContext, modeConf);
            if (setup.isLenient())
                jxContext.setLenient(true); // return null insted of exception on non existing property
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
                if (obj.length == 0)
                    obj = null;
            }
            return obj;
        } catch (Exception e) {
            throw new ConfigurationException("Module does not support <" + name + ">" + "attribute.", e);
        }
    }


    public static Iterator getAttributeNames(JXPathHelperConfiguration setup, Object contextObj) throws ConfigurationException {

        if (contextObj == null)
            return null;
        try {
            JXPathBeanInfo info = JXPathIntrospector.getBeanInfo(contextObj.getClass());
            java.beans.PropertyDescriptor[] properties = info.getPropertyDescriptors();
            java.util.List names = new java.util.LinkedList();
            for (int i = 0; i < properties.length; i++) {
                names.add(properties[i].getName());
            }
            return names.listIterator();
        } catch (Exception e) {
            throw new ConfigurationException("Error retrieving attribute names for class: " + contextObj.getClass(), e);
        }

    }
}
