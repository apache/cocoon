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
package org.apache.butterfly.source;


/**
 * Description of SourceException.
 * 
 * @version CVS $Id: SourceNotFoundException.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class SourceNotFoundException extends RuntimeException {

    public SourceNotFoundException() {
        super();
    }
    
    public SourceNotFoundException(String arg0) {
        super(arg0);
    }

    public SourceNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public SourceNotFoundException(Throwable arg0) {
        super(arg0);
    }
}
