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
package org.apache.cocoon.portal.factory.impl;

import org.apache.cocoon.portal.aspect.impl.AbstractAspectalizable;
import org.apache.cocoon.portal.factory.Producible;
import org.apache.cocoon.portal.factory.ProducibleDescription;



/**
 * This interface marks an object that can be created by a factory.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AbstractProducible.java,v 1.5 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public abstract class AbstractProducible 
    extends AbstractAspectalizable 
    implements Producible {
    
    protected String name;

    protected String id;
    
    transient protected ProducibleDescription description;
    
    /**
     * @return The configured name
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * Set the layout description
     */
    public void setDescription(ProducibleDescription description) {
        this.description = description;
    }

    /**
     * Get the unique id of this object
     * @return String Unique id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the unique id of this object
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Initialize the object. This should only be called once directly
     * after the creation
     */
    public void initialize(String name, String id) {
        this.name = name;
        this.id = id;
    }

}
