/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.store;

import java.io.File;
import java.io.IOException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;


public class JCSDefaultStore extends JCSTransientStore implements Contextualizable, Parameterizable {

    /** The location of the JCS default properties file */
    private static final String DEFAULT_PROPERTIES = "org/apache/cocoon/components/store/default.ccf";

    /** The context containing the work and the cache directory */
    private Context m_context;

    // ---------------------------------------------------- Lifecycle
    
    public JCSDefaultStore() {
    }

    /**
     * Contextualize the Component
     *
     * @param  context the Context of the Application
     * @exception  ContextException
     */
    public void contextualize(Context context) throws ContextException {
        m_context = context;
    }
    
    /**
     *  TODO: describe options
     * 
     * @param parameters the configuration parameters
     * @exception  ParameterException
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        
        super.parameterize(parameters);
        
        // get the directory to use
        try {
            final File workDir = (File) m_context.get(Constants.CONTEXT_WORK_DIR);
            if (parameters.getParameterAsBoolean("use-cache-directory", false)) {
                final File cacheDir = (File) m_context.get(Constants.CONTEXT_CACHE_DIR);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using cache directory: " + cacheDir);
                }
                setDirectory(cacheDir);
            } else if (parameters.getParameterAsBoolean("use-work-directory", false)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using work directory: " + workDir);
                }
                setDirectory(workDir);
            } else if (parameters.getParameter("directory", null) != null) {
                String dir = parameters.getParameter("directory");
                dir = IOUtils.getContextFilePath(workDir.getPath(), dir);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using directory: " + dir);
                }
                setDirectory(new File(dir));
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using default directory: " + workDir);
                }
                setDirectory(workDir);
            }
        } catch (ContextException ce) {
            throw new ParameterException("Unable to get directory information from context.", ce);
        } catch (IOException e) {
            throw new ParameterException("Unable to set directory", e);
        }
        
    }
    
    protected String getDefaultPropertiesFile() {
        return DEFAULT_PROPERTIES;
    }
    
    /**
     * Sets the disk cache location.
     */
    private void setDirectory(final File directory)
    throws IOException 
    {

        /* Does directory exist? */
        if (!directory.exists()) 
        {
            /* Create it anew */
            if (!directory.mkdirs()) 
            {
                throw new IOException(
                "Error creating store directory '" + directory.getAbsolutePath() + "'. ");
            }
        }

        /* Is given file actually a directory? */
        if (!directory.isDirectory()) 
        {
            throw new IOException("'" + directory.getAbsolutePath() + "' is not a directory");
        }

        /* Is directory readable and writable? */
        if (!(directory.canRead() && directory.canWrite())) 
        {
            throw new IOException(
                "Directory '" + directory.getAbsolutePath() + "' is not readable/writable"
            );
        }
        
        m_properties.setProperty("jcs.auxiliary.DC.attributes.DiskPath",directory.getAbsolutePath());
    }

}
