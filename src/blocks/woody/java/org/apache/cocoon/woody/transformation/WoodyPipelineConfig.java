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

import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Variables;
import org.xml.sax.SAXException;

/**
 * WoodyPipeLineConfig
 */
public class WoodyPipelineConfig {

    /**
     * Default key under which the woody form is stored in the JXPath context.
     */
    public static final String WOODY_FORM = "woody-form";
    
    /** 
     * Name of the request attribute under which the Woody form is stored (optional). */
    private final String attributeName;
    
    /**
     * Pointer to the current request object.     */
    private final Request request;
    
    /**
     * Initialized jxpathcontext to evaluate passed expressions with.     */
    private final JXPathContext jxpathContext;
    
    /** 
     * Containts locale specified as a parameter to the transformer, if any. */
    private final Locale localeParameter;

    /** 
     * The locale currently used by the transformer. */
    private Locale locale;


    
    private WoodyPipelineConfig(String attName, Request req, JXPathContext jxpc, Locale localeParam) {
        this.attributeName = attName;
        this.request = req;
        this.jxpathContext =jxpc;
        this.localeParameter = localeParam;
    }
    
   
    public static WoodyPipelineConfig createConfig(Map objectModel, Parameters parameters) {
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
        
        String attributeName = parameters.getParameter("attribute-name", null);
        
        Locale localeParameter = null;
        String localeStr = parameters.getParameter("locale", null);
        if (localeStr != null) {
            localeParameter = I18nUtils.parseLocale(localeStr);
        }
        
        return new WoodyPipelineConfig(attributeName, request, jxpc, localeParameter);
    }
    
    
    /**
     * Overloads {@see #findForm(String)} by setting the jxpath-expression to null
     */
    public Form findForm() throws SAXException {
        return this.findForm(null);
    }

    /**
     * Finds the form from the current request-context based on the settings of 
     * this configuration object.  The fall-back search-procedure is as follows:
     * <ol><li>Use the provided jxpathEpression (if not null)</li>
     * <li>Use the setting of the 'attribute-name' parameter on the request</li>
     * <li>Obtainn the form from it's default location in the flow context</li>
     * </ol> 
     * 
     * @param jxpathExpression that should be pointing to the form
     * @return the found form if found
     * @throws SAXException in any of the folowing cases:
     * <ul><li>The provided jxpathExpression (if not null) is not pointing to 
     * a {@see Form} instance.</li>
     * <li>The request is not holding a {@see Form} instance under the key 
     * specified by 'attribute-name' (if specified)</li>
     * <li>Both jxpathExpresiion and 'attribute-name' were not specified AND
     * also the default location was not holding a valid {@see Form} instance.</li>
     * </ol> 
     */
    public Form findForm(String jxpathExpression) throws SAXException {

        if (jxpathExpression != null) {
            
            Object form = this.jxpathContext.getValue(jxpathExpression);
            if (form == null) {
                throw new SAXException("No form found at location \"" + jxpathExpression + "\".");
            }
            if (!(form instanceof Form)) {
                throw new SAXException("Object returned by expression \"" + jxpathExpression + "\" is not a Woody Form.");
            }
            return (Form)form;
        } else if (this.attributeName != null) { // then see if an attribute-name was specified
            Object form = this.request.getAttribute(this.attributeName);
            
            if (form == null) {
                throw new SAXException("No form found in request attribute with name \"" + this.attributeName + "\"");
            }
            if (!(form instanceof Form)) {
                throw new SAXException("Object found in request (attribute = '" + this.attributeName + "') is not a Woody Form.");
            }
            return (Form)form;
        } else { // and then see if we got a form from the flow
            jxpathExpression = "/" + WoodyPipelineConfig.WOODY_FORM;
            Object form = null;
            try {
                form = this.jxpathContext.getValue(jxpathExpression);
            } catch (JXPathException e) { /* do nothing */ }
            if (form != null) {
                return (Form)form;
            } else {
                throw new SAXException("No Woody form found.");
            }
        }
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

//    public void setJxpathContext(JXPathContext jxpathContext) {
//        this.jxpathContext = jxpathContext;
//    }

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
