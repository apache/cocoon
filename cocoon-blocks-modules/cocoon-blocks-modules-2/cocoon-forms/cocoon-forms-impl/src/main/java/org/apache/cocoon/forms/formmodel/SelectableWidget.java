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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.datatype.SelectionList;

/**
 * A {@link Widget} that can have a selection list. The initial selection list is set by the
 * widget's {@link WidgetDefinition}, and can be changed afterwards. The selection list can
 * be removed by setting the list to <code>null</code>.
 * 
 * @version $Id$
 */
public interface SelectableWidget extends Widget {

    /**
     * Set the widget's selection list given a {@link SelectionList}.
     * 
     * @param selectionList the selection list or <code>null</code> to have no selection list.
     */
    public void setSelectionList(SelectionList selectionList);

    /**
     * Set the widget's selection list given a source URI where the list will be read from.
     * 
     * @param uri the selection list's URI
     */
    public void setSelectionList(String uri);

    /**
     * Set the widgdet's selection given an object and XPath expressions.
     * 
     * @param model the selection list model. This is typically a collection or an array of objects
     *        in which <code>valuePath</code> and <code>labelPath</code> will extract some data.
     * @param valuePath the XPath expression to extract values
     * @param labelPath the XPath expression to extract labels (can be absent in which case the value is
     *        used as label).
     */
    public void setSelectionList(Object model, String valuePath, String labelPath);
}
