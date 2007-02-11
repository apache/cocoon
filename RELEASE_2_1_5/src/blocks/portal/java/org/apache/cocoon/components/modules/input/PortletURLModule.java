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
package org.apache.cocoon.components.modules.input;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.portlet.PortletEnvironment;
import org.apache.cocoon.environment.portlet.PortletObjectModelHelper;
import org.apache.cocoon.util.NetUtils;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Input module to be used in together with
 * {@link org.apache.cocoon.transformation.LinkRewriterTransformer}
 * in JSR-168 (Portlet) environment. Allows creation of render, action, and
 * resource URLs using syntax:
 * <ul>
 *   <li><code>portlet:action:&lt;path&gt;</code> for action URL;
 *   <li><code>portlet:render:&lt;path&gt;</code> for render URL;
 *   <li><code>portlet:resource:&lt;path&gt;</code> for resource URL;
 * </ul>
 *
 * Outside of portlet environment, prefixes (<code>portlet:action:</code>,
 * <code>portlet:render:</code>, <code>portlet:resource:</code>) are omitted.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletURLModule.java,v 1.2 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class PortletURLModule extends AbstractInputModule implements ThreadSafe {

    public static final String NAME_RENDER = "render";
    public static final String NAME_RESOURCE = "resource";
    public static final String NAME_ACTION = "action";

    private static final String PREFIX_RENDER = NAME_RENDER + ":";
    private static final String PREFIX_RESOURCE = NAME_RESOURCE + ":";
    private static final String PREFIX_ACTION = NAME_ACTION + ":";

    private static final List returnNames;

    static {
        List tmp = new ArrayList();
        tmp.add(NAME_RENDER);
        tmp.add(NAME_RESOURCE);
        tmp.add(NAME_ACTION);
        returnNames = tmp;
    }

    public Iterator getAttributeNames(Configuration modeConf, Map objectModel) throws ConfigurationException {
        return PortletURLModule.returnNames.iterator();
    }

    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
        if (name == null) {
            throw new NullPointerException("Attribute name is null");
        }

        Request request = ObjectModelHelper.getRequest(objectModel);

        RenderResponse renderResponse = PortletObjectModelHelper.getRenderResponse(objectModel);
        if (renderResponse != null) {
            PortletURL url = null;
            if (name.startsWith(PREFIX_RENDER)) {
                url = renderResponse.createRenderURL();
                name = name.substring(PREFIX_RENDER.length());
                if (name.length() > 0 && name.charAt(0) == '/') {
                    name = name.substring(1);
                }
            } else if (name.startsWith(PREFIX_RESOURCE)) {
                name = name.substring(PREFIX_RESOURCE.length());
                if (name.length() == 0 || name.charAt(0) != '/') {
                    String uri = request.getContextPath() + "/" + request.getServletPath();
                    name = NetUtils.absolutize(uri, name);
                }
                return renderResponse.encodeURL(name);
            } else if (name.startsWith(PREFIX_ACTION)) {
                url = renderResponse.createActionURL();
                name = name.substring(PREFIX_ACTION.length());
                if (name.length() > 0 && name.charAt(0) == '/') {
                    name = name.substring(1);
                }
            } else {
                throw new IllegalArgumentException("Invalid attribute name '" + name + "' for '" + getClass().getName() + "'");
            }

            Map parameters = new HashMap(7);
            name = NetUtils.deparameterize(name, parameters);
            if (name.length() > 0) {
                parameters.put(PortletEnvironment.PARAMETER_PATH_INFO, name);
            }
            for (Iterator i = parameters.keySet().iterator(); i.hasNext();) {
                String param = (String) i.next();
                Object values = parameters.get(param);
                if (values instanceof String) {
                    url.setParameter(param, (String) values);
                } else {
                    url.setParameter(param, (String[]) values);
                }
            }

            return url.toString();
        } else {
            if (name.startsWith(PREFIX_RENDER)) {
                return name.substring(PREFIX_RENDER.length());
            } else if (name.startsWith(PREFIX_RESOURCE)) {
                return name.substring(PREFIX_RESOURCE.length());
            } else if (name.startsWith(PREFIX_ACTION)) {
                return name.substring(PREFIX_ACTION.length());
            } else {
                throw new IllegalArgumentException("Invalid attribute name '" + name + "' for '" + getClass().getName() + "'");
            }
        }
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
        Object result = getAttribute(name, modeConf, objectModel);
        if (result != null) {
            return new Object[]{result};
        }
        return null;
    }
}
