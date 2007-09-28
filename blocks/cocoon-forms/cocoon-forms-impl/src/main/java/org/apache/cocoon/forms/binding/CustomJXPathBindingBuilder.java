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
package org.apache.cocoon.forms.binding;

import java.lang.reflect.Method;

import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
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
 * @version $Id$
 */
public class CustomJXPathBindingBuilder extends JXPathBindingBuilderBase implements BeanFactoryAware {

    private BeanFactory beanFactory;

    private static Log LOG = LogFactory.getLog( ValueJXPathBinding.class );
    private static final Class[] DOMELEMENT_METHODARGS;
    private static final Class[] EMPTY_METHODARGS;

    static {
        DOMELEMENT_METHODARGS = new Class[1];
        DOMELEMENT_METHODARGS[0] = Element.class;
        EMPTY_METHODARGS = null;
    }
    
    public void setBeanFactory( BeanFactory beanFactory )
                                                  throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    /**
     * Builds the custom Binding class and wraps it into a CustomJXPathBinding
     *
     * @param bindingElm configuration element describing the binding to build
     * @param assistant helper-class for building possible nested bindings
     * @return the freshly built binding based on the configuration element
     * @throws BindingException
     */
    public JXPathBindingBase buildBinding(Element bindingElm, Assistant assistant)
    throws BindingException {

        try {
            // hard way deprecation
            if (DomHelper.getAttribute(bindingElm, "class", null) != null) {
                throw new RuntimeException("The 'class' attribute is not supported anymore at "
                                           + DomHelper.getLocationObject( bindingElm )
                                           + ". Use a 'ref' attribute to address a Spring bean");
            }
            if (DomHelper.getAttribute(bindingElm, "builderclass", null) != null) {
                throw new RuntimeException("The 'builderclass' attribute is not supported anymore at "
                                           + DomHelper.getLocationObject( bindingElm )
                                           + ". Use a 'builderref' attribute to address a Spring bean");
            }

            CommonAttributes commonAtts =
                JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path", ".");
            String widgetId = DomHelper.getAttribute(bindingElm, "id", null);

            Object bindingInstance = null;

            
            String beanRef = DomHelper.getAttribute(bindingElm, "ref", null);
            if(beanRef != null) {
                bindingInstance = beanFactory.getBean( beanRef );

            } else {
                String builderRefName =
                    DomHelper.getAttribute(bindingElm, "builderref", null);
                String factoryMethodName =
                    DomHelper.getAttribute(bindingElm, "factorymethod", null);
                Element configNode =
                    DomHelper.getChildElement(bindingElm, BindingManager.NAMESPACE, "config");

                // only do it if attributes exist
                if (builderRefName != null && factoryMethodName != null) {
	                Object builder = beanFactory.getBean( builderRefName );
	                Method factoryMethod;
	                Object[] args = null;
	                try {
	                    factoryMethod = builder.getClass().getMethod(factoryMethodName, DOMELEMENT_METHODARGS);
	                    args = new Object[1];
	                    args[0] = configNode;
	                } catch (NoSuchMethodException e) {
	                    factoryMethod = null;
	                }

	                if (factoryMethod == null) {
	                    factoryMethod = builder.getClass().getMethod(factoryMethodName, EMPTY_METHODARGS);
	                    args = null;
	                }

	                // we pass null to indicate that the method should be static
	                bindingInstance = factoryMethod.invoke(null, args);
                }
            }

            // do inheritance
            CustomJXPathBinding otherBinding = (CustomJXPathBinding) assistant.getContext().getSuperBinding();
            if (otherBinding != null) {
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);

                if (xpath == null) {
                    xpath = otherBinding.getXPath();
                }
                if (widgetId == null) {
                    widgetId = otherBinding.getId();
                }
                if (bindingInstance == null) {
                    bindingInstance = otherBinding.getWrappedBinding();
                }
            }

            CustomJXPathBinding customBinding =
                new CustomJXPathBinding(commonAtts, widgetId, xpath, (AbstractCustomBinding)bindingInstance);

            return customBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building custom binding", e,
                                       DomHelper.getLocationObject(bindingElm));
        }
    }
}
