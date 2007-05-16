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
package org.apache.cocoon.portal.services.aspects.impl.support;

import java.util.Collection;

import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspect;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext;
import org.apache.cocoon.portal.services.aspects.support.AspectChainImpl;
import org.apache.cocoon.portal.services.aspects.support.BasicAspectContextImpl;

/**
 * The aspect context is passed to every aspect.
 * @since 2.2
 *
 * @version $Id$
 */
public final class ProfileManagerAspectContextImpl
    extends BasicAspectContextImpl
    implements ProfileManagerAspectContext {

    public static final int PHASE_COPLET_TYPES = 0;
    public static final int PHASE_COPLET_DEFINITIONS = 1;
    public static final int PHASE_COPLET_INSTANCES =2;
    public static final int PHASE_COPLET_LAYOUT = 3;

    protected final int phase;
    protected Object result;

    public ProfileManagerAspectContextImpl(PortalService service,
                                           AspectChainImpl   chain,
                                           int           phase) {
        super(service, chain);
        this.phase = phase;
    }

	/**
	 * @see org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext#invokeNext(java.lang.Object)
	 */
	public void invokeNext(Object object) {
        // type check first
        if ( object == null ) {
            throw new PortalRuntimeException("Profile information can't be null (phase = " + this.phase + ").");
        }
        if ( phase == PHASE_COPLET_LAYOUT ) {
            if ( !(object instanceof Layout) ) {
                throw new PortalRuntimeException("Profile information must be of type Layout (phase = " + this.phase + ").");
            }
        } else {
            if ( !(object instanceof Collection) ) {
                throw new PortalRuntimeException("Profile information must be of type Collection (phase = " + this.phase + ").");
            }
        }
        final ProfileManagerAspect aspect = (ProfileManagerAspect)this.getNext();
        if ( aspect != null ) {
            switch (phase) {
                case PHASE_COPLET_TYPES : aspect.prepareCopletTypes(this, (Collection)object);
                                          break;
                case PHASE_COPLET_DEFINITIONS : aspect.prepareCopletDefinitions(this, (Collection)object);
                                                break;
                case PHASE_COPLET_INSTANCES : aspect.prepareCopletInstances(this, (Collection)object);
                                              break;
                case PHASE_COPLET_LAYOUT : aspect.prepareLayout(this, (Layout)object);
                                           break;
            }
        } else {
            this.result = object;
        }
    }

    /**
     * Return the resulting profile depending on the phase.
     * @return The resulting profile.
     */
    public Object getResult() {
        return this.result;
    }
}
