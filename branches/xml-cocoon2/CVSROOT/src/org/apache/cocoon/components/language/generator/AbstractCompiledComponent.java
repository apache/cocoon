/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.generator;

import org.apache.avalon.AbstractLoggable;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.util.pool.Pool;
import org.apache.cocoon.PoolClient;

/**
 * This interface is the common base of all Compiled Components.  This
 * includes Sitemaps and XSP Pages
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-02-16 22:07:33 $
 */
public abstract class AbstractCompiledComponent extends AbstractLoggable implements PoolClient, CompiledComponent {
    private Pool pool;

    public void setPool(Pool pool) {
        if (this.pool == null) {
           this.pool = pool;
        }
    }

    public void returnToPool() {
       this.pool.put(this);
    }
}