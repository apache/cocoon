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

import java.io.File;
import java.io.IOException;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.IOUtils;
import org.apache.excalibur.store.impl.AbstractFilesystemStore;

/**
 * Stores objects on the filesystem: String objects as text files,
 * all other objects are serialized.
 *
 * @version $Id$
 */
public final class FilesystemStore
    extends AbstractFilesystemStore
    implements Serviceable, Parameterizable {

    protected File workDir;
    protected File cacheDir;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        final Settings settings = (Settings)manager.lookup(Settings.ROLE);
        this.workDir = new File(settings.getWorkDirectory());
        this.cacheDir = new File(settings.getCacheDirectory());
        manager.release(settings);
    }

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
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
