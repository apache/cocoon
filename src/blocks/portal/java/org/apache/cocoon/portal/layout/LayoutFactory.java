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
package org.apache.cocoon.portal.layout;

import java.util.List;

import org.apache.cocoon.ProcessingException;

/**
 * This factory is for creating and managing layout objects
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: LayoutFactory.java,v 1.5 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public interface LayoutFactory  {
    
    String ROLE = LayoutFactory.class.getName();
    
    /**
     * This method is invoked for a newly loaded profile
     */
    void prepareLayout(Layout layout)
    throws ProcessingException;
    
    /**
     * Create a new layout instance.
     * The instance is also registered at the profile manager.
     */
    Layout newInstance(String name)
    throws ProcessingException;
    
    /**
     * Remove the layout instance.
     * The instance (and all childs) will also be unregistered from
     * the profile manager.
     */
    void remove(Layout layout)
    throws ProcessingException;
    
    List getLayoutDescriptions();
    
}
