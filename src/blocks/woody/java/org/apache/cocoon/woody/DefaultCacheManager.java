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
package org.apache.cocoon.woody;

import java.io.IOException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.collections.FastHashMap;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;

/**
 * Component implementing the {@link CacheManager} role.
 * 
 * @version $Id: DefaultCacheManager.java,v 1.3 2004/02/11 09:53:44 antonio Exp $
 */
public class DefaultCacheManager 
  extends AbstractLogEnabled 
  implements CacheManager, ThreadSafe, Serviceable, Disposable, Configurable, Component {
      
    protected ServiceManager manager;
    protected Configuration configuration;
    protected FastHashMap cache = new FastHashMap();

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
    }

    /**
     * Configurable
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    public Object get(Source source, String prefix) {
        String key = prefix + source.getURI();
        SourceValidity newValidity = source.getValidity();

        if (newValidity == null) {
            cache.remove(key);
            return null;
        }

        Object[] objectAndValidity = (Object[])cache.get(key);
        if (objectAndValidity == null)
            return null;

        SourceValidity storedValidity = (SourceValidity)objectAndValidity[1];
        int valid = storedValidity.isValid();
        boolean isValid;
        if (valid == 0) {
            valid = storedValidity.isValid(newValidity);
            isValid = (valid == 1);
        } else {
            isValid = (valid == 1);
        }

        if (!isValid) {
            cache.remove(key);
            return null;
        }

        return objectAndValidity[0];
    }

    public void set(Object object, Source source, String prefix) throws IOException {
        String key = prefix + source.getURI();
        SourceValidity validity = source.getValidity();
        if (validity != null) {
            Object[] objectAndValidity = {object,  validity};
            cache.put(key, objectAndValidity);
        }
    }

    /**
     * Disposable
     */
    public void dispose() {
        this.manager = null;
        this.cache = null;
    }
}
