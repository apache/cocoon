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

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;
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
 * &lt;wb:insert-node&gt;
 *   &lt;!-- in here comes a template that will be inserted in the target
 *           document --&gt;
 * &lt;/wb:insert-node&gt;
 * </code></pre>
 *
 * @version CVS $Id: InsertNodeJXPathBindingBuilder.java,v 1.2 2004/03/28 20:51:24 antonio Exp $
 */
public class InsertNodeJXPathBindingBuilder
    extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link InsertNodeJXPathBinding} configured
     * with the nested template of the bindingElm.
     */
    public JXPathBindingBase buildBinding(
        Element bindingElm,
        JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);

            DocumentFragment domTemplate = null;

            String src = bindingElm.getAttribute("src");
            if (!src.equals("")) {
                ServiceManager manager = assistant.getServiceManager();
                SourceResolver sourceResolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
                Source source = null;
                try {
                    source = sourceResolver.resolveURI(src);
                    Document document = SourceUtil.toDOM(source);
                    Element element = document.getDocumentElement();

                    String xpath = bindingElm.getAttribute("xpath");
                    if (!xpath.equals("")) {
                        XPathProcessor xpathProcessor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
                        try {
                            Node node = xpathProcessor.selectSingleNode(document, xpath);
                            if (node == null)
                                throw new BindingException("XPath expression \"" + xpath + "\" didn't return a result.");
                            if (!(node instanceof Element))
                                throw new BindingException("XPath expression \"" + xpath + "\" did not return an element node.");
                            element = (Element)node;
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
            } else {
                domTemplate = bindingElm.getOwnerDocument().createDocumentFragment();
                NodeList nested = bindingElm.getChildNodes();
                int size = nested.getLength();
                for (int i = 0; i < size; i++) {
                    domTemplate.appendChild(nested.item(i).cloneNode(true));
                }
            }

            return new InsertNodeJXPathBinding(commonAtts, domTemplate);
        } catch (Exception e) {
            throw new BindingException("Error building the insert-node binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}
