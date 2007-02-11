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
package org.apache.cocoon.components.store;

import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.BTreeObjectIterator;
import com.coyotegulch.jisp.IndexedObjectDatabase;
import com.coyotegulch.jisp.KeyNotFound;
import com.coyotegulch.jisp.KeyObject;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

/**
 * This store is based on the Jisp library
 * (http://www.coyotegulch.com/jisp/index.html). This store uses B-Tree indexes
 * to access variable-length serialized data stored in files.
 *
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: JispFilesystemStore.java,v 1.2 2003/04/27 15:16:15 cziegeler Exp $
 */
public final class JispFilesystemStore extends AbstractLogEnabled
    implements org.apache.excalibur.store.Store,
               Contextualizable,
               ThreadSafe,
               Initializable,
               Parameterizable {

    protected File m_workDir;
    protected File m_cacheDir;

    /**
     *  The directory repository
     */
    protected File m_directoryFile;
    protected volatile String m_directoryPath;

    /**
     *  The database
     */
    private File m_databaseFile;
    private File m_indexFile;

    private int m_Order;
    private IndexedObjectDatabase m_Database;
    private BTreeIndex m_Index;

    /**
     *  Sets the repository's location
     *
     * @param directory the new directory value
     * @exception  IOException
     */
    public void setDirectory(final String directory)
        throws IOException {
        this.setDirectory(new File(directory));
    }

    /**
     *  Sets the repository's location
     *
     * @param directory the new directory value
     * @exception  IOException
     */

    public void setDirectory(final File directory)
        throws IOException {
        this.m_directoryFile = directory;

        /* Save directory path prefix */
        this.m_directoryPath = IOUtils.getFullFilename(this.m_directoryFile);
        this.m_directoryPath += File.separator;

        if (!this.m_directoryFile.exists()) {
            /* Create it new */
            if (!this.m_directoryFile.mkdir()) {
                throw new IOException("Error creating store directory '" +
                                      this.m_directoryPath + "'");
            }
        }

        /* Is given file actually a directory? */
        if (!this.m_directoryFile.isDirectory()) {
            throw new IOException("'" + this.m_directoryPath + "' is not a directory");
        }

        /* Is directory readable and writable? */
        if (!(this.m_directoryFile.canRead() && this.m_directoryFile.canWrite())) {
            throw new IOException("Directory '" + this.m_directoryPath +
                                  "' is not readable/writable");
        }
    }

    /**
     * Contextualize the Component
     *
     * @param  context the Context of the Application
     * @exception  ContextException
     */
    public void contextualize(final Context context) throws ContextException {
        this.m_workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
        this.m_cacheDir = (File)context.get(Constants.CONTEXT_CACHE_DIR);
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

        try {
            if (params.getParameterAsBoolean("use-cache-directory", false)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using cache directory: " + m_cacheDir);
                }
                setDirectory(m_cacheDir);
            } else if (params.getParameterAsBoolean("use-work-directory", false)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using work directory: " + m_workDir);
                }
                setDirectory(m_workDir);
            } else if (params.getParameter("directory", null) != null) {
                String dir = params.getParameter("directory");
                dir = IOUtils.getContextFilePath(m_workDir.getPath(), dir);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using directory: " + dir);
                }
                setDirectory(new File(dir));
            } else {
                try {
                    // Default
                    setDirectory(m_workDir);
                } catch (IOException e) {
                    // Ignored
                }
            }
        } catch (IOException e) {
            throw new ParameterException("Unable to set directory", e);
        }

        String databaseName = params.getParameter("datafile", "cocoon.dat");
        String indexName = params.getParameter("m_indexFile", "cocoon.idx");
        m_Order = params.getParameterAsInteger("order", 301);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Database file name = " + databaseName);
            getLogger().debug("Index file name = " + indexName);
            getLogger().debug("Order=" + m_Order);
        }

        m_databaseFile = new File(m_directoryFile, databaseName);
        m_indexFile = new File(m_directoryFile, indexName);
    }

    /**
     * Initialize the Component
     */
    public void initialize() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("initialize() JispFilesystemStore");
        }

        try {
            if (m_databaseFile.exists()) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("initialize(): Datafile exists");
                }
                m_Database = new IndexedObjectDatabase(m_databaseFile.toString(), false);
                m_Index = new BTreeIndex(m_indexFile.toString());
                m_Database.attachIndex(m_Index);
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("initialize(): Datafile does not exist");
                }
                m_Database = new IndexedObjectDatabase(m_databaseFile.toString(), false);
                m_Index = new BTreeIndex(m_indexFile.toString(),
                                        m_Order, new JispStringKey(), false);
                m_Database.attachIndex(m_Index);
            }
        } catch (KeyNotFound ignore) {
        } catch (Exception e) {
            getLogger().error("initialize(..) Exception", e);
        }
    }

    /**
     * Returns the repository's full pathname
     *
     * @return the directory as String
     */
    public String getDirectoryPath() {
        return this.m_directoryPath;
    }

    /**
     * Returns a Object from the store associated with the Key Object
     *
     * @param key the Key object
     * @return the Object associated with Key Object
     */
    public synchronized Object get(Object key) {
        Object value = null;
        try {
            value = m_Database.read(wrapKeyObject(key), m_Index);
            if (getLogger().isDebugEnabled()) {
                if (value != null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Found key: " + key);
                    }
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("NOT Found key: " + key);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("get(..): Exception", e);
        }
        return value;
    }

    /**
     *  Store the given object in the indexed data file.
     *
     * @param key the key object
     * @param value the value object
     * @exception  IOException
     */
    public synchronized void store(Object key, Object value)
        throws IOException {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("store(): Store file with key: "
                                  + key.toString());
            getLogger().debug("store(): Store file with value: "
                                  + value.toString());
        }

        if (value instanceof Serializable) {
            try {
                KeyObject[] keyArray = new KeyObject[1];
                keyArray[0] = wrapKeyObject(key);
                m_Database.write(keyArray, (Serializable) value);
            } catch (Exception e) {
                getLogger().error("store(..): Exception", e);
            }
        } else {
            throw new IOException("Object not Serializable");
        }
    }

    /**
     *  Holds the given object in the indexed data file.
     *
     * @param key the key object
     * @param value the value object
     * @exception IOException
     */
    public synchronized void hold(Object key, Object value)
        throws IOException {
        this.store(key, value);
    }

    /**
     * Frees some values of the data file.<br>
     */
    public synchronized void free() {
       // implementation is missing
    }

    /**
     * Clear the Store of all elements
     */
    public synchronized void clear() {
        BTreeObjectEnumeration enum = new BTreeObjectEnumeration(m_Database.createIterator(m_Index),this);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("clear(): Clearing the database ");
        }
        
        while(enum.hasMoreElements()) {
            Object tmp = enum.nextElement();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("clear(): Removing key: " + tmp.toString());
            }
            this.remove(tmp);
        }
    }

    /**
     * Removes a value from the data file with the given key.
     *
     * @param key the key object
     */
    public synchronized void remove(Object key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("remove(..) Remove item");
        }

        try {
            KeyObject[] keyArray = new KeyObject[1];
            keyArray[0] = wrapKeyObject(key);
            m_Database.remove(keyArray);
        } catch (KeyNotFound ignore) {
        } catch (Exception e) {
            getLogger().error("remove(..): Exception", e);
        }
    }

    /**
     *  Test if the the index file contains the given key
     *
     * @param key the key object
     * @return true if Key exists and false if not
     */
    public synchronized boolean containsKey(Object key) {
        long res = -1;

        try {
            res = m_Index.findKey(wrapKeyObject(key));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("containsKey(..): res=" + res);
            }
        } catch (KeyNotFound ignore) {
        } catch (Exception e) {
            getLogger().error("containsKey(..): Exception", e);
        }

        if (res > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a Enumeration of all Keys in the indexed file.<br>
     *
     * @return  Enumeration Object with all existing keys
     */
    public Enumeration keys() {
        BTreeObjectEnumeration enum = new BTreeObjectEnumeration(m_Database.createIterator(m_Index),this);
        return enum;
    }

    public int size() {
        int cnt = 0;

        BTreeObjectEnumeration enum = new BTreeObjectEnumeration(m_Database.createIterator(m_Index),this);

        while(enum.hasMoreElements()) {
            cnt++;
        }
        return cnt;
    }

    /**
     * This method wraps around the key Object a Jisp KeyObject.
     *
     * @param key the key object
     * @return the wrapped key object
     */
    private KeyObject wrapKeyObject(Object key) {
        return new JispStringKey(String.valueOf(key));
    }

    class BTreeObjectEnumeration implements Enumeration {
        private BTreeObjectIterator m_Iterator;
        private JispFilesystemStore m_Store;

        public BTreeObjectEnumeration(BTreeObjectIterator iterator, JispFilesystemStore store) {
            m_Iterator = iterator;
            m_Store = store;
        }

        public boolean hasMoreElements() {
            boolean hasMore = false;
            Object tmp = null;

            try {
                tmp = m_Iterator.getKey();

                if(m_Iterator.moveNext()) {
                    hasMore = true;
                }
    
                /* resets iterator to the old state **/
                m_Iterator.moveTo((KeyObject)tmp);
            } catch (IOException ioe) {
                m_Store.getLogger().error("store(..): Exception", ioe);
            } catch (ClassNotFoundException cnfe) {
                m_Store.getLogger().error("store(..): Exception", cnfe);
            }
            return hasMore;
        }

        public Object nextElement() {
            Object tmp = null;

            try {
                tmp = m_Iterator.getKey();
                m_Iterator.moveNext();
            } catch (IOException ioe) {
                m_Store.getLogger().error("store(..): Exception", ioe);
            } catch (ClassNotFoundException cnfe) {
                m_Store.getLogger().error("store(..): Exception", cnfe);
            }
            // make a string out of it (JispStringKey is not usefull here)
            return tmp.toString();
        }
    }
}
