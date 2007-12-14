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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * BaseLinkModule returns a relative link (<code>../</code>,
 * <code>../../</code> etc) to the base of the current request or sitemap URI.  For
 * instance, if called within a &lt;map:match pattern="a/b/c.xsp"> pipeline,
 * <code>{baselink:SitemapBaseLink}</code> would evaluate to <code>../../</code>.
 *
 * <p>Based on {@link RequestURIModule}
 *
 * @version $Id$
 */
public class BaseLinkModule extends AbstractInputModule
                            implements ThreadSafe {

    private static final List NAMES = new ArrayList(2);
    static {
        NAMES.add("RequestBaseLink");
        NAMES.add("SitemapBaseLink");
    }

    
    public Object getAttribute(final String name,
                               final Configuration modeConf,
                               final Map objectModel)
    throws ConfigurationException {

        String uri;
        if (name.equals("SitemapBaseLink"))
            uri = ObjectModelHelper.getRequest(objectModel).getSitemapURI();
        else if (name.equals("RequestBaseLink"))
            uri = ObjectModelHelper.getRequest(objectModel).getRequestURI();
        else
            uri = "";

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        StringBuffer result = new StringBuffer(uri.length());

        int nextIndex = 0;
        while ((nextIndex = uri.indexOf('/', nextIndex) + 1) > 0) {
            result.append("../");
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Returns " + result + " for uri " + uri + " and attribute " + name);
        }

        return result.toString();
    }

    public Iterator getAttributeNames(final Configuration modeConf, final Map objectModel)
    throws ConfigurationException {

        return NAMES.iterator();
    }

    public Object[] getAttributeValues(final String name,
                                       final Configuration modeConf,
                                       final Map objectModel)
    throws ConfigurationException {

        Object result = getAttribute(name, modeConf, objectModel);
        return result == null ? null : new Object[]{result};
    }
}
