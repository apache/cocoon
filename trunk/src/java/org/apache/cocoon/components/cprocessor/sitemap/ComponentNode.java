/*
 * Created on Dec 23, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.cocoon.components.cprocessor.sitemap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.util.StringUtils;

/**
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * 
 * @avalon.component
 * @avalon.service type=ComponentNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=component-node
 */
public class ComponentNode implements Configurable {
    
    public static final String ROLE = ComponentNode.class.getName();
    
    private Collection m_labels;
    private String m_idRef;
    private String m_mimeType;
    
    public ComponentNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        m_idRef = config.getChild("component").getAttribute("id-ref");
        Collection labels = splitLabels(config.getAttribute("label",null));
        m_labels = Collections.unmodifiableCollection(labels);
        m_mimeType = config.getAttribute("mime-type",null);
    }
    
    public Collection getLabels() {
        return m_labels;
    }
    
    public String getIdRef() {
        return m_idRef;
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
