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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet;
import org.apache.cocoon.portal.pluto.om.common.Support;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;
import org.apache.pluto.om.servlet.ServletDefinition;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletDefinitionListImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class PortletDefinitionListImpl extends AbstractSupportSet
implements PortletDefinitionList, java.io.Serializable, Support {

    // PortletDefinitionList implementation.

    public PortletDefinition get(ObjectID objectId)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            PortletDefinition portletDefinition = (PortletDefinition)iterator.next();
            if (portletDefinition.getId().equals(objectId)) {
                return portletDefinition;
            }
        }
        return null;
    }

    // Support implementation.
    
    public void preBuild(Object parameter) throws Exception
    {
        Vector structure = (Vector)parameter;
        PortletApplicationDefinition portletApplication = (PortletApplicationDefinition)structure.get(0);
        Map servletMap = (Map)structure.get(1);

        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            PortletDefinition portlet = (PortletDefinition)iterator.next();

            ((Support)portlet).preBuild(portletApplication);

            ServletDefinition servlet = null;
            if (servletMap != null) {
                servlet = (ServletDefinition)servletMap.get(portlet.getId().toString());
            }

            ((Support)portlet).postBuild(servlet);

        }
    }
    

    public void postBuild(Object parameter) throws Exception {
    }

    public void postLoad(Object parameter) throws Exception {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ((PortletDefinitionImpl)iterator.next()).postLoad(parameter);
        }
    }

    public void postStore(Object parameter) throws Exception {
    }

    public void preStore(Object parameter) throws Exception {
    }

    // additional methods.

    public PortletDefinition get(String objectId)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            PortletDefinition portletDefinition = (PortletDefinition)iterator.next();
            if (portletDefinition.getId().toString().equals(objectId)) {
                return portletDefinition;
            }
        }
        return null;
    }

}
