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
package org.apache.cocoon.portal.om;


/**
 *
 * @version $Id$
 */
public class Item extends AbstractParameters {

	private Layout layout;

    private CompositeLayout parentLayout;

	/**
	 * Returns the layout.
	 * @return Layout
	 */
	public final Layout getLayout() {
		return layout;
	}

	/**
	 * Sets the layout.
	 * @param layout The layout to set
	 */
	public final void setLayout(Layout layout) {
		this.layout = layout;
		if(layout != null) {
            layout.setParent(this);
        }
    }

    public final CompositeLayout getParent() {
        return this.parentLayout;
    }

    public final void setParent(CompositeLayout layout) {
        this.parentLayout = layout;
    }
}
