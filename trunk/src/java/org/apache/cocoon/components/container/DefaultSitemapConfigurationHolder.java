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
package org.apache.cocoon.components.container;

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.environment.EnvironmentHelper;

/**
 * This is the implementation for the sitemap configuration holder that implements
 * the the sitemap component configurations
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultSitemapConfigurationHolder.java,v 1.1 2004/01/07 15:57:30 cziegeler Exp $
 */
public final class DefaultSitemapConfigurationHolder
    implements SitemapConfigurationHolder {

    /** The role of the sitemap component */
    private String role;

    /** The prepared configurations indexed by the ChainedConfiguration */
    private Map preparedConfigurations;

    public DefaultSitemapConfigurationHolder(String role) {
        this.role = role;
    }

    /**
     * @see SitemapConfigurationHolder#getConfiguration()
     */
    public ChainedConfiguration getConfiguration() {
        Map confs = EnvironmentHelper.getCurrentProcessor().getComponentConfigurations();
        return (ChainedConfiguration) (confs == null ? null : confs.get(this.role));
    }

    /**
     * @see SitemapConfigurationHolder#getPreparedConfiguration()
     */
    public Object getPreparedConfiguration() {
        if ( null != this.preparedConfigurations ) {
            ChainedConfiguration conf = this.getConfiguration();
            if ( null != conf ) {
                return this.preparedConfigurations.get( conf );
            }
        }
        return null;
    }

    /**
     * @see SitemapConfigurationHolder#setPreparedConfiguration(ChainedConfiguration, java.lang.Object)
     */
    public void setPreparedConfiguration(ChainedConfiguration configuration,
                                          Object preparedConfig) {
        if ( null == this.preparedConfigurations ) {
            this.preparedConfigurations = new HashMap(5);
        }
        this.preparedConfigurations.put(configuration, preparedConfig);
    }

}
