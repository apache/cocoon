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
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-07-27 21:48:31 $
 */
public interface Processor {
    /**
     * Process the given <code>Environment</code> producing the output to the
     * specified <code>OutputStream</code>.
     */
    public boolean process(Environment environment, OutputStream out)
    throws Exception;
}
