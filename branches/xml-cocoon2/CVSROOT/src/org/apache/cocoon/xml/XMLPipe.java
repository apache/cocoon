/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.xml;

/**
 * This interface glues together an XML producer and consumer to create a 
 * SAX pipe.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-09-27 14:34:01 $
 */

public interface XMLPipe extends XMLConsumer, XMLProducer {}
