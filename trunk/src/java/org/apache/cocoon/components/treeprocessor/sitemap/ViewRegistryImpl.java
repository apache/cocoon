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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.util.StringUtils;
import org.apache.cocoon.xml.LocationAugmentationPipe;

/**
 * Registry for view related data.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * 
 * @avalon.component
 * @avalon.service type=ViewRegistry
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=view-registry
 */
public class ViewRegistryImpl extends AbstractLogEnabled implements ViewRegistry, Configurable {
    
    /**
     * Pseudo-label for views <code>from-position="first"</code> (i.e. generator).
     */
    public static final String FIRST_POS_LABEL = "!first!";

    /**
     * Pseudo-label for views <code>from-position="last"</code> (i.e. serializer).
     */
    public static final String LAST_POS_LABEL = "!last!";
    
    private static final String COMPONENT_CONFIG = "component";
    private static final String VIEW_CONFIG = "view";
    private static final String IDREF_ATTR = "id-ref";
    private static final String LABEL_ATTR = "label";
    private static final String FROM_LABEL_ATTR = "from-label";
    private static final String FROM_POSITION_ATTR = "from-position";
    
    // component ids -> labels
    private Map m_componentLabels;
    
    // labels -> view ids
    private Map m_labelViews;
    
    // ---------------------------------------------------- lifecycle
    
    public ViewRegistryImpl() {
    }
    
    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration[] components = configuration.getChildren(COMPONENT_CONFIG);
        m_componentLabels = new HashMap(components.length);
        for (int i = 0; i < components.length; i++) {
            String idref = components[i].getAttribute(IDREF_ATTR);
            String label = components[i].getAttribute(LABEL_ATTR);
            m_componentLabels.put(idref,splitLabels(label));
        }
        Configuration[] views = configuration.getChildren(VIEW_CONFIG);
        m_labelViews = new HashMap(views.length);
        for (int i = 0; i < views.length; i++) {
            String idref = views[i].getAttribute(IDREF_ATTR);
            String label = views[i].getAttribute(FROM_LABEL_ATTR,null);
            if (label == null) {
                String position = views[i].getAttribute(FROM_POSITION_ATTR);
                if ("first".equals(position)) {
                    label = FIRST_POS_LABEL;
                } else if ("last".equals(position)) {
                    label = LAST_POS_LABEL;
                } else {
                    String msg = "Bad value for 'from-position' at " 
                        + configuration.getAttribute(
                                LocationAugmentationPipe.LOCATION_ATTR,
                                LocationAugmentationPipe.UNKNOWN_LOCATION);
                    throw new ConfigurationException(msg);
                }
            }
            addViewForLabel(label,idref);
        }
    }
    
    // ---------------------------------------------------- ViewRegistry implementation
    
    public Collection getViewsForStatement(String role, String componentId, Configuration statement) {
        
        // Compute the views attached to this component
        Set views = null;

        // Build the set of all labels for this statement
        Set labels = new HashSet();
        
        // 1 - labels defined on the component
        Collection componentLabels = (Collection) m_componentLabels.get(componentId);
        if (componentLabels != null) {
            labels.addAll(componentLabels);
        }

        // 2 - labels defined on this statement
        String statementLabels = statement.getAttribute(LABEL_ATTR, null);
        if (statementLabels != null) {
            labels.addAll(splitLabels(statementLabels));
        }

        // 3 - pseudo-label depending on the role
        if (Generator.ROLE.equals(role)) {
            labels.add(FIRST_POS_LABEL);
        } else if (Serializer.ROLE.equals(role)) {
            labels.add(LAST_POS_LABEL);
        }

        // Build the set of views attached to these labels
        views = new HashSet();

        // Iterate on all labels for this statement
        Iterator labelIter = labels.iterator();
        while(labelIter.hasNext()) {
            // Iterate on all views for this label
            Collection coll = (Collection) m_labelViews.get(labelIter.next());
            if (coll != null) {
                views.addAll(coll);
            }
        }

        // Don't keep empty result
        if (views.size() == 0) {
            views = null;

            if (getLogger().isDebugEnabled()) {
                getLogger().debug(statement.getName() + " has no views at " + statement.getLocation());
            }
        } else {
            if (getLogger().isDebugEnabled()) {
                // Dump matching views
                StringBuffer buf = new StringBuffer(statement.getName() + " will match views [");
                Iterator iter = views.iterator();
                while(iter.hasNext()) {
                    buf.append(iter.next()).append(" ");
                }
                buf.append("] at ").append(statement.getLocation());

                getLogger().debug(buf.toString());
            }
        }

        return views;
    }

    /**
     * Add a view for a label. This is used to register all views that start from
     * a given label.
     *
     * @param label the label (or pseudo-label) for the view
     * @param view the view name
     */
    private void addViewForLabel(String label, String view) {
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("views:addViewForLabel(" + label + ", " + view + ")");
        }
        Set views = (Set) m_labelViews.get(label);
        if (views == null) {
            views = new HashSet();
            m_labelViews.put(label, views);
        }

        views.add(view);
    }
    
    /**
     * Split a list of space/comma separated labels into a Collection
     *
     * @return the collection of labels (may be empty, nut never null)
     */
    private static final Collection splitLabels(String labels) {
        if (labels == null) {
            return Collections.EMPTY_SET;
        } else {
            return Arrays.asList(StringUtils.split(labels, ", \t\n\r"));
        }
    }
}
