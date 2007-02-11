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
package org.apache.cocoon.caching.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.EventRegistry;

/**
 * This implementation of <code>EventRegistry</code> handles 
 * persistence by serializing an <code>EventRegistryDataWrapper</code> to 
 * disk.
 * 
 * @since 2.1
 * @author <a href="mailto:ghoward@apache.org">Geoff Howard</a>
 * @version CVS $Id: DefaultEventRegistryImpl.java,v 1.12 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public class DefaultEventRegistryImpl 
        extends AbstractDoubleMapEventRegistry
        implements EventRegistry, 
                   Contextualizable {

    private File m_persistentFile;
    private static final String PERSISTENT_FILE = "ev_cache.ser";    

    /**
     * Set up the persistence file.
     */
    public void contextualize(Context context) throws ContextException {
        org.apache.cocoon.environment.Context ctx =
                (org.apache.cocoon.environment.Context) context.get(
                                    Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        // set up file 
        m_persistentFile = new File(
                    ctx.getRealPath("/WEB-INF"), 
                        DefaultEventRegistryImpl.PERSISTENT_FILE);
        if (m_persistentFile == null) {
            throw new ContextException("Could not obtain persistent file. " +
                "The cache event registry cannot be " +
                "used inside an unexpanded WAR file.");
        }
    }
    
    /** 
     * Persist by simple object serialization.  If the serialization fails, an 
     * error is logged but not thrown because missing/invalid state is handled 
     * at startup.
     */
    protected void persist(EventRegistryDataWrapper registryWrapper) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(
                                        new FileOutputStream(this.m_persistentFile));       
            oos.writeObject(registryWrapper);
            oos.flush();
        } catch (FileNotFoundException e) {
            getLogger().error("Unable to persist EventRegistry", e);
        } catch (IOException e) {
            getLogger().error("Unable to persist EventRegistry", e);
        } finally {
            try {
                if (oos != null) oos.close();
            } catch (IOException e) {}
        }
    }

    /* 
     * I don't think this needs to get synchronized because it should 
     * only be called during initialize, which should only be called 
     * once by the container.
     */
    protected boolean recover() {
        if (this.m_persistentFile.exists()) {
            ObjectInputStream ois = null;
            EventRegistryDataWrapper ecdw = null;
            try {
                ois = new ObjectInputStream(
                    new FileInputStream(this.m_persistentFile));
                ecdw = (EventRegistryDataWrapper)ois.readObject();
            } catch (FileNotFoundException e) {
                getLogger().error("Unable to retrieve EventRegistry", e);
                createBlankCache();
                return false;
            } catch (IOException e) {
                getLogger().error("Unable to retrieve EventRegistry", e);
                createBlankCache();
                return false;
            } catch (ClassNotFoundException e) {
                getLogger().error("Unable to retrieve EventRegistry", e);
                createBlankCache();
                return false;
            } finally {
                try {
                    if (ois != null) ois.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            unwrapRegistry(ecdw);
        } else {
            getLogger().warn(this.m_persistentFile + " does not exist - Unable to " +
                "retrieve EventRegistry.");
            createBlankCache();
            return false;
        }
        return true;
    }

}
