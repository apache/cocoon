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

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationAttributes;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * InsertNodeJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link InsertNodeJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:insert-node&gt;
 *   &lt;!-- in here comes a template that will be inserted in the target
 *           document --&gt;
 * &lt;/fb:insert-node&gt;
 * </code></pre>
 *
 * @version $Id$
 */
public class InsertNodeJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link InsertNodeJXPathBinding} configured
     * with the nested template of the bindingElm.
     */
    public JXPathBindingBase buildBinding(Element bindingElm,
                                          JXPathBindingManager.Assistant assistant)
    throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);

            DocumentFragment domTemplate = null;

            String src = DomHelper.getAttribute(bindingElm, "src", null);
            if (src != null) {
                ServiceManager manager = assistant.getServiceManager();
                SourceResolver sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                Source source = null;
                try {
                    source = sourceResolver.resolveURI(src);
                    Document document = SourceUtil.toDOM(source);
                    Element element = document.getDocumentElement();

                    String xpath = DomHelper.getAttribute(bindingElm, "xpath", null);
                    if (xpath != null) {
                        XPathProcessor xpathProcessor = (XPathProcessor) manager.lookup(XPathProcessor.ROLE);
                        try {
                            Node node = xpathProcessor.selectSingleNode(document, xpath);
                            if (node == null) {
                                throw new BindingException("XPath expression '" + xpath + "' didn't return a result.",
                                                           DomHelper.getLocationObject(bindingElm));
                            }
                            if (!(node instanceof Element)) {
                                throw new BindingException("XPath expression '" + xpath + "' did not return an element node.",
                                                           DomHelper.getLocationObject(bindingElm));
                            }
                            element = (Element) node;
                        } finally {
                            manager.release(xpathProcessor);
                        }
                    }
                    domTemplate = document.createDocumentFragment();
                    domTemplate.appendChild(element);
                } finally {
                    if (source != null) {
                        sourceResolver.release(source);
                    }
                    manager.release(sourceResolver);
                }
            } else if (bindingElm.hasChildNodes()) {
                // FIXME: using the binding's document prevents it to be garbage collected.
                //        --> create a new Document and use doc.importNode();
                domTemplate = bindingElm.getOwnerDocument().createDocumentFragment();
                NodeList nested = bindingElm.getChildNodes();
                int size = nested.getLength();
                for (int i = 0; i < size; i++) {
                    Node node = nested.item(i).cloneNode(true);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        LocationAttributes.remove((Element) node, true);
                    }
                    domTemplate.appendChild(node);
                }
            }

            // do inheritance
            InsertNodeJXPathBinding otherBinding = (InsertNodeJXPathBinding) assistant.getContext().getSuperBinding();
            if (otherBinding != null) {
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);

                if (domTemplate == null) {
                    domTemplate = otherBinding.getTemplate();
                }
            }

            return new InsertNodeJXPathBinding(commonAtts, domTemplate);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building the insert-node binding", e,
                                       DomHelper.getLocationObject(bindingElm));
        }
    }
}
