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
package org.apache.cocoon.ojb.odmg.components;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.ojb.odmg.OJB;
import org.odmg.Database;
import org.odmg.Implementation;
import org.odmg.ODMGException;


/**
 * Implementation of the OdmgImplementation. Create a ODMG Implementation and store it for future use
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: OdmgImplementationImpl.java,v 1.1 2004/01/27 06:15:14 giacomo Exp $
 */
public class OdmgImplementationImpl
    extends AbstractLogEnabled
    implements OdmgImplementation, Configurable, Initializable, Disposable, ThreadSafe {

    private final static String DEFAULT_CONNECTION ="default";
    private final static int DEFAULT_MODE = Database.OPEN_READ_WRITE;
    private Implementation odmg;
    
    private Hashtable databases = new Hashtable();
    
    /*  (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration myconf)
        throws ConfigurationException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("OJB-ODMG: configuration");
        }
    }

    /*  (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        synchronized(this.databases) {
            final Set keys = this.databases.keySet();
            for( Iterator i = keys.iterator(); i.hasNext(); )
            {
                final Database db = (Database)i.next();
                try
                {
                    db.close();
                }
                catch( final ODMGException e) {
                    getLogger().error( "OJB-ODMG: Cannot close Database", e);
                }
                i.remove();
            }
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("OJB-ODMG: Disposed OK!");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
        throws Exception {
        try {
            // Get the Implementation
            this.odmg = OJB.getInstance();
            
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("OJB-ODMG: Started OK!");
            }
        } catch (Throwable t) {
            if (this.getLogger().isFatalErrorEnabled()) {
                this.getLogger().fatalError("OJB-ODMG: Started failed: Cannot get an ODMG Implementation.", t);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.Odmg#getInstance()
     */
    public Implementation getInstance() throws ODMGException {
        Database db = (Database)this.databases.get( DEFAULT_CONNECTION );
        if(null == db ) {
            db = this.odmg.newDatabase();
            db.open(DEFAULT_CONNECTION, DEFAULT_MODE);
            synchronized (this.databases) {
                this.databases.put( DEFAULT_CONNECTION + DEFAULT_MODE, db );
            }
        }
        return this.odmg;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.Odmg#getInstance(java.lang.String, int)
     */
    public Implementation getInstance(String connection, int mode) throws ODMGException {
        Database db = (Database)this.databases.get( connection + mode);
        if(null == db ) {
            db = this.odmg.newDatabase();
            db.open(connection, mode);
            synchronized (this.databases) {
                this.databases.put( connection + mode, db );
            }
        }
        return this.odmg;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.Odmg#getInstance(java.lang.String)
     */
    public Implementation getInstance(String connection) throws ODMGException {
        Database db = (Database)this.databases.get( connection + DEFAULT_MODE);
        if(null == db ) {
            db = this.odmg.newDatabase();
            db.open(connection, DEFAULT_MODE);
            synchronized (this.databases) {
                this.databases.put( connection + DEFAULT_MODE, db );
            }
        }
        return this.odmg;
    }
    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.OdmgImplementation#getInstance(int)
     */
    public Implementation getInstance( int mode ) throws ODMGException 
    {
        Database db = (Database)this.databases.get( DEFAULT_CONNECTION+ mode);
        if(null == db ) {
            db = this.odmg.newDatabase();
            db.open(DEFAULT_CONNECTION, mode);
            synchronized (this.databases) {
                this.databases.put( DEFAULT_CONNECTION+ mode, db );
            }
        }
        return this.odmg;
    }
}
