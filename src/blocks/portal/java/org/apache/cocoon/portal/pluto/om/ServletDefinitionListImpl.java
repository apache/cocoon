/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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
 * @version CVS $Id: ServletDefinitionListImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
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
