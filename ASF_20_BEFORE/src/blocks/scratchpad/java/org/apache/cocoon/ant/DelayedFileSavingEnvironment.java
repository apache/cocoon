/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: DelayedFileSavingEnvironment.java,v 1.2 2004/03/01 03:50:57 antonio Exp $
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

