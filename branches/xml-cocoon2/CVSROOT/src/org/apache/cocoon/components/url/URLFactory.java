/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.url;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.avalon.thread.ThreadSafe;

/**
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version $Id: URLFactory.java,v 1.1.2.2 2001-04-20 20:50:04 bloritsch Exp $
 */
public interface URLFactory extends ThreadSafe {

    /**
     * Get an URL
     */
    URL getURL(String location) throws MalformedURLException;
    URL getURL(URL base, String location) throws MalformedURLException;
}