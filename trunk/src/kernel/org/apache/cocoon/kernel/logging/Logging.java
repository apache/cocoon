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
package org.apache.cocoon.kernel.logging;


/**
 * <p>The {@link Logging} interface marks a component requiring to log through
 * the core kernel logging facility.</p>
 *
 * <p>Instead of relying on this interface, blocks should define their logging
 * requirements in their deployment descriptor (as with any other required
 * block) but in some cases the core loggers might be required (for example,
 * during the initialization of a block providing loggers).</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public interface Logging {

    /**
     * <p>Setup the {@link Logger} instance this component can use to
     * perform logging operations.</p>
     *
     * @param logger a <b>non null</b> {@link Logger} instance.
     */
    public void logger(Logger logger);

}
