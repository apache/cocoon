/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.sitemap;

import org.apache.avalon.Component;

/**
 * This interface is used by the sitemap engine to access the sitemap components
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-10-08 20:58:58 $
 */
public interface ComponentHolder extends Component {
    Component get() throws Exception;
    void put (Component component);
    String getName ();
}
