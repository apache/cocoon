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
package org.apache.cocoon.portal.aspect.impl;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.aspect.AspectDescription;



/**
 * A configured aspect
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultAspectDescription.java,v 1.5 2003/07/03 11:36:09 cziegeler Exp $
 */
public class DefaultAspectDescription 
    implements AspectDescription {

    protected String name;
    
    protected String className;
    
    protected String persistence;

    protected boolean autoCreate;
    
    protected String defaultValue;
    
    /**
     * Create a new description from a {@link Configuration} object.
     * All values must be stored as attributes
     */
    public static AspectDescription newInstance(Configuration conf)
    throws ConfigurationException {
        DefaultAspectDescription adesc = new DefaultAspectDescription();
        adesc.setClassName(conf.getAttribute("class"));
        adesc.setName(conf.getAttribute("name"));
        adesc.setPersistence(conf.getAttribute("store"));
        adesc.setAutoCreate(conf.getAttributeAsBoolean("auto-create", false));
        adesc.setDefaultValue(conf.getAttribute("value", null));
        
        return adesc;
    }
    
    /**
     * @return The class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return The configred name
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setClassName(String string) {
        className = string;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @return The role of the store
     */
    public String getStoreName() {
        return persistence;
    }

    /**
     * @param string
     */
    public void setPersistence(String string) {
        persistence = string;
    }

    /**
     * If the data is not available, create it automatically (or not)
     */
    public boolean isAutoCreate() {
        return autoCreate;
    }

    /**
     * Set auto create
     */
    public void setAutoCreate(boolean b) {
        autoCreate = b;
    }

    /**
     * Default value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }
    
    public String toString() {
        return ("AspectDescription name=" + this.name + 
                 ", class=" + this.className +
                 ", persistence=" + this.persistence +
                 ", autoCreate=" + this.autoCreate +
                 ", defaultValue=" + this.defaultValue);
    }
}
