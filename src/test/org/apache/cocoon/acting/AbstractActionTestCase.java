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
package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.fortress.testcase.FortressTestCase;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.components.source.SourceResolverAdapter;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.environment.mock.MockRedirector;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockResponse;
import org.apache.excalibur.source.SourceResolver;

/**
 * Testcase for  action components. 
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: AbstractActionTestCase.java,v 1.4 2004/03/08 14:04:19 cziegeler Exp $
 */
public abstract class AbstractActionTestCase extends FortressTestCase
{
    private MockRequest request = new MockRequest();
    private MockResponse response = new MockResponse();
    private MockContext context = new MockContext();
    private MockRedirector redirector = new MockRedirector();
    private HashMap objectmodel = new HashMap();

    /**
     * Create a new generator test case.
     *
     * @param name Name of test case.
     */
    public AbstractActionTestCase(String name) {
        super(name);
    }

    public final MockRequest getRequest() {
        return request;
    }

    public final MockResponse getResponse() {
        return response;
    }

    public final MockContext getContext() {
        return context;
    }

    public final MockRedirector getRedirector() { 
        return redirector;
    }

    public final Map getObjectModel() {
        return objectmodel;
    }

    public void setUp() {
        objectmodel.put(ObjectModelHelper.REQUEST_OBJECT, request);
        objectmodel.put(ObjectModelHelper.RESPONSE_OBJECT, response);
        objectmodel.put(ObjectModelHelper.CONTEXT_OBJECT, context);
    }

    /**
     * Perform the action component.
     *
     * @param type Hint of the action. 
     * @param source Source for the action.
     * @param parameters Action parameters.
     */
    public final Map act(String type, String source, Parameters parameters) {

        Action action = null;
        SourceResolver resolver = null;

        Map result = null;
        try {

            resolver = (SourceResolver) lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if action name is not null", type);
            action = (Action) lookup(Action.ROLE + "/" + type);
            assertNotNull("Test lookup of action", action);

            result = action.act(redirector, new SourceResolverAdapter(resolver),
                                objectmodel, source, parameters);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve action", ce);
            fail("Could not retrieve action: " + ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: " + e);
        } finally {
            if (action != null) {
                release(action);
            }
            release(resolver);
        }
        return result;
    }
}
