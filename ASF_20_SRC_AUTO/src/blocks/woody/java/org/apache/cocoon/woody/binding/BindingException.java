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
package org.apache.cocoon.woody.binding;

import org.apache.avalon.framework.CascadingException;

/**
 * This exception is thrown when something goes wrong with the binding.
 *
 * @version CVS $Id: BindingException.java,v 1.3 2004/03/05 13:02:26 bdelacretaz Exp $
 */
public class BindingException extends CascadingException {
    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Exception e) {
        super(message, e);
    }
}
