/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import org.apache.pluto.services.PortletContainerEnvironment;

/**
 * This interface marks a factory/service as a component that nees
 * access to the portlet container environment.
 * This method is invoke in the component initialization phase just
 * before the initialize method.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletContainerEnabled.java,v 1.2 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public interface PortletContainerEnabled {
    
    void setPortletContainerEnvironment(PortletContainerEnvironment env);
}
