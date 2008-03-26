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
package org.apache.cocoon.portal.sitemap.impl;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.ajax.AjaxHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.RequestContext;
import org.apache.cocoon.portal.spi.RequestContextProvider;
import org.apache.cocoon.processing.ProcessInfoProvider;

/**
 * This service provides the current request context.
 *
 * @version $Id$
 */
public class CocoonRequestContextProvider implements RequestContextProvider {

    /** The process info provider. */
    protected ProcessInfoProvider processInfoProvider;

    public void setProcessInfoProvider(final ProcessInfoProvider p) {
        this.processInfoProvider = p;
    }

    /**
     * @see org.apache.cocoon.portal.spi.RequestContextProvider#getCurrentRequestContext()
     */
    public RequestContext getCurrentRequestContext() {
        // TODO Should we cache the object creation?
        return new RequestContextImpl(this.processInfoProvider);
    }

    public static final class RequestContextImpl implements RequestContext {

        protected final ProcessInfoProvider provider;

        public RequestContextImpl(ProcessInfoProvider prov) {
            this.provider = prov;
        }

        /**
         * @see org.apache.cocoon.portal.RequestContext#getBasePath()
         */
        public String getBasePath() {
            return ObjectModelHelper.getRequest(provider.getObjectModel()).getSitemapURIPrefix();
        }

        /**
         * @see org.apache.cocoon.portal.RequestContext#getRequest()
         */
        public HttpServletRequest getRequest() {
            return this.provider.getRequest();
        }

        /**
         * @see org.apache.cocoon.portal.RequestContext#getResponse()
         */
        public HttpServletResponse getResponse() {
            return this.provider.getResponse();
        }

        /**
         * @see org.apache.cocoon.portal.RequestContext#isAjaxRequest()
         */
        public boolean isAjaxRequest() {
            final HttpServletRequest req = this.getRequest();
            return req.getParameter(AjaxHelper.AJAX_REQUEST_PARAMETER) != null;
        }

        /**
         * @see org.apache.cocoon.portal.RequestContext#getServletContext()
         */
        public ServletContext getServletContext() {
            return this.provider.getServletContext();
        }

    }
}
