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
package org.apache.cocoon.woody;

import org.apache.cocoon.woody.formmodel.*;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.excalibur.source.Source;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.HashMap;

/**
 * Component implementing the {@link FormManager} role.
 *
 * <p>Some important TODO's: cache FormDefiniton's (now they are reparsed and recreated each
 * on each request), and make the list of widget implementations configurable instead of hardcoded.
 */
public class DefaultFormManager implements FormManager, ThreadSafe, Composable {
    private ComponentManager componentManager;
    private Map widgetDefinitionBuilders = new HashMap();
    private FormDefinitionBuilder formDefinitionBuilder;
    private boolean initialized = false;

    public void compose(ComponentManager componentManager) throws ComponentException {
        this.componentManager = componentManager;
    }

    public void lazyInitialize() throws Exception {
        // Initialisation is only done after the FormManager has been fully created, because
        // the WidgetDefinitionBuilders that we create here need themselves access to
        // the FormManager (which they can only lookup after the FormManager itself has
        // passed all lifecycle stages).

        if (initialized)
            return;

        LifecycleHelper lifecycleHelper = new LifecycleHelper(null, null, componentManager, null, null, null);

        // TODO the stuff below must be done based on external configuration information

        // Setup all the widget definition builders
        WidgetDefinitionBuilder widgetDefinitionBuilder;

        widgetDefinitionBuilder = new FieldDefinitionBuilder();
        lifecycleHelper.setupComponent(widgetDefinitionBuilder);
        widgetDefinitionBuilders.put("field", widgetDefinitionBuilder);

        widgetDefinitionBuilder = new RepeaterDefinitionBuilder();
        lifecycleHelper.setupComponent(widgetDefinitionBuilder);
        widgetDefinitionBuilders.put("repeater", widgetDefinitionBuilder);

        widgetDefinitionBuilder = new BooleanFieldDefinitionBuilder();
        lifecycleHelper.setupComponent(widgetDefinitionBuilder);
        widgetDefinitionBuilders.put("booleanfield", widgetDefinitionBuilder);

        widgetDefinitionBuilder = new MultiValueFieldDefinitionBuilder();
        lifecycleHelper.setupComponent(widgetDefinitionBuilder);
        widgetDefinitionBuilders.put("multivaluefield", widgetDefinitionBuilder);

        // special case
        formDefinitionBuilder = new FormDefinitionBuilder();
        lifecycleHelper.setupComponent(formDefinitionBuilder);

        initialized = true;
    }

    public FormDefinition getFormDefinition(Source source) throws Exception {
        lazyInitialize();

        // TODO caching!!
        Document formDocument;
        try {
            InputSource inputSource = new InputSource(source.getInputStream());
            inputSource.setSystemId(source.getURI());
            formDocument = DomHelper.parse(inputSource);
        }
        catch (Exception exc) {
            throw new CascadingException("Could not parse form definition from " + source.getURI(), exc);
        }

        Element formElement = formDocument.getDocumentElement();

        // check that the root element is a wd:form element
        if (!(formElement.getLocalName().equals("form") || Constants.WD_NS.equals(formElement.getNamespaceURI())))
            throw new Exception("Expected a Woody form element at " + DomHelper.getLocation(formElement));

        FormDefinition formDefinition = (FormDefinition)formDefinitionBuilder.buildWidgetDefinition(formElement);
        return formDefinition;
    }

    public WidgetDefinition buildWidgetDefinition(Element widgetDefinition) throws Exception {
        lazyInitialize();

        String widgetName = widgetDefinition.getLocalName();
        WidgetDefinitionBuilder builder = (WidgetDefinitionBuilder)widgetDefinitionBuilders.get(widgetName);
        if (builder == null)
            throw new Exception("Unkown kind of widget \"" + widgetName + "\" specified at " + DomHelper.getLocation(widgetDefinition));
        return builder.buildWidgetDefinition(widgetDefinition);
    }
}
