/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.store;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;


public class JCSTransientStore extends AbstractJCSStore {

    /** The location of the JCS default properties file */
    private static final String DEFAULT_PROPERTIES = "org/apache/cocoon/components/store/transient.ccf";
    
    public JCSTransientStore() {
    }

    public void parameterize(Parameters parameters) throws ParameterException {
        super.parameterize(parameters);
        int maxobjects = parameters.getParameterAsInteger("maxobjects",-1);
        if (maxobjects != -1) {
            String key = "jcs.region." + m_region + ".cacheattributes.MaxObjects";
            m_properties.setProperty(key,String.valueOf(maxobjects));
        }
    }
    
    protected String getDefaultPropertiesFile() {
        return DEFAULT_PROPERTIES;
    }
}
