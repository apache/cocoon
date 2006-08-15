/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;
import org.apache.cocoon.portal.om.CopletInstance;

/**
 * Base class for renderer aspects.
 *
 * @version $Id$
 */
public abstract class AbstractAspect
    extends AbstractComponent
    implements RendererAspect {

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(java.util.Properties)
     */
    public Object prepareConfiguration(Properties configuration) 
    throws PortalException { 
        return configuration;
    }

    /**
     * Return the coplet instance with the given id.
     * @return The coplet instance or null.
     */
    protected CopletInstance getCopletInstance(String id) {
        return this.portalService.getProfileManager().getCopletInstance(id);
    }
}
