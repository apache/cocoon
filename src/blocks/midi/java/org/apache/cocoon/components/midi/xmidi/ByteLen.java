/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:   "This product includes software
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

package org.apache.cocoon.components.midi.xmidi;

/**
 * The MIDI file parsing parts of this class are based on code from the XMidi project, written
 * by  Peter Arthur Loeb (http://www.palserv.com/XMidi/) and used with permission.
 * The warranty disclaimer of the MIT license (http://www.opensource.org/licenses/mit-license.html)
 * applies to Peter Arthur Loeb's code.
 *
 * @author <a href="mailto:mark.leicester@energyintellect.com">Mark Leicester</a>
 * @author <a href="mailto:peter@palserv.com">Peter Loeb</a>
 */

public class ByteLen {
/**
 * Default constructor.
 * As of Jan 8, 2001, this is not used.
*/
	public ByteLen() {}

/**
 * Constructor used in the
 * {@link com.palserv.XMidi.MX#deltaToInt(byte[],int) MX.deltaToInt}
 * method to create this class, which it then returns.
 * @param byte[] b - a byte array; used to set ba
 * @param int l - a length; used to set len
*/
	public ByteLen(byte[] b, int l) {
		ba = b;
		len = l;
	}

/**
 * A byte array.
*/
	public byte[] ba;

/**
 * As used in the
 * {@link com.palserv.XMidi.MX#deltaToInt(byte[],int) MX.deltaToInt}
 * method, it is the length of the delta field being converted,
 * not the length of the array.
 * <p>
 * There is nothing about this class that requires that this variable
 * be used in this way.  It could be any int.
*/
	public int len;
}
