/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.aspect;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.Publisher;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: EventAspectContext.java,v 1.2 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public interface EventAspectContext {
    
    /**
     * Invoke next aspect 
     */
    void invokeNext(PortalService service);

    /** 
     * Get the {@link Parameters} of the aspect.
     */
    Parameters getAspectParameters();
    
    /**
     * Get the encoder
     */
    EventConverter getEventConverter();
    
    /**
     * Get the publisher
     */
    Publisher getEventPublisher();
    
    /**
     * Get the object model
     */
    Map getObjectModel();
}
