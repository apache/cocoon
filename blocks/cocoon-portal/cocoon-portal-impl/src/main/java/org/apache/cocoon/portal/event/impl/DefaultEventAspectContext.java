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
package org.apache.cocoon.portal.event.impl;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;
import org.apache.cocoon.portal.services.aspects.support.AspectChain;
import org.apache.cocoon.portal.services.aspects.support.BasicAspectContextImpl;

/**
 *
 * @version $Id$
 */
public final class DefaultEventAspectContext
    extends BasicAspectContextImpl
    implements EventAspectContext {

    public DefaultEventAspectContext(PortalService service, AspectChain chain) {
        super(service, chain);
    }

	/**
	 * @see org.apache.cocoon.portal.event.aspect.EventAspectContext#invokeNext()
	 */
	public void invokeNext() {
        final EventAspect aspect = (EventAspect) this.getNext();
		if (aspect != null ) {
            aspect.process(this);
		}

	}
}
