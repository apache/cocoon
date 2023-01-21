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
package org.apache.cocoon.portal.services.aspects;

import java.util.Properties;

import org.apache.cocoon.portal.PortalService;

/**
 * The base interface for all aspect contexts.
 * This interface provides basic access to some environment information like
 * the current {@link PortalService} and the configuration for the aspect.
 *
 * @since 2.2
 * @version $Id$
 */
public interface BasicAspectContext {

    /**
     * Get the {@link Properties} of the aspect.
     * @return The aspect properties.
     */
    Properties getAspectProperties();

    /**
     * Get the portal service.
     * @return The portal service.
     */
    PortalService getPortalService();
}
