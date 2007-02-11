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
package org.apache.garbage.serializer.encoding;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: DefaultEncoder.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
 */
public class DefaultEncoder extends UnknownCharset implements Encoder {

    /**
     * Create a new instance of this <code>DefaultEncoder</code>.
     */
    public DefaultEncoder() {
        super();
    }

    /**
     * Encode the specified character as a sequence of characters.
     * <p>
     * This method will return the same character nested into an array.
     * </p>
     */
    public char[] encode(char character) {
        char c[] = { character };
        return(c);
    }
}
