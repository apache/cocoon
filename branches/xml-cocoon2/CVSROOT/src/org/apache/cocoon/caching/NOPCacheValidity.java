/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.caching;

/**
 * A validation object which is always valid.
 * This might be the most used CacheValidity object for serializers.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-17 10:32:56 $
 */
public final class NOPCacheValidity
implements CacheValidity {

    public boolean isValid(CacheValidity validity) {
        return validity instanceof NOPCacheValidity;
    }

    public String toString() {
        return "NOPCacheValidity";
    }

}
