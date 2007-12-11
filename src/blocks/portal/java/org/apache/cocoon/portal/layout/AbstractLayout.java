/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.layout;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.cocoon.portal.factory.impl.AbstractProducible;
import org.apache.cocoon.portal.pluto.om.common.ParameterImpl;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.pluto.om.common.Parameter;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public abstract class AbstractLayout
    extends AbstractProducible 
    implements Layout, Parameters {
    
    protected String rendererName;
    
    protected Item parent;

    protected Map parameters = new LinkedMap(3);

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.Parameters#getParameters()
     */
    public final Map getParameters() {
        return parameters;
    }

    public final Set getCastorParameters()
    {
        Set set = new HashSet(this.parameters.size());
        Iterator iterator = this.parameters.entrySet().iterator();
        Map.Entry entry;
        while (iterator.hasNext())
        {
            entry = (Map.Entry) iterator.next();
            ParameterImpl param = new ParameterImpl();
            param.setName((String) entry.getKey());
            param.setValue((String) entry.getValue());
            set.add(param);
        }
        return set;
    }

    public void addParameter(Parameter parameter)
    {
        parameters.put(parameter.getName(), parameter.getValue());
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getRendererName()
     */
    public String getRendererName() {
        if ( this.rendererName == null ) {
            return ((LayoutDescription)this.description).getDefaultRendererName();
        }
        return this.rendererName;
    }

    public void setLayoutRendererName(String value) {
		this.rendererName = value;
	}
    
    public Item getParent() {
        return this.parent;
    }
    
    public void setParent(Item item) {
        this.parent = item;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.Layout#getLayoutRendererName()
     */
    public String getLayoutRendererName() {
        return this.rendererName;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        AbstractLayout clone = (AbstractLayout)super.clone();
        
        // we don't clone the parent; we just set it to null
        clone.rendererName = this.rendererName;
        clone.parameters = new LinkedMap(this.parameters);
        clone.parent = null;
        
        return clone;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.Layout#copy()
     */
    public Layout copy() {
        try {
            return (Layout)this.clone();
        } catch (CloneNotSupportedException cnse) {
            // ignore
        }
        return null;
    }
}
