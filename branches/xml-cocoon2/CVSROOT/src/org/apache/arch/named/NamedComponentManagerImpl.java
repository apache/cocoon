/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.named;

import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.arch.Composer;
import org.apache.arch.Component;
import org.apache.arch.ComponentManager;
import org.apache.arch.ComponentNotFoundException;

import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.arch.config.ConfigurationException;

/**
 * Default implementation for <code>NamedComponentManage</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:51 $
 */
public class NamedComponentManagerImpl
  implements NamedComponentManager, Composer, Configurable
{
  /**
   * The global component manager
   */
  protected ComponentManager manager;

  /**
   * A hashtable holding all known component types
   */
  protected Hashtable types = new Hashtable();

  /**
   * Load all component types and their named instances. This information is
   * supplied through the sitemap configuration file.
   *
   * @param conf The configuration information
   * @exception ConfigurationException If an error occurs during processing
   */
  public void setConfiguration(Configuration conf)
    throws ConfigurationException
  {
    Enumeration t = conf.getConfigurations("component-type");
    while (t.hasMoreElements()) {
      Configuration tc = (Configuration) t.nextElement();
      String typeName = tc.getAttribute("name");
      Hashtable components = new Hashtable();
      this.types.put(typeName, components);

      Enumeration c = tc.getConfigurations("component-instance");
      while (c.hasMoreElements()) {
        Configuration cc = (Configuration) c.nextElement();
        String componentName = cc.getAttribute("name");
        String componentClass = cc.getAttribute("class");
        NamedComponent component = null;

	try {
	  component =
            (NamedComponent)
            this.getClass().getClassLoader().
            loadClass(componentClass).newInstance();
	} catch (Exception e) {
	  throw new ConfigurationException(
	    "Error instantiating component" + componentClass, conf
	  );
	}

        components.put(componentName, component);
        if (component instanceof Configurable) {
          ((Configurable) component).setConfiguration(cc);
        }
      }
    }
  }

  /**
   * Set the global component manager.
   *
   * @param manager The global <code>ComponentManager</code>
   */
  public void setComponentManager(ComponentManager manager) {
    this.manager = manager;
    Enumeration t = this.types.elements();
    while (t.hasMoreElements()) {
      Hashtable components = (Hashtable) t.nextElement();
      Enumeration c = components.elements();
      while (c.hasMoreElements()) {
        NamedComponent component = (NamedComponent) c.nextElement();
        if (component instanceof Composer) {
          ((Composer) component).setComponentManager(this.manager);
        }
      }
    }
  }

  /**
   * Retrieve a named component for a given component type.
   *
   * @param type The component type
   * @param name The component name
   * @return The named component within the given type
   * @exception ComponentNotFoundException If the given type or component do
   * not exist
   */
  public NamedComponent getComponent(String type, String name)
    throws ComponentNotFoundException
  {
    Hashtable components = (Hashtable) this.types.get(type);
    if (components == null) {
      throw new ComponentNotFoundException("No such type: " + type);
    }

    NamedComponent component = (NamedComponent) components.get(name);
    if (component == null) {
      throw new ComponentNotFoundException("No such component: " + name);
    }

    return component;
  }

  /**
   * Return an enumeration of all known component types.
   *
   * @param type The component type
   * @return The list of known component types
   */
  public Enumeration getTypes() {
    return this.types.elements();
  }

  /**
   * Return an enumeration of all named instances in a component
   * collection of a given type
   *
   * @param type The component type
   * @exception ComponentNotFoundException If the given type does not exist
   * @return The list of components within a given type
   */
  public Enumeration getComponents(String type)
    throws ComponentNotFoundException
  {
    Hashtable components = (Hashtable) this.types.get(type);
    if (components == null) {
      throw new ComponentNotFoundException("No such type: " + type);
    }

    return components.elements();
  }
}
