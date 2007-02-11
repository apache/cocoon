/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplet;

import org.apache.cocoon.ProcessingException;

/**
 * This factory is for creating and managing coplet objects
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: CopletFactory.java,v 1.5 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public interface CopletFactory  {
    
    String ROLE = CopletFactory.class.getName();
    
    void prepare(CopletData copletData)
    throws ProcessingException;
    
    void prepare(CopletInstanceData copletInstanceData)
    throws ProcessingException;
    
    /** 
     * Create a new coplet instance.
     * This is also registered at the profile manager.
     */
    CopletInstanceData newInstance(CopletData copletData)
    throws ProcessingException;
    
    /**
     * Remove the coplet instance data.
     * This is also unregistered at the profile manager.
     */
    void remove(CopletInstanceData copletInstanceData)
    throws ProcessingException;
}
