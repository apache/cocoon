/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.caching;

import org.apache.cocoon.components.store.MRUMemoryStore;

/**
 * An implementation for the StreamCache which simply stores the
 * cached objects in the memory.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-18 15:14:17 $
 */
public final class StreamMemoryCache
extends MRUMemoryStore
implements StreamCache {

}
