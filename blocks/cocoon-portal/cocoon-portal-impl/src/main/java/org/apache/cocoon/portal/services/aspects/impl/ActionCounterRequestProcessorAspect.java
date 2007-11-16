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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.portal.services.aspects.RequestProcessorAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * This aspect "disables" the back button of the browser and tries to avoid
 * problems with the user browsing in multiple windows.
 * This event attaches a unique number to each request. For each user only the
 * current number is "active". Every request comming in containing an older
 * number is disregarded and therefore ignored.
 * WARNING: This aspect solves some problems while introducing new ones. Some
 *          features of the portal do NOT work when this aspect is used.
 *
 * @version $Id$
 */
public class ActionCounterRequestProcessorAspect
	extends AbstractBean
	implements RequestProcessorAspect {

    protected final static String ATTRIBUTE_NAME = ActionCounterRequestProcessorAspect.class.getName();

    /** The name of the parameter to check */
    protected String parameterName = "cocoon-portal-action";

	/**
	 * @see org.apache.cocoon.portal.services.aspects.RequestProcessorAspect#process(org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext)
	 */
	public void process(RequestProcessorAspectContext context) {
        final String requestParameterName = context.getAspectProperties().getProperty("parameter-name", this.parameterName);

        int actionCount;

        Integer actionValue = (Integer) context.getPortalService().getUserService().getAttribute(ATTRIBUTE_NAME);
        if (null == actionValue) {
            actionValue = new Integer(0);
            context.getPortalService().getUserService().setAttribute(ATTRIBUTE_NAME, actionValue);
            actionCount = 0;
        } else {
            actionCount = actionValue.intValue() + 1;
            context.getPortalService().getUserService().setAttribute(ATTRIBUTE_NAME, new Integer(actionCount));
        }

        final HttpServletRequest request = this.portalService.getRequestContext().getRequest();
        String value = request.getParameter( requestParameterName );
        if ( value != null && actionCount > 0) {
            // get number
            int number = 0;
            try {
                number = Integer.parseInt( value );
            } catch (Exception ignore) {
                number = -1;
            }

            if ( number == actionCount - 1) {
                // and invoke next one
                context.invokeNext();
            }
        }
        context.getPortalService().getLinkService().addUniqueParameterToLink( requestParameterName, String.valueOf(actionCount));

        final HttpServletResponse response = this.portalService.getRequestContext().getResponse();
        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Thu, 01 Jan 2000 00:00:00 GMT");
	}

    public void setParameterName(String value) {
        this.parameterName = value;
    }
}
