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
package org.apache.cocoon.forms.datatype;

import org.apache.cocoon.forms.datatype.*;
import org.w3c.dom.Element;

/**
 * Builds {@link SelectionList}s from an XML description.
 * 
 * @version CVS $Id: SelectionListBuilder.java,v 1.1 2004/03/09 10:34:00 reinhard Exp $
 */
public interface SelectionListBuilder {
    
    static final String ROLE = SelectionListBuilder.class.getName();

    SelectionList build(Element selectionListElement, Datatype datatype) throws Exception;
}
