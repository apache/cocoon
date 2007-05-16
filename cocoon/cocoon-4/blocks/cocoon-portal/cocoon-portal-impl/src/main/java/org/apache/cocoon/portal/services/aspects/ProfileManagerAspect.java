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

import java.util.Collection;

import org.apache.cocoon.portal.om.Layout;

/**
 * The behaviour of the used profile manager can be extended by assigning one
 * or more profile manager aspects to the profile manager.
 *
 * @since 2.2
 * @version $Id$
 */
public interface ProfileManagerAspect {

    /**
     * Prepare the coplet types directly after loading.
     * @param context
     * @param copletTypes
     */
    void prepareCopletTypes(ProfileManagerAspectContext context,
                            Collection copletTypes);
                 
    /**
     * Prepare the coplet definitions directly after loading.
     * @param context
     * @param copletDefinitions
     */
    void prepareCopletDefinitions(ProfileManagerAspectContext context,
                                  Collection copletDefinitions);

    /**
     * Prepare the coplet instances directly after loading.
     * @param context
     * @param copletInstances
     */
    void prepareCopletInstances(ProfileManagerAspectContext context,
                                Collection copletInstances);

    /**
     * Prepare the layout directly after loading.
     * @param context
     * @param layout
     */
    void prepareLayout(ProfileManagerAspectContext context,
                       Layout layout);
}
