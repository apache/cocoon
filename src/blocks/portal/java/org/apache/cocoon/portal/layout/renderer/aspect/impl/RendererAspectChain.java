/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;

/**
 * This chain holds all configured renderer aspects for one renderer.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: RendererAspectChain.java,v 1.3 2003/06/15 16:56:09 cziegeler Exp $
 */
public final class RendererAspectChain {
    
    protected List aspects = new ArrayList(3);
    
    protected List configs = new ArrayList(3);
    
    protected List aspectDescriptions = new ArrayList(2);
    
    public void configure(ComponentSelector selector, Configuration conf) 
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
                    } catch (ComponentException se) {
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
    
    public void dispose(ComponentSelector selector) {
        Iterator i = this.aspects.iterator();
        while (i.hasNext()) {
            selector.release((Component)i.next()); 
        }
        this.aspects.clear();
    }
}
