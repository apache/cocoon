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
package org.apache.cocoon.environment;

import java.util.Map;

/**
 * A set of constants and methods to access the content of the object model.
 *
 * <p>
 * The object model is a <code>Map</code> used to pass information about the
 * calling environment to the sitemap and its components (matchers, actions,
 * transformers, etc).
 *
 * <p>
 * This class provides accessors only for the objects in the object model that are
 * common to every environment and which can thus be used safely. Some environments
 * provide additional objects, but they are not described here and accessing them
 * should be done in due cause since this ties the application to that particular
 * environment.
 *
 * @version $Id$
 */
public final class ObjectModelHelper {

    /** Key for the environment {@link Request} in the object model. */
    public final static String REQUEST_OBJECT  = "request";

    /** Key for the environment {@link Response} in the object model. */
    public final static String RESPONSE_OBJECT = "response";

    /** Key for the environment {@link Context} in the object model. */
    public final static String CONTEXT_OBJECT  = "context";

    /** Key for the expiration value (Long) in the object model. */
    public final static String EXPIRES_OBJECT  = "expires";

    /** Key for the throwable object, only available within a &lt;map:handle-errors&gt;. */
    public final static String THROWABLE_OBJECT = "throwable";

    /**
     * Key for a {@link Map} containing information from
     * a parent request provided to a sub-request (internal processing)
     */
    public final static String PARENT_CONTEXT = "parent-context";


    private ObjectModelHelper() {
        // Forbid instantiation
    }

    /**
     * Return {@link Request} object stored in the provided
     * <code>objectModel</code> map.
     *
     * @param objectModel current objectModel map
     * @return request retrieved from the objectModel map
     */
    public static Request getRequest(Map objectModel) {
        return (Request) objectModel.get(REQUEST_OBJECT);
    }

    /**
     * Return {@link Response} object stored in the provided
     * <code>objectModel</code> map.
     *
     * @param objectModel current objectModel map
     * @return response retrieved from the objectModel map
     */
    public static Response getResponse(Map objectModel) {
        return (Response) objectModel.get(RESPONSE_OBJECT);
    }

    /**
     * Return {@link Context} object stored in the provided
     * <code>objectModel</code> map.
     *
     * @param objectModel current objectModel map
     * @return context retrieved from the objectModel map
     */
    public static Context getContext(Map objectModel) {
        return (Context) objectModel.get(CONTEXT_OBJECT);
    }

    /**
     * Return <code>expires</code> object stored in the provided
     * <code>objectModel</code> map.
     *
     * <p>
     * <code>expires</code> is an expiration timestamp. This object
     * is present in the <code>objectModel</code> only in the context
     * of a pipeline with configured expires parameter.
     *
     * @param objectModel current objectModel map
     * @return expiration timestamp retrieved from the objectModel map
     */
    public static Long getExpires(Map objectModel) {
        return (Long) objectModel.get(EXPIRES_OBJECT);
    }

    /**
     * Return {@link Throwable} object stored in the provided
     * <code>objectModel</code> map.
     *
     * <p>
     * <code>Throwable</code> object is present in the objectModel
     * only within a <code>&lt;map:handle-errors&gt;</code> section
     * of the sitemap. When outside of this section, null is returned.
     *
     * @param objectModel current objectModel map
     * @return throwable retrieved from the objectModel map
     */
    public static Throwable getThrowable(Map objectModel) {
        return (Throwable) objectModel.get(THROWABLE_OBJECT);
    }
}
