/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch;

/**
 * A <code>Composer</code> is a class that need to connect to software
 * components using a &quot;role&quot; abstraction, thus not depending on
 * particular implementations.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:18:58 $
 */
public interface Composer {
    
    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     * <br>
     * The <code>Composer</code> implementation should use this method to
     * acquire the instance of the <code>ComponentManager</code> nedeed for
     * retrieving those <code>Component</code> objects it needs for execution.
     *
     * @param manager The <code>ComponentManager</code> to which this
     *                <code>Composer</code> can request the needed 
     *                <code>Component</code> instances.
     */
    public void setComponentManager(ComponentManager manager);
    
}