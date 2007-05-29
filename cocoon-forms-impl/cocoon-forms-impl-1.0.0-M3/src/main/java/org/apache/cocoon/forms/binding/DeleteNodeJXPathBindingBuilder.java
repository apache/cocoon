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

import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;

import org.w3c.dom.Element;

/**
 * DeleteNodeJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link DeleteNodeJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:delete-node /&gt;
 * </code></pre>
 *
 * @version $Id$
 */
public class DeleteNodeJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link DeleteNodeJXPathBinding}.
     */
    public JXPathBindingBase buildBinding(Element bindingElm, Assistant assistant) throws BindingException {
        CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);

        // do inheritance
        DeleteNodeJXPathBinding otherBinding = (DeleteNodeJXPathBinding) assistant.getContext().getSuperBinding();
        if (otherBinding != null) {
            commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);
        }

        return new DeleteNodeJXPathBinding(commonAtts);
    }
}
