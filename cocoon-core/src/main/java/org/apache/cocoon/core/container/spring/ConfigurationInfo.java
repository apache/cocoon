/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.util.Map;

/**
 * This bean stores information about a complete Avalon style configuration.
 * It can be passed to an {@link XmlConfigCreator} to create a Spring like
 * configuration.
 *
 * @since 2.2
 * @version $Id$
 */
public class ConfigurationInfo {

    protected Map components;
    
    protected String rootLogger;

    public Map getComponents() {
        return components;
    }

    public void setComponents(Map components) {
        this.components = components;
    }

    public String getRootLogger() {
        return rootLogger;
    }

    public void setRootLogger(String rootLogger) {
        this.rootLogger = rootLogger;
    }


}
