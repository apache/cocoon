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
package org.apache.cocoon.components.store.impl;

import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.impl.AbstractFilesystemStore;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;
import java.io.File;
import java.io.IOException;

/**
 * Stores objects on the filesystem: String objects as text files,
 * all other objects are serialized.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: FilesystemStore.java,v 1.3 2003/12/26 18:43:39 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type="Store"
 * @avalon.lifestyle type="singleton"
 */
public final class FilesystemStore
extends AbstractFilesystemStore
implements Store, Contextualizable, Parameterizable {

    protected File workDir;
    protected File cacheDir;

    public void contextualize(final Context context)
    throws ContextException {
        this.workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
        this.cacheDir = (File)context.get(Constants.CONTEXT_CACHE_DIR);
    }

    public void parameterize(Parameters params)
    throws ParameterException {
        try {
            if (params.getParameterAsBoolean("use-cache-directory", false)) {
                if (this.getLogger().isDebugEnabled())
                    getLogger().debug("Using cache directory: " + cacheDir);
                setDirectory(cacheDir);
            } else if (params.getParameterAsBoolean("use-work-directory", false)) {
                if (this.getLogger().isDebugEnabled())
                    getLogger().debug("Using work directory: " + workDir);
                setDirectory(workDir);
            } else if (params.getParameter("directory", null) != null) {
                String dir = params.getParameter("directory");
                dir = IOUtils.getContextFilePath(workDir.getPath(), dir);
                if (this.getLogger().isDebugEnabled())
                    getLogger().debug("Using directory: " + dir);
                setDirectory(new File(dir));
            } else {
                try {
                    // Legacy: use working directory by default
                    setDirectory(workDir);
                } catch (IOException e) {
                    // Legacy: Always was ignored
                }
            }
        } catch (IOException e) {
            throw new ParameterException("Unable to set directory", e);
        }
    }
}
