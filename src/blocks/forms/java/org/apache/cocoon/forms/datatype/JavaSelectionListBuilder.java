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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Builds {@link SelectionList}s from a JavaSelectionList class
 * 
 *  
 */
public class JavaSelectionListBuilder extends AbstractLogEnabled implements
		SelectionListBuilder, Serviceable {

	private ServiceManager manager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.cocoon.forms.datatype.SelectionListBuilder#build(org.w3c.dom.Element,
	 *      org.apache.cocoon.forms.datatype.Datatype)
	 */
	public SelectionList build(Element selectionListElement, Datatype datatype)
			throws Exception {
		String className = DomHelper
				.getAttribute(selectionListElement, "class");
		boolean nullable = DomHelper.getAttributeAsBoolean(
				selectionListElement, "nullable", true);

		try {
			Class clasz = Class.forName(className);
			if (JavaSelectionList.class.isAssignableFrom(clasz)) {
				JavaSelectionList list = (JavaSelectionList) clasz
						.newInstance();
				LifecycleHelper.setupComponent(list, getLogger(), null,
						this.manager, null, null, true);
				list.setDatatype(datatype);
				list.setNullable(nullable);
                // pass the attributes to the SelectionList
				NamedNodeMap attrs = selectionListElement.getAttributes();
				int size = attrs.getLength();
				for (int i = 0; i < size; i++) {
					Node attr = attrs.item(i);
					String name = attr.getNodeName();
					list.setAttribute(name, attr.getNodeValue());
				}
				return list;
			} else {
				return new StaticSelectionList(datatype);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw e;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;

	}
}
