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

package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.flow.FlowHelper;

import java.util.Map;

/**
 * FlowContinuationModule provides access to the flow web continuation
 * object. To get access to the properties use XPath syntax. If
 * requested object is not found then an exception will be thrown.
 *
 * @author <a href="mailto:danielf@nada.kth.se">Daniel Fagerstrom</a>
 * @version CVS $Id: FlowContinuationModule.java,v 1.1 2004/04/04 09:43:43 gcasper Exp $
 */
public class FlowContinuationModule extends AbstractJXPathModule
    implements ThreadSafe {

    protected Object getContextObject(Configuration modeConf,
                                      Map objectModel) {

        return FlowHelper.getWebContinuation(objectModel);
    }
}
