/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util.url.context;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.servlet.ServletContext;

/**
 *  This class implements the handler for the context: URL
 *
 * @author: <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author: <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-01-31 15:03:59 $
 */
public class Handler extends URLStreamHandler {

    /** The ServletContext */
    private static ServletContext context;

    /**
     * Sets the ServletContext to pull files from
     */
    public static void setContext(ServletContext context) {
        if (Handler.context == null) {
            Handler.context = context;
        }
    }

    /**
     * empty constructor
     */
    public Handler() {
        super();
    }

    /**
     * Open the connection
     */
    public URLConnection openConnection(URL url) throws IOException {

        String file = url.getFile();

        /* Remove all the forward slashes at the beginning of the filename */
        if (file.charAt(0) == '/') {
            file = file.substring(1);
        }

        URL resource = Handler.context.getResource(file);

        URLConnection connection = null;

        if (resource != null) {
            connection = resource.openConnection();
        } else {
            throw new FileNotFoundException(file);
        }

        return connection;
    }

}