/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment.commandline;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.net.MalformedURLException;

import org.apache.cocoon.Main;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.AbstractEnvironment;


/**
 * This environment is sample the links of the resource.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-02-15 20:29:25 $
 */

public class LinkSamplingEnvironment extends AbstractCommandLineEnvironment {

    private boolean skip = false;

    public LinkSamplingEnvironment(String uri, File contextFile, Map attributes, Map parameters)
    throws MalformedURLException, IOException {
        super(uri, Constants.LINK_VIEW, contextFile, new ByteArrayOutputStream());
        getLogger().debug("LinkSamplingEnvironment: uri=" + uri);
        this.objectModel.put(Constants.REQUEST_OBJECT, new CommandLineRequest(null, uri, null, attributes, parameters));
        this.objectModel.put(Constants.RESPONSE_OBJECT, new CommandLineResponse());
    }

    /**
     * Set the ContentType
     */
    public void setContentType(String contentType) {
        if (Constants.LINK_CONTENT_TYPE.equals(contentType) == false) {
            this.skip = true;
        }
    }

    /**
     * Indicates if other links are present.
     */
    public Collection getLinks() throws IOException {
        ArrayList list = new ArrayList();
        if (!skip) {
            BufferedReader buffer = new BufferedReader(
                new InputStreamReader(
                    new ByteArrayInputStream(
                        ((ByteArrayOutputStream) super.stream).toByteArray()
                    )
                )
            );
            while (true) {
                String line = buffer.readLine();
                if (line == null) break;
                list.add(line);
            }
        }
        return list;
    }
}
