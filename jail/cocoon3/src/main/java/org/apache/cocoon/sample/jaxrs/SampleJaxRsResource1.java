/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sample.jaxrs;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.rest.jaxrs.response.URLResponseBuilder;

@Path("/sample")
public class SampleJaxRsResource1 {

    private Settings settings;

    @GET
    @Path("/parameter-passing/{id}")
    public Response anotherService(@PathParam("id") String id, @QueryParam("req-param") String reqParam,
            @Context UriInfo uriInfo, @Context Request request) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", "Donald Duck");
        data.put("id", id);
        data.put("reqparam", reqParam);
        data.put("testProperty", this.settings.getProperty("testProperty"));

        return URLResponseBuilder.newInstance("servlet:sample:/controller/screen", data).build();
    }

    @GET
    @Path("/sax-pipeline/unauthorized")
    public Response saxPipelineUnauthorized() {
        return URLResponseBuilder.newInstance("servlet:sample:/sax-pipeline/unauthorized").build();
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
