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
package org.apache.cocoon.portal.aspect;

import java.util.Map;


/**
 * This interface marks an object that can be used by aspects.
 * An aspect can store any arbitrary information in an aspectalizable object.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: Aspectalizable.java,v 1.8 2004/03/05 13:02:09 bdelacretaz Exp $
 */
public interface Aspectalizable {

    /**
     * Get the data associated with an aspect
     * @param aspectName The aspect name
     * @return The data or null if the aspect is not associated with this object
     */
    Object getAspectData(String aspectName);
    
    /**
     * Set the data associated with an aspect
     * The data is only set if the object is associated with the given aspect
     * @param aspectName The aspect name
     * @param data The data
     */
    void setAspectData(String aspectName, Object data);
    
    /**
     * Return all aspect datas 
     * @return A map of data objects, the keys are built by the aspect names
     */
    Map getAspectDatas();
    
    /**
     * Return all persistent aspect datas 
     * @return A map of data objects, the keys are built by the aspect names
     */
    Map getPersistentAspectData();

    void addPersistentAspectData(String aspectName, Object data);
    
    /**
     * Is this aspect supported
     */
    boolean isAspectSupported(String aspectName);

    /**
     * This method is invoked once to set the handler
     */
    void setAspectDataHandler(AspectDataHandler handler);
}
