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
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-03-30 17:14:24 $
 *
 */

public interface Context {

    public Object getAttribute(String name);

	public URL getResource(String path) throws MalformedURLException;

	public java.lang.String getMimeType(String file);

}
