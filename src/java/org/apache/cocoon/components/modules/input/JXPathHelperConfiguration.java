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

import org.apache.commons.jxpath.FunctionLibrary;

/**
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: JXPathHelperConfiguration.java,v 1.3 2004/05/01 00:05:45 joerg Exp $
 */
public class JXPathHelperConfiguration {

    /**
     * Contains all globally registered extension classes and
     * packages. Thus the lookup and loading of globally registered
     * extensions is done only once.
     *
     */
    private FunctionLibrary library = null;

    /** set lenient mode for jxpath (i.e. throw an exception on
     * unsupported attributes) ? 
     */
    private boolean lenient = true;


    public JXPathHelperConfiguration() {
    }

    public JXPathHelperConfiguration(FunctionLibrary library, boolean lenient) {
        this.library = library;
        this.lenient = lenient;
    }

    public boolean isLenient() {
        return lenient;
    }

    public FunctionLibrary getLibrary() {
        return library;
    }

}
