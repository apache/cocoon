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
package org.apache.cocoon.woody.transformation;

import java.io.IOException;
import java.util.Map;
import java.util.Locale;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.Transformer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import org.xml.sax.SAXException;

/**
 * See description of {@link WidgetReplacingPipe}.
 */
public class WoodyTemplateTransformer extends WidgetReplacingPipe implements Transformer {

    /** Name of the request attribute under which the Woody form is stored (optional). */
    private String attributeName;
    private Request request;
    private JXPathContext jxpathContext;
    /** Containts locale specified as a parameter to the transformer, if any. */
    private Locale localeParameter;
    /** The locale currently used by the transformer. */
    private Locale locale;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {

        // create and set the jxpathContext...
        Object flowContext = FlowHelper.getContextObject(objectModel);
        WebContinuation wk = FlowHelper.getWebContinuation(objectModel);
        JXPathContext jxpc = JXPathContext.newContext(flowContext);
        Variables vars = jxpc.getVariables();
        vars.declareVariable("continuation", wk);
        Request request = ObjectModelHelper.getRequest(objectModel);
        vars.declareVariable("request", request);
        Session session = request.getSession(false);
        vars.declareVariable("session", session);
        vars.declareVariable("parameters", parameters);

        this.jxpathContext = jxpc;
        this.attributeName = parameters.getParameter("attribute-name", null);
        this.request = request;

        String localeStr = parameters.getParameter("locale", null);
        if (localeStr != null)
            localeParameter = I18nUtils.parseLocale(localeStr);

        init(null, this);
    }

    public JXPathContext getJXPathContext() {
        return jxpathContext;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Request getRequest() {
        return request;
    }

    public JXPathContext getJxpathContext() {
        return jxpathContext;
    }

    public void setJxpathContext(JXPathContext jxpathContext) {
        this.jxpathContext = jxpathContext;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocaleParameter() {
        return localeParameter;
    }
}
