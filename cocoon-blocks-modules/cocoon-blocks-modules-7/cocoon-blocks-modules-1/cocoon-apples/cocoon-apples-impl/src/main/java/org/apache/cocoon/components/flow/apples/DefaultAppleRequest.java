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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.environment.Request;

/**
 * DefaultAppleRequest wraps the nested &lt;map:paramater&gt; 's and the 
 * active Cocoon Environment Request to implement the service of the 
 * {@link AppleRequest} interface. 
 */
public class DefaultAppleRequest implements AppleRequest {

    private final Map params;
    private final Request cocoonRequest;

    /**
     * Constructs DefaultAppleRequest
     * @param params the nested <code>&lt;map:parameter&gt;</code>'s from the sitemap 
     * @param request the active cocoon request
     */
    public DefaultAppleRequest(List params, Request request) {
        this.params = AppleHelper.makeMapFromArguments(params);
        this.cocoonRequest = request;
    }
    

    public Request getCocoonRequest() {
        return cocoonRequest;
    }
    

    public Set getSitemapParameterNames() {
        return this.params.keySet();
    }


    public String getSitemapParameter(String key, String defaultValue) {
        String value = getSitemapParameter(key);
        if (value == null) {
            value = defaultValue;
        }        
        return value;
    }


    public String getSitemapParameter(String key) {
        return (String)this.params.get(key);
    }


}
