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
package org.apache.cocoon.kernel.composition;

/**
 * <p>An exception identifying that an error occurred creating or accessing a
 * {@link Wire}.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public class WiringException extends RuntimeException {
    /**
     * <p>Create a new {@link WiringException} instance.</p>
     */
    public WiringException() {
        super();
    }

    /**
     * <p>Create a new {@link WiringException} instance with a
     * specified detail message.</p>
     *
     * @param message The detail message of this exception.
     */
    public WiringException(String message) {
        super(message);
    }
    
    /**
     * <p>Create a new {@link WiringException} instance with a
     * specified detail message and cause.</p>
     *
     * @param message The detail message of this exception.
     * @param cause The cause of this exception.
     */
    public WiringException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * <p>Create a new {@link WiringException} instance with a
     * specified cause.</p>
     *
     * @param cause The cause of this exception.
     */
    public WiringException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}
