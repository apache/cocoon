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
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.impl.AbstractJispFilesystemStore;

/**
 * This store is based on the Jisp library
 * (http://www.coyotegulch.com/jisp/index.html). This store uses B-Tree indexes
 * to access variable-length serialized data stored in files.
 *
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: DefaultStore.java,v 1.9 2004/01/28 17:24:22 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=Store
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=persistent-store
 */
public class DefaultStore extends AbstractJispFilesystemStore
    implements Store,
               Contextualizable,
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
