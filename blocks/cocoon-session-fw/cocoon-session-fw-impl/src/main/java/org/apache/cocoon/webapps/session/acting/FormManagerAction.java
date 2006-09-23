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
package org.apache.cocoon.webapps.session.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.session.FormManager;

/**
 * This action invokes the form manager to process incomming form values
 *
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
*/
public final class FormManagerAction
    extends ServiceableAction
    implements ThreadSafe {

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws ProcessingException {
        FormManager formManager = null;
        try {
            formManager = (FormManager)this.manager.lookup(FormManager.ROLE);
            formManager.processInputFields();
        } catch (ServiceException ce) {
            throw new ProcessingException("Error during lookup of formManager component.", ce);
        } finally {
            this.manager.release( formManager );
        }

        return EMPTY_MAP;
    }

}
