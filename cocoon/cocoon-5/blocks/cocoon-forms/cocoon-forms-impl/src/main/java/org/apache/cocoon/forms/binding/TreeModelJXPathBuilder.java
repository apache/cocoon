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
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * @version $Id$
 */
public class TreeModelJXPathBuilder extends JXPathBindingBuilderBase {

	public JXPathBindingBase buildBinding(Element bindingElm, Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path", null);
            String widgetId = DomHelper.getAttribute(bindingElm, "id", null);

            return new TreeModelJXPath(commonAtts, widgetId, xpath);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building binding", e,
                                       DomHelper.getLocationObject(bindingElm));
        }
	}

}
