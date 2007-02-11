/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.AbstractInputModule;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.http.HttpCookie;
import org.apache.regexp.RE;

/**
 * Input module for cookies. Retrieves the value of the requested cookie.
 * 
 * @author Jon Evans <jon.evans@misgl.com>
 * @version CVS $Id:$
 */
public class CookieModule extends AbstractInputModule implements ThreadSafe {
    
    /**
     * @return the value of the cookie whose name matches the one requested,
     * or <code>null</code> if there is no match.
     */
    public Object getAttribute(String name, Configuration modeConf,
            Map objectModel) throws ConfigurationException {
        
        HttpCookie cookie = (HttpCookie) getCookieMap(objectModel).get(name);
        String value = (cookie == null ? null : cookie.getValue());
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Cookie[" + name + "]=" + value);
        }
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration,
     *      java.util.Map)
     */
    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
            throws ConfigurationException {
        
        return getCookieMap(objectModel).keySet().iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String,
     *      org.apache.avalon.framework.configuration.Configuration,
     *      java.util.Map)
     */
    public Object[] getAttributeValues(String name, Configuration modeConf,
            Map objectModel) throws ConfigurationException {
        
        Map allCookies = getCookieMap(objectModel);
        
        Iterator it = allCookies.values().iterator();
        List matched = new LinkedList();
        RE regexp = new RE(name);
        while (it.hasNext()) {
            HttpCookie cookie = (HttpCookie) it.next();
            if (regexp.match(cookie.getName())) {
                matched.add(cookie.getValue());
            }
        }
        return matched.toArray();
    }

    /**
     * @param objectModel
     *            Object Model for the current request
     * @return a Map of {see: HttpCookie}s for the current request, keyed on
     *         cookie name.
     */
    protected Map getCookieMap(Map objectModel) {
        return ObjectModelHelper.getRequest(objectModel).getCookieMap();
    }
}
