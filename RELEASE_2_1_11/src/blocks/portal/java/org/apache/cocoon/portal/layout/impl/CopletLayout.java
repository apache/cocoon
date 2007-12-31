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

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.AbstractLayout;
import org.apache.cocoon.portal.layout.Layout;

/**
 * A coplet layout holds a coplet.
 * 
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public final class CopletLayout extends AbstractLayout {
    
    private CopletInstanceData copletInstanceData;
    
    public void setCopletInstanceData(CopletInstanceData cid) {
        this.copletInstanceData = cid;
        if ( this.getId() == null ) {
            this.setId(cid.getId());
        }
    }
    
    public CopletInstanceData getCopletInstanceData() {
        return this.copletInstanceData;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        CopletLayout clone = (CopletLayout)super.clone();
        
        clone.copletInstanceData = null;
        
        return clone;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.Layout#copy()
     */
    public Layout copy() {
        CopletLayout clone = (CopletLayout)super.copy();

        if ( this.copletInstanceData != null ) {
            clone.copletInstanceData = this.copletInstanceData.copy();
        }
            
        return clone;
    }
}
