/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection.helpers;

import org.apache.cocoon.Constants;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.environment.http.HttpRequest;
import org.apache.cocoon.environment.http.HttpResponse;

/**
 * This abstract class provides the basis for testing the environment with
 * developer specified java code via the CodedSelectorFactory. This class also
 * provides the environments context, request, response and session
 * variables for developer ease.
 *
 * @author <a href="mailto:Marcus.Crafter@osa.de">Marcus Crafter</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-03-23 13:48:55 $
 */
public abstract class CodedSelectorHelper
{
    public abstract boolean evaluate(Map objectModel);

    protected CodedSelectorHelper() { }

    protected void initialize(Map objectModel)
    {
        context = (ServletContext)
                  objectModel.get(Constants.CONTEXT_OBJECT);
        request = (HttpRequest)
                  objectModel.get(Constants.REQUEST_OBJECT);
        response = (HttpResponse)
                   objectModel.get(Constants.RESPONSE_OBJECT);
        session = request.getSession(false);
    }

    protected ServletContext context;
    protected HttpRequest request;
    protected HttpResponse response;
    protected HttpSession session;
}
