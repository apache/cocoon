/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.cocoon.Cocoon;

/**
 * The <code>ComposerAction</code> will allow any <code>Action</code>
 * that extends this to access SitemapComponents.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-02-12 05:56:49 $
 */
public abstract class ComposerAction extends AbstractAction implements Composer {

    /** The component manager instance */
    protected ComponentManager manager=null;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        this.manager=manager;
    }
}
