/*****************************************************************************
 * Copyright (C) The Apache Software Foundation.  All rights reserved.       *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

/**
 * This interface encapsulates the request parameters needed by the processing
 * chain to generate the response. Even if highly influenced by the Servlet
 * API model, this inteface allows better protocol abstraction which is
 * needed to provide hooks for other type of requests, such as command line and 
 * processing API behavior, 
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $Date: 1999/10/17 16:05:43 $
 * @since 2.0
 */

public interface Request {

}