/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

*/
package org.apache.cocoon.portal.pluto.om.common;

import java.io.*;

/**
 * Wraps around the internal Object IDs. By holding both
 * the string and the integer version of an Object ID this class
 * helps speed up the internal processing.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ObjectIDImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
 */
public class ObjectIDImpl implements org.apache.pluto.om.common.ObjectID, java.io.Serializable
{

    private String  stringOID;
    private int     intOID;

    private ObjectIDImpl (int oid)
    {
        stringOID = String.valueOf (oid);
        intOID    = oid;
    }

        
    private ObjectIDImpl (int oid, String stringOID)
    {
        this.stringOID = stringOID;
        intOID    = oid;
    }   

    // internal methods.
    
    private void readObject (ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        intOID = stream.readInt ();

        stringOID = String.valueOf (intOID);
    }

    private void writeObject (ObjectOutputStream stream) throws IOException
    {
        stream.write (intOID);
    }

    
    // addtional methods.
    
    public boolean equals (Object object)
    {
        boolean result = false;

        if (object instanceof ObjectIDImpl)            
            result = (intOID == ((ObjectIDImpl) object).intOID);  
        else if (object instanceof String)
            result = stringOID.equals (object);
        else if (object instanceof Integer)
            result = (intOID == ((Integer)object).intValue());        
        return (result);
    }

    public int hashCode ()
    {
        return (intOID);
    }

    public String toString ()
    {
        return (stringOID);
    }

    public int intValue ()
    {
        return (intOID);
    }

    static public ObjectIDImpl createFromString(String idStr)
    {
        char[] id = idStr.toCharArray();
        int _id  = 1;
        for (int i=0; i<id.length; i++)
        {
            if ((i%2)==0)   _id *= id[i];
            else            _id ^= id[i];
            _id = Math.abs(_id);
        }
        return new ObjectIDImpl(_id, idStr);
    }
}
