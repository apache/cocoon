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
package org.apache.cocoon.portal.layout.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.portal.factory.impl.AbstractProducibleDescription;
import org.apache.cocoon.portal.layout.LayoutDescription;


/**
 * A configured layout
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public class DefaultLayoutDescription
    extends AbstractProducibleDescription
    implements LayoutDescription  {

    protected String defaultRendererName;
    
    protected List rendererNames = new ArrayList(2);
    
    protected String itemClassName;
    
    public String getDefaultRendererName() {
        return defaultRendererName;
    }

    /**
     * @param string
     */
    public void setDefaultRendererName(String string) {
        defaultRendererName = string;
    }

    /**
     * @return the names of all allowed renderers
     */
    public Iterator getRendererNames() {
        return this.rendererNames.iterator();
    }

    public void addRendererName(String name) {
        this.rendererNames.add( name );
    }
    
    /**
     * @return Returns the itemClassName.
     */
    public String getItemClassName() {
        return this.itemClassName;
    }
    /**
     * @param itemClassName The itemClassName to set.
     */
    public void setItemClassName(String itemClassName) {
        this.itemClassName = itemClassName;
    }
}
