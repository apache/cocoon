/* 
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.classloader;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * A <code>ClassLoader</code> factory, setting up the classpath given a
 * &lt;classpath&gt; configuration.
 * 
 * @version $Id$
 */
public interface ClassLoaderFactory {
    final static String ROLE = ClassLoaderFactory.class.getName();
    
    ClassLoader createClassLoader(ClassLoader parent, Configuration config) throws ConfigurationException;
}
