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
package org.apache.cocoon.components.cprocessor.sitemap.impl;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.NamedContainerNode;
import org.apache.cocoon.components.cprocessor.sitemap.ViewNode;

/**
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * 
 * @avalon.component
 * @avalon.service type=ViewNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=view-node
 */
public class ViewNodeImpl extends NamedContainerNode implements ViewNode {
    
    private static final String FROM_LABEL_ATTR = "from-label";
    private static final String FROM_POSITION_ATTR = "from-position";
    
    private String m_label;
    
    public ViewNodeImpl() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        // Get the label or position (pseudo-label) of this view.
        m_label = config.getAttribute(FROM_LABEL_ATTR, null);
        
        if (m_label == null) {
            String position = config.getAttribute(FROM_POSITION_ATTR);
            if ("first".equals(position)) {
                m_label = FIRST_POS_LABEL;
            } else if ("last".equals(position)) {
                m_label = LAST_POS_LABEL;
            } else {
                String msg = "Bad value for 'from-position' at " + getLocation();
                throw new ConfigurationException(msg);
            }
        }
    }
    
    public String getLabel() {
        return m_label;
    }
    
}
