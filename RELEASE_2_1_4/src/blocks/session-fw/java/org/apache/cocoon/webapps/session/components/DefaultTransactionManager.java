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
package org.apache.cocoon.webapps.session.components;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.session.TransactionManager;
import org.apache.cocoon.webapps.session.context.SessionContext;

/**
 * This is the default implementation for the transaction manager.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultTransactionManager.java,v 1.3 2003/09/24 21:22:33 cziegeler Exp $
*/
public final class DefaultTransactionManager
extends AbstractLogEnabled
implements Component, ThreadSafe, TransactionManager, Contextualizable {

    protected Context context;

    /**
     * Get the transaction states from the current session
     */
    private TransactionState getSessionContextsTransactionState(SessionContext context) {
        final Request request = ContextHelper.getRequest(this.context);
        final Session session = request.getSession(true);
        Map transactionStates = (Map)session.getAttribute(this.getClass().getName());
        if (transactionStates == null) {
            transactionStates = new HashMap(5, 3);
            session.setAttribute(this.getClass().getName(), transactionStates);
        }
        TransactionState state = (TransactionState)transactionStates.get(context);
        if ( state == null ) {
            state = new TransactionState();
            transactionStates.put(context, state);
        }
        return state;
    }

    private class TransactionState {
        /** number readers reading*/
        public int nr=0;
        /** number of readers total (reading or waiting to read)*/
        public int nrtotal=0;
        /** number writers writing, 0 or 1 */
        public int nw=0;
        /** number of writers total (writing or waiting to write)*/
        public int nwtotal=0;
    }

    /**
     *  Reset the transaction management state.
     */
    public void resetTransactions(SessionContext context) {
        TransactionState ts = this.getSessionContextsTransactionState(context);
        ts.nr=0;
        ts.nrtotal=0;
        ts.nw=0;
        ts.nwtotal=0;
    }

    /**
     *  Start a reading transaction.
     *  This call must always be matched with a stopReadingTransaction().
     *  Otherwise the session context is blocked.
     */
    public synchronized void startReadingTransaction(SessionContext context)
    throws ProcessingException {
        TransactionState ts = this.getSessionContextsTransactionState(context);
        ts.nrtotal++;
        while (ts.nw!=0) {
            try {
                wait();
            } catch (InterruptedException local) {
                throw new ProcessingException("Interrupted", local);
            }
        }
        ts.nr++;
    }

    /**
     *  Stop a reading transaction.
     *  This call must always be done for each startReadingTransaction().
     *  Otherwise the session context is blocked.
     */
    public synchronized void stopReadingTransaction(SessionContext context) {
        TransactionState ts = this.getSessionContextsTransactionState(context);
        ts.nr--;
        ts.nrtotal--;
        if (ts.nrtotal==0) notify();
    }

    /**
     *  Start a writing transaction.
     *  This call must always be matched with a stopWritingTransaction().
     *  Otherwise the session context is blocked.
     */
     public synchronized void startWritingTransaction(SessionContext context)
     throws ProcessingException {
         TransactionState ts = this.getSessionContextsTransactionState(context);
         ts.nwtotal++;
         while (ts.nrtotal+ts.nw != 0) {
            try {
                wait();
            } catch (InterruptedException local) {
                throw new ProcessingException("Interrupted", local);
            }
        }
        ts.nw=1;
     }

    /**
     *  Stop a writing transaction.
     *  This call must always be done for each startWritingTransaction().
     *  Otherwise the session context is blocked.
     */
    public synchronized void stopWritingTransaction(SessionContext context) {
        TransactionState ts = this.getSessionContextsTransactionState(context);
        ts.nw=0;
        ts.nwtotal--;
        notifyAll();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

}
