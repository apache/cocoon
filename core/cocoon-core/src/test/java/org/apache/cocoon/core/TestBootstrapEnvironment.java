/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.core;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.MutableSettings;

public class TestBootstrapEnvironment
    implements BootstrapEnvironment {
    
    private final String configuration;
    private String processorClassName;

    public TestBootstrapEnvironment(String configuration,
                                    String processorClassName) {
        this.configuration = configuration;
        this.processorClassName = processorClassName;
    }

    /**
     * @see org.apache.cocoon.core.BootstrapEnvironment#configure(org.apache.cocoon.core.MutableSettings)
     */
    public void configure(MutableSettings settings) {
        settings.setConfiguration(this.configuration);
        settings.setWorkDirectory("work");
        settings.setProcessorClassName(this.processorClassName);
    }

    public void configure(DefaultContext context) {
    }
}
