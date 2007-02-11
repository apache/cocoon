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
package org.apache.cocoon.portal.tools.transformation;

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.portal.tools.PortalToolCatalogue;
import org.apache.cocoon.portal.tools.PortalToolManager;
import org.apache.cocoon.transformation.I18nTransformer;

/**
 * 
 * @version CVS $Id$
 */
public class PortalToolsI18nTransformer extends I18nTransformer {
    
    public static String ROLE = PortalToolsI18nTransformer.class.getName();
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) throws ConfigurationException {
        /*
        <catalogues default="portalTools">
          <catalogue id="portalTools" name="portalTools" location="cocoon:/i18n"/>
        </catalogues>
        */
        
        if (conf.getChild("catalogues").getAttribute("new", "no").equals("no")) {
            super.configure(conf);
            return;
        }
        DefaultConfiguration root = new DefaultConfiguration("root");
        DefaultConfiguration defconf = new DefaultConfiguration("catalogues");
        defconf.setAttribute("default", "default");
        root.addChild(defconf);
        PortalToolManager ptm = null;
        try {
            ptm = (PortalToolManager) this.manager.lookup(PortalToolManager.ROLE);
            List i18nc = ptm.getI18n();
            for(Iterator it = i18nc.iterator(); it.hasNext();) {
                PortalToolCatalogue ptc = (PortalToolCatalogue) it.next();
                DefaultConfiguration catConf = new DefaultConfiguration("catalogue");
	            catConf.setAttribute("id", ptc.getId());
	            catConf.setAttribute("name", ptc.getName());
                catConf.setAttribute("location", ptc.getLocation());
                defconf.addChild(catConf);
            }
            super.configure(root);
        } catch (ServiceException e) {
           e.printStackTrace();
        } catch (ConfigurationException e) {
           e.printStackTrace();
        } finally {
            this.manager.release(ptm);            
        }
    }

}
