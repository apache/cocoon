/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.avalon.Configurable;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.4.2 $ $Date: 2000-07-11 03:10:03 $
 */
public interface LinkResolver {

    /**
     * Resolve a link against a source into the target URI space.
     */
    public String resolve(String source, String partition);
}
