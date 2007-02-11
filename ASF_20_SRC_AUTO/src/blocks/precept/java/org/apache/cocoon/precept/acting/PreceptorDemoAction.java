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

package org.apache.cocoon.precept.acting;

import java.util.Collection;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.precept.Instance;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 25, 2002
 * @version CVS $Id: PreceptorDemoAction.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public class PreceptorDemoAction extends AbstractPreceptorAction {

    private final static String VIEW1 = "view1";
    private final static String VIEW2 = "view2";
    private final static String VIEW3 = "view3";
    private final static String VIEW4 = "view4";
    private final static String VIEW_THANKS = "thanks";
    private final static String VIEW_ERROR = "error";

    private final static String[] SET_PERSON = {
        "cocoon-installation/user/firstname",
        "cocoon-installation/user/lastname",
        "cocoon-installation/user/email",
        "cocoon-installation/user/age"
    };

    private final static String[] SET_INSTALLATION = {
        "cocoon-installation/number",
        "cocoon-installation/live-url",
        "cocoon-installation/publish"
    };

    private final static String[] SET_SYSTEM = {
        "cocoon-installation/system/os",
        "cocoon-installation/system/processor",
        "cocoon-installation/system/ram",
        "cocoon-installation/system/servlet-engine",
        "cocoon-installation/system/java-version"
    };

    public Map introspection(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        getLogger().debug("start of flow");

        Session session = createSession(objectModel);
        Instance instance = createInstance("feedback");

        session.setAttribute("form-feedback", instance);
        return (page(VIEW1));

    }

    public Map doPrev1(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        populate(objectModel, "form-feedback", SET_INSTALLATION);
        return (page(VIEW1));

    }

    public Map doPrev2(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        populate(objectModel, "form-feedback", SET_SYSTEM);
        return (page(VIEW2));

    }

    public Map doPrev3(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        return (page(VIEW3));
    }


    public Map doNext2(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        getLogger().debug("populating");
        populate(objectModel, "form-feedback", SET_PERSON);

        Collection errors = validate(objectModel, "form-feedback", SET_PERSON);
        if (errors != null) {
            getLogger().debug("some constraints FAILED");
            pass(objectModel, errors);
            return (page(VIEW1));
        }
        else {
            getLogger().debug("all constraints are ok");
            return (page(VIEW2));
        }
    }

    public Map doNext3(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        getLogger().debug("populating");
        populate(objectModel, "form-feedback", SET_INSTALLATION);

        Collection errors = validate(objectModel, "form-feedback", SET_INSTALLATION);

        if (errors != null) {
            getLogger().debug("some constraints FAILED");
            pass(objectModel, errors);
            return (page(VIEW2));
        }
        else {
            getLogger().debug("all constraints are ok");
            return (page(VIEW3));
        }
    }

    public Map doNext4(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        getLogger().debug("populating");
        populate(objectModel, "form-feedback", SET_SYSTEM);

        Collection errors = validate(objectModel, "form-feedback", SET_SYSTEM);
        if (errors != null) {
            getLogger().debug("some constraints FAILED");
            pass(objectModel, errors);
            return (page(VIEW3));
        }
        else {
            getLogger().debug("all constraints are ok");
            return (page(VIEW4));
        }
    }

    public Map doSubmit(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        getLogger().debug("submitting");
        Collection errors = validate(objectModel, "form-feedback");
        if (errors != null) {
            getLogger().debug("some constraints FAILED");
            pass(objectModel, errors);
            return (page(VIEW_ERROR));
        }
        else {
            getLogger().debug("instance is valid - submitting");

            /*
             * do whatever you want with the instance data
             */

            return (page(VIEW_THANKS));
        }
    }
}
