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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public abstract class ViewablePipelineComponentNode extends AbstractPipelineComponentNode {

    private Map m_views = new HashMap();
    protected Collection m_labels;
        
    public ViewablePipelineComponentNode() {
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_labels = splitLabels(config.getAttribute("label",null));
    }
    
    public void initialize() throws Exception {
        super.initialize();
        // add the labels defined at the component node
        m_labels.addAll(m_component.getLabels());
    }
    
    /**
     * Split a list of space/comma separated labels into a Collection
     *
     * @return the collection of labels (may be empty, nut never null)
     */
    private static final Collection splitLabels(String labels) {
        if (labels == null) {
            return new HashSet(0);
        } else {
            return Arrays.asList(StringUtils.split(labels, ", \t\n\r"));
        }
    }

    protected final ViewNode getViewNode(String name) {
        ViewNode view = (ViewNode) m_views.get(name);
        if (view == null) {
            try {
                view = (ViewNode) lookup(ViewNode.ROLE + "/v-" + name);
                if (m_labels.contains(view.getLabel())) {
                    m_views.put(name,view);
                }
                else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("view '" + name + "' not applicable " 
                            + "to statement at '" + getLocation() + "'");
                    }
                    view = null;
                }
            }
            catch (ServiceException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("no such view: '" + name + "'");
                }
                view = null;
            }
        }
        return view;
    }
}
