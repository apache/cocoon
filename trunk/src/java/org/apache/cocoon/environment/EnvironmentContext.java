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
package org.apache.cocoon.environment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;

/**
 * Experimental code for cleaning up the environment handling
 * This is an internal class, and it might change in an incompatible way over time.
 * For developing your own components/applications based on Cocoon, you shouldn't 
 * really need it.
 * 
 * The environment context can store any additional objects for an environment.
 * This is an alternative to using the attributes of an environment and
 * can be used to store internal objects/information wihtout exposing
 * it to clients of the environment object.
 * Each object added to the environment context is disposed when the
 * processing of the environment is finished. If you don't want to
 * dispose an object, use a key that starts with "global:"!
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentContext.java,v 1.4 2004/01/09 08:36:37 cziegeler Exp $
 * @since 2.2
 */
public class EnvironmentContext 
implements Disposable {
    
    /** The corresponding environment */
    protected Environment environment;
    
    /** The attributes */
    protected Map attributes;
    
    /**
     * Constructor
     */
    public EnvironmentContext(Environment environment) {
        this.attributes = new HashMap();
        this.environment = environment;
    }
    
    /**
     * Return the corresponding environment
     * @return The environment
     */
    public Environment getEnvironment() {
        return this.environment;
    }
    
    /**
     * Add an object to the environment.
     * If an object with the same key is already stored, this is overwritten.
     * Each object is disposed when the environment is finished. However,
     * if you add an object with a key that starts with "global:", then
     * the object is not disposed!
     * 
     * @param key   The key for the object
     * @param value The object itself
     */
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
    
    /**
     * Return the object associated with the key
     * @param key The unique key
     * @return The object or null
     */
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }
    
    /**
     * Remove the object associated with the key
     * @param key The unique key
     */
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        final Iterator iter = this.attributes.entrySet().iterator();
        while ( iter.hasNext() ) {
            Map.Entry entry = (Map.Entry)iter.next();
            if ( !((String) entry.getKey()).startsWith("global:") ) {
                ContainerUtil.dispose(entry.getValue());
            }
        }
        this.attributes.clear();
    }

}

