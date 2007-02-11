/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: PreceptorDemoAction.java,v 1.3 2003/11/20 16:24:14 joerg Exp $
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
