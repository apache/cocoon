/*
 
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.
 
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
import org.apache.cocoon.util.StringUtils;

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
