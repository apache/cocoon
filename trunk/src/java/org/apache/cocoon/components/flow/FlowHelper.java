/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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
package org.apache.cocoon.components.flow;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import java.util.Map;

/**
 * Provides the interface between the flow controller layer and the 
 * view layer. A view can obtain the context object sent by a flow
 * script and the current web continuation, if any.
 */
public class FlowHelper {

    /**
     * Request attribute name used to store flow context.
     */
    public static final String CONTEXT_OBJECT = "cocoon.flow.context";

    /**
     * Request attribute name used to store flow continuation.
     */
    public static final String CONTINUATION_OBJECT = "cocoon.flow.continuation";

    /**
     * Get the flow context object associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @return The context object 
     */
    public final static Object getContextObject(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return request.getAttribute(CONTEXT_OBJECT);
    }

    /**
     * Get the web continuation associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @return The web continuation
     */
    public final static WebContinuation getWebContinuation(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (WebContinuation)request.getAttribute(CONTINUATION_OBJECT);
    }

    /**
     * Set the web continuation associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @param kont The web continuation
     */
    public final static void setWebContinuation(Map objectModel,
                                          WebContinuation kont) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(CONTINUATION_OBJECT, kont);
    }

    /**
     * Set the flow context object associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @param obj The context object 
     */
    public final static void setContextObject(Map objectModel, Object obj) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(CONTEXT_OBJECT, obj);
    }
    
    /**
     * Unwrap a Rhino object (getting the raw java object) and convert undefined to null
     */
    public static Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }
}
