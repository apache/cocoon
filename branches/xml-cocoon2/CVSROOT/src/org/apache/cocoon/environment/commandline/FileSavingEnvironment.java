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

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.AbstractEnvironment;

/**
 * This environment is used to save the requested file to disk.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-02-15 20:29:23 $
 */

public class FileSavingEnvironment extends AbstractCommandLineEnvironment {

    public FileSavingEnvironment(String uri, File context, Map attributes, Map parameters, Map links, OutputStream stream)
    throws MalformedURLException {
        super(uri, null, context, stream);
        getLogger().debug("FileSavingEnvironment: uri=" + uri);
        this.objectModel.put(Constants.LINK_OBJECT, links);
        this.objectModel.put(Constants.REQUEST_OBJECT, new CommandLineRequest(null, uri, null, attributes, parameters));
        this.objectModel.put(Constants.RESPONSE_OBJECT, new CommandLineResponse());
    }
}


