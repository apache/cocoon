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
package org.apache.cocoon.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the entry point for Cocoon execution as an HTTP Servlet.
 *
 * @version $Id$
 */

// FIXME: This class should be merged with the o.a.c.sitemap.SitemapServlet
// that is used in block mode.
public class SitemapServlet extends HttpServlet {

    /** The Cocoon request processor. */
    protected RequestProcessor processor;


    public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        this.processor.service(req, res);
    }

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        super.init();
        this.processor = new RequestProcessor(getServletContext());
    }
}
