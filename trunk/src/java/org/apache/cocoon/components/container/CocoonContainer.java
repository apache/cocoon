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

import org.apache.avalon.fortress.impl.AbstractContainer;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Constants;

import java.util.Map;

/**
 * Customize the Fortress container to handle Cocoon semantics.
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class CocoonContainer extends DefaultContainer
{
    /**
     * Provide some validation for the core Cocoon components
     *
     * @param conf The configuration
     * @throws ConfigurationException if the coniguration is invalid
     */
    public void configure( Configuration conf ) throws ConfigurationException
    {
        if ( !"cocoon".equals( conf.getName() ) ) throw new ConfigurationException( "Invalid configuration format",
                conf );
        String confVersion = conf.getAttribute( "version" );

        if ( !Constants.CONF_VERSION.equals( confVersion ) ) throw new ConfigurationException(
                "Uncompatible configuration format", conf );

        super.configure( conf );
    }

    /**
     * Ensure that we return the latest and greatest component for the role/hint combo if possible.
     * Otherwise default to normal behavior.
     *
     * @param role The role of the component we are looking up.
     * @param hint The hint for the component we are looking up.
     * @return The component for the role/hint combo
     * @throws ServiceException if the role/hint combo cannot be resolved.
     */
    public Object get( final String role, final Object hint ) throws ServiceException
    {
        Object component = null;

        if ( null != hint
             && !AbstractContainer.DEFAULT_ENTRY.equals( hint )
             && !AbstractContainer.SELECTOR_ENTRY.equals( hint ) )
        {
            Map implementations = (Map) m_mapper.get( role );
            if ( null != implementations )
            {
                component = implementations.get( hint );
            }
        }

        if ( null == component )
        {
            component = super.get( role, hint );
        }

        return component;
    }
}
