/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment.commandline;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.Map;

import java.net.MalformedURLException;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.environment.AbstractEnvironment;

import org.apache.log.Logger;
import org.apache.log.LogKit;

/**
 * This environment is used to save the requested file to disk.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-12-30 21:36:46 $
 */

public class FileSavingEnvironment extends AbstractCommandLineEnvironment {

    private Logger log = LogKit.getLoggerFor("cocoon");

    public FileSavingEnvironment(String uri, File context, Map attributes, Map parameters, Map links, OutputStream stream)
    throws MalformedURLException {
        super(uri, null, context, stream);
        log.debug("FileSavingEnvironment: uri=" + uri);
        this.objectModel.put(Cocoon.LINK_OBJECT, links);
        this.objectModel.put(Cocoon.REQUEST_OBJECT, new CommandLineRequest(null, uri, null, attributes, parameters));
        this.objectModel.put(Cocoon.RESPONSE_OBJECT, new CommandLineResponse());
    }
}


