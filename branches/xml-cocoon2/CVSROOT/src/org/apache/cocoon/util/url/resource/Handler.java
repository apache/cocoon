/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util.url.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.apache.avalon.logger.Loggable;
import org.apache.cocoon.util.ClassUtils;
import org.apache.log.Logger;

/**
 *  This class implements the handler for the resource: URL
 *
 * @author: <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author: <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-04-25 17:09:27 $
 */
public class Handler extends URLStreamHandler implements Loggable {
    private Logger log;

    /**
     * Empty constructor
     */
    public Handler() {
        super();
    }

    public void setLogger(Logger logger) {
        this.log = logger;
    }

    /**
     * Opens the URLConnection
     */
    public URLConnection openConnection(URL url) throws IOException {

        ClassLoader loader = ClassUtils.getClassLoader();

        String file = url.getFile();
        this.log.debug("the resource is here: " + file);

        /* Remove all the forward slashes at the beginning of the filename */
        if (file.charAt(0) == '/') {
            file = file.substring(1);
        }

        URL resource = loader.getResource(file);

        URLConnection connection = null;

        if (resource != null) {
            connection = resource.openConnection();
        } else {
            throw new FileNotFoundException(file);
        }

        return connection;
    }
}
