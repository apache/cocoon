/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;


/**
 * This is a bean factory post processor which sets up a child settings object.
 *
 * The settings object is created by reading several property files and merging of
 * the values. If there is more than one definition for a property, the last one wins.
 * The property files are read in the following order:
 * 1) Property provider (if configured in the bean factory)
 * 2) Add properties from configured directories {@link #directories}.
 * 3) Add additional properties configured at {@link #additionalProperties}
 * 4) System properties
 *
 * @since 1.0
 * @version $Id$
 */
public class ChildSettingsBeanFactoryPostProcessor
    extends AbstractSettingsBeanFactoryPostProcessor {

    /** Unique name for this child settings context. */
    protected String name;

    /**
     * Set the unique name for this settings context.
     * @param newName The new name.
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsBeanFactoryPostProcessor#getRunningMode()
     */
    protected String getRunningMode() {
        return this.getParentSettings().getRunningMode();
    }

    /**
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsBeanFactoryPostProcessor#getNameForPropertyProvider()
     */
    protected String getNameForPropertyProvider() {
        return this.name;
    }
}
