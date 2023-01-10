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
package org.apache.cocoon.components.flow;

import java.util.Map;

import org.apache.cocoon.el.objectmodel.ObjectModel;

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
     * @param newObjectModel TODO
     * @param kont The web continuation
     */
    public final static void setWebContinuation(Map objectModel,
                                          ObjectModel newObjectModel, WebContinuation kont) {
        objectModel.put(CONTINUATION_OBJECT, kont);
        newObjectModel.putAt("cocoon/continuation", kont);
    }

    /**
     * Set the flow context object associated with the current request
     *
     * @param objectModel The Cocoon Environment's object model
     * @param newObjectModel TODO
     * @param obj The context object 
     */
    public final static void setContextObject(Map objectModel, ObjectModel newObjectModel, Object obj) {
        objectModel.put(CONTEXT_OBJECT, obj);
        newObjectModel.put(ObjectModel.CONTEXTBEAN, obj);
        newObjectModel.fillContext();
    }
}
