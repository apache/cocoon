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
package org.apache.cocoon.forms.datatype;

import org.apache.cocoon.forms.util.DomHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Builds {@link SelectionList}s from a JavaSelectionList classes configured in Spring context
 *
 * @version $Id$
 */
public class JavaSelectionListBuilder implements SelectionListBuilder, BeanFactoryAware {

    private static Log LOG = LogFactory.getLog( JavaSelectionListBuilder.class );
    private BeanFactory beanFactory;

    public void setBeanFactory( BeanFactory beanFactory )
                                                  throws BeansException
    {
        this.beanFactory = beanFactory;
    }
    
	public SelectionList build(Element selectionListElement, Datatype datatype)
    throws Exception {
        
        // hard way deprecation
        if (DomHelper.getAttribute(selectionListElement, "class", null) != null) {
            throw new RuntimeException("The 'class' attribute is not supported anymore at " 
                                       + DomHelper.getLocationObject( selectionListElement )
                                       + ". Use a 'ref' attribute to address a Spring bean");
        }

        String beanRef = DomHelper.getAttribute(selectionListElement, "ref");
		boolean nullable = DomHelper.getAttributeAsBoolean(selectionListElement, "nullable", true);

		try {
			Object bean = beanFactory.getBean(beanRef);
			if (bean != null && bean instanceof JavaSelectionList) {
				JavaSelectionList list = (JavaSelectionList) bean;
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
                LOG.warn("Spring bean reference " + beanRef + " is not a " 
                         + JavaSelectionList.class.getName() + ", , returning empty selection list: " 
                         + DomHelper.getLocationObject( selectionListElement ).getDescription());
				return new StaticSelectionList(datatype);
			}
		} catch (BeansException be) {
            LOG.warn("Spring bean reference " + beanRef 
                     + " does not exist, returning empty selection list: "
                     + DomHelper.getLocationObject( selectionListElement ).getDescription());
            return new StaticSelectionList(datatype);
		} catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Got exception in build, re-throwing", e);
            }
			throw e;
		}
	}
}
