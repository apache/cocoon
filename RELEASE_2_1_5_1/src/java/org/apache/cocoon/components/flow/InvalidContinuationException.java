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
package org.apache.cocoon.components.flow;

import org.apache.cocoon.ProcessingException;

/**
 * This Exception is thrown whenever an invalid continuation is given.
 *
 * @author <a href="mailto:tcollen@neuagency.com">Tony Collen</a>
 * @version CVS $Id: InvalidContinuationException.java,v 1.2 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public class InvalidContinuationException extends ProcessingException {

    /**
     * Construct a new <code>InvalidContinuationException</code> instance.
     */
    public InvalidContinuationException(String message) {
        super(message, null);
    }

    /**
     * Construct a new <code>InvalidContinuationException</code> that references
     * a parent Exception.
     */
    public InvalidContinuationException(String message, Throwable t) {
        super(message, t);
    }
}
