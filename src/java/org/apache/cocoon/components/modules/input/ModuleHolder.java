/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: ModuleHolder.java,v 1.3 2004/03/05 13:02:48 bdelacretaz Exp $
 */
public class ModuleHolder {

    public String name = null;
    public InputModule input = null;
    public Configuration config = null;

    public ModuleHolder() {
        super();
    }

    public ModuleHolder(String name, Configuration config) {
        this();
        this.name = name;
        this.config = config;
    }
    
    public ModuleHolder(String name, Configuration config, InputModule input) {
        this(name, config);
        this.input = input;
    }

}
