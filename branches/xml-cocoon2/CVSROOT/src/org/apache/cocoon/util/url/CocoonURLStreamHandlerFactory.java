/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import org.apache.avalon.logger.AbstractLoggable;
import org.apache.avalon.logger.Loggable;
import org.apache.cocoon.util.ClassUtils;

/**
 *  This class implements the Factory for URLStreamHandlers
 *
 * @author: <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-04-25 17:09:21 $
 */
public class CocoonURLStreamHandlerFactory extends AbstractLoggable implements URLStreamHandlerFactory {

    /** Nothing done here */
    public CocoonURLStreamHandlerFactory() {}

    /** Try to use our protocol handlers */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        getLogger().info("Finding class for org.apache.cocoon.util.url." + protocol + ".Handler");

        try {
            URLStreamHandler handler =
                (URLStreamHandler) ClassUtils.newInstance("org.apache.cocoon.util.url." +
                                                          protocol + ".Handler");

            ((Loggable) handler).setLogger(getLogger());

            return handler;
        } catch (Exception e) {
            getLogger().warn("Could not load a new instance of the '" + protocol + "' handler", e);
        }

        return null;
    }
}
