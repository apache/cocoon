/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.corona.servlet.component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.corona.pipeline.component.Finisher;
import org.apache.cocoon.corona.pipeline.component.Starter;
import org.apache.cocoon.corona.servlet.util.HttpContextHelper;

public class RedirectorComponent implements Starter, Finisher {

    private Map<String, Object> parameters;
    private String uri;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.corona.pipeline.component.Starter#execute()
     */
    public void execute() {
        HttpServletResponse response = HttpContextHelper.getResponse(this.parameters);

        try {
            String location = response.encodeRedirectURL(this.uri);
            response.sendRedirect(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getContentType() {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.corona.pipeline.component.PipelineComponent#setConfiguration(java.util.Map)
     */
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.uri = (String) configuration.get("uri");
    }

    public void setInputParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.corona.pipeline.component.Finisher#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream outputStream) {
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RedirectorComponent(" + this.uri + ")";
    }
}
