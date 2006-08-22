/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.services.aspects;

import java.util.Properties;

import org.apache.cocoon.portal.PortalService;

/**
 * The base interface for all aspect contexts.
 *
 * @since 2.2
 * @version $Id$
 */
public interface BasicAspectContext {

    /** 
     * Get the {@link Properties} of the aspect.
     */
    Properties getAspectProperties();

    /**
     * Get the portal service.
     */
    PortalService getPortalService();
}
