/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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
 * The JCS Configuration file - for details see: 
 * http://jakarta.apache.org/turbine/jcs/BasicJCSConfiguration.html
 * 
 * @author <a href="mailto:cmoss@tvnz.co.nz">Corin Moss</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 */
public abstract class AbstractJCSStore
    extends AbstractReadWriteStore
    implements Store, ThreadSafe {
    
    /** The Java Cache System object */
    protected JCS m_JCS;
    
    /**The Region as used by JCS*/
    private String m_region;

    /**The group name as used by JCS getGroupKeys*/
    private String m_group;
    

     
    public void setup(final File configFile, 
                      final String regionName, 
                      final String groupName) 
    throws IOException, CacheException {
   
        this.m_region = regionName;
        this.m_group = groupName;
        
        if ( this.getLogger().isDebugEnabled() ) {
            if ( configFile != null ) {
                getLogger().debug("CEM Loading config: '" + configFile.getAbsolutePath() + "'");
            }
            getLogger().debug("CEM Loading region: '" + this.m_region + "'");
            getLogger().debug("CEM Loading group: '" + this.m_group + "'");
        }

        if ( configFile != null ) {
            /* Does config exist? */
            // if (this.m_JCSConfigFile.exists()) 
            // {
                getLogger().debug("CEM Setting full path: " + configFile.getAbsolutePath());
                
                JCS.setConfigFilename( configFile.getAbsolutePath() );
           // } else {         
            //    throw new IOException( "Error reading JCS Config '" + this.m_JCSConfigFile.getAbsolutePath() + "'. File not found." );
           // }
        }

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
