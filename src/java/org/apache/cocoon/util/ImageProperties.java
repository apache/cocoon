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
package org.apache.cocoon.util;

/**
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: ImageProperties.java,v 1.3 2004/03/08 14:03:30 cziegeler Exp $
 */
final public class ImageProperties {
    final public int width;
    final public int height;
    final public char[] comment;
    final public String type;

    public ImageProperties(int width, int height, char[] comment, String type) {
        this.width = width;
        this.height = height;
        this.comment = comment;
        this.type = type;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(type).append(" ").append(width).append("x").append(height);
        if (comment != null) {
            sb.append(" (").append(comment).append(")");
        }
        return (sb.toString());
    }
}
