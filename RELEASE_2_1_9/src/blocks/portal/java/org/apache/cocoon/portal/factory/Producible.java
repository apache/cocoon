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
package org.apache.cocoon.portal.factory;

import org.apache.cocoon.portal.aspect.Aspectalizable;



/**
 * This interface marks an object that can be created by a factory.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: Producible.java,v 1.3 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public interface Producible 
    extends Aspectalizable {
    
    /**
     * The name given from the factory
     */
    String getName();

    /**
     * Get the unique id of this object
     * @return String Unique id
     */
    String getId();

    /**
     * Set the layout description
     */
    void setDescription(ProducibleDescription description);
    
    /**
     * Initialize the object. This should only be called once directly
     * after the creation
     */
    void initialize(String name, String id);
}
