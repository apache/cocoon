/* 
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.flow;


import org.apache.cocoon.util.jmx.ModelMBeanImpl;

import java.util.Date;
import java.util.Iterator;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

/**
 * The ContinuationsManagerImplMBean adds JMX managability for ContinuationsManagerImpl.
 *
 * @version $Id: ThreadSafeComponentHandler.java 312637 2005-10-10 13:00:42Z cziegeler $
 * @since 2.2
 */
public class ContinuationsManagerImplMBean
extends ModelMBeanImpl {
    
    private final ContinuationsManagerImpl manager;
    
    protected void defineManagedResource() {
        super.defineManagedResource();
        defineAttribute("defaultTimeToLive", false, true);
        defineAttribute("bindContinuationsToSession", false, true);
        defineAttribute("expirationCheckInterval", false, true);
        defineAttribute("expirationSet", false, true);
    }
    /**
     * Construction of PoolableComponentHandlerMBean
     *
     * @param manager The managed ContinuationsManager instance
     */
    public ContinuationsManagerImplMBean(final ContinuationsManagerImpl manager)
        throws MBeanException, InstanceNotFoundException {
        super( manager );
        this.manager = manager;
    }
    
    public int getDefaultTimeToLive() {
        return manager.defaultTimeToLive;
    }
    
    public void setDefaultTimeToLive(final int ttl) {
        manager.defaultTimeToLive = ttl;
    }
    
    public boolean getBindContinuationsToSession() {
        return manager.bindContinuationsToSession;
    }
    
    public long getExpirationCheckInterval() {
        return manager.expirationCheckInterval;
    }
    
    public String[] getExpirationSet() {
        final String [] lines = new String[ manager.expirations.size()];
        int idx = 0;
        for(final Iterator i = manager.expirations.iterator(); i.hasNext(); ) {
            final StringBuffer wkSet = new StringBuffer();
            final WebContinuation wk = (WebContinuation) i.next();
            final long lat = wk.getLastAccessTime() + wk.getTimeToLive();
            wkSet.append("WK: ")
                    .append(wk.getId())
                    .append(" ExpireTime [");

            if (lat < System.currentTimeMillis()) {
                wkSet.append("Expired");
            } else {
                final Date date = new Date();
                date.setTime(lat);
                wkSet.append(date);
            }
            wkSet.append("]");
            lines[idx++] = wkSet.toString();
        }
        return lines;
    }
    
}
