
package org.apache.cocoon.components.cprocessor;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.xml.LocationAugmentationPipe;

/**
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public abstract class AbstractNode extends AbstractLogEnabled 
implements Node, Configurable, Serviceable {

    private String m_location;
    protected ServiceManager m_manager;
    
    // ---------------------------------------------------- Lifecycle
    
    public AbstractNode() {
    }
       
    public void configure(final Configuration config) throws ConfigurationException {
        m_location = getConfigLocation(config);
    }
    
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
    }
    
    // ---------------------------------------------------- Node implementation
    
    /**
     * Get the location of this node.
     */
    public final String getLocation() {
        return m_location;
    }
    
    // ---------------------------------------------------- Convenience methods
    
    protected final Object lookup(String role) throws ServiceException {
        return m_manager.lookup(role);
    }
    
    protected final boolean hasService(String role) throws ServiceException {
        return m_manager.hasService(role);
    }
    
    protected final void release(Object component) throws ServiceException {
        m_manager.release(component);
    }
    
    /**
     * Get the location information that is encoded as a location attribute
     * on the current configuration element.
     * 
     * @param config  the configuration element to read the location from.
     * @return  the location if the location attribute exists, else <code>Unknown</code>.
     */
    protected final String getConfigLocation(Configuration config) {
        return config.getAttribute(LocationAugmentationPipe.LOCATION_ATTR,
            LocationAugmentationPipe.UNKNOWN_LOCATION);
    }
    
}
