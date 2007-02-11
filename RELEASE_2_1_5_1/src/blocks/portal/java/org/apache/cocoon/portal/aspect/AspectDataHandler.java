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
 * This interface is able to get a stored aspect
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: AspectDataHandler.java,v 1.6 2004/03/05 13:02:09 bdelacretaz Exp $
 */
public interface AspectDataHandler {

    Object getAspectData(Aspectalizable owner, String aspectName);
    
    void setAspectData(Aspectalizable owner, String aspectName, Object data);

    Map getAspectDatas(Aspectalizable owner);
    
    Map getPersistentAspectDatas(Aspectalizable owner);

    boolean isAspectSupported(String aspectName);
    
}
