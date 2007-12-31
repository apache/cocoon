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
package org.apache.cocoon.portal.profile.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;

/**
 * This data object holds all information about the current user:
 * - references to the configuration
 * - all selected coplets (coplet instance datas)
 * - layout objects
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: MapProfileLS.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class UserProfile {
    
    protected Map copletBaseDatas;
    
    protected Map copletDatas;
    
    protected Map copletInstanceDatas;
    
    protected Map layouts;
    
    protected Layout rootLayout;
    
    /**
     * @return Returns the copletBaseDatas.
     */
    public Map getCopletBaseDatas() {
        return copletBaseDatas;
    }
    
    /**
     * @param copletBaseDatas The copletBaseDatas to set.
     */
    public void setCopletBaseDatas(Map copletBaseDatas) {
        this.copletBaseDatas = copletBaseDatas;
    }
    
    /**
     * @return Returns the copletDatas.
     */
    public Map getCopletDatas() {
        return copletDatas;
    }
    
    /**
     * @param copletDatas The copletDatas to set.
     */
    public void setCopletDatas(Map copletDatas) {
        this.copletDatas = copletDatas;
    }
    
    /**
     * @return Returns the copletInstanceDatas.
     */
    public Map getCopletInstanceDatas() {
        return copletInstanceDatas;
    }
    
    /**
     * @param copletInstanceDatas The copletInstanceDatas to set.
     */
    public void setCopletInstanceDatas(Map copletInstanceDatas) {
        this.copletInstanceDatas = copletInstanceDatas;
    }
    
    /**
     * @return Returns the layouts.
     */
    public Map getLayouts() {
        return layouts;
    }
    
    /**
     * @return Returns the rootLayout.
     */
    public Layout getRootLayout() {
        return rootLayout;
    }
    
    /**
     * @param rootLayout The rootLayout to set.
     */
    public void setRootLayout(Layout rootLayout) {
        this.rootLayout = rootLayout;
        this.layouts = new HashMap();
        this.cacheLayouts(this.layouts, rootLayout);
    }
    
    protected void cacheLayouts(Map layoutMap, Layout layout) {
        if ( layout != null ) {
            if ( layout.getId() != null ) {
                layoutMap.put( layout.getId(), layout );
            }
            if ( layout instanceof CompositeLayout ) {
                final CompositeLayout cl = (CompositeLayout)layout;
                final Iterator i = cl.getItems().iterator();
                while ( i.hasNext() ) {
                    final Item current = (Item)i.next();
                    this.cacheLayouts( layoutMap, current.getLayout() );
                }
            }
        }        
    }
    
}
