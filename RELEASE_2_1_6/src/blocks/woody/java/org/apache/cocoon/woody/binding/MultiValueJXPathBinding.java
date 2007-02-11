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
package org.apache.cocoon.woody.binding;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 * Simple binding for multi fields: on save, first deletes the target data
 * before recreating it from scratch.
 *
 * @version CVS $Id: MultiValueJXPathBinding.java,v 1.5 2004/03/09 13:54:08 reinhard Exp $
 */
public class MultiValueJXPathBinding extends JXPathBindingBase {

    private final String multiValueId;
    private final String multiValuePath;
    private final String rowPath;
    private final JXPathBindingBase updateBinding;
    private final Convertor convertor;
    private final Locale convertorLocale;

    public MultiValueJXPathBinding(
        JXPathBindingBuilderBase.CommonAttributes commonAtts, String multiValueId,
        String multiValuePath, String rowPath,
        JXPathBindingBase[] updateBindings, Convertor convertor, Locale convertorLocale) {
        super(commonAtts);
        this.multiValueId = multiValueId;
        this.multiValuePath = multiValuePath;
        this.rowPath = rowPath;
        this.updateBinding = new ComposedJXPathBindingBase(JXPathBindingBuilderBase.CommonAttributes.DEFAULT, updateBindings);
        this.convertor = convertor;
        this.convertorLocale = convertorLocale;
    }

    public void doLoad(Widget frmModel, JXPathContext jctx) throws BindingException {
        Widget widget = frmModel.getWidget(this.multiValueId);
        if (widget == null) {
            throw new BindingException("The widget with the ID [" + this.multiValueId
                    + "] referenced in the binding does not exist in the form definition.");
        }

        // Move to multi value context
        Pointer ptr = jctx.getPointer(this.multiValuePath);
        if (ptr.getNode() != null) {
            // There are some nodes to load from

            JXPathContext multiValueContext = jctx.getRelativeContext(ptr);
            // build a jxpath iterator for pointers
            Iterator rowPointers = multiValueContext.iterate(this.rowPath);

            LinkedList list = new LinkedList();

            while (rowPointers.hasNext()) {
                Object value = rowPointers.next();

                if (value != null && convertor != null) {
                    if (value instanceof String) {
                        value = convertor.convertFromString((String)value, convertorLocale, null);
                    } else {
                        getLogger().warn("Convertor ignored on backend-value which isn't of type String.");
                    }
                }

                list.add(value);
            }

            widget.setValue(list.toArray());
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("done loading values " + toString());
    }

    public void doSave(Widget frmModel, JXPathContext jctx) throws BindingException {
        Widget widget = frmModel.getWidget(this.multiValueId);
        Object[] values = (Object[])widget.getValue();

        JXPathContext multiValueContext = jctx.getRelativeContext(jctx.createPath(this.multiValuePath));
        // Delete all that is already present
        multiValueContext.removeAll(this.rowPath);

        boolean update = false;

        if (values != null) {
            // first update the values
            for (int i = 0; i < values.length; i++) {
                String path = this.rowPath + '[' + (i+1) + ']';
                Pointer rowPtr = multiValueContext.createPath(path);

                Object value = values[i];
                if (value != null && convertor != null) {
                    value = convertor.convertToString(value, convertorLocale, null);
                }

                rowPtr.setValue(value);
            }

            // now perform any other bindings that need to be performed when the value is updated
            this.updateBinding.saveFormToModel(frmModel, multiValueContext);

            update = true;
        }


        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done saving " + toString() + " -- on-update == " + update);
        }



    }

    public String toString() {
        return "MultiValueJXPathBinding [widget=" + this.multiValueId + ", xpath=" + this.multiValuePath + "]";
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.updateBinding.enableLogging(logger);
    }
}
