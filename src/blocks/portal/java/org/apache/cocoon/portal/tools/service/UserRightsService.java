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
package org.apache.cocoon.portal.tools.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.cocoon.portal.profile.PortalUser;
import org.apache.cocoon.portal.tools.helper.MultipleRoleMatcher;
import org.apache.cocoon.portal.tools.helper.RoleMatcher;
import org.apache.cocoon.portal.tools.helper.SingleRoleMatcher;
import org.apache.excalibur.source.Source;

/**
 * Service, that provides access to the user rights configuration.
 * 
 * @version CVS $Id: UserRightsService.java 156704 2005-03-09 22:57:22Z antonio $
 */
public class UserRightsService {
    
    /** 
     * The properties' location.
     */
    private Source location;

    /**
     * The properties.
     */
    private Properties properties;

    /**
     * Signals when the properties have been loaded last.
     */
    private long lastModified = -1;

    /**
     * Signals whether to reload the properties. 
     */
    private boolean reload = false;

    /**
     * Holds the userrights.
     */
    private Map userrights;

    /**
     * @return The location
     */
    
    public Source getLocation() {
        return this.location;
    }

    /**
     * @param location The location to set
     */

    public void setLocation(Source location) {
        this.location = location;
    }

    /**
     * @return The reload
     */
    public boolean getReload() {
        return this.reload;
    }

    /**
     * @param reload The reload to set
     */
    public void setReload(boolean reload) {
        this.reload = reload;
    }

    /**
     * Initialize the bean.
     */
    public void initialize() {
        boolean load;

        // Check if called for the first time
        if (this.properties == null) {
            load = true;
        } else {
            // Check if reload is required
            load = this.reload;
        }

        try {
            if (load) {
                // Check file timestamp
                long lastModified = this.location.getLastModified();
                if (this.lastModified >= lastModified) {
                    load = false;
                }

                if (load) {
                    this.lastModified = lastModified;
                    this.properties = new Properties();
                    this.properties.load(this.location.getInputStream());
                    this.parseProperties();
                }
            }
        } catch (IOException e) {
            throw new CascadingRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @return Whether the current user is allowed to call the given url.
     */
    public boolean userIsAllowed(String url, PortalUser user) {
        this.initialize();

        boolean isAllowed = true;

        // Iterate all userrights
        Iterator iterator = this.userrights.entrySet().iterator();
        Map.Entry entry;
        int[] pattern;
        RoleMatcher[] matcher;
        while (iterator.hasNext() && isAllowed) {
            entry = (Map.Entry)iterator.next();
            pattern = (int[])entry.getKey();

            // If userright matches try to find a matching role
            if (WildcardHelper.match(new HashMap(), url, pattern)) {
                matcher = (RoleMatcher[])entry.getValue();

                isAllowed = false;

                int length = matcher.length;
                for (int i = 0; i < length; i++) {
                    if (matcher[i].matches(user)) {
                        isAllowed = true;
                    }
                }
            }
        }

        return isAllowed;
    }

    public boolean userFunctionIsAllowed(String id, PortalUser user) {
        this.initialize();

        boolean isAllowed = true;

        // Iterate all userrights
        Iterator iterator = this.userrights.entrySet().iterator();
        Map.Entry entry;
        int[] pattern;
        RoleMatcher[] matcher;
        while (iterator.hasNext() && isAllowed) {
            entry = (Map.Entry)iterator.next();
            pattern = (int[])entry.getKey();

            // If userright matches try to find a matching role
            if (WildcardHelper.match(new HashMap(), id, pattern)) {
                matcher = (RoleMatcher[])entry.getValue();

                isAllowed = false;

                int length = matcher.length;
                for (int i = 0; i < length; i++) {
                    if (matcher[i].matches(user)) {
                        isAllowed = true;
                    }
                }
            }
        }

        return isAllowed;
    }

    /**
     * Parse the properties.
     */
    private void parseProperties() {
        Map userrights = new HashMap();

        Iterator iterator = this.properties.entrySet().iterator();
        Map.Entry entry;
        while (iterator.hasNext()) {
            entry = (Map.Entry)iterator.next();
            userrights.put(
                WildcardHelper.compilePattern((String)entry.getKey()),
                this.buildRoles((String)entry.getValue()));
        }

        this.userrights = userrights;
    }

    /**
     * @return A list representing the given roles.
     */
    private RoleMatcher[] buildRoles(String roles) {
        StringTokenizer tokenizer = new StringTokenizer(roles, ",", false);

        RoleMatcher[] result = new RoleMatcher[tokenizer.countTokens()];

        String token;
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            if (token.indexOf(MultipleRoleMatcher.ROLE_SEPARATOR) == -1) {
                result[i] = new SingleRoleMatcher(token);
            } else {
                result[i] = new MultipleRoleMatcher(token);
            }
            i++;
        }

        return result;
    }
}
