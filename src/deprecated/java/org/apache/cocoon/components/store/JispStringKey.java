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
package org.apache.cocoon.components.store;

import com.coyotegulch.jisp.KeyObject;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Wrapper class for String Keys to be compatible with the
 * Jisp KeyObject.
 *
 * @deprecated Use the Avalon Excalibur Store instead.
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @version CVS $Id: JispStringKey.java,v 1.3 2004/03/05 13:02:41 bdelacretaz Exp $
 */
final class JispStringKey extends KeyObject {
    final static long serialVersionUID = -6894793231339165076L;
    private String mKey;

    /**
     *  Constructor for the JispStringKey object
     */
    public JispStringKey() {
        mKey = new String("");
    }

    /**
     *  Constructor for the JispStringKey object
     *
     * @param keyValue the Value of the Key as String
     */
    public JispStringKey(String keyValue) {
        mKey = keyValue;
    }

    /**
     * Compares two String Keys
     *
     * @param key the KeyObject to be compared
     * @return 0 if equal, 1 if greater, -1 if less
     */

    public int compareTo(KeyObject key) {
        if (key instanceof JispStringKey) {
            int comp = mKey.trim().compareTo(((JispStringKey) key).mKey.trim());
            if (comp == 0) {
                return KEY_EQUAL;
            } else {
                if (comp < 0) {
                    return KEY_LESS;
                } else {
                    return KEY_MORE;
                }
            }
        } else {
            return KEY_ERROR;
        }
    }

    /**
     *  Composes a null Kewy
     *
     * @return a null Key
     */
    public KeyObject makeNullKey() {
        return new JispStringKey();
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @exception IOException
     */
    public void writeExternal(ObjectOutput out)
        throws IOException {
        String outKey;
        outKey = new String(mKey);
        out.writeUTF(outKey);
    }

    /**
     * The object implements the readExternal method to restore its contents
     * by calling the methods of DataInput for primitive types and readObject
     * for objects, strings and arrays. The readExternal method must read the
     * values in the same sequence and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @exception IOException
     * @exception ClassNotFoundException
     */

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        mKey = in.readUTF();
    }

    /**
     * Overrides the toString() method
     *
     * @return the Key as String
     */
    public String toString() {
        return mKey;
    }
}



