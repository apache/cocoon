/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import java.io.IOException;
import java.io.OutputStream;

import org.xml.sax.SAXException;

import org.apache.cocoon.environment.Environment;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-08-04 21:11:04 $
 */
public interface Processor {
    /**
     * Process the given <code>Environment</code> producing the output
     */
    public boolean process(Environment environment)
    throws Exception;
}
