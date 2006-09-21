/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.core.container;

import org.apache.avalon.framework.service.ServiceException;

/**
 * This exception indicates the a services manager was not able to find a component.
 * @since 2.2
 * @version $Id$
 */
public class ServiceNotFoundException extends ServiceException {

    public ServiceNotFoundException(String arg0, String arg1, Throwable arg2) {
        super(arg0, arg1, arg2);
    }

    public ServiceNotFoundException(String arg0, String arg1) {
        super(arg0, arg1);
    }
}
