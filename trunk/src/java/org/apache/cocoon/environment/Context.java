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
package org.apache.cocoon.environment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.io.InputStream;

/**
 * Defines an interface to provide client context information .
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: Context.java,v 1.2 2004/03/08 14:02:43 cziegeler Exp $
 *
 */

public interface Context {

    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    void removeAttribute(String name);

    Enumeration getAttributeNames();

    URL getResource(String path) throws MalformedURLException;

    String getRealPath(String path);

    String getMimeType(String file);

    String getInitParameter(String name);

    InputStream getResourceAsStream(String path);
}
