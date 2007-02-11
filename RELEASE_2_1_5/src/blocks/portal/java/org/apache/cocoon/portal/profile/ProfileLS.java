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
package org.apache.cocoon.portal.profile;

import java.util.Map;

import org.apache.excalibur.source.SourceValidity;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: ProfileLS.java,v 1.5 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public interface ProfileLS {
    
    String ROLE = ProfileLS.class.getName();
    
    //  TODO define ExceptionType later
    Object loadProfile(Object key, Map parameters) throws Exception;  
    
    //TODO define ExceptionType later
    void saveProfile(Object key, Map parameters, Object profile) throws Exception;  
    
    SourceValidity getValidity(Object key, Map parameters);
}
