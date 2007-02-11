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
package org.apache.cocoon.servlet.multipart;

/**
 * Exception thrown when on a parse error such as
 * a malformed stream.
 *
 * @author <a href="mailto:j.tervoorde@home.nl">Jeroen ter Voorde</a>
 * @version CVS $Id: MultipartException.java,v 1.2 2004/03/05 13:02:58 bdelacretaz Exp $
 */
public class MultipartException extends Exception {

    /**
     * Constructor MultipartException
     */
    public MultipartException() {
        super();
    }

    /**
     * Constructor MultipartException
     *
     * @param text
     */
    public MultipartException(String text) {
        super(text);
    }
}
