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
package org.apache.cocoon.components.cprocessor.sitemap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.AbstractNode;
import org.apache.cocoon.util.StringUtils;

/**
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public abstract class AbstractComponentNode extends AbstractNode implements ComponentNode, Configurable {
    
    private Collection m_labels;
    private String m_componentHint;
    private String m_mimeType;
    
    public AbstractComponentNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        m_componentHint = config.getChild("component").getAttribute("hint",null);
        Collection labels = splitLabels(config.getAttribute("label",null));
        m_labels = Collections.unmodifiableCollection(labels);
        m_mimeType = config.getAttribute("mime-type",null);
    }
    
    public Collection getLabels() {
        return m_labels;
    }
    
    public String getComponentHint() {
        return m_componentHint;
    }
    
    public String getMimeType() {
        return m_mimeType;
    }
    
    /**
     * Split a list of space/comma separated labels into a Collection
     *
     * @return the collection of labels (may be empty, never null)
     */
    private static final Collection splitLabels(String labels) {
        if (labels == null) {
            return Collections.EMPTY_SET;
        } else {
            return Arrays.asList(StringUtils.split(labels, ", \t\n\r"));
        }
    }
    
}
