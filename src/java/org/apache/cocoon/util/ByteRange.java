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
 * @author <a href="mailto:stuart.roebuck@adolos.co.uk">Stuart Roebuck</a>
 * @version CVS $Id: ByteRange.java,v 1.2 2004/03/05 13:03:00 bdelacretaz Exp $
 */
final public class ByteRange {

    
    private final long start;
    private final long end;

    
    public ByteRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    
    public ByteRange(String string) throws NumberFormatException {
        string = string.trim();
        int dashPos = string.indexOf('-');
        int length = string.length();
        if (string.indexOf(',') != -1) {
            throw new NumberFormatException("Simple ByteRange String contains a comma.");
        }
        if (dashPos > 0) {
            this.start = Integer.parseInt(string.substring(0, dashPos));
        } else {
            this.start = Long.MIN_VALUE;
        }
        if (dashPos < length - 1) {
            this.end = Integer.parseInt(string.substring(dashPos + 1, length));
        } else {
            this.end = Long.MAX_VALUE;
        }
        if (this.start > this.end) {
            throw new NumberFormatException("Start value is greater than end value.");
        }
    }

    
    public long getStart() {
        return this.start;
    }

    
    public long getEnd() {
        return this.end;
    }

    
    public long length() {
        return this.end - this.start + 1;
    }

    
    public ByteRange intersection(ByteRange range) {
        if (range.end < this.start || this.end < range.start) {
            return null;
        } else {
            long start = (this.start > range.start) ? this.start : range.start;
            long end = (this.end < range.end) ? this.end : range.end;
            return new ByteRange(start, end);
        }
    }


    public String toString() {
        return this.start + "-" + this.end;
    }

    
}
