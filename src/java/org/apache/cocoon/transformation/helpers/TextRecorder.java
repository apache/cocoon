/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.transformation.helpers;

/**
 * This class records all character SAX events and creates a string
 * from them.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version $Id$
*/
public final class TextRecorder extends NOPRecorder {

    /**
     * Buffer collecting all character events.
     */
    private StringBuffer buffer;

    public TextRecorder() {
        super();
        this.buffer = new StringBuffer();
    }

    public void characters(char ary[], int start, int length) {
        this.buffer.append(ary, start, length);
    }

    /**
     * @return Recorded text so far, trimmed.
     */
    public String getText() {
        return this.buffer.toString().trim();
    }
}
