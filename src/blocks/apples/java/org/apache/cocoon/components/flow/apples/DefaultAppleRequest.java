/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
