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
package org.apache.cocoon.el.util;

import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;

public class ELUtils {
    public static void parse(String str, ParseHandler handler) {
        parse(CharBuffer.wrap(str), handler);
    }

    public static void parse(CharBuffer buf, ParseHandler handler) {
        boolean inExpression = false;

        while (buf.hasRemaining()) {
            int length = 0;
            CharBuffer slice = buf.slice();

            if (inExpression) {
                try {
                    for (char ch = buf.get(); ch != '}'; ch = buf.get(), length++)
                        if (ch == '\\') {
                            buf.get();
                            length++;
                        }

                    if (length > 0)
                        handler.handleExpression(slice.subSequence(0, length));
                } catch (BufferUnderflowException e) {
                    throw new RuntimeException("Missing '}'");
                }
            } else {
                while (buf.hasRemaining())
                    if (buf.get() == '$' && buf.hasRemaining()
                            && buf.get() == '{')
                        break;
                    else
                        length++;

                if (length > 0)
                    handler.handleText(slice.subSequence(0, length));
            }
            inExpression = !inExpression;
        }
    }
}