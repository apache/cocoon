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
package org.apache.cocoon.components.flow.apples;

import java.util.Set;

import org.apache.cocoon.environment.Request;

/**
 * AppleRequest defines the services an AppleController can collect
 * from the current request.
 */
public interface AppleRequest {

    /**
     * @return the wrapped cocoon environment Request
     */
    public Request getCocoonRequest();
    
    
    /**
     * @return Set of String's listing all available sitemap-parameters passed.
     */
    public Set getSitemapParameterNames();
    
    /**
     * Finds a named parameter in the request.
     * @param key of parameter to lookup
     * @return the parameter-value
     */
    public String getSitemapParameter(String key);

    /**
     * Finds a named parameter in the request using the overloaded method
     * {@link #getSitemapParameter(String)} but lets the returned value
     * default to the second argument in case the delegation resulted into
     * <code>null</code>
     * @param key of parameter to lookup
     * @param defaultValue return-value in case the lookup returned <code>null</code>
     * @return the parameter-value or if that was null: the defaultValue passed. 
     */    
    public String getSitemapParameter(String key, String defaultValue);    
}
