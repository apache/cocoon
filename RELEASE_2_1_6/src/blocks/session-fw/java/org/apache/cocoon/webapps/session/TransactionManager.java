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
package org.apache.cocoon.webapps.session;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.session.context.SessionContext;

/**
 *  Transaction management<p>
 *  </p>
 *  Transactions are a series of get/set calls to a session context which must
 *  be seen as atomic (single modification).
 *  We distingish between reading and writing. Usually parallel reading is
 *  allowed but if one thread wants to write, no other can read or write.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: TransactionManager.java,v 1.2 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public interface TransactionManager {

    /** Avalon role */
    String ROLE = TransactionManager.class.getName();;


    /**
     *  Reset the transaction management state.
     */
    void resetTransactions(SessionContext context);

    /**
     *  Start a reading transaction.
     *  This call must always be matched with a stopReadingTransaction().
     *  Otherwise the session context is blocked.
     */
    void startReadingTransaction(SessionContext context)
    throws ProcessingException;

    /**
     *  Stop a reading transaction.
     *  This call must always be done for each startReadingTransaction().
     *  Otherwise the session context is blocked.
     */
    void stopReadingTransaction(SessionContext context);

    /**
     *  Start a writing transaction.
     *  This call must always be matched with a stopWritingTransaction().
     *  Otherwise the session context is blocked.
     */
    void startWritingTransaction(SessionContext context)
    throws ProcessingException;

    /**
     *  Stop a writing transaction.
     *  This call must always be done for each startWritingTransaction().
     *  Otherwise the session context is blocked.
     */
    void stopWritingTransaction(SessionContext context);

}
