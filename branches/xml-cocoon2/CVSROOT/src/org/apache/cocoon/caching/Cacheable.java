/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.caching;

/**
 * This marker interface declares a (sitemap) component as cacheable.
 *
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-11 10:52:11 $
 */
public interface Cacheable {

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    long generateKey();

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    CacheValidity generateValidity();
}
