/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-08-31 16:41:01 $
 */

public interface Constants {

    public static final String NAME                = "@name@";
    public static final String VERSION             = "@version@";
    public static final String CONF_VERSION        = "2.0";
    public static final String YEAR                = "@year@";
    public static final String RELOAD_PARAM        = "cocoon-reload";
    public static final String SHOWTIME_PARAM      = "cocoon-showtime";
    public static final String VIEW_PARAM          = "cocoon-view";
    public static final String TEMPDIR_PROPERTY    = "org.apache.cocoon.properties.tempdir";
    public static final String DEFAULT_CONF_FILE   = "cocoon.xconf";
    public static final String DEFAULT_DEST_DIR    = "./site";
    public static final String DEFAULT_TEMP_DIR    = "./work";
    public static final String LINK_CONTENT_TYPE   = "x-application/x-cocoon-links";
    public static final String LINK_VIEW           = "links";
    public static final String LINK_CRAWLING_ROLE  = "static";
    public static final String PARSER_PROPERTY     = "org.apache.cocoon.components.parser.Parser";
    public static final String DEFAULT_PARSER      = "org.apache.cocoon.components.parser.XercesParser";
    public static final String XSP_PREFIX          = "xsp";
    public static final String XSP_URI             = "http://apache.org/xsp";
    public static final String XSP_REQUEST_PREFIX  = "xsp-request";
    public static final String XSP_REQUEST_URI     = XSP_URI + "/request";
    public static final String XSP_RESPONSE_PREFIX = "xsp-response";
    public static final String XSP_RESPONSE_URI    = XSP_URI + "/response";  

}
