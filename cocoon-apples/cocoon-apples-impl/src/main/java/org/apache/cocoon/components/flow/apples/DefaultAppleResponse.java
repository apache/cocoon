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
package org.apache.cocoon.components.flow.apples;

import org.apache.cocoon.environment.Response;

/**
 * DefaultAppleResponse provides a default implementation for the
 * {@link AppleResponse}.
 * 
 * @version $Id$
 */
public class DefaultAppleResponse implements AppleResponse {
    private String uri;
    private Object data;
    private boolean redirect = false;
    private Response cocoonResponse;

    public DefaultAppleResponse(Response cocoonResponse) {
        this.cocoonResponse = cocoonResponse;
    }

    public void sendPage(String uri, Object bizData) {
        this.uri = uri;
        this.data = bizData;
        this.redirect = false;
    }

    public void redirectTo(String uri) {
        this.uri = uri;
        this.redirect = true;
    }

    protected boolean isRedirect() {
        return redirect;
    }

    protected String getURI() {
        return uri;
    }

    protected Object getData() {
        return data;
    }

    public Response getCocoonResponse() {
        return cocoonResponse;
    }
}
