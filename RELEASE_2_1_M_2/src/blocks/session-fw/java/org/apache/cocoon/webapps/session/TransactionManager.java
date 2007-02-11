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
 * @version CVS $Id: TransactionManager.java,v 1.1 2003/05/04 20:19:41 cziegeler Exp $
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
