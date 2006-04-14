/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.accessor;

import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.components.accessor.Accessor;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

public class AccessorTestCase extends SitemapComponentTestCase {

    public void testRequestAccessor() throws ServiceException {
        ServiceSelector accessorSelector =
            (ServiceSelector)this.lookup(Accessor.ROLE + "Selector");
        Accessor accessor = (Accessor)accessorSelector.select("request");
        Request request = (Request)accessor.getObject();
        assertEquals("HTTP/1.1", request.getProtocol());
        accessorSelector.release(accessor);
        this.release(accessorSelector);
    }

    public void testSessionAccessor() throws ServiceException {
        // Create a session
        getRequest().getSession();
        ServiceSelector accessorSelector =
            (ServiceSelector)this.lookup(Accessor.ROLE + "Selector");
        Accessor accessor = (Accessor)accessorSelector.select("session");
        Session session = (Session)accessor.getObject();
        assertEquals("MockSession", session.getId());
        accessorSelector.release(accessor);
        this.release(accessorSelector);
    }

    public void testContextAccessor() throws ServiceException {
        getContext().setAttribute("foo", "bar");
        ServiceSelector accessorSelector =
            (ServiceSelector)this.lookup(Accessor.ROLE + "Selector");
        Accessor accessor = (Accessor)accessorSelector.select("context");
        Context context = (Context)accessor.getObject();
        assertEquals("bar", context.getAttribute("foo"));
        accessorSelector.release(accessor);
        this.release(accessorSelector);
    }

    public void testMapAccessor() throws ServiceException {
        // Create a session
        getRequest().getSession();
        getContext().setAttribute("foo", "bar");
        ServiceSelector accessorSelector =
            (ServiceSelector)this.lookup(Accessor.ROLE + "Selector");
        Accessor accessor = (Accessor)accessorSelector.select("cocoon");
        Map map = (Map)accessor.getObject();
        assertEquals("HTTP/1.1", ((Request)map.get("request")).getProtocol());
        assertEquals("MockSession", ((Session)map.get("session")).getId());
        assertEquals("bar", ((Context)map.get("context")).getAttribute("foo"));
        accessorSelector.release(accessor);
        this.release(accessorSelector);
    }
}
