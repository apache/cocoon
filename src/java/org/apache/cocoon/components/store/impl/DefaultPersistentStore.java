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

import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.IndexedObjectDatabase;
import com.coyotegulch.jisp.KeyNotFound;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;
import org.apache.excalibur.store.impl.AbstractJispFilesystemStore;

/**
 * This store is based on the Jisp library
 * (http://www.coyotegulch.com/jisp/index.html). This store uses B-Tree indexes
 * to access variable-length serialized data stored in files.
 *
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @deprecated Use the JCS store instead
 * @version CVS $Id: DefaultPersistentStore.java,v 1.3 2004/05/24 07:05:17 cziegeler Exp $
 */
public class DefaultPersistentStore extends AbstractJispFilesystemStore
    implements org.apache.excalibur.store.Store,
               Contextualizable,
               ThreadSafe,
               Parameterizable,
               Disposable {

    /** The context containing the work and the cache directory */
    protected Context context;
    
    /**
     * Contextualize the Component
     *
     * @param  context the Context of the Application
     * @exception  ContextException
     */
    public void contextualize(final Context context) throws ContextException {
        this.context = context;
    }

    /**
     *  Configure the Component.<br>
     *  A few options can be used
     *  <UL>
     *    <LI> datafile = the name of the data file (Default: cocoon.dat)
     *    </LI>
     *    <LI> m_indexFile = the name of the index file (Default: cocoon.idx)
     *    </LI>
     *    <LI> order = The page size of the B-Tree</LI>
     *  </UL>
     *
     * @param params the configuration paramters
     * @exception  ParameterException
     */
    public void parameterize(Parameters params) throws ParameterException {
        
        // get the directory to use
        try {
            final File workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
            if (params.getParameterAsBoolean("use-cache-directory", false)) {
                final File cacheDir = (File)context.get(Constants.CONTEXT_CACHE_DIR);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using cache directory: " + cacheDir);
                }
                this.setDirectory(cacheDir);
            } else if (params.getParameterAsBoolean("use-work-directory", false)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using work directory: " + workDir);
                }
                this.setDirectory(workDir);
            } else if (params.getParameter("directory", null) != null) {
                String dir = params.getParameter("directory");
                dir = IOUtils.getContextFilePath(workDir.getPath(), dir);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using directory: " + dir);
                }
                this.setDirectory(new File(dir));
            } else {
                try {
                    // Default
                    this.setDirectory(workDir);
                } catch (IOException e) {
                    // Ignored
                }
            }
        } catch (ContextException ce) {
            throw new ParameterException("Unable to get directory information from context.", ce);
        } catch (IOException e) {
            throw new ParameterException("Unable to set directory", e);
        }

        // get store configuration
        final String databaseName = params.getParameter("datafile", "cocoon.dat");
        final String indexName = params.getParameter("m_indexFile", "cocoon.idx");
        final int order = params.getParameterAsInteger("order", 301);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Database file name = " + databaseName);
            getLogger().debug("Index file name = " + indexName);
            getLogger().debug("Order=" + order);
        }

        // open index and dat file
        final File databaseFile = new File(m_directoryFile, databaseName);
        final File indexFile = new File(m_directoryFile, indexName);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Initializing JispFilesystemStore");
        }
        try {
            final boolean databaseExists = databaseFile.exists();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Datafile exists: " + databaseExists);
            }
            super.m_Database = new IndexedObjectDatabase(databaseFile.toString(),
                                                         !databaseExists);

            if (!databaseExists) {
                // Create new index
                super.m_Index = new BTreeIndex(indexFile.toString(),
                                               order, super.getNullKey(), false);
            } else {
                // Open existing index
                super.m_Index = new BTreeIndex(indexFile.toString());
            }
            super.m_Database.attachIndex(super.m_Index);
        } catch (KeyNotFound ignore) {
        } catch (Exception e) {
            getLogger().error("Exception during initialization of jisp store.", e);
        }
    }

    public void dispose() {
        try {
            getLogger().debug("Disposing");

            if (super.m_Index != null) {
                super.m_Index.close();
            }

            if (super.m_Database != null) {
                super.m_Database.close();
            }
        } catch (Exception e) {
            getLogger().error("dispose(..) Exception", e);
        }
    }
}
