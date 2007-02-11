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
package org.apache.cocoon.components;

import java.io.IOException;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

/**
 * Components implementing this marker interface have a lifecycle of one
 * request-response cycle. This means if during one cycle a component accepting this
 * interface is looked up several times, it's always the same instance.
 * Each internal subrequest happens in the same cycle, so an instance looked up in 
 * either the "main" request or in any of the subrequests is available to all
 * other requests in this cycle. 
 * In addition, the first time this component is looked up during a request,
 * the {@link #setup(SourceResolver, Map)} method is called.
 *
 * @see org.apache.cocoon.components.RequestLifecycleComponent
 * @deprecated Use the {@link org.apache.cocoon.components.persistence.RequestDataStore} instead.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: GlobalRequestLifecycleComponent.java,v 1.6 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public interface GlobalRequestLifecycleComponent {

    /**
     * Set the {@link SourceResolver} and the objectModel 
     * used to process the current request.
     */
    void setup(SourceResolver resolver, Map objectModel)
    throws ProcessingException, SAXException, IOException;
}
