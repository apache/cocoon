/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.filters;

import org.apache.cocoon.framework.Configurable;
import org.apache.cocoon.framework.ConfigurationException;

/**
 * The <code>FilterFactory</code> interface must be implemented by all those
 * factories creating instances of <code>Filter</code> objects.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-02-09 01:32:58 $
 * @since Cocoon 2.0
 */
public interface FilterFactory extends Configurable {
    /**
     * Return a non configured instance of a <code>Filter</code>.
     */
    public Filter getFilter()
    throws ConfigurationException;
}
