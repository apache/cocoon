/* ========================================================================== *
 *                                                                            *
 * Copyright 2004 The Apache Software Foundation.                             *
 *                                                                            *
 * Licensed  under the Apache License,  Version 2.0 (the "License");  you may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at                                                     *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless  required  by  applicable law or  agreed  to in  writing,  software *
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           *
 *                                                                            *
 * See  the  License for  the  specific language  governing  permissions  and *
 * limitations under the License.                                             *
 *                                                                            *
 * ========================================================================== */
package org.apache.cocoon.kernel.identification;

/**
 * <p>An exception identifying a block identification error.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class IdentificationException extends Exception {

    /**
     * <p>Create a new {@link IdentificationException} instance.</p>
     */
    public IdentificationException() {
        super();
    }

    /**
     * <p>Create a new {@link IdentificationException} instance with a
     * specified detail message.</p>
     *
     * @param message The detail message of this exception.
     */
    public IdentificationException(String message) {
        super(message);
    }
    
    /**
     * <p>Create a new {@link IdentificationException} instance with a
     * specified detail message and cause.</p>
     *
     * @param message The detail message of this exception.
     * @param cause The cause of this exception.
     */
    public IdentificationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * <p>Create a new {@link IdentificationException} instance with a
     * specified cause.</p>
     *
     * @param cause The cause of this exception.
     */
    public IdentificationException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}
