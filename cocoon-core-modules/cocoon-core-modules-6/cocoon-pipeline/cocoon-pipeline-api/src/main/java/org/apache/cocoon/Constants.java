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
package org.apache.cocoon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The <code>Constants</code> used throughout the core of the Cocoon engine.
 *
 * @version $Id$
 */
public final class Constants {

    /** Our properties are now here: */
    private static final String PROPS_FILE = "org/apache/cocoon/cocoon.properties";

    static final Properties properties;

    /**
     * Load the cocoon properties
     */
    static {
        properties = new Properties();
        try {
            final InputStream is = Constants.class.getClassLoader().getResourceAsStream(PROPS_FILE);
            if ( null == is ) {
                throw new ConstantsInitializationException("Cocoon cannot find required properties from " + PROPS_FILE);
            }
            properties.load(is);
        } catch (IOException ioe) {
            throw new ConstantsInitializationException("Cocoon cannot load required properties from " + PROPS_FILE, ioe);
        }

    }

    /**
     * The name of this project.
     * @deprecated This will be removed soon.
     */
    public static final String NAME = "Cocoon";

    /** The version of this build. */
    public static final String VERSION = properties.getProperty("version");

    /**
     * The full name of this project.
     * @deprecated This will be removed soon.
     */
    public static final String COMPLETE_NAME = "Apache Cocoon " + VERSION;

    /** The version of the configuration schema */
    public static final String CONF_VERSION  = "2.2";

    /** The year of the build */
    public static final String YEAR = properties.getProperty("year");

    /**
     * The request parameter name to add a line of the request duration.
     *
     * FIXME(GP): Isn't this Servlet specific?
     */
    public static final String SHOWTIME_PARAM = "cocoon-showtime";

    /**
     * The request parameter name to request a specific view of a resource.
     *
     * FIXME(GP): Isn't this Servlet specific?
     */
    public static final String VIEW_PARAM = "cocoon-view";

    /**
     * The request parameter name to trigger a specific action.
     *
     * FIXME(GP): Isn't this Servlet specific?
     */
    public static final String ACTION_PARAM = "cocoon-action";

    /**
     * The request parameter prefix to trigger a specific action.
     *
     * FIXME(GP): Isn't this Servlet specific?
     */
    public static final String ACTION_PARAM_PREFIX = "cocoon-action-";

    /** The URI for xml namespaces */
    public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

    /**
     * Mime-type for the link view
     *
     * FIXME(GP): Isn't this Environment specific?
     */
    public static final String LINK_CONTENT_TYPE = "application/x-cocoon-links";

    /**
     * Name of the request value for the link view
     *
     * FIXME(GP): Isn't this Environment specific?
     */
    public static final String LINK_VIEW = "links";

    /**
     * Key of the Map of index translation table.
     * <p>Presence of this Map in the ObjectModel indicates to the Sitemap that link
     * translation mode has been requested by the environment. Sitemap adds LinkTranslator
     * transformer to the pipeline, which replaces all the links in the input document with
     * the links from this translation table.
     * <p>
     * TODO(VG): Move this declaration to ObjectModelHelper
     * comment found at ObjectModelHelper(JH):
     * LINK_OBJECT should also be moved to CommandLineEnvironment
     */
    public static final String LINK_OBJECT = "link";

    /**
     * Key of the List for collecting links.
     * <p>Presence of this Map in the ObjectModel indicates to the Sitemap that link
     * gathering mode has been requested by the environment. Sitemap adds LinkGatherer
     * transformer to the pipeline, which gathers the links in the input document into
     * this List.
     * <p>
     */
    public static final String LINK_COLLECTION_OBJECT = "link-collection";

    /**
     * The name of a <code>NotifyingObject</code> in the so called objectModel <code>Map</code>.
     * @deprecated Usage of Notifying object has been deprecated in favor of using Exception object at
     *             {@link org.apache.cocoon.environment.ObjectModelHelper#THROWABLE_OBJECT}.
     */
    public static final String NOTIFYING_OBJECT = "notifying-object";

    /**
     * The default URI to be used when a URI requested refers to
     * a directory, e.g. http://localhost:8080/site/
     */
    public static final String INDEX_URI = "index";

    /**
     * The directory to use as context root.
     */
    public static final String DEFAULT_CONTEXT_DIR = "./webapp";

    /**
     * The diretory to use to use for the generated output.
     */
    public static final String DEFAULT_DEST_DIR = "./site";

    /**
     * The diretory to use for generated files.
     * @deprecated This will be removed soon.
     */
    public static final String DEFAULT_WORK_DIR = "./work";

    /**
     * How a default configuration file is named.
     * @deprecated This will be removed soon.
     */
    public static final String DEFAULT_CONF_FILE = "cocoon.xconf";

    /** The namespace URI for the Error/Exception XML */
    public static final String ERROR_NAMESPACE_URI = "http://apache.org/cocoon/error/2.1";

    /** The namespace prefix for the Error/Exception XML */
    public static final String ERROR_NAMESPACE_PREFIX = "error";

    /** Application <code>Context</code> Key for the environmental Context (= ServletContext) */
    public static final String CONTEXT_ENVIRONMENT_CONTEXT = "environment-context";

    /**
     * Application <code>Context</code> Key for the work directory path.
     * @deprecated Use {@link org.apache.cocoon.configuration.Settings#getWorkDirectory()} instead.
     */
    public static final String CONTEXT_WORK_DIR = "work-directory";

    /**
     * Application <code>Context</code> Key for the cache directory path.
     * @deprecated Use {@link org.apache.cocoon.configuration.Settings#getCacheDirectory()} instead.
     */
    public static final String CONTEXT_CACHE_DIR = "cache-directory";

    /** Application <code>Context</code> key for the current environment prefix */
    public static final String CONTEXT_ENV_PREFIX = "env-prefix";

    /** Application <code>Context</code> key prefix for the current sitemap virtual components */
    public static final String CONTEXT_VPC_PREFIX = "vpc-";

    /** Path to the wiring.xml relative to the context root directory */
    public static final String WIRING = "wiring.xml";

    public static final String BLOCK_META_DIR = "COB-INF";

    /**
     * Application <code>Context</code> Key for the default encoding.
     * @deprecated Use {@link org.apache.cocoon.configuration.Settings#getFormEncoding()}.
     */
    public static final String CONTEXT_DEFAULT_ENCODING = "default-encoding";
}
