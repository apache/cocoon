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
package org.apache.cocoon.ojb.broker.components;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ojb.components.AbstractOjbImpl;
import org.apache.ojb.broker.PBFactoryException;
import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;

/**
* Implementation of the JdoPMF. Create one PMF and store it for future use
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: PBFactoryImpl.java,v 1.1 2004/02/22 04:44:18 antonio Exp $
*/
public class PBFactoryImpl extends AbstractOjbImpl implements PBFactory, Configurable, Initializable,
Disposable, ThreadSafe
{
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration myconf) throws ConfigurationException
    {
		if (this.getLogger().isDebugEnabled())
			this.getLogger().debug("OJB-JDO: configuration");
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose()
    {
        super.dispose();
		if (this.getLogger().isDebugEnabled())
			this.getLogger().debug("OJB-PB: Disposed OK!");
    }

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Initializable#initialize()
	 */
	public void initialize() throws Exception
	{
        super.initialize();
		try
		{
			if (this.getLogger().isDebugEnabled()) {
				this.getLogger().debug("OJB-PB: Started OK!");
            }
		}
		catch (Throwable t)
		{
			if (this.getLogger().isFatalErrorEnabled()) {
				this.getLogger().fatalError("OJB-PB: Started failed: Cannot create a Persistence Broker Factory.",t);
			}
		}
	}

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.broker.components.PBFactory#defaultPersistenceBroker()
     */
    public PersistenceBroker defaultPersistenceBroker() throws PBFactoryException {
        return PersistenceBrokerFactory.defaultPersistenceBroker();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.broker.components.PBFactory#createPersistenceBroker(java.lang.String, java.lang.String, java.lang.String)
     */
    public PersistenceBroker createPersistenceBroker(String jcdAlias, String user, String password) throws PBFactoryException {
        return PersistenceBrokerFactory.createPersistenceBroker(jcdAlias, user, password);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.broker.components.PBFactory#createPersistenceBroker(org.apache.ojb.broker.PBKey)
     */
    public PersistenceBroker createPersistenceBroker(PBKey key) throws PBFactoryException {
        return PersistenceBrokerFactory.createPersistenceBroker(key);
    }
}
