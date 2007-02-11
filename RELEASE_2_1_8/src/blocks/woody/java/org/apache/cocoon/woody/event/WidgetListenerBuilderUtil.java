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
package org.apache.cocoon.woody.event;

import org.apache.cocoon.woody.event.impl.JavaClassWidgetListenerBuilder;
import org.apache.cocoon.woody.event.impl.JavaScriptWidgetListenerBuilder;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Quick hack to avoid declaring a component selector and all that stuff for now (should be removed
 * in a near future)
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: WidgetListenerBuilderUtil.java,v 1.5 2004/03/09 13:54:14 reinhard Exp $
 */
public class WidgetListenerBuilderUtil {

    public static WidgetListener getWidgetListener(Element element, Class listenerClass) throws Exception {
        if (element.getLocalName().equals("java")) {
            return JavaClassWidgetListenerBuilder.INSTANCE.buildListener(element, listenerClass);
        } else if (element.getLocalName().equals("javascript")) {
            return JavaScriptWidgetListenerBuilder.INSTANCE.buildListener(element, listenerClass);
        } else {
            throw new IllegalArgumentException("Unknown listener element " + element.getTagName() +
                " at " + DomHelper.getLocation(element));
        }
    }
}
