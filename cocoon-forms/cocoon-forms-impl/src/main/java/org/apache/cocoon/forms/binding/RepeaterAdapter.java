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

import java.util.Collection;

import org.apache.cocoon.forms.formmodel.Repeater.RepeaterRow;

/**
 * @version $Id$
 */
public interface RepeaterAdapter {

	public void setBinding(EnhancedRepeaterJXPathBinding binding);
	public void setJXCollection(RepeaterJXPathCollection collection);

	public void setCollection(Collection c);

	// TODO expand with widget path
	public RepeaterSorter sortBy(String path);
	public RepeaterFilter getFilter();

	public RepeaterItem getItem(int i);
	public RepeaterItem generateItem(RepeaterRow row);
	public void populateRow(RepeaterItem item) throws BindingException;
}
