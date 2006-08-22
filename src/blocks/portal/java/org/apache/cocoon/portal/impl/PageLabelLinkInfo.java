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

package org.apache.cocoon.portal.impl;

import org.apache.cocoon.environment.Request;

/**
 * Helper class containing the information about common parts for each link that will be generated
 * in the portal page.
 *
 * @version $Id:  $
 */
public class PageLabelLinkInfo extends LinkInfo
{

    /**
     * The label manager
     */
    protected PageLabelManager labelManager;

    public PageLabelLinkInfo(PageLabelManager manager, Request request, int defaultPort, int defaultSecurePort) {
        super(request, defaultPort, defaultSecurePort);
        this.labelManager = manager;
    }

    protected String getRelativeURI(Request request) {
        if (labelManager == null) {
            return super.getRelativeURI(request);
        }
        String sitemapURI = request.getSitemapURI();
        if (labelManager.isLabel(sitemapURI)) {
            return "";
        }

        return super.getRelativeURI(request);
    }
}
