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
package org.apache.cocoon.forms.binding;

import java.lang.reflect.Method;

import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * CustomJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link CustomJXPathBinding} out of the configuration in the
 * provided configElement which looks like one of the following:
 * 
 * <p> 1. No additional configuration requirements:
 * <pre><code>
 * &lt;fb:custom id="<i>widget-id</i>" path="<i>xpath expression</i>"
 *     class="your.package.CustomBindingX" /&gt;
 * </code></pre>
 *
 * <p> 2. With custom configuration requirements:
 * <pre><code>
 * &lt;fb:custom id="<i>widget-id</i>" path="<i>xpath expression</i>"
 *     builderclass="your.package.CustomBindingXBuilder" 
 *     factorymethod="makeBinding"
 * &gt;
 *   &lt;fb:config custom-atts="someValue"&gt;
 *     &lt;!-- in here come the nested custom elements (recommended in own namespace) 
 *             that make up the custom config --&gt;
 *   &lt;/fb:config&gt;
 * &lt;/fb:context&gt;
 * </code></pre>
 *
 * @version CVS $Id: CustomJXPathBindingBuilder.java,v 1.1 2004/04/01 22:49:56 mpo Exp $
 */
public class CustomJXPathBindingBuilder extends JXPathBindingBuilderBase {
    private static final Class[] DOMELEMENT_METHODARGS;
    private static final Class[] EMPTY_METHODARGS;
    
    static {
        DOMELEMENT_METHODARGS = new Class[1];
        DOMELEMENT_METHODARGS[0] = Element.class;
        EMPTY_METHODARGS = null;
    }

    /**
     * Builds the custom Binding class and wraps it into a CustomJXPathBinding
     * 
     * @param bindingElm configuration element describing the binding to build  
     * @param assistant helper-class for building possible nested bindings 
     * @return the freshly built binding based on the configuration element
     * @throws BindingException
     */
    public JXPathBindingBase buildBinding(Element bindingElm, 
        Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path", ".");
            String widgetId = DomHelper.getAttribute(bindingElm, "id");

            Object bindingInstance;
            
            String className = DomHelper.getAttribute(bindingElm, "class", null);
            if(className != null) {
                Class clazz = Class.forName(className);
                bindingInstance = clazz.newInstance();
                
            } else {
                String builderClassName = DomHelper.getAttribute(bindingElm, "builderclass");
                String factoryMethodName = DomHelper.getAttribute(bindingElm, "factorymethod");                
                Element configNode = DomHelper.getChildElement(bindingElm, BindingManager.NAMESPACE, "config");
                
                Class builderClass = Class.forName(builderClassName);
                Method factoryMethod = null; 
                Object[] args;
                if (configNode != null) {                    
                    factoryMethod = builderClass.getMethod(factoryMethodName, DOMELEMENT_METHODARGS);
                    args = new Object[1];
                    args[1] = configNode;
                } else {
                    factoryMethod = builderClass.getMethod(factoryMethodName, EMPTY_METHODARGS);
                    args = null;
                }
                  
                // we pass null to indicate that the method should be static
                bindingInstance = factoryMethod.invoke(null, args);
            }

            CustomJXPathBinding customBinding = new CustomJXPathBinding(commonAtts, widgetId, xpath, (Binding)bindingInstance);
            return customBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building custom binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}
