/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.forms.binding;

import java.util.Iterator;

import org.apache.cocoon.forms.binding.JXPathBindingBuilderBase.CommonAttributes;
import org.apache.cocoon.forms.formmodel.EnhancedRepeater;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

public class EnhancedRepeaterJXPathBinding extends RepeaterJXPathBinding {

	private String adapterClass;


    public EnhancedRepeaterJXPathBinding(CommonAttributes commonAtts,
                                         String repeaterId,
                                         String repeaterPath,
                                         String rowPath, String rowPathForInsert,
                                         JXPathBindingBase[] childBindings,
                                         JXPathBindingBase insertBinding,
                                         JXPathBindingBase[] deleteBindings,
                                         JXPathBindingBase[] identityBindings,
                                         String adapterClass) {
		super(commonAtts, repeaterId, repeaterPath, rowPath, rowPathForInsert,
                childBindings, insertBinding, deleteBindings, identityBindings);
		this.adapterClass = adapterClass;
	}

	public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Repeater repeater = (Repeater) selectWidget(frmModel, super.getId());
        if (!(repeater instanceof EnhancedRepeater)) {
            super.doLoad(frmModel, jxpc);
            return;
        }

        EnhancedRepeater rep = (EnhancedRepeater) repeater;
        RepeaterAdapter adapter;
		if (this.adapterClass != null) {
			try {
				adapter = (RepeaterAdapter) Thread.currentThread().getContextClassLoader().loadClass(this.adapterClass).newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Cannot instantiate adapter class for advanced repeater binding", e);
			}
		} else {
			adapter = new RepeaterJXPathAdapter();
		}

        RepeaterJXPathCollection collection = new RepeaterJXPathCollection();
		//Pointer ptr = jxpc.getPointer(super.getRepeaterPath());
		//JXPathContext repeaterContext = jxpc.getRelativeContext(ptr);
		collection.init(jxpc, super.getRowPath(), adapter);
		adapter.setBinding(this);
		adapter.setJXCollection(collection);
		rep.setCollection(collection);
		rep.doPageLoad();
	}

	public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
		Repeater repeater = (Repeater) selectWidget(frmModel, super.getId());
		if (!(repeater instanceof EnhancedRepeater)) {
			super.doSave(frmModel, jxpc);
			return;
		}

        EnhancedRepeater rep = (EnhancedRepeater) repeater;
		rep.doPageSave();
		Pointer ptr = jxpc.getPointer(super.getRepeaterPath());
		JXPathContext repeaterContext = jxpc.getRelativeContext(ptr);
		RepeaterJXPathCollection collection = rep.getCollection();
        // iterate updated rows. note: we don't iterate over the whole context
        for (Iterator iter = collection.getUpdatedRows().iterator(); iter.hasNext();) {
            RepeaterItem item = (RepeaterItem) iter.next();
            getRowBinding().saveFormToModel(item.getRow(), item.getContext());
        }

        for (Iterator iter = collection.getDeletedRows().iterator(); iter.hasNext();) {
            RepeaterItem item = (RepeaterItem) iter.next();
            jxpc.removePath(item.getContext().createPath(".").asPath());
        }

        // insert rows
        int indexCount = collection.getOriginalCollectionSize() - collection.getDeletedRows().size();
        for (Iterator iter = collection.getInsertedRows().iterator(); iter.hasNext();) {
            indexCount++;
            RepeaterItem item= (RepeaterItem) iter.next();

            // Perform the insert row binding.
            if (getInsertRowBinding() != null) {
            	getInsertRowBinding().saveFormToModel(item.getRow(), repeaterContext);
            }
            // -->  create the path to let the context be created
            Pointer newRowContextPointer = repeaterContext.createPath(
                    super.getInsertRowPath() + "[" + indexCount + "]");
            JXPathContext newRowContext =
                repeaterContext.getRelativeContext(newRowContextPointer);
            //    + rebind to children for update
            super.getRowBinding().saveFormToModel(item.getRow(), newRowContext);
        }
	}

}
