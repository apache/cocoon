/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.named;

import java.util.Enumeration;

import org.apache.arch.Component;
import org.apache.arch.ComponentNotFoundException;

/**
 * This interface defines the manager responsibilities for a collection
 * of named components of a given type.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:52 $
 */
public interface NamedComponentManager extends Component {
  /**
   * Return a named component instance for a given component type.
   *
   * @param type The component type
   * @param name The component name
   * @return The named component within the given type
   * @exception ComponentNotFoundException If the given type or name do not
   * exist
   */
  public NamedComponent getComponent(String type, String name)
    throws ComponentNotFoundException;

  /**
   * Return an enumeration of all known component types.
   *
   * @param type The component type
   * @return The list of known component types
   */
  public Enumeration getTypes();

  /**
   * Return an enumeration of all named instances in a component
   * collection of a given type
   *
   * @param type The component type
   * @exception ComponentNotFoundException If the given type does not exist
   * @return The list of components within a given type
   */
  public Enumeration getComponents(String type)
    throws ComponentNotFoundException;
}
