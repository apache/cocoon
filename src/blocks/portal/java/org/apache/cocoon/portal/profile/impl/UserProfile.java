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
package org.apache.cocoon.portal.profile.impl;

import java.util.Map;

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
     * @param layouts The layouts to set.
     */
    public void setLayouts(Map layouts) {
        this.layouts = layouts;
    }
}
