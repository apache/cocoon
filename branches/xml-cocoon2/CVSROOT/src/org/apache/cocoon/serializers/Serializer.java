/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serializers;

import java.io.*;
import org.apache.cocoon.*;

/**
 * The Serializer interface outlines the methods that must be implemented
 * in order to provide XML serialization capabilities. Serialization
 * is used here with the general design patter definition, indicating the
 * ability to encode a memory representation into an ordered octet sequence.
 *
 * While the Java Serialization mechanism uses the same design idea, the 
 * implementation is different and the two things must not be confused. This
 * interface should be implemented by those classes willing to give a way
 * to write XML as serial files (such as XML textual syntax, HTML, PDF, JPG
 * or others)
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:53 $
 * @since 2.0
 */

public interface Serializer extends EventListener {

	/**
	 * This method makes available to this class all the required information
	 * following an improved Servlet model that better separates contexts.
	 * 
	 * @param request The request wrapper
	 * @param request The response wrapper
	 * @param request The context wrapper
	 */    
    void set(Request request, Response response, Context context);

    /**
     * Set the OutputStream used by this Formatter for writing data.
     * <br>
     * (NOTE (Pier) An OutputStream, rather than a Writer is used since the
     * formatter can output binary data, and such data may be corrupted in
     * case a Writer is used, during character localization).
     *
     * It is the implementation responsibility to wrap the output stream
     * with a write that uses the required char->bytes encoder.
     * 
     * @param out The OutputStream used for serializing.
     */
    void setOutputStream(OutputStream out);
}