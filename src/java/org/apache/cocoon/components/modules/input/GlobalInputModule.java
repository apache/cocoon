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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Iterator;
import java.util.Map;

/**
 * This simple module allows to define global parameters in a sitemap. The
 * values are inherited from one sitemap to its sub sitemaps and can be
 * extended there.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: GlobalInputModule.java,v 1.3 2004/02/19 22:13:28 joerg Exp $
 */
public final class GlobalInputModule 
    extends AbstractLogEnabled
    implements InputModule, Serviceable, ThreadSafe {

    private ServiceManager manager;
    
    /**
     * Serviceable
     */
    public void service(ServiceManager manager) {
        this.manager = manager;
    }
    
    /**
     * Standard access to an attribute's value. If more than one value
     * exists, the first is returned. If the value does not exist,
     * null is returned. To get all values, use
     * {@link #getAttributeValues(String, Configuration, Map)} or
     * {@link #getAttributeNames(Configuration, Map)} and
     * {@link #getAttribute(String, Configuration, Map)} to get them one by one.
     * @param name a String that specifies what the caller thinks
     * would identify an attribute. This is mainly a fallback if no
     * modeConf is present.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        SitemapVariableHolder holder = null;
        try {
            holder = (SitemapVariableHolder)this.manager.lookup(SitemapVariableHolder.ROLE);
            return holder.get(name); 
        } catch (ServiceException ce) {
            throw new ConfigurationException("Unable to lookup SitemapVariableHolder.", ce);
        } finally {
            this.manager.release(holder);
        }
    }


    /**
     * Returns an Iterator of String objects containing the names
     * of the attributes available. If no attributes are available,
     * the method returns an empty Iterator.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        SitemapVariableHolder holder = null;
        try {
            holder = (SitemapVariableHolder)this.manager.lookup(SitemapVariableHolder.ROLE);
            return holder.getKeys(); 
        } catch (ServiceException ce) {
            throw new ConfigurationException("Unable to lookup SitemapVariableHolder.", ce);
        } finally {
            this.manager.release(holder);
        }
    }


    /**
     * Returns an array of String objects containing all of the values
     * the given attribute has, or null if the attribute does not
     * exist. As an alternative,
     * {@link #getAttributeNames(Configuration, Map)} together with
     * {@link #getAttribute(String, Configuration, Map)} can be used to get the
     * values one by one.
     * @param name a String that specifies what the caller thinks
     * would identify an attributes. This is mainly a fallback
     * if no modeConf is present.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        Object o = this.getAttribute(name, modeConf, objectModel);
        if (o != null) {
            return new Object[] {o};
        }
        return null;
    }

}

