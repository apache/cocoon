/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import org.apache.avalon.util.pool.ObjectFactory;
import org.apache.avalon.Poolable;

import org.apache.avalon.Configuration;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Configurable;
import org.apache.avalon.Composer;

/** Factory for Cocoon components.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-10-19 14:42:36 $
 */
public class ComponentFactory implements ObjectFactory {
	/** The class which this <code>ComponentFactory</code>
	 * should create.
	 */
	private Class componentClass;

	/** The configuration for this component.
	 */
	private Configuration conf;

	/** The component manager for this component.
	 */
	private ComponentManager manager;

	/** Construct a new component factory for the specified component.
	 * @param componentClass the class to instantiate (must have a default constructor).
	 * @param config the <code>Configuration</code> object to pass to new instances.
	 * @param manager the component manager to pass to <code>Composer</code>s.
	 */
	public ComponentFactory(Class componentClass, Configuration config, ComponentManager manager) {
		this.componentClass = componentClass;
		this.conf = config;
		this.manager = manager;
	}

	public Poolable newInstance() throws Exception {
		Poolable comp = (Poolable)componentClass.newInstance();
		
		if ( comp instanceof Configurable ) {
			((Configurable)comp).configure(this.conf);
		}

		if ( comp instanceof Composer) {
			((Composer)comp).compose(this.manager);
		}

		return comp;
	}
	
	public Class getCreatedClass() {
		return componentClass;
	}
}
