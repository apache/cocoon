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
package org.apache.cocoon.forms.formmodel;

import org.w3c.dom.Element;

/**
 * Interface for classes that can build widgets from an XML description.
 *
 * <p>These builder classes should be thread safe, only a single instance of them
 * will be created.
 *
 * <p>Implementations may implement Avalon's Composable interface to gain access
 * to other components.
 * 
 * @version CVS $Id: WidgetDefinitionBuilder.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public interface WidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception;

}
