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
package org.apache.cocoon.portal.event.aspect;

import java.util.Properties;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.EventConverter;

/**
 *
 * @version $Id$
 */
public interface EventAspectContext {

    /**
     * Invoke next aspect 
     */
    void invokeNext(PortalService service);

    /** 
     * Get the {@link Properties} of the aspect.
     */
    Properties getAspectProperties();

    /**
     * Get the encoder
     */
    EventConverter getEventConverter();
}
