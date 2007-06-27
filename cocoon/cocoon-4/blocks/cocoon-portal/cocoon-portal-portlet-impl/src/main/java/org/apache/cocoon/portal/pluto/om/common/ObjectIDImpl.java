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
package org.apache.cocoon.portal.pluto.om.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Wraps around the internal Object IDs. By holding both
 * the string and the integer version of an Object ID this class
 * helps speed up the internal processing.
 *
 * @version $Id$
 */
public class ObjectIDImpl 
implements org.apache.pluto.om.common.ObjectID, java.io.Serializable {

    private String  stringOID;
    private int     intOID;

    private ObjectIDImpl (int oid, String stringOID)  {
        this.stringOID = stringOID;
        intOID    = oid;
    }   

    // internal methods.
    private void readObject (ObjectInputStream stream) throws IOException {
        intOID = stream.readInt ();

        stringOID = String.valueOf (intOID);
    }

    private void writeObject (ObjectOutputStream stream) throws IOException {
        stream.write (intOID);
    }

    // addtional methods.

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals (Object object) {
        boolean result = false;

        if (object instanceof ObjectIDImpl) {
            result = (intOID == ((ObjectIDImpl) object).intOID);  
        } else if (object instanceof String) {
            result = stringOID.equals (object);
        } else if (object instanceof Integer) {
            result = (intOID == ((Integer)object).intValue());   
        }
        return (result);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode () {
        return intOID;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString () {
        return stringOID;
    }

    public int intValue () {
        return (intOID);
    }

    static public ObjectIDImpl createFromString(String idStr) {
        char[] id = idStr.toCharArray();
        int _id  = 1;
        for (int i=0; i<id.length; i++) {
            if ((i%2)==0)   _id *= id[i];
            else            _id ^= id[i];
            _id = Math.abs(_id);
        }
        return new ObjectIDImpl(_id, idStr);
    }
}
