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
package org.apache.cocoon.components.flow;

import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import java.util.Map;

/**
 * Provides the interface between the flow controller layer and the 
 * view layer. A view can obtain the context object sent by a flow
 * script and the current web continuation, if any.
 */
public class FlowHelper {

    // Constants defining keys in the object model used to store the various objects.
    // These constants are private so that access to these objects only go through the
    // accessors provided below.
    //
    // These objects are stored in the object model rather than as request attributes,
    // as object model is cloned for subrequests (see EnvironmentWrapper), whereas
    // request attributes are shared between the "real" request and all of its
    // child requests.

    /**
     * Request attribute name used to store flow context.
     */
    private static final String CONTEXT_OBJECT = "cocoon.flow.context";

    /**
     * Request attribute name used to store flow continuation.
     */
    private static final String CONTINUATION_OBJECT = "cocoon.flow.continuation";

    /**
     * Get the flow context object associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @return The context object 
     */
    public final static Object getContextObject(Map objectModel) {
        return objectModel.get(CONTEXT_OBJECT);
    }

    /**
     * Get the web continuation associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @return The web continuation
     */
    public final static WebContinuation getWebContinuation(Map objectModel) {
        return (WebContinuation)objectModel.get(CONTINUATION_OBJECT);
    }

    /**
     * Set the web continuation associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @param kont The web continuation
     */
    public final static void setWebContinuation(Map objectModel,
                                          WebContinuation kont) {
        objectModel.put(CONTINUATION_OBJECT, kont);
    }

    /**
     * Set the flow context object associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @param obj The context object 
     */
    public final static void setContextObject(Map objectModel, Object obj) {
        objectModel.put(CONTEXT_OBJECT, obj);
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
