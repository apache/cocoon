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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This abstract class provides the basis for testing the environment with
 * developer specified java code via the CodedSelectorFactory. This class also
 * provides the environments context, request, response and session
 * variables for developer ease.
 *
 * @author <a href="mailto:Marcus.Crafter@osa.de">Marcus Crafter</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-02-15 20:29:58 $
 */
public abstract class CodedSelectorHelper
{
    public abstract boolean evaluate(Map objectModel);

    protected CodedSelectorHelper() { }

    protected void initialize(Map objectModel)
    {
        context = (ServletContext)
                  objectModel.get(Constants.CONTEXT_OBJECT);
        request = (HttpServletRequest)
                  objectModel.get(Constants.REQUEST_OBJECT);
        response = (HttpServletResponse)
                   objectModel.get(Constants.RESPONSE_OBJECT);
        session = request.getSession();
    }

    protected ServletContext context;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;
}
