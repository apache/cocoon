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

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.PackageFunctions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class JXPathHelperConfiguration {

    /**
     * Contains all globally registered extension classes and
     * packages. Thus the lookup and loading of globally registered
     * extensions is done only once.
     */
    private FunctionLibrary library;

    /**
     * Set lenient mode for jxpath
     * (i.e. throw an exception on unsupported attributes)?
     * Defaults to true.
     */
    private boolean lenient;

    /**
     * Contains all registered namespace prefixes.
     */
    private Map namespaces;


    /**
     * Create empty jxpath configuration
     */
    public JXPathHelperConfiguration() {
        this.lenient = true;
    }

    /**
     * Create root jxpath configuration
     */
    public JXPathHelperConfiguration(Configuration config)
    throws ConfigurationException {
        this.lenient = config.getChild("lenient").getValueAsBoolean(true);
        this.library = new FunctionLibrary();
        setup(config);

        // the following is necessary to be able to use methods on objects without
        // explicitely registering extension functions (see PackageFunctions javadoc)
        this.library.addFunctions(new PackageFunctions("", null));
    }

    /**
     * Create child jxpath configuration
     */
    public JXPathHelperConfiguration(JXPathHelperConfiguration global, Configuration config)
    throws ConfigurationException {
        this.lenient = global.lenient;
        this.library = new FunctionLibrary();
        this.library.addFunctions(global.getLibrary());
        if (global.getNamespaces() != null) {
            this.namespaces = new HashMap(global.getNamespaces());
        }
        setup(config);
    }


    public boolean isLenient() {
        return this.lenient;
    }

    public FunctionLibrary getLibrary() {
        return this.library;
    }

    public Map getNamespaces() {
        return this.namespaces;
    }


    private void setup(Configuration config)
    throws ConfigurationException {
        getFunctions(config);
        getPackages(config);
        getNamespaces(config);
    }

    /**
     * Register all extension functions listed in the configuration
     * through <code>&lt;function name="fully.qualified.Class"
     * prefix="prefix"/&gt;</code> in the given FunctionLibrary.
     *
     * @param conf a <code>Configuration</code> value
     */
    private void getFunctions(Configuration conf) {

        Configuration[] children = conf.getChildren("function");
        int i = children.length;
        while (i-- > 0) {
            String clazzName = children[i].getAttribute("name", null);
            String prefix = children[i].getAttribute("prefix", null);
            if (clazzName != null && prefix != null) {
                try {
                    Class clazz = Class.forName(clazzName);
                    this.library.addFunctions(new ClassFunctions(clazz, prefix));
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
     * @param conf a <code>Configuration</code> value
     */
    private void getPackages(Configuration conf) {

        Configuration[] children = conf.getChildren("package");
        int i = children.length;
        while (i-- > 0) {
            String packageName = children[i].getAttribute("name", null);
            String prefix = children[i].getAttribute("prefix", null);
            if (packageName != null && prefix != null) {
                this.library.addFunctions(new PackageFunctions(packageName, prefix));
            }
        }
    }

    /**
     * Register all namespaces listed in the configuration
     * through <code>&lt;namespace uri="uri:foo"
     * prefix="bar"/&gt;</code> in the configuration.
     *
     * @param conf a <code>Configuration</code> value
     */
    private void getNamespaces(Configuration conf)
    throws ConfigurationException {

        Configuration[] children = conf.getChildren("namespace");
        int i = children.length;
        if (i > 0) {
            this.namespaces = new HashMap(i + 2);
        }
        while (i-- > 0) {
            String uri = children[i].getAttribute("uri");
            String prefix = children[i].getAttribute("prefix");
            if (uri != null && prefix != null) {
                this.namespaces.put(prefix, uri);
            }
        }
    }
}
