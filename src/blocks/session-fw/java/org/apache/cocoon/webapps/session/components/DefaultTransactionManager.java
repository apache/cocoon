/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version CVS $Id: DefaultTransactionManager.java,v 1.4 2004/03/05 13:02:22 bdelacretaz Exp $
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
