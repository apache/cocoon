/*
 
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 
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

package org.apache.cocoon.components.modules.output;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;

/**
 * Abstraction layer to encapsulate different output
 * destinations. Configuration option &lt;key-prefix&gt; defaults to
 * "org.apache.cocoon.components.modules.output.OutputModule"+":"
 *
 * Can be used with different isolation-level: default is "0" being
 * no isolation at all, values are immediately visible but are removed
 * on a rollback; "1" keeps the values at a save place until either
 * rollback or commit is called. Then values are either discarded or
 * copied to the final destination.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SessionAttributeOutputModule.java,v 1.1 2003/03/09 00:09:05 pier Exp $
 */
public class SessionAttributeOutputModule extends AbstractOutputModule implements OutputModule {
    
    public final String PREFIX = "org.apache.cocoon.components.modules.output.OutputModule";
    public final String TRANS_PREFIX = "org.apache.cocoon.components.modules.output.OutputModule.SessionAttributeOutputModule.transient";
    public final String ROLLBACK_LIST = "org.apache.cocoon.components.modules.output.OutputModule.SessionAttributeOutputModule.rollback";
    
    /**
     * communicate an attribute value to further processing logic.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param request The request object
     * @param name The attribute's label, consisting of "table.column"
     * or "table.column[index]" in case of multiple attributes of the
     * same spec.
     * @param value The attriute's value.
     * */
    public void setAttribute( Configuration modeConf, Map objectModel, String name, Object value ) {
        if (this.settings.get("isolation-level","0").equals("1")) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("setting transient ['"+name+"'] to ['"+value+"']");
            this.transientSetAttribute(objectModel, TRANS_PREFIX, name, value);
        } else {
            // use read uncommitted isolation level

            Session session = ObjectModelHelper.getRequest(objectModel).getSession();

            name = getName(name);

            if (!this.attributeExists(objectModel, ROLLBACK_LIST, name)) {
                Object tmp = session.getAttribute(name);
                this.transientSetAttribute(objectModel, ROLLBACK_LIST, name, tmp);
            }

            if (getLogger().isDebugEnabled())
                getLogger().debug("setting ['"+name+"'] to ['"+value+"']");
            session.setAttribute(name, value);
        }

    }
    
    
    
    /**
     * If a database transaction needs to rollback, this is called to
     * inform the further processing logic about this fact. All
     * already set attribute values are invalidated. <em>This is difficult
     * because only the request object can be used to synchronize this
     * and build some kind of transaction object. Beaware that sending
     * your data straight to some beans or other entities could result
     * in data corruption!</em>
     * */
    public void rollback( Configuration modeConf, Map objectModel, Exception e ) {
        if (this.settings.get("isolation-level","0").equals("1")) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("rolling back");
            this.rollback(objectModel, TRANS_PREFIX);
        } else {

            if (getLogger().isDebugEnabled())
                getLogger().debug("start rolling back");
            
            Session session = ObjectModelHelper.getRequest(objectModel).getSession();
            Object tmp = this.prepareCommit(objectModel,ROLLBACK_LIST);
            if (tmp != null) {
                Map rollbackList = (Map) tmp;
                Iterator iter = rollbackList.keySet().iterator();
                while(iter.hasNext()) {
                    String key = (String) iter.next();
                    Object val = rollbackList.get(key);
                    if (val != null) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("rolling back ['"+key+"'] to ['"+val+"']");
                        session.setAttribute(key, val);
                    } else {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("rolling back ['"+key+"']");
                        session.removeAttribute(key);
                    }
                }
            }
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("done rolling back");

        String prefix = (String) this.settings.get("key-prefix", PREFIX );
        if (prefix!="") {
            ObjectModelHelper.getRequest(objectModel).getSession().setAttribute(prefix+":",e.getMessage());
        } else {
            ObjectModelHelper.getRequest(objectModel).getSession().setAttribute("errorMessage",e.getMessage());
        }
    }
    
    
    /**
     * Signal that the database transaction completed
     * successfully. See notes on @link{rollback}.
     * */
    public void commit( Configuration modeConf, Map objectModel ) {
        if (this.settings.get("isolation-level","0").equals("1")) {

            Logger logger = getLogger();
            if (logger.isDebugEnabled())
                logger.debug("prepare commit");

            Map aMap = this.prepareCommit(objectModel, TRANS_PREFIX);
            if (aMap == null) {
                return;
            }
            
            Iterator iter = aMap.keySet().iterator();
            if (!iter.hasNext()){
                return;
            }
            
            String prefix = (String) this.settings.get("key-prefix", PREFIX );
            if (prefix != "") {
                prefix = prefix+":";
            } else {
                prefix = null;
            }
            Session session = ObjectModelHelper.getRequest(objectModel).getSession();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object value = aMap.get(key);
                if (prefix != null) { key = prefix + key; }
                if (logger.isDebugEnabled())
                    logger.debug("committing ['"+key+"'] to ['"+value+"']");
                session.setAttribute(key, value);
            }

            if (logger.isDebugEnabled())
                logger.debug("done commit");

        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("commit");
            this.prepareCommit(objectModel, ROLLBACK_LIST);
        }
        
    }
    
    protected String getName( String name ) {
        String prefix = (String) this.settings.get("key-prefix", PREFIX );
        return (prefix == "" ? name : prefix+":"+name);
    }

}
