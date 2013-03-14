/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;

/**
 * This object is set to a {@link ParentAware} component and allows
 * access to the parent component.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public class ComponentLocatorImpl 
    implements ComponentLocator {

    protected ComponentManager manager;
    protected String           role;
    
    public ComponentLocatorImpl(ComponentManager manager, String role) {
        this.manager = manager;
        this.role = role;
    }
    
    public Object lookup()
    throws ComponentException {
        return this.manager.lookup( this.role );
    }
    
    public void release(Object parent) {
        this.manager.release( (Component) parent);
    }
}
