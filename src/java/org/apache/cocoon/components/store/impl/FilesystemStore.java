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
package org.apache.cocoon.components.store.impl;

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
 * @version CVS $Id: FilesystemStore.java,v 1.2 2004/03/05 13:02:51 bdelacretaz Exp $
 */
public final class FilesystemStore
extends AbstractFilesystemStore
implements Contextualizable, Parameterizable {

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
