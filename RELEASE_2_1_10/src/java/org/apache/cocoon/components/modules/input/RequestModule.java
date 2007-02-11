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

package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * RequestModule provides access to Request object properties.
 * To get access to request properties use XPath syntax, e.g. to get the request
 * context path use <code>'contextPath'</code> as the attribute name.<br/>
 * More complex expressions are also supported, e.g.:
 * <pre>
 * 'userPrincipal/name'
 * </pre>
 * will return the name property of the Principal object returned by the
 * request.getUserPrincipal() method. If requested object is not found then
 * an exception will be thrown.
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @version $Id$
 */
public class RequestModule extends AbstractJXPathModule
    implements ThreadSafe {

    protected Object getContextObject(Configuration modeConf,
                                      Map objectModel) {

        return ObjectModelHelper.getRequest(objectModel);
    }
}
