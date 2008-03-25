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
package org.apache.cocoon.servletservice.postable.components;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

/**
 * A factory for <code>service-consumer:</code> source.
 *
 * @see ServletServiceConsumerSource
 * @version $Id$
 * @since 1.0.0
 */
public class ServletServiceConsumerSourceFactory implements SourceFactory {

    private ProcessInfoProvider processInfoProvider;

    public Source getSource(String location, Map parameters) throws IOException, MalformedURLException {
        HttpServletRequest request = processInfoProvider.getRequest();
        if (!"POST".equals(request.getMethod())) {
            throw new MalformedURLException("Cannot create consumer source for request that is not POST.");
        }

        return new ServletServiceConsumerSource(request);
    }

    public void release(Source source) {
    }

    public ProcessInfoProvider getProcessInfoProvider() {
        return processInfoProvider;
    }

    public void setProcessInfoProvider(ProcessInfoProvider processInfoProvider) {
        this.processInfoProvider = processInfoProvider;
    }
}
