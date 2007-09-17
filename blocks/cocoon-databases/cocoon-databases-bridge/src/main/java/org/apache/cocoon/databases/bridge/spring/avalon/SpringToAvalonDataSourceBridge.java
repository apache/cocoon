package org.apache.cocoon.databases.bridge.spring.avalon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

/**
 * This class replaces standard Avalon's DataSourceSelector to provide access to database connections
 * defined as Spring beans.
 *  
 * @version $Id$
 */
public class SpringToAvalonDataSourceBridge implements ServiceSelector {
    private ServiceSelector dataSourceSelector;
    private Map springDataSources;

    public boolean isSelectable(Object policy) {
        return dataSourceSelector.isSelectable(policy) || springDataSources.containsKey(policy);
    }

    public void release(Object object) {
        if (object instanceof DataSourceComponent)
            dataSourceSelector.release(object);
    }

    public Object select(Object policy) throws ServiceException {
        if (dataSourceSelector.isSelectable(policy))
            return dataSourceSelector.select(policy);
        else if (springDataSources.containsKey(policy))
            return new SpringDataSourceWrapper((DataSource) springDataSources.get(policy));
        else
            return null;
    }
    
    private class SpringDataSourceWrapper implements DataSourceComponent {
        
        private DataSource springDataSource;
        
        private SpringDataSourceWrapper(DataSource springDataSource) {
            this.springDataSource = springDataSource;
        }

        public Connection getConnection() throws SQLException {
            return springDataSource.getConnection();
        }

        public void configure(Configuration configuration) throws ConfigurationException {
            //do nothing   
        }
        
    }

    public Map getSpringDataSources() {
        return springDataSources;
    }

    public void setSpringDataSources(Map springDataSources) {
        this.springDataSources = springDataSources;
    }

    public ServiceSelector getDataSourceSelector() {
        return dataSourceSelector;
    }

    public void setDataSourceSelector(ServiceSelector dataSourceSelector) {
        this.dataSourceSelector = dataSourceSelector;
    }

}
