/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.ant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.commandline.AbstractCommandLineEnvironment;
import org.apache.cocoon.environment.commandline.CommandLineRequest;
import org.apache.cocoon.environment.commandline.CommandLineResponse;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.MIMEUtils;
import org.apache.cocoon.util.NetUtils;

/**
 *   A command line file saving environment writing files in a delayed mode.
 *   File writing is delayed until the content-type is clear, until then
 *   output is written into temporary buffer
 *
 * @author    huber@apache.org
 * @version CVS $Id: DelayedFileSavingEnvironment.java,v 1.3 2004/03/05 10:07:25 bdelacretaz Exp $
 */
public class DelayedFileSavingEnvironment extends AbstractCommandLineEnvironment {

    private DelayedFileOutputStream dfos;
    private UriType uriType;
    private File destDir;


    /**
     * Constructor for the DelayedFileSavingEnvironment object
     * It uses the default view
     *
     * @param  context                    Cocoon's context directory
     * @param  attributes                 Description of Parameter
     * @param  links                      Description of Parameter
     * @param  log                        Logger of this environment
     * @param  parameters                 Description of Parameter
     * @param  uriType                    uri of this environment
     * @param  dfos                       Description of Parameter
     * @exception  MalformedURLException  Description of Exception
     */
    public DelayedFileSavingEnvironment(
            UriType uriType,
            File context,
            Map attributes,
            Map parameters,
            Map links,
            DelayedFileOutputStream dfos,
            Logger log) throws MalformedURLException {
        super(uriType.getDeparameterizedUri(), null, context, dfos, log);

        this.uriType = uriType;
        String deparameterizedUri = uriType.getDeparameterizedUri();

        this.dfos = dfos;
        if (getLogger().isDebugEnabled()) {
            this.getLogger().debug("DelayedFileSavingEnvironment: uri = " + deparameterizedUri);
        }
        this.objectModel.put(Constants.LINK_OBJECT, links);
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, new CommandLineRequest(this, null, deparameterizedUri, null, attributes, parameters));
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT, new CommandLineResponse());
    }


    /**
     * Constructor for the DelayedFileSavingEnvironment object
     * Cocoon's view is explictly set.
     *
     * @param  context                    Cocoon's context directory
     * @param  attributes                 Description of Parameter
     * @param  links                      Description of Parameter
     * @param  log                        Logger of this environment
     * @param  parameters                 Description of Parameter
     * @param  uriType                    uri of this environment
     * @param  view                       Description of Parameter
     * @param  dfos                       Description of Parameter
     * @exception  MalformedURLException  Description of Exception
     */
    public DelayedFileSavingEnvironment(
            UriType uriType,
            String view,
            File context,
            Map attributes,
            Map parameters,
            Map links,
            DelayedFileOutputStream dfos,
            Logger log) throws MalformedURLException {

        super(uriType.getDeparameterizedUri(), view, context, dfos, log);

        this.uriType = uriType;
        String deparameterizedUri = uriType.getDeparameterizedUri();

        this.dfos = dfos;

        if (getLogger().isDebugEnabled()) {
            this.getLogger().debug("DelayedFileSavingEnvironment: uri = " + deparameterizedUri);
        }
        this.objectModel.put(Constants.LINK_OBJECT, links);
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, new CommandLineRequest(this, null, deparameterizedUri, null, attributes, parameters));
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT, new CommandLineResponse());
    }


    /**
     *   Sets the destDir attribute of the DelayedFileSavingEnvironment object
     *
     * @param  destDir   The new destDir value
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }


    /**
     *   Gets the destDir attribute of the DelayedFileSavingEnvironment object
     *
     * @return     The destDir value
     */
    public File getDestDir() {
        return this.destDir;
    }


    /**
     * Commit the response
     *
     * @exception  IOException  Description of Exception
     */
    public void commitResponse()
             throws IOException {

        final File file = getFile();
        if (getLogger().isDebugEnabled()) {
            this.getLogger().debug("DelayedFileSavingEnvironment: filename = " + String.valueOf(file));
        }
        dfos.setFileOutputStream(file);

        uriType.setDestFile(file);

        super.commitResponse();
    }


    /**
     *   Gets the filename attribute of the DelayedFileSavingEnvironment object
     *
     * @return    The filename value
     */
    protected File getFile() {
        // calculate filename
        String filename = uriType.getFilename();
        String type = contentType;
        String ext = uriType.getExtension();
        String defaultExt = MIMEUtils.getDefaultExtension(type);

        if ((ext == null) || (!ext.equals(defaultExt))) {
            filename += defaultExt;
        }
        File file = IOUtils.createFile(destDir, NetUtils.decodePath(filename));
        return file;
    }

}

