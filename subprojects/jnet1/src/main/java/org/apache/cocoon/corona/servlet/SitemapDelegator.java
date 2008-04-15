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
package org.apache.cocoon.corona.servlet;

import java.io.OutputStream;
import java.util.Map;

public class SitemapDelegator {

    private static final ThreadLocal<SitemapServlet> SITEMAP_SERVLET = new ThreadLocal<SitemapServlet>();

    public static void delegate(String requestURI, Map<String, Object> parameters, OutputStream outputStream) throws Exception {
        SitemapServlet sitemapServlet = SITEMAP_SERVLET.get();

        if (sitemapServlet == null) {
            throw new IllegalStateException("No current SitemapServlet.");
        }

        sitemapServlet.invoke(requestURI, parameters, outputStream);
    }

    public static final void removeSitemapServlet() {
        SITEMAP_SERVLET.set(null);
    }

    public static final void setSitemapServlet(SitemapServlet sitemapServlet) {
        SITEMAP_SERVLET.set(sitemapServlet);
    }
}
