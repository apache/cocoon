/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * @version $Id$
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
