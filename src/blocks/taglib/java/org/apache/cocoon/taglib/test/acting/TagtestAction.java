/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.taglib.test.acting;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

/**
 * @author ?
 * @version CVS $Id: TagtestAction.java,v 1.4 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class TagtestAction extends ServiceableAction implements ThreadSafe {

    /*
     * @see Action#act(Redirector, SourceResolver, Map, String, Parameters)
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters par)
            throws Exception {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession();
        Enumeration locales = request.getLocales();
        List info = new ArrayList();

        request.setAttribute("BrowserLocales", locales);

        info.add(request.getRemoteAddr());
        info.add(request.getRemoteHost());
        info.add(request.getRemoteUser());
        info.add(request.getContentType());
        info.add(request.getLocale());

        session.setAttribute("RequestInfo", info);
        return null;
    }
}
