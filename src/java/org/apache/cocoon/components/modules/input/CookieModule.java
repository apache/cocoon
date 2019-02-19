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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.http.HttpCookie;

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

        HttpCookie cookie = getCookieMap(objectModel).get(name);
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
    public Iterator<String> getAttributeNames(Configuration modeConf, Map objectModel)
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

        Pattern pattern = Pattern.compile(name);
        List<String> matched = new LinkedList<String>();
        Map<String, HttpCookie> allCookies = getCookieMap(objectModel);

        for (HttpCookie cookie : allCookies.values()) {
            Matcher matcher = pattern.matcher(cookie.getName());
            if (matcher.matches()) {
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
    protected Map<String, HttpCookie> getCookieMap(Map objectModel) {
        @SuppressWarnings("unchecked")
        Map<String, HttpCookie> result = (Map<String, HttpCookie>) ObjectModelHelper.getRequest(objectModel).getCookieMap();
        return result;
    }
}
