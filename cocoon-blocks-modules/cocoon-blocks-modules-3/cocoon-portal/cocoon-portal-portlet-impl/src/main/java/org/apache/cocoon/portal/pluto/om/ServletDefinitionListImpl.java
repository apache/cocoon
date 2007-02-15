/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.pluto.om;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.ServletDefinitionListCtrl;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet;
import org.apache.cocoon.portal.pluto.om.common.Support;

/**
 *
 * @version $Id$
 */
public class ServletDefinitionListImpl extends AbstractSupportSet
implements ServletDefinitionList, ServletDefinitionListCtrl, java.io.Serializable, Support {

    /**
     * @see org.apache.pluto.om.servlet.ServletDefinitionList#get(java.lang.String)
     */
    public ServletDefinition get(String name) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ServletDefinition servletDefinition = (ServletDefinition)iterator.next();
            if (servletDefinition.getServletName().equals(name)) {
                return servletDefinition;
            }
        }
        return null;
    }

    /**
     * @see org.apache.pluto.om.servlet.ServletDefinitionListCtrl#add(java.lang.String, java.lang.String)
     */
    public ServletDefinition add(String name, String className) {
        ServletDefinitionImpl servletDefinition = new ServletDefinitionImpl();
        servletDefinition.setServletName(name);
        servletDefinition.setServletClass(className);

        super.add(servletDefinition);

        return servletDefinition;
    }

    /**
     * @see org.apache.pluto.om.servlet.ServletDefinitionListCtrl#remove(java.lang.String)
     */
    public ServletDefinition remove(String name) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ServletDefinition servletDefinition = (ServletDefinition)iterator.next();
            if (servletDefinition.getServletName().equals(name)) {
                super.remove(servletDefinition);
                return servletDefinition;
            }
        }
        return null;
    }

    /**
     * @see org.apache.pluto.om.servlet.ServletDefinitionListCtrl#remove(org.apache.pluto.om.servlet.ServletDefinition)
     */
    public void remove(ServletDefinition servletDefinition) {
        super.remove(servletDefinition);
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet#preBuild(java.lang.Object)
     */
    public void preBuild(Object parameter) throws Exception {
        Vector structure = (Vector)parameter;
        WebApplicationDefinition webApplicationDefinition =  (WebApplicationDefinition)structure.get(0);
        Collection servletMappings = (Collection)structure.get(1);
        HashMap servletMap = (HashMap)structure.get(2);

        // build internal hashtable to cross link mappings with servlets
        HashMap mappings = new HashMap(servletMappings.size());
        Iterator iterator = servletMappings.iterator();
        while (iterator.hasNext()) {
            ServletMapping servletMapping = (ServletMapping)iterator.next();
            mappings.put(servletMapping.getServletName(),servletMapping);
        }
        // update servlets
        iterator = this.iterator();
        while (iterator.hasNext()) {
            ServletDefinition servlet = (ServletDefinition)iterator.next();
            ((Support)servlet).preBuild(webApplicationDefinition);

            servletMap.put(servlet.getServletName(), servlet);
            ServletMapping servletMapping = (ServletMapping)mappings.get(servlet.getServletName());
            if ( servletMapping != null) {
                ((Support)servlet).postBuild(servletMapping);
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet#postBuild(java.lang.Object)
     */
    public void postBuild(Object parameter) throws Exception {
        // nothing to do 
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet#postLoad(java.lang.Object)
     */
    public void postLoad(Object parameter) throws Exception {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ((ServletDefinitionImpl)iterator.next()).postLoad(parameter);
        }

    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet#postStore(java.lang.Object)
     */
    public void postStore(Object parameter) throws Exception {
        // nothing to do 
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet#preStore(java.lang.Object)
     */
    public void preStore(Object parameter) throws Exception {
        // nothing to do 
    }
    
}
