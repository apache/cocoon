/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Defines an interface to provide client context information .  
 * 
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-18 12:05:53 $
 *
 */

public interface Context {

    Object getAttribute(String name);

    URL getResource(String path) throws MalformedURLException;

    String getMimeType(String file);

}
