/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch;

/**
 * This interface specifies how the mapping between <code>Component</code>
 * roles and their actual implementations is held.
 * <br>
 * It's beyond the scope of this interface to define how the mapping is created
 * and managed, but only to provide a way for composers to access the
 * components they require.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:18:58 $
 */
public interface ComponentManager {

    /**
     * Get the <code>Component</code> associated with the given role.
     *
     * @param name The role of the <code>Component</code> to retrieve.
     * @exception ComponentNotFoundException If the given role is not associated
     *                                       with a <code>Component</code>.
     * @exception ComponentNotAccessibleException If a <code>Component</code>
     *                                            instance cannot be created.
     */
    public Component getComponent(String role)
    throws ComponentNotFoundException, ComponentNotAccessibleException;
}
