package org.apache.cocoon;

/**
 * The Cocoon strings.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:11 $
 */

public interface Defaults {

    public static final String NAME = "Cocoon";
    public static final String VERSION = "1.6-dev";

    public static final String INIT_ARG = "properties";
    public static final String PROPERTIES = "cocoon.properties";
    public static final String INTERNAL_PROPERTIES = "org/apache/cocoon/" + PROPERTIES;

    public static final String SHOW_STATUS = "selfservlet.enabled";
    public static final String STATUS_URL = "selvservlet.uri";
    public static final String STATUS_URL_DEFAULT = "/Cocoon.xml";
 	
    public static final String PARSER_PROP = "parser";
    public static final String PARSER_DEFAULT = "org.apache.cocoon.parser.OpenXMLParser";
    public static final String CACHE_PROP = "cache";
    public static final String CACHE_DEFAULT = "org.apache.cocoon.cache.CocoonCache";
    public static final String STORE_PROP = "store";
    public static final String STORE_DEFAULT = "org.apache.cocoon.store.CocoonStore";

    public static final String PRODUCER_PROP = "producer";
    public static final String PROCESSOR_PROP = "processor";
    public static final String FORMATTER_PROP = "formatter";
    public static final String BROWSERS_PROP = "browser";
    public static final String INTERPRETER_PROP = "interpreter";
    
    public static final String COCOON_PROCESS_PI = "cocoon-process";
    public static final String COCOON_FORMAT_PI = "cocoon-format";
    public static final String STYLESHEET_PI = "xml-stylesheet";
    
    public static final String DEFAULT_BROWSER = "default";
    
    public static final boolean DEBUG = false;
    public static final boolean VERBOSE = true;

}