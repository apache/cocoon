/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;
import org.springframework.util.StringUtils;

/**
 * This is a simple component that uses a {@link  ConfigurationInfo} to create
 * a Spring like configuration xml document.
 *
 * TODO: LogManager?
 * TODO: configure(Core)
 * TODO: register aliases for shorthands
 * @since 2.2
 * @version $Id$
 */
public class XmlConfigCreator {

    protected static final String XMLHEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    protected static final String DOCTYPE =
        "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">\n";

    public String createConfig(Map components) 
    throws Exception {
        final List pooledRoles = new ArrayList();
        final StringBuffer buffer = new StringBuffer();
        buffer.append(XMLHEADER);
        buffer.append(DOCTYPE);
        buffer.append("<beans>\n");

        // start with "static" components: ServiceManager
        buffer.append("<bean");
        this.appendAttribute(buffer, "id", ServiceManager.class.getName());
        this.appendAttribute(buffer, "class", AvalonServiceManager.class.getName());
        buffer.append("/>\n");

        final Iterator i = components.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry entry = (Map.Entry)i.next();
            final ComponentInfo current = (ComponentInfo)entry.getValue();
            final String role = current.getRole();
    
            String className = current.getServiceClassName();
            boolean isSelector = false;
            boolean singleton = true;
            boolean poolable = false;
            // Test for Selector - we skip them
            if ( className.equals("org.apache.cocoon.core.container.DefaultServiceSelector")
                 || className.equals("org.apache.cocoon.components.treeprocessor.sitemap.ComponentsSelector") ) {
                // Add selector
                className = AvalonServiceSelector.class.getName();
                isSelector = true;
            } else {
                if ( current.getModel() == ComponentInfo.MODEL_NON_THREAD_SAFE_POOLED 
                    || current.getModel() == ComponentInfo.MODEL_POOLED ) {
                    poolable = true;
                    singleton = false;
                } else if ( current.getModel() != ComponentInfo.MODEL_SINGLETON ) {
                    singleton = false;
                }
            }
            buffer.append("<bean");
            if ( !poolable ) {
                this.appendAttribute(buffer, "name", this.xml(role));
            } else {
                this.appendAttribute(buffer, "name", this.xml(role + "Pooled"));                
            }
            this.appendAttribute(buffer, "class", className);
            this.appendAttribute(buffer, "init-method", current.getInitMethodName());
            this.appendAttribute(buffer, "destroy-method", current.getDestroyMethodName());
            this.appendAttribute(buffer, "singleton", String.valueOf(singleton));
            if ( !isSelector ) {
                buffer.append("/>\n");
            } else {
                buffer.append(">\n");
                buffer.append("  <constructor-arg ref=\"");
                buffer.append(ServiceManager.class.getName());
                buffer.append("\"/>\n");
                buffer.append("  <constructor-arg type=\"java.lang.String\"><value>");
                buffer.append(role.substring(0, role.length()-8));
                buffer.append("</value></constructor-arg>\n");
                if ( current.getConfiguration() != null
                     && current.getConfiguration().getAttribute("default", null) != null ) {
                    buffer.append("  <property name=\"default\"><value>");
                    buffer.append(current.getConfiguration().getAttribute("default"));
                    buffer.append("</value></property>\n");
                }
                buffer.append("</bean>\n");
            }
            if ( poolable ) {
                // add the factory for poolables
                buffer.append("<bean");
                this.appendAttribute(buffer, "name", this.xml(role));
                this.appendAttribute(buffer, "class", PoolableFactoryBean.class.getName());
                this.appendAttribute(buffer, "singleton", "true");
                this.appendAttribute(buffer, "init-method", "initialize");
                this.appendAttribute(buffer, "destroy-method", "dispose");
                buffer.append(">\n");
                buffer.append("  <constructor-arg type=\"java.lang.String\"><value>");
                buffer.append(this.xml(role) + "Pooled");
                buffer.append("</value></constructor-arg>\n");
                buffer.append("  <constructor-arg type=\"java.lang.String\"><value>");
                buffer.append(className);
                buffer.append("</value></constructor-arg>\n");
                if ( current.getConfiguration() != null ) {
                    final String poolMax = current.getConfiguration().getAttribute("pool-max", null);
                    if ( poolMax != null ) {
                        buffer.append("  <constructor-arg><value>");
                        buffer.append(poolMax);
                        buffer.append("</value></constructor-arg>\n");
                    }
                }
                if ( current.getPoolInMethodName() != null ) {
                    buffer.append("  <property name=\"poolInMethodName\"><value>");
                    buffer.append(current.getPoolInMethodName());
                    buffer.append("</value></property>\n");
                }
                if ( current.getPoolOutMethodName() != null ) {
                    buffer.append("<property name=\"poolOutMethodName\"><value>");
                    buffer.append(current.getPoolOutMethodName());
                    buffer.append("</property>\n");
                }
                buffer.append("</bean>\n");
                pooledRoles.add(role);
            }
        }
        buffer.append("</beans>\n");

        // now change roles for pooled components (from {role} to {role}Pooled
        final Iterator prI = pooledRoles.iterator();
        while ( prI.hasNext() ) {
            final String role = (String)prI.next();
            final Object info = components.remove(role);
            components.put(role + "Pooled", info);
        }
        return buffer.toString();
    }

    protected String xml(String value) {
        value = StringUtils.replace(value, "&", "&amp;");
        value = StringUtils.replace(value, "<", "&lt;");
        value = StringUtils.replace(value, ">", "&gt;");
        return value;
    }

    protected void appendAttribute(StringBuffer buffer, String attr, String value) {
        if ( value != null ) {
            buffer.append(' ');
            buffer.append(attr);
            buffer.append("=\"");
            buffer.append(value);
            buffer.append("\"");
        }
    }
}
