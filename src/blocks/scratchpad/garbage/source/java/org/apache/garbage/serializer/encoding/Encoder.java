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
 * @version CVS $Id: Encoder.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
 */
public interface Encoder extends Verifier {

    /**
     * Encode the specified character as a sequence of characters.
     * <p>
     * For example, in XML, the character &quot;<code>&amp;</code>&quot; can
     * be encoded as &quot;<code>&amp;amp;</code>&quot;, or the Unicode
     * &quot;<code>\u012F</code>&quot; character can be encoded as
     * &quot;<code>&amp;#x012F;</code>&quot;
     * </p>
     */
    public char[] encode(char character);
}
