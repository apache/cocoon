/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.caching;

import org.apache.cocoon.components.store.Store;

/**
 * This is the StreamCache. It stores cached <code>StreamPipelines</code>.
 * The objects stored in this cache are <code>CachedStreamObjects</code>.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-17 10:33:00 $
 */
public interface StreamCache
extends Store {

}
