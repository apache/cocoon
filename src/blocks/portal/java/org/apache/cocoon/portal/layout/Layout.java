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

import org.apache.cocoon.portal.factory.Producible;


/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public interface Layout 
    extends Parameters, Producible {

    /**
     * Get the name of the {@link org.apache.cocoon.portal.layout.renderer.Renderer} to draw this layout.
     * If this layout has an own renderer {@link #getLayoutRendererName()}
     * return this, otherwise the default renderer is returned.
     * @return String The role name
     */
    String getRendererName();
    
    /** 
     * Get the name of a custom {@link org.apache.cocoon.portal.layout.renderer.Renderer} for this layout.
     * @return String The role name
     */
    String getLayoutRendererName();
    
    Item getParent();

    void setParent(Item item);

    /**
     * Make a copy of this layout object and of all it's children.
     * This includes copies of items and copletinstancedatas.
     */
    Layout copy();

}
