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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: PortletObjectModelHelper.java,v 1.2 2003/12/03 13:20:29 vgritsenko Exp $
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
