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
package org.apache.cocoon.components.language.programming.java;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.language.programming.CodeFormatter;

/**
 * This class implements <code>CodeFormatter</code>.
 * This implementation doesn't do anything but returning the
 * passed code as is.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: NullFormatter.java,v 1.2 2004/03/08 13:58:32 cziegeler Exp $
 */
public class NullFormatter extends AbstractLogEnabled implements CodeFormatter {

    /**
     * This is a dummy <code>CodeFormatter</code>
     *
     * @param code The input source code
     * @param encoding The encoding used for constant strings embedded in the
     * source code
     * @return The formatted source code
     */
    public String format(String code, String encoding) {
        getLogger().debug("NullFormatter called");
        return code;
    }
}
