/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components;

import java.util.Iterator;

/** RoleInfo Interface
 * @author <a href="mailto:ricardo@apache,org">Ricardo Rocha</a>
 * @author <a href="mailto:giacomo@apache,org">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-05 15:40:36 $
 */
public interface RoleInfo {
    public String lookup(String shorthandName);
    public Iterator shorthandNames();
    public String defaultClass(String role);
}