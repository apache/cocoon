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
package org.apache.cocoon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The <code>Constants</code> use throughout the core of the Cocoon engine.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:proyal@managingpartners.com">Peter Royal</a>
 * @version CVS $Id: Constants.java,v 1.13 2004/05/27 13:14:36 cziegeler Exp $
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
                throw new RuntimeException("Cocoon cannot find required properties from " + PROPS_FILE);
            }
            properties.load(is);
        } catch (IOException ioe) {
            throw new RuntimeException("Cocoon cannot load required properties from " + PROPS_FILE);
        }

    }

    /** The name of this project. */
    public static final String NAME = properties.getProperty("name");

    /** The version of this build. */
    public static final String VERSION = properties.getProperty("version");

    /** The full name of this project. */
    public static final String COMPLETE_NAME = properties.getProperty("fullname") + " " + VERSION;

    /** The version of the configuration schema */
    public static final String CONF_VERSION  = "2.1";

    /** The year of the build */
    public static final String YEAR = properties.getProperty("year");

    /**
     * The request parameter name to reload the configuration.
     *
     * FIXME(GP): Isn't this Servlet specific?
     */
    public static final String RELOAD_PARAM = "cocoon-reload";

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

    /** The name of the property holding the class for a XML parser */
    public static final String PARSER_PROPERTY = "org.apache.excalibur.xml.sax.SAXParser";

    /** The name of the class for the default XML parser to use */
    public static final String DEFAULT_PARSER  = "org.apache.excalibur.xml.impl.JaxpParser";

    /** The namespace for the XSP core logicsheet. */
    public static final String XSP_URI = "http://apache.org/xsp";

    /**
     * The namespace prefix for the request logicsheet.
     *
     * FIXME(GP): Would logicsheets belong to the core?
     */
    public static final String XSP_REQUEST_PREFIX = "xsp-request";

    /**
     * The namespace for the request logicsheet.
     *
     * FIXME(GP): Would logicsheets belong to the core?
     */
    public static final String XSP_REQUEST_URI = XSP_URI + "/request/2.0";

    /**
     * The namespace prefix for the response logicsheet.
     *
     * FIXME(GP): Would logicsheets belong to the core?
     */
    public static final String XSP_RESPONSE_PREFIX = "xsp-response";

    /**
     * The namespace for the response logicsheet.
     *
     * FIXME(GP): Would logicsheets belong to the core?
     */
    public static final String XSP_RESPONSE_URI = XSP_URI + "/response/2.0";

    /**
     * The namespace prefix for the cookie logicsheet.
     *
     * FIXME(GP): Would logicsheets belong to the core?
     */
    public static final String XSP_COOKIE_PREFIX = "xsp-cookie";

    /**
     * The namespace for the cookie logicsheet.
     *
     * FIXME(GP): Would logicsheets belong to the core?
     */
    public static final String XSP_COOKIE_URI = XSP_URI + "/cookie/2.0";

    /**
     * Don't know exactly what this is for. (I can guess it's for the FormValidator)
     *
     * FIXME(GP): Isn't this component specific?
     */
    public static final String XSP_FORMVALIDATOR_PATH = "org.apache.cocoon.acting.FormValidatorAction.results";

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

    /** Don't know exactly what this is for (and it is not used in the code base) */
    public static final String LINK_CRAWLING_ROLE = "static";

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
     */
    public static final String DEFAULT_WORK_DIR = "./work";

    /**
     * How a default configuration file is named.
     */
    public static final String DEFAULT_CONF_FILE = "cocoon.xconf";

    /** The namespace URI for the Error/Exception XML */
    public static final String ERROR_NAMESPACE_URI = "http://apache.org/cocoon/error/" + CONF_VERSION;

    /** The namespace prefix for the Error/Exception XML */
    public static final String ERROR_NAMESPACE_PREFIX = "error";

    /** Application <code>Context</code> Key for the environmental Context */
    public static final String CONTEXT_ENVIRONMENT_CONTEXT = "environment-context";

    /** Application <code>Context</code> Key for the global classloader */
    public static final String CONTEXT_CLASS_LOADER = "class-loader";

    /** Application <code>Context</code> Key for the work directory path */
    public static final String CONTEXT_WORK_DIR = "work-directory";

    /** Application <code>Context</code> Key for the upload directory path */
    public static final String CONTEXT_UPLOAD_DIR = "upload-directory";

    /** Application <code>Context</code> Key for the cache directory path */
    public static final String CONTEXT_CACHE_DIR = "cache-directory";

    /** Application <code>Context</code> Key for the current classpath */
    public static final String CONTEXT_CLASSPATH = "classpath";

    /**
     * Application <code>Context</code> Key for the URL to the configuration file
     * (usually named cocoon.xconf)
     */
    public static final String CONTEXT_CONFIG_URL = "config-url";

    /** Application <code>Context</code> Key for the default encoding */
    public static final String CONTEXT_DEFAULT_ENCODING = "default-encoding";

    
    /**
     * Should descriptors be reloaded?
     *
     * FIXME(GP): Isn't this Action specific only?
     */
    public static final boolean DESCRIPTOR_RELOADABLE_DEFAULT = true;
    
}












