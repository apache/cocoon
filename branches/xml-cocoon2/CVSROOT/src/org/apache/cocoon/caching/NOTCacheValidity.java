/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.caching;

/**
 * A validation object which is never valid.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: NOTCacheValidity.java,v 1.1.2.1 2001-05-05 21:34:43 giacomo Exp $
 */
public final class NOTCacheValidity implements CacheValidity {

    public boolean isValid(CacheValidity validity) {
        return false;
    }

    public String toString() {
        return "NOTCacheValidity";
    }

}
