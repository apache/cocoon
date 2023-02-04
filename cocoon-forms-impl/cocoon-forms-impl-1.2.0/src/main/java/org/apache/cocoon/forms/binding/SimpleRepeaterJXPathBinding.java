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

import java.util.Iterator;

import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 * Simple binding for repeaters: on save, first deletes the target data
 * before recreating it from scratch.
 * <p>
 * For a smarter binding that avoids deletion and recreation, consider
 * {@link org.apache.cocoon.forms.binding.RepeaterJXPathBinding}
 *
 * @version $Id$
 */
public class SimpleRepeaterJXPathBinding extends JXPathBindingBase {

    private final String repeaterId;
    private final String repeaterPath;
    private final String rowPath;
    private final boolean clearOnLoad;
    private final JXPathBindingBase rowBinding;
    private final boolean deleteIfEmpty;


    public SimpleRepeaterJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts,
                                       String repeaterId,
                                       String repeaterPath,
                                       String rowPath,
                                       boolean clearOnLoad,
                                       boolean deleteIfEmpty,
                                       JXPathBindingBase rowBinding) {
        super(commonAtts);
        this.repeaterId = repeaterId;
        this.repeaterPath = repeaterPath;
        this.rowPath = rowPath;
        this.rowBinding = rowBinding;
        this.rowBinding.setParent(this);
        this.clearOnLoad = clearOnLoad;
        this.deleteIfEmpty = deleteIfEmpty;
    }

    public String getId() { return repeaterId; }
    public String getRepeaterPath() { return repeaterPath; }
    public String getRowPath() { return rowPath; }
    public boolean getClearOnLoad() { return clearOnLoad; }
    public boolean getDeleteIfEmpty() { return deleteIfEmpty; }
    public JXPathBindingBase[] getChildBindings() { return ((ComposedJXPathBindingBase)rowBinding).getChildBindings(); }

    public void doLoad(Widget frmModel, JXPathContext jctx)
    throws BindingException {
        // Find the repeater and clear it
        Repeater repeater = (Repeater) selectWidget(frmModel, this.repeaterId);

        if (this.clearOnLoad) {
            repeater.clear();
        }

        // Move to repeater context
        Pointer ptr = jctx.getPointer(this.repeaterPath);
        if (ptr.getNode() != null) {
            // There are some nodes to load from

            JXPathContext repeaterContext = jctx.getRelativeContext(ptr);
            // build a jxpath iterator for pointers
            Iterator rowPointers = repeaterContext.iteratePointers(this.rowPath);

            //iterate through it
            int rowNum = 0;
            while (rowPointers.hasNext()) {
                // Get a row. It is created if needed (depends on clearOnLoad)
                Repeater.RepeaterRow thisRow;
                if (repeater.getSize() > rowNum) {
                    thisRow = repeater.getRow(rowNum);
                } else {
                    thisRow = repeater.addRow();
                }
                rowNum++;

                // make a jxpath sub context on the iterated element
                Pointer jxp = (Pointer) rowPointers.next();
                JXPathContext rowContext = repeaterContext.getRelativeContext(jxp);

                this.rowBinding.loadFormFromModel(thisRow, rowContext);
            }
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done loading rows " + this);
        }
    }

    public void doSave(Widget frmModel, JXPathContext jctx)
    throws BindingException {
        // Find the repeater
        Repeater repeater = (Repeater) selectWidget(frmModel, this.repeaterId);

        if (repeater.getSize() == 0 && this.deleteIfEmpty) {
            // Repeater is empty : erase all
            jctx.removeAll(this.repeaterPath);
        } else {
            // Repeater is not empty
            // Move to repeater context and create the path if needed
            JXPathContext repeaterContext =
                jctx.getRelativeContext(jctx.createPath(this.repeaterPath));

            // Delete all that is already present
            repeaterContext.removeAll(this.rowPath);

            for (int i = 0; i < repeater.getSize(); i++) {
                Pointer rowPtr = repeaterContext.createPath(
                        this.rowPath + '[' + (i+1) + ']');
                JXPathContext rowContext =
                    repeaterContext.getRelativeContext(rowPtr);
                this.rowBinding.saveFormToModel(repeater.getRow(i), rowContext);
            }
        }
    }

    public String toString() {
        return this.getClass().getName()+ " [widget=" + this.repeaterId +
            ", xpath=" + this.repeaterPath + "]";
    }
}
