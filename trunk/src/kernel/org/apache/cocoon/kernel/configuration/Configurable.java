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
package org.apache.cocoon.kernel.configuration;

/**
 * <p>The {@link Configurable} class identifies an object able to be configured
 * through the use of parameters.</p>
 *
 * <p>Note that a {@link Parameters} parameter can contain an entire
 * {@link Configuration} tree, nullifying the requirement of a configuration
 * method using a {@link Configuration}
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public interface Configurable {

    /**
     * <p>Configure this instance with the specified {@link Parameters}.</p>
     *
     * @param parameters the {@link Parameters} configuring the instance.
     * @throws ConfigurationException if this instance could not be configured.
     */
    public void configure(Parameters parameters)
    throws ConfigurationException;
}
