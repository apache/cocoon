/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import org.xml.sax.SAXException;

/**
 * The processing exception signals an error during content chain processing.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:48 $
 * @version 2.0
 */
 
public class ProcessingException extends SAXException {

    /**
     * Create a new ProcessorException.
     *
     * @param message The error or warning message.
     */
    public ProcessingException (String message) {
        super(message);
    }
    
    /**
     * Create a new ProcessingException wrapping an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, and its message will become the default message for
     * the ProcessingException.</p>
     *
     * @param e The exception to be wrapped in a ProcessingException.
     */
    public ProcessingException (Exception e) {
        super(e);
    }
    
    /**
     * Create a new ProcessingException from an existing exception with
     * the given message.
     *
     * <p>The existing exception will be embedded in the new
     * one, but the new exception will have its own message.</p>
     *
     * @param message The detail message.
     * @param e The exception to be wrapped in a ProcessingException.
     */
    public ProcessingException (String message, Exception e) {
        super(message, e);
    }    
}