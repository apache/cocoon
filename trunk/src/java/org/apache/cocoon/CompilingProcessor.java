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
package org.apache.cocoon;

import org.apache.cocoon.environment.Environment;

/**
 * CompilingProcessor does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.3 $
 */
public interface CompilingProcessor extends Processor, Modifiable
{
    String ROLE = CompilingProcessor.class.getName();

    /**
     * Process the given <code>Environment</code> to generate Java code for specified XSP files.
     *
     * @param fileName a <code>String</code> value
     * @param environment an <code>Environment</code> value
     * @exception Exception if an error occurs
     */
    public void precompile( String fileName,
                            Environment environment,
                            String markupLanguage,
                            String programmingLanguage )
            throws Exception;
}
