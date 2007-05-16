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

import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;

/**
 * Dummy class cause binding doesn't accept null results from binding builders
 *
 * @version $Id$
 */
public class ImportJXPathBinding extends JXPathBindingBase {

	public ImportJXPathBinding() {
		super(JXPathBindingBuilderBase.CommonAttributes.DEFAULT);
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.forms.binding.JXPathBindingBase#doLoad(org.apache.cocoon.forms.formmodel.Widget, org.apache.commons.jxpath.JXPathContext)
	 */
	public void doLoad(Widget frmModel, JXPathContext jxpc)
    throws BindingException {
		// dont do anything

	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.forms.binding.JXPathBindingBase#doSave(org.apache.cocoon.forms.formmodel.Widget, org.apache.commons.jxpath.JXPathContext)
	 */
	public void doSave(Widget frmModel, JXPathContext jxpc)
    throws BindingException {
        // dont do anything
	}
}
