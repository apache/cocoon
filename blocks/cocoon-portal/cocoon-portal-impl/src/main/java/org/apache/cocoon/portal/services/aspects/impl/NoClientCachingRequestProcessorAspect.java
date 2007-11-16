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
package org.apache.cocoon.portal.services.aspects.impl;

import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.portal.services.aspects.RequestProcessorAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * This aspect implementation sets some headers on the response that tell
 * clients/proxies to not cache. This "disables" the back button on the
 * client.
 *
 * @version $Id$
 */
public class NoClientCachingRequestProcessorAspect
	extends AbstractBean
	implements RequestProcessorAspect {

	/**
	 * @see org.apache.cocoon.portal.services.aspects.RequestProcessorAspect#process(org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext)
	 */
	public void process(RequestProcessorAspectContext context) {
        final HttpServletResponse response = this.portalService.getRequestContext().getResponse();
        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Thu, 01 Jan 2000 00:00:00 GMT");

        context.invokeNext();
	}
}
