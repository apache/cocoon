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
package org.apache.cocoon.environment.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import java.util.Map;

/**
 * A set of constants and methods to access the JSR-168 (Portlet)
 * specific objects from the object model.
 *
 * The object model is a <code>Map</code> used to pass information about the
 * calling environment to the sitemap and its components (matchers, actions,
 * transformers, etc).
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @version CVS $Id: PortletObjectModelHelper.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
 */
public final class PortletObjectModelHelper {

    /** Key for the environment {@link javax.portlet.RenderRequest} in the object model. */
    public final static String RENDER_REQUEST_OBJECT = "render-request";

    /** Key for the environment {@link javax.portlet.ActionRequest} in the object model. */
    public final static String ACTION_REQUEST_OBJECT = "action-request";

    /** Key for the environment {@link javax.portlet.RenderResponse} in the object model. */
    public final static String RENDER_RESPONSE_OBJECT = "render-response";

    /** Key for the environment {@link javax.portlet.ActionResponse} in the object model. */
    public final static String ACTION_RESPONSE_OBJECT = "action-response";

    /** Key for the environment {@link javax.portlet.PortletRequest} in the object model. */
    public static final String PORTLET_REQUEST_OBJECT = "portlet-request";

    /** Key for the environment {@link javax.portlet.PortletResponse} in the object model. */
    public static final String PORTLET_RESPONSE_OBJECT = "portlet-response";

    /** Key for the environment {@link javax.portlet.PortletContext} in the object model. */
    public static final String PORTLET_CONTEXT_OBJECT = "portlet-context";

    private PortletObjectModelHelper() {
        // Forbid instantiation
    }

    public static final RenderRequest getRenderRequest(Map objectModel) {
        return (RenderRequest) objectModel.get(RENDER_REQUEST_OBJECT);
    }

    public static final RenderResponse getRenderResponse(Map objectModel) {
        return (RenderResponse) objectModel.get(RENDER_RESPONSE_OBJECT);
    }

    public static final ActionRequest getActionRequest(Map objectModel) {
        return (ActionRequest) objectModel.get(ACTION_REQUEST_OBJECT);
    }

    public static final ActionResponse getActionResponse(Map objectModel) {
        return (ActionResponse) objectModel.get(ACTION_RESPONSE_OBJECT);
    }

    public static final PortletRequest getPortletRequest(Map objectModel) {
        return (PortletRequest) objectModel.get(PORTLET_REQUEST_OBJECT);
    }

    public static final PortletResponse getPortletResponse(Map objectModel) {
        return (PortletResponse) objectModel.get(PORTLET_RESPONSE_OBJECT);
    }

    public static final PortletContext getPortletContext(Map objectModel) {
        return (PortletContext) objectModel.get(PORTLET_CONTEXT_OBJECT);
    }

    public static final void setPortletRequest(Map objectModel, PortletRequest object) {
        if (objectModel.get(PORTLET_REQUEST_OBJECT) != null) {
            throw new IllegalStateException("PortletRequest has been set already");
        }
        objectModel.put(PORTLET_REQUEST_OBJECT, object);
        if (object instanceof ActionRequest) {
            objectModel.put(ACTION_REQUEST_OBJECT, object);
        }
        if (object instanceof RenderRequest) {
            objectModel.put(RENDER_REQUEST_OBJECT, object);
        }
    }

    public static final void setPortletResponse(Map objectModel, PortletResponse object) {
        if (objectModel.get(PORTLET_RESPONSE_OBJECT) != null) {
            throw new IllegalStateException("PortletResponse has been set already");
        }
        objectModel.put(PORTLET_RESPONSE_OBJECT, object);
        if (object instanceof ActionResponse) {
            objectModel.put(ACTION_RESPONSE_OBJECT, object);
        }
        if (object instanceof RenderResponse) {
            objectModel.put(RENDER_RESPONSE_OBJECT, object);
        }
    }

    public static final void setPortletContext(Map objectModel, PortletContext object) {
        if (objectModel.get(PORTLET_CONTEXT_OBJECT) != null) {
            throw new IllegalStateException("PortletContext has been set already");
        }
        objectModel.put(PORTLET_CONTEXT_OBJECT, object);
    }
}
