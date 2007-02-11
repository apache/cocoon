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
package org.apache.cocoon.woody.formmodel;

/**
 * This exception is thrown by {@link ExpressionContextImpl} when it asked for
 * the value of a required field, when that required field does not yet have
 * a value.
 * 
 * @version $Id: CannotYetResolveWarning.java,v 1.5 2004/03/09 13:53:55 reinhard Exp $
 */
public class CannotYetResolveWarning extends  RuntimeException {
    public String getMessage() {
        return "An expression variable could not yet be resolved.";
    }
}
