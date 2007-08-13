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
package org.apache.cocoon.samples.castor;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 *
 * @version $Id$
 */
public class TestBeanAction extends AbstractAction {

    public TestBeanAction() {
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters param)
    throws Exception {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Context context = ObjectModelHelper.getContext(objectModel);
        if (context != null) {
            context.setAttribute("Wale",new  TestBean("Wale in the big sea", "context"));
        }

        HttpSession session =request.getSession(true);
        session.setAttribute("Mouse",new  TestBean("Liveing in the session","session"));
        objectModel.put("Lion", new TestBean("Lion:walking on the sitemap","sitemap") );
        request.setAttribute("Hamster",new TestBean("Hamster:Wer hat nach mir gefragt","request")  );
        session.setAttribute("Elefant",new  TestBean("Elefant:from Africa","session"));
        request.setAttribute("Elefant",new  TestBean("Elefant:from India","request"));

        return objectModel;
    }
}
