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
package org.apache.cocoon.portal.services;

import java.util.Collection;

import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.om.LayoutType;

/**
 * This factory is for creating and managing layout objects.
 *
 * @version $Id$
 */
public interface LayoutFactory  {

    /**
     * Return all available types.
     * @return A non-null collection of {@link LayoutType}s.
     */
    Collection getLayoutTypes();

    /**
     * Create a new layout instance.
     * The instance is also registered at the profile manager.
     */
    Layout newInstance(String type)
    throws LayoutException;

    /**
     * Create a new layout instance.
     * The instance is also registered at the profile manager.
     */
    Layout newInstance(String type, String id)
    throws LayoutException;

    /**
     * Create a new layout instance for the layout object.
     * The instance is also registered at the profile manager.
     * @param layout
     * @return A new layout instance.
     */
    LayoutInstance newInstance(Layout layout);

    /**
     * Remove the layout instance.
     * The instance (and all childs) will also be unregistered from
     * the profile manager.
     */
    void remove(Layout layout);
}
