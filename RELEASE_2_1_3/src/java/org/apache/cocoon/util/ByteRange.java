/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.util;

/**
 * @author <a href="mailto:stuart.roebuck@adolos.co.uk">Stuart Roebuck</a>
 * @version CVS $Id: ByteRange.java,v 1.1 2003/03/09 00:09:42 pier Exp $
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
