/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.store;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.impl.AbstractReadWriteStore;

/**
 * TODO - This store implementation should be moved to excalibur store
 * 
 * @author <a href="mailto:cmoss@tvnz.co.nz">Corin Moss</a>
 */
public abstract class AbstractJCSStore
    extends AbstractReadWriteStore
    implements Store, ThreadSafe {
    
    /**The JCS Configuration file - for details see: 
    *http://jakarta.apache.org/turbine/jcs/BasicJCSConfiguration.html
    */
    protected File m_JCSConfigFile;
    
    /** The Java Cache System object */
    protected JCS m_JCS;
    
    /**The Region as used by JCS*/
    protected String m_region;

    /**The group name as used by JCS getGroupKeys*/
    protected String m_group;
    

     
    public void setup(final File configFile, final String regionName, final String groupName) 
      throws IOException, CacheException {
   
        this.m_JCSConfigFile = configFile;
        this.m_region = regionName;
        this.m_group = groupName;
        
        getLogger().debug("CEM Loading config: '" + this.m_JCSConfigFile.getAbsolutePath() + "'");
        getLogger().debug("CEM Loading region: '" + this.m_region + "'");
        getLogger().debug("CEM Loading group: '" + this.m_group + "'");

        /* Does config exist? */
       // if (this.m_JCSConfigFile.exists()) 
       // {
            getLogger().debug("CEM Setting full path: " + this.m_JCSConfigFile.getAbsolutePath());
            
            JCS.setConfigFilename( this.m_JCSConfigFile.getAbsolutePath() );
       // } else {         
        //    throw new IOException( "Error reading JCS Config '" + this.m_JCSConfigFile.getAbsolutePath() + "'. File not found." );
       // }


        try {
           m_JCS = JCS.getInstance( m_region );
        } catch (CacheException ce) { 
            throw new CacheException( "Error initialising JCS with region: " + this.m_region );
        }
         

    } 
    
     /**
     * Returns a Object from the store associated with the Key Object
     *
     * @param key the Key object
     * @return the Object associated with Key Object
     */
    
    protected Object doGet(Object key) 
    {
        Object value = null;
        
        value = m_JCS.get(key);
        if (getLogger().isDebugEnabled()) 
        {
            if (value != null) 
            {
                getLogger().debug("Found key: " + key);
            } 
            else 
            {
                getLogger().debug("NOT Found key: " + key);
            }
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
    protected void doStore(Object key, Object value)
        throws IOException 
    {
        
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("store(): Store file with key: "
                + key.toString());
            getLogger().debug("store(): Store file with value: "
                + value.toString());
        }
        
        //This test is not really pertinent here - we
        //won't always be storing serializable objects (I don't think)
        if (value instanceof Serializable) 
        {
            try 
            {
                m_JCS.put(key, value);
            } 
            catch (CacheException ce) 
            {
                getLogger().error("store(..): Exception", ce);
            }
        } 
        else 
        {
            throw new IOException("Object not Serializable");
        }
    }
    
    /**
     * Frees some values of the data file.<br>
     * TODO: implementation
     */
    public void free() 
    {
        // if we ever implement this, we should implement doFree()
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.impl.AbstractReadWriteStore#doFree()
     */
    protected void doFree() {
    }
    
    /**
     * Clear the Store of all elements
     */
    protected void doClear() 
    {
        
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("clear(): Clearing the database ");
        }
        
        try 
        {
            //No args - should remove all
            //This is not well documented, although the source
            //suggests that this will work
            m_JCS.remove();               
        } 
        catch (CacheException ce) 
        {
            getLogger().error("store(..): Exception", ce);
        }
    }
    
    /**
     * Removes a value from the data file with the given key.
     *
     * @param key the key object
     */
    protected void doRemove(Object key)
    {
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("remove(..) Remove item");
        }
        
        try 
        {
           m_JCS.remove(key); 
        } 
         //Need to revisit this exception - what happens
         //if no match found for key  - is an exception thrown?
        catch (CacheException ce) 
        {
            getLogger().error("remove(..): Exception", ce);
        }
    }
    
    /**
     *  Test if the the index file contains the given key
     *
     * @param key the key object
     * @return true if Key exists and false if not
     */
    protected boolean doContainsKey(Object key) 
    {
         
        //All we have available is a null check
        if (m_JCS.get(key) != null) {
            return true;
        } 
        else {
            return false;
        }
    }
    
    
     /**
     * Returns an Enumeration of all Keys in the cache.<br>
     * this is a bit of a hack - I don't believe that the group
     * needs to be passed in as a string in this way - we should
     * be able to retreive it.
     * FIX ME!!
     *
     * @return  Enumeration Object with all existing keys
     */
    protected Enumeration doGetKeys() 
    {
      return new org.apache.commons.collections.iterators.IteratorEnumeration(
         this.m_JCS.getGroupKeys(this.m_group).iterator()
      );
    }
    
    protected int doGetSize() 
    {
        //The following is protected - I shall try and 
        //find a way to get to it, but I'm not sure yet
        //return this.m_JCS.cacheControl.getSize();
        
        //Nothing seems to rely on this out side of the instrumentation
        //so, I'll be bad
        return 0;
    }
    
    
    
}
