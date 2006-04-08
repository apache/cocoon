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
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.component.Component;

import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Builds {@link SelectionList}s from a JavaSelectionList class
 *
 * @version $Id$
 */
public class JavaSelectionListBuilder extends AbstractLogEnabled
                                      implements SelectionListBuilder,
                                                 Contextualizable, Serviceable, ThreadSafe, Component {

    /**
     * The Avalon Context
     */
    private Context context;

    /**
     * The Service Manager
     */
	private ServiceManager manager;

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

	public SelectionList build(Element selectionListElement, Datatype datatype)
    throws Exception {
		String className = DomHelper.getAttribute(selectionListElement, "class");
		boolean nullable = DomHelper.getAttributeAsBoolean(selectionListElement, "nullable", true);

		try {
			Class clasz = Class.forName(className);
			if (JavaSelectionList.class.isAssignableFrom(clasz)) {
				JavaSelectionList list = (JavaSelectionList) clasz.newInstance();
				LifecycleHelper.setupComponent(list, getLogger(), this.context, this.manager, null, true);
				list.setDatatype(datatype);
				list.setNullable(nullable);

                // pass the attributes to the SelectionList
				NamedNodeMap attrs = selectionListElement.getAttributes();
				final int size = attrs.getLength();
				for (int i = 0; i < size; i++) {
					final Node attr = attrs.item(i);
					final String name = attr.getNodeName();
					list.setAttribute(name, attr.getNodeValue());
				}

				return list;
			} else {
                getLogger().warn("Class " + className + " does not implement JavaSelectionList, returning empty selection list.");
				return new StaticSelectionList(datatype);
			}
		} catch (Exception e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Got exception in build, re-throwing", e);
            }
			throw e;
		}
	}
}
