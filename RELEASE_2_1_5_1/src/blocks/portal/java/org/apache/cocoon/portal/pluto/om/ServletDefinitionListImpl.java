/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.om;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.ServletDefinitionListCtrl;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet;
import org.apache.cocoon.portal.pluto.om.common.Support;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ServletDefinitionListImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class ServletDefinitionListImpl extends AbstractSupportSet
implements ServletDefinitionList, ServletDefinitionListCtrl, java.io.Serializable, Support {

    // ServletDefinitionList implementation.

    public ServletDefinition get(String name)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ServletDefinition servletDefinition = (ServletDefinition)iterator.next();
            if (servletDefinition.getServletName().equals(name)) {
                return servletDefinition;
            }
        }
        return null;
    }

    // ServletDefinitionListCtrl implementation.

    public ServletDefinition add(String name, String className)
    {
        ServletDefinitionImpl servletDefinition = new ServletDefinitionImpl();
        servletDefinition.setServletName(name);
        servletDefinition.setServletClass(className);

        super.add(servletDefinition);

        return servletDefinition;
    }

    public ServletDefinition remove(String name)
    {
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

    public void remove(ServletDefinition servletDefinition)
    {
        super.remove(servletDefinition);
    }

    // Support implementation.

    public void preBuild(Object parameter) throws Exception
    {
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

            if (servlet.getInitParameterSet() != null) {
                if (servlet.getInitParameterSet().get("portlet-guid") != null) {
                    String guid = servlet.getInitParameterSet().get("portlet-guid").getValue();
                    servletMap.put(guid, servlet);

                    ServletMapping servletMapping = (ServletMapping)mappings.get(servlet.getServletName());
                    if (mappings==null) {
                        throw new ProcessingException("No corresponding servlet mapping found for servlet name '"+servlet.getServletName()+"'");
                    }
                    ((Support)servlet).postBuild(servletMapping);

                }
            }
        }

    }

    public void postBuild(Object parameter) throws Exception {
    }

    public void postLoad(Object parameter) throws Exception {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ((ServletDefinitionImpl)iterator.next()).postLoad(parameter);
        }

    }

    public void postStore(Object parameter) throws Exception {
    }

    public void preStore(Object parameter) throws Exception {
    }
    
}
