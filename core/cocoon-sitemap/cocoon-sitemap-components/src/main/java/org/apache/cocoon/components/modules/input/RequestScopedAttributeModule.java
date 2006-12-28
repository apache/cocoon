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

package org.apache.cocoon.components.modules.input;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.environment.Request;

/**
 * This is an extension of the {@link RequestAttributeModule}. It has the same
 * features but requires to define the scope of an attribute: either "global" or
 * "request"; so the name follows this form: SCOPE:KEY.
 * 
 * @since 2.2
 * @version $Id$
 */
public class RequestScopedAttributeModule extends RequestAttributeModule {

    private static final class KeyInfo {
        public final int scope;
        public final String key;
        
        public KeyInfo(String name) throws ConfigurationException {
            final int pos = name.indexOf(':');
            if ( pos == -1 ) {
                throw new ConfigurationException("Scope is missing in '" + name + '.');
            }
            final String scopeValue = name.substring(0, pos);
            this.key = name.substring(pos + 1);
            if ( "global".equalsIgnoreCase(scopeValue) ) {
                this.scope = Request.GLOBAL_SCOPE;
            } else if ("request".equalsIgnoreCase(scopeValue)) {
                this.scope = Request.REQUEST_SCOPE;
            } else {
                throw new ConfigurationException("Unknown value for scope: " + scopeValue);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute( String name, Configuration modeConf, Map objectModel )
    throws ConfigurationException {
        final KeyInfo info = new KeyInfo(name);
        return this.getAttribute(info.key, modeConf, objectModel, info.scope);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
    throws ConfigurationException {
        final KeyInfo info = new KeyInfo(name);
        return this.getAttributeValues(info.key, modeConf, objectModel, info.scope );
    }


}
