/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;

/**
 * This chain holds all configured renderer aspects for one renderer.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: RendererAspectChain.java,v 1.5 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public final class RendererAspectChain {
    
    protected List aspects = new ArrayList(3);
    
    protected List configs = new ArrayList(3);
    
    protected List aspectDescriptions = new ArrayList(2);
    
    public void configure(ServiceSelector selector, Configuration conf) 
    throws ConfigurationException {
        if ( conf != null ) {
            Configuration[] aspects = conf.getChildren("aspect");
            if ( aspects != null ) {
                for(int i=0; i < aspects.length; i++) {
                    final Configuration current = aspects[i];
                    final String role = current.getAttribute("type");
                    try {
                        RendererAspect rAspect = (RendererAspect) selector.select(role);
                        this.aspects.add(rAspect);               
                        Parameters aspectConfiguration = Parameters.fromConfiguration(current);
                        Object compiledConf = rAspect.prepareConfiguration(aspectConfiguration);
                        this.configs.add(compiledConf);
                        
                        Iterator descriptionIterator = rAspect.getAspectDescriptions(compiledConf);
                        if ( descriptionIterator != null ) {
                            while ( descriptionIterator.hasNext() ) {
                                this.aspectDescriptions.add( descriptionIterator.next() );
                            }
                        }
                    } catch (ParameterException pe) {
                        throw new ConfigurationException("Unable to configure renderer aspect " + role, pe);
                    } catch (ServiceException se) {
                        throw new ConfigurationException("Unable to lookup aspect " + role, se);
                    }
                }
            }
        } else {
            throw new ConfigurationException("No aspects configured");
        }
    }
    
    public Iterator getIterator() {
        return this.aspects.iterator();
    }
    
    public Iterator getConfigIterator() {
        return this.configs.iterator();
    }
    
    public Iterator getAspectDescriptionIterator() {
        return this.aspectDescriptions.iterator();
    }
    
    public void dispose(ServiceSelector selector) {
        Iterator i = this.aspects.iterator();
        while (i.hasNext()) {
            selector.release(i.next()); 
        }
        this.aspects.clear();
    }
}
