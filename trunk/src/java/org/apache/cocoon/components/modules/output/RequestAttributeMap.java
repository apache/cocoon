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

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

/**
 * Abstraction layer to encapsulate different output
 * destinations. This module outputs to a request attribute
 * java.util.Map object that contains all the attributes that were
 * set. Configuration option &lt;key-prefix&gt; defaults to
 * "org.apache.cocoon.components.modules.output.OutputModule"
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: RequestAttributeMap.java,v 1.1 2003/03/09 00:09:05 pier Exp $
 */
public class RequestAttributeMap extends AbstractOutputModule implements OutputModule {
    
    public final String PREFIX = "org.apache.cocoon.components.modules.output.OutputModule";
    public final String TRANS_PREFIX = "org.apache.cocoon.components.modules.output.OutputModule.RequestAttributeMap.transient";
    
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
        if (getLogger().isDebugEnabled())
            getLogger().debug("setting transient ['"+name+"'] to ['"+value+"']");
        super.transientSetAttribute(objectModel, TRANS_PREFIX, name, value );
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
        if (getLogger().isDebugEnabled())
            getLogger().debug("rolling back");
        super.rollback(objectModel, TRANS_PREFIX);
    }
    
    
    /**
     * Signal that the database transaction completed
     * successfully. See notes on @link{rollback}.
     * */
    public void commit( Configuration modeConf, Map objectModel ) {
        if (getLogger().isDebugEnabled())
            getLogger().debug("prepare commit");
        Map aMap = super.prepareCommit(objectModel,TRANS_PREFIX);
        if (aMap == null) {
            // nothing to do
            return;
        }
        
        String prefix = (String) this.settings.get("key-prefix", PREFIX);
        Request request = ObjectModelHelper.getRequest(objectModel);
        Object temp = request.getAttribute(prefix);
        Map old = null;
        if (temp == null) {
            old = aMap;
        } else {
            old = (Map) temp;
            old.putAll(aMap);
        }
        request.setAttribute(prefix, old);
        if (getLogger().isDebugEnabled())
            getLogger().debug("done commit to ['"+prefix+"']");
    }
    
}
