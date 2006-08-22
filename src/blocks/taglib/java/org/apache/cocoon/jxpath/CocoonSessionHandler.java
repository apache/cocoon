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
package org.apache.cocoon.jxpath;

import java.util.Enumeration;

import org.apache.cocoon.environment.Session;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.jxpath.DynamicPropertyHandler;

/**
 * Implementation of the DynamicPropertyHandler interface that provides
 * access to attributes of a Cocoon Session.
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id$
 */
public class CocoonSessionHandler implements DynamicPropertyHandler {

    public String[] getPropertyNames(Object session) {
        final Enumeration e = ((Session) session).getAttributeNames();
        return (String[]) EnumerationUtils.toList(e).toArray();
    }

    public Object getProperty(Object session, String property) {
        return ((Session) session).getAttribute(property);
    }

    public void setProperty(Object session, String property, Object value) {
        ((Session) session).setAttribute(property, value);
    }
}
