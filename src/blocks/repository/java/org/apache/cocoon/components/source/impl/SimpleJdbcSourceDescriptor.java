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
package org.apache.cocoon.components.source.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.EventAware;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NameValueEvent;
import org.apache.cocoon.components.source.SourceDescriptor;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

/**
 * Simple SourceDescriptor implementation that can stores
 * properties over JDBC.
 * 
 * <p>
 *  The descriptor is to be configured with the name of a datasource that
 *  contains a table with the following scheme:
 *  <p>
 *  <code>
 *    CREATE TABLE SOURCEPROPS(<br>
 *      SOURCE VARCHAR NOT NULL,<br>
 *      NAMESPACE VARCHAR NOT NULL,<br>
 *      NAME VARCHAR NOT NULL,<br>
 *      VALUE VARCHAR NOT NULL,<br>
 *      CONSTRAINT SYS_CT_11 UNIQUE(SOURCE,NAMESPACE,NAME))<br>
 *   </code>
 *  </p>
 * </p>
 * <p>
 *  The implementation will attempt to connect to the EventAware cache in
 *  order to notify it during changes. If it can't find the EventAware cache
 *  sources that are described by this SourceDescriptor will NOT be cacheable.
 * </p>
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $ID$
 */
public class SimpleJdbcSourceDescriptor
extends AbstractConfigurableSourceDescriptor
implements SourceDescriptor, Serviceable, Configurable, Initializable, ThreadSafe {
    
    
    private static final String STMT_SELECT_SINGLE =
        "SELECT value FROM sourceprops WHERE source=? AND namespace=? AND name=?;";
    
    private static final String STMT_SELECT_ALL =
        "SELECT namespace, name, value FROM sourceprops WHERE source=?;";
    
    private static final String STMT_INSERT =
        "INSERT INTO sourceprops (source,namespace,name,value) VALUES (?,?,?,?);";
    
    private static final String STMT_DELETE =
        "DELETE FROM sourceprops WHERE source=? AND namespace=? AND name=?;";
    
    private ServiceManager m_manager;
    private EventAware m_cache;
    private DataSourceComponent m_datasource;
    
    private String m_datasourceName;
    private String m_eventName;
    
    
    // ---------------------------------------------------- Lifecycle
    
    public SimpleJdbcSourceDescriptor() {
    }
    
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
        if (manager.hasService(Cache.ROLE + "/EventAware")) {
            m_cache = (EventAware) manager.lookup(Cache.ROLE + "/EventAware");
        } else {
            getLogger().warn("EventAware cache was not found: sources won't be cacheable.");
        }
    }
    
    /**
     * Configuration options:
     * 
     * <ul>
     *  <li>element <code>property</code> (multiple,required) 
     *      - define a property that this store should handle.</li>
     *  <li>element <code>datasource</code> (single,optional,[cocoondb]) 
     *      - the name of the excalibur datasource to use.</li>
     * </ul>
     */
    public void configure(final Configuration configuration) throws ConfigurationException {
        super.configure(configuration);
        m_datasourceName = configuration.getChild("datasource",true).getValue("cocoondb");
    }
    
    public void initialize() throws Exception {
        ServiceSelector datasources = (ServiceSelector) m_manager.lookup(
            DataSourceComponent.ROLE + "Selector");
        m_datasource = (DataSourceComponent) datasources.select(m_datasourceName);
    }
    
    // ---------------------------------------------------- SourceInspection
    
    public SourceProperty[] getSourceProperties(Source source)
        throws SourceException {

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = m_datasource.getConnection();
            stmt = connection.prepareStatement(STMT_SELECT_ALL);
            stmt.setString(1,source.getURI());
            ResultSet result = stmt.executeQuery();
            List properties = new ArrayList();
            while (result.next()) {
                SourceProperty property = new SourceProperty(
                    result.getString(1),result.getString(2),result.getString(3));
                if (handlesProperty(property.getNamespace(),property.getName())) {
                    properties.add(property);
                }
            }
            result.close();
            stmt.close();
            return (SourceProperty[]) properties.toArray(
                new SourceProperty[properties.size()]);
        } 
        catch (SQLException e) {
            throw new SourceException("Error retrieving properties from database",e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } 
                catch (SQLException e) {}
            }
        }
    }
    
    public SourceProperty doGetSourceProperty(
        Source source,
        String namespace,
        String name)
        throws SourceException {
        
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = m_datasource.getConnection();
            stmt = connection.prepareStatement(STMT_SELECT_SINGLE);
            stmt.setString(1,source.getURI());
            stmt.setString(2,namespace);
            stmt.setString(3,name);
            ResultSet result = stmt.executeQuery();
            SourceProperty property = null;
            if (result.next()) {
                property = new SourceProperty(
                    namespace,
                    name,
                    result.getString(1));
            }
            result.close();
            stmt.close();
            return property;
        } 
        catch (SQLException e) {
            throw new SourceException("Error retrieving property from database",e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } 
                catch (SQLException e) {}
            }
        }
    }
        
    public void doSetSourceProperty(Source source, SourceProperty property)
        throws SourceException {
        
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = m_datasource.getConnection();
            stmt = connection.prepareStatement(STMT_DELETE);
            stmt.setString(1,source.getURI());
            stmt.setString(2,property.getNamespace());
            stmt.setString(3,property.getName());
            int count = stmt.executeUpdate();
            stmt.close();
            
            stmt = connection.prepareStatement(STMT_INSERT);
            stmt.setString(1,source.getURI());
            stmt.setString(2,property.getNamespace());
            stmt.setString(3,property.getName());
            stmt.setString(4,property.getValueAsString());
            
            count += stmt.executeUpdate();
            stmt.close();
            connection.commit();
            
            if (m_cache != null && count > 0) {
                m_cache.processEvent(new NameValueEvent(m_eventName,source.getURI()));
            }
        }
        catch (SQLException e) {
            throw new SourceException("Error setting property",e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    
    public void doRemoveSourceProperty(
        Source source,
        String namespace,
        String name)
        throws SourceException {
        
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = m_datasource.getConnection();
            stmt = connection.prepareStatement(STMT_DELETE);
            stmt.setString(1,source.getURI());
            stmt.setString(2,namespace);
            stmt.setString(3,name);
            
            int count = stmt.executeUpdate();
            stmt.close();
            connection.commit();
            
            if (m_cache != null && count > 0) {
                m_cache.processEvent(new NameValueEvent(m_eventName,source.getURI()));
            }
        }
        catch (SQLException e) {
            throw new SourceException("Error removing propery",e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    
    public SourceValidity getValidity(Source source) {
        if (m_cache != null) {
            return new EventValidity(new NameValueEvent(getEventName(),source.getURI()));
        }
        return null;
    }

    private final String getEventName() {
        if (m_eventName == null) {
            Connection connection = null;
            try {
                connection = m_datasource.getConnection();
                String catalogName = connection.getCatalog();
                m_eventName = (catalogName != null) 
                    ? catalogName + "/sourceprops"
                    : "sourceprops";
            }
            catch (SQLException e) {
                getLogger().warn("Error getting catalog name from jdbc connection.",e);
                m_eventName = "sourceprops";
            }
            finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
        return m_eventName;
    }
}
